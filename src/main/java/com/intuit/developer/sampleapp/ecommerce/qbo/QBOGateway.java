package com.intuit.developer.sampleapp.ecommerce.qbo;

import com.intuit.developer.sampleapp.ecommerce.domain.Company;
import com.intuit.developer.sampleapp.ecommerce.domain.Customer;
import com.intuit.developer.sampleapp.ecommerce.domain.SalesItem;
import com.intuit.developer.sampleapp.ecommerce.mappers.CustomerMapper;
import com.intuit.developer.sampleapp.ecommerce.mappers.SalesItemMapper;
import com.intuit.developer.sampleapp.ecommerce.repository.CompanyRepository;
import com.intuit.developer.sampleapp.ecommerce.repository.CustomerRepository;
import com.intuit.developer.sampleapp.ecommerce.repository.SalesItemRepository;
import com.intuit.ipp.core.IEntity;
import com.intuit.ipp.data.*;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.services.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface to QBO via QBO v3 SDK.
 */
public class QBOGateway {

    @Autowired
    private DataServiceFactory dataServiceFactory;

	@Autowired
	private CompanyRepository companyRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SalesItemRepository salesItemRepository;

    public void createCustomerInQBO(Customer customer) {
        DataService dataService = dataServiceFactory.getDataService(customer.getCompany());
        final com.intuit.ipp.data.Customer qboObject = CustomerMapper.buildQBOObject(customer);
        final com.intuit.ipp.data.Customer returnedQBOObject = createObjectInQBO(dataService, qboObject);

	    // TODO: comment on best practice of not embedding QBO ID on app domain class
        customer.setQboId(returnedQBOObject.getId());
        customerRepository.save(customer);
    }

	/**
	 * Called when "Sync" on the Admin->Settings page is invoked in the UI
	 *
	 * SalesItems are pushed to QBO as QBO Items.
	 * QBO Items can be viewed on the "Products and Services" page within QBO.
	 *
	 * NOTE: this example is simplified for the sample application; if you are syncing large sets of objects,
	 *       please use the BatchOperation API
	 */
    public void createItemInQBO(SalesItem salesItem) {
	    DataService dataService = dataServiceFactory.getDataService(salesItem.getCompany());

	    // copy SalesItem to QBO Item
	    Item qboItem = SalesItemMapper.buildQBOObject(salesItem);

	    // find an Income Account to associate with QBO item
	    ReferenceType accountRef = findAccountReference(dataService, AccountTypeEnum.INCOME, "SalesOfProductIncome");
	    qboItem.setIncomeAccountRef(accountRef);

	    // save the item in OBO
	    qboItem = createObjectInQBO(dataService, qboItem);

	    // update the SalesItem in app
        salesItem.setQboId(qboItem.getId());
        salesItemRepository.save(salesItem);
    }

	/**
	 * Get a ReferenceType wrapper for a QBO Account
	 *
	 * ReferenceType values are used to associate different QBO entities to each other.
	 */
	private ReferenceType findAccountReference(DataService dataService, AccountTypeEnum accountType, String accountSubType) {
		Account account = findAccount(dataService, accountType, accountSubType);
		ReferenceType referenceType = new ReferenceType();
		referenceType.setValue(account.getId());
		return referenceType;
	}

	public static final String INCOME_ACCOUNT_QUERY = "select * from account where accounttype = '%s' and accountsubtype = '%s'";
	/**
	 * Search for an account in QBO based on AccountType and AccountSubType
	 */
	private Account findAccount(DataService dataService, AccountTypeEnum accountType, String accountSubType) {
		String accountQuery = String.format(INCOME_ACCOUNT_QUERY, accountType.value(), accountSubType);
		try {
			final QueryResult queryResult = dataService.executeQuery(accountQuery);
			if (queryResult.getEntities().size() == 0) {
				throw new RuntimeException("Could not find an account of type " + accountType.value() + " and subtype " + accountSubType);
			}

			final List<Account> entities = (List<Account>) queryResult.getEntities();
			return entities.get(0);
		} catch (FMSException e) {
			throw new RuntimeException("Failed to execute income account query: " + accountQuery, e);
		}
	}


	public List<Account> getAccounts(long appCompanyId) {
		Company company = companyRepository.findOne(appCompanyId);
		DataService service = dataServiceFactory.getDataService(company);

		try {
			return service.findAll(new Account());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static final String INVENTORY_ITEM_QUERY = "select * from item where active=%b and type = '%s";
	public List<Item> getItems(long appCompanyId) {
		Company company = companyRepository.findOne(appCompanyId);
		DataService service = dataServiceFactory.getDataService(company);

		List<Item> items = new ArrayList<Item>();
		try {
			String inventoryItemQuery = String.format(INVENTORY_ITEM_QUERY, true, ItemTypeEnum.INVENTORY);
			QueryResult result = service.executeQuery(inventoryItemQuery);
			for(IEntity entity : result.getEntities()) {
				items.add((Item)entity);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		return items;
	}

	public static final String CUSTOMERS_QUERY = "select * from customer where active = true";
	public List<com.intuit.ipp.data.Customer> getCustomers(long appCompanyId) {
		Company company = companyRepository.findOne(appCompanyId);
		DataService service = dataServiceFactory.getDataService(company);

		final List<com.intuit.ipp.data.Customer> qboCustomers = new ArrayList<com.intuit.ipp.data.Customer>();
		try {
			QueryResult queryResult = service.executeQuery(CUSTOMERS_QUERY);
			for(IEntity entity: queryResult.getEntities()) {
				com.intuit.ipp.data.Customer customer = (com.intuit.ipp.data.Customer) entity;
				qboCustomers.add(customer);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return qboCustomers;
	}


    private <T extends IEntity> T createObjectInQBO(DataService dataService, T qboObject) {
        try {
            final T createdObject = dataService.add(qboObject);
            return createdObject;
        } catch (FMSException e) {
	        //NOTE: a StaleObjectException can be received while creating a new object; this error indicates that an
	        //      object with that QBO ID already exists.
            throw new RuntimeException("Failed create an " + qboObject.getClass().getName() + " in QBO", e);
        }

    }


}
