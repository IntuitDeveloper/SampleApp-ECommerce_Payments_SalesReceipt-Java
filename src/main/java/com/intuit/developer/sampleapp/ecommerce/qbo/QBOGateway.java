package com.intuit.developer.sampleapp.ecommerce.qbo;

import com.intuit.developer.sampleapp.ecommerce.domain.*;
import com.intuit.developer.sampleapp.ecommerce.domain.Company;
import com.intuit.developer.sampleapp.ecommerce.domain.Customer;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Interface to QBO via QBO v3 SDK.
 */
public class QBOGateway {

    @Autowired
    private QBOServiceFactory qboServiceFactory;

	@Autowired
	private CompanyRepository companyRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SalesItemRepository salesItemRepository;

    public void createSalesReceiptInQBO(ShoppingCart cart) {
        Customer customer = cart.getCustomer();
        DataService dataService = qboServiceFactory.getDataService(customer.getCompany());
        // Make a sales receipt
        SalesReceipt receipt = new SalesReceipt();

        // Do Construct the necessary lines items from the shopping cart
        appendLineItems(receipt, cart);

        // Set the reference to the customer
        ReferenceType customerRef = new ReferenceType();
        customerRef.setValue(customer.getQboId());
        receipt.setCustomerRef(customerRef);

        receipt = createObjectInQBO(dataService, receipt);
    }


    public void createCustomerInQBO(Customer customer) {
        DataService dataService = qboServiceFactory.getDataService(customer.getCompany());
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
	    DataService dataService = qboServiceFactory.getDataService(salesItem.getCompany());

	    // copy SalesItem to QBO Item
        // This also populates some necessary default constant values
	    Item qboItem = SalesItemMapper.buildQBOObject(salesItem);

        //
        // We need to do lookups for accounts to associate the item to
        //

	    // find an Income Account to associate with QBO item
	    ReferenceType accountRef = findAccountReference(dataService, AccountTypeEnum.INCOME, AccountSubTypeEnum.SALES_OF_PRODUCT_INCOME);
        qboItem.setIncomeAccountRef(accountRef);

        // find an Asset Account to associate with QBO item
        ReferenceType assetAccountRef = findAccountReference(dataService, AccountTypeEnum.OTHER_CURRENT_ASSET, AccountSubTypeEnum.INVENTORY);
        qboItem.setAssetAccountRef(assetAccountRef);

        // find a Cost of Goods Sold account to use as the expense account reference on the QBO Item
        ReferenceType cogAccountRef = findAccountReference(dataService, AccountTypeEnum.COST_OF_GOODS_SOLD, AccountSubTypeEnum.SUPPLIES_MATERIALS_COGS);
        qboItem.setExpenseAccountRef(cogAccountRef);

        // Set the inventory start date to be today
        // This is not set in the mapper because this should only be set when the item is first created in QBO
        qboItem.setInvStartDate(new Date());

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
	private ReferenceType findAccountReference(DataService dataService, AccountTypeEnum accountType, AccountSubTypeEnum accountSubType) {
		Account account = findAccount(dataService, accountType, accountSubType.value());
		ReferenceType referenceType = new ReferenceType();
		referenceType.setValue(account.getId());
		return referenceType;
	}

	public static final String ACCOUNT_TYPE_QUERY = "select * from account where accounttype = '%s' and accountsubtype = '%s'";
	/**
	 * Search for an account in QBO based on AccountType and AccountSubType
	 */
	private Account findAccount(DataService dataService, AccountTypeEnum accountType, String accountSubType) {
		String accountQuery = String.format(ACCOUNT_TYPE_QUERY, accountType.value(), accountSubType);
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
		DataService service = qboServiceFactory.getDataService(company);

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
		DataService service = qboServiceFactory.getDataService(company);

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
		DataService service = qboServiceFactory.getDataService(company);

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

    private void appendLineItems(SalesReceipt salesReceipt, ShoppingCart cart) {
        // Make a list of lines
        List<Line> lineItems = new ArrayList<>();

        // For each cart item there will be a line item
        for (CartItem cartItem : cart.getCartItems()) {
            Line line = new Line();
            // Create a line detail
            SalesItemLineDetail lineDetail = new SalesItemLineDetail();

            // Make the line item reference the sales item from the cart item
            ReferenceType itemRef = new ReferenceType();
            itemRef.setValue(cartItem.getSalesItem().getQboId());
            lineDetail.setItemRef(itemRef);

            // Set the quantity
            lineDetail.setQty(BigDecimal.valueOf(cartItem.getQuantity()));

            // Set the unit price
            lineDetail.setUnitPrice(cartItem.getSalesItem().getUnitPrice().getAmount());
            line.setSalesItemLineDetail(lineDetail);
            line.setDescription(cartItem.getSalesItem().getName());
            // Set the line total
            line.setAmount(lineDetail.getUnitPrice().multiply(lineDetail.getQty()));
            line.setDetailType(LineDetailTypeEnum.SALES_ITEM_LINE_DETAIL);

            // Add the line item to the list
            lineItems.add(line);
        }
        // Create Discount Line Item
        Line discountLine = new Line();
        discountLine.setDetailType(LineDetailTypeEnum.DISCOUNT_LINE_DETAIL);
        //discountLine.setAmount(cart.getPromotionSavings().getAmount());
        DiscountLineDetail discountLineDetail = new DiscountLineDetail();
        discountLineDetail.setDiscountPercent(new BigDecimal(ShoppingCart.PROMOTION_PERCENTAGE));
        discountLineDetail.setPercentBased(true);
        discountLine.setDiscountLineDetail(discountLineDetail);
        lineItems.add(discountLine);

        // Create Tax Line Item
        Line taxLine = new Line();
        taxLine.setDetailType(LineDetailTypeEnum.TAX_LINE_DETAIL);
        taxLine.setAmount(cart.getTax().getAmount());
        TaxLineDetail taxLineDetail = new TaxLineDetail();
        taxLineDetail.setTaxPercent(new BigDecimal(ShoppingCart.TAX_PERCENTAGE));
        taxLineDetail.setPercentBased(true);
        taxLine.setTaxLineDetail(taxLineDetail);
        TxnTaxDetail txnTaxDetail = new TxnTaxDetail();
        List<Line> txnTaxLines = new ArrayList<>();
        txnTaxLines.add(taxLine);
        txnTaxDetail.setTaxLine(txnTaxLines);
        txnTaxDetail.setTotalTax(cart.getTax().getAmount());
        salesReceipt.setTxnTaxDetail(txnTaxDetail);
        salesReceipt.setLine(lineItems);
    }

}
