package com.intuit.developer.sampleapp.ecommerce.qbo;

import com.intuit.developer.sampleapp.ecommerce.controllers.OrderConfirmation;
import com.intuit.developer.sampleapp.ecommerce.domain.*;
import com.intuit.developer.sampleapp.ecommerce.domain.Customer;
import com.intuit.developer.sampleapp.ecommerce.mappers.CustomerMapper;
import com.intuit.developer.sampleapp.ecommerce.mappers.SalesItemMapper;
import com.intuit.developer.sampleapp.ecommerce.repository.CustomerRepository;
import com.intuit.developer.sampleapp.ecommerce.repository.SalesItemRepository;
import com.intuit.ipp.core.IEntity;
import com.intuit.ipp.data.*;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.services.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.Class;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Ref;
import java.util.*;

/**
 * Interface to QBO via QBO v3 SDK.
 */
public class QBOGateway {

    @Autowired
    private QBOServiceFactory qboServiceFactory;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SalesItemRepository salesItemRepository;

    public void createSalesReceiptInQBO(ShoppingCart cart, OrderConfirmation confirmation) {
        Customer customer = cart.getCustomer();
        DataService dataService = qboServiceFactory.getDataService(customer.getCompany());
        // Make a sales receipt
        SalesReceipt receipt = new SalesReceipt();

        //Append the necessary lines items from the shopping cart
        appendLineItems(receipt, cart);

        // Set the reference to the customer
        ReferenceType customerRef = new ReferenceType();
        customerRef.setValue(customer.getQboId());
        receipt.setCustomerRef(customerRef);
        receipt.setPaymentType(PaymentTypeEnum.CREDIT_CARD);
        receipt = createObjectInQBO(dataService, receipt);

        // Keep the receipt document number to send in the confirmation.
        confirmation.setOrderNumber(receipt.getDocNumber());
    }

	public void createCustomerInQBO(Customer customer) {
		DataService dataService = qboServiceFactory.getDataService(customer.getCompany());

        /* In order to prevent syncing the same data into QBO more than once, query to see if the entity already exists.
           This solution is only meant to provide a better sample app experience (e.g. if you wipe out your database,
           we don't want the sample app to keep creating the same data over and over in QBO).

           In a production app keeping data in two systems in sync is a difficult problem to solve; this code is not
           meant to demonstrate production quality sync functionality.
         */

		com.intuit.ipp.data.Customer returnedQBOObject = findCustomer(dataService, customer);
		if (returnedQBOObject == null) {
			final com.intuit.ipp.data.Customer qboObject = CustomerMapper.buildQBOObject(customer);
			returnedQBOObject = createObjectInQBO(dataService, qboObject);
		}

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
        /* In order to prevent syncing the same data into QBO more than once, query to see if the entity already exists.
           This solution is only meant to provide a better sample app experience (e.g. if you wipe out your database,
           we don't want the sample app to keep creating the same data over and over in QBO).

           In a production app keeping data in two systems in sync is a difficult problem to solve; this code is not
           meant to demonstrate production quality sync functionality.

         */

	    com.intuit.ipp.data.Item returnedQBOObject = findItem(dataService, salesItem);
	    if (returnedQBOObject == null) {
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
		    returnedQBOObject = createObjectInQBO(dataService, qboItem);
	    }

	    // update the SalesItem in app
	    salesItem.setQboId(returnedQBOObject.getId());
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

	/**
	 * Search for an account in QBO based on AccountType and AccountSubType
	 */
	public static final String ACCOUNT_TYPE_QUERY = "select * from account where accounttype = '%s' and accountsubtype = '%s'";
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

	/**
	 * Finds a QBO customer where the customer's first & last name equals the passed in application's customer's first & last name.
	 */
	public static final String CUSTOMER_QUERY = "select * from customer where active = true and givenName = '%s' and familyName = '%s'";
	public com.intuit.ipp.data.Customer findCustomer(DataService dataService, Customer customer) {
		String query = String.format(CUSTOMER_QUERY, customer.getFirstName(), customer.getLastName());
		return executeQuery(dataService, query, com.intuit.ipp.data.Customer.class);
	}

	/**
	 * Finds a QBO item where the item's name equals the passed in salesItem's name.
	 */
	public static final String ITEM_QUERY = "select * from item where active = true and name = '%s'";
	public com.intuit.ipp.data.Item findItem(DataService dataService, SalesItem salesItem) {
		String query = String.format(ITEM_QUERY, salesItem.getName());
		return executeQuery(dataService, query, com.intuit.ipp.data.Item.class);
	}

	private <T extends IEntity> T executeQuery(DataService dataService, String query, Class<T> qboType) {
		try {
			final QueryResult queryResult = dataService.executeQuery(query);
			final List<? extends IEntity> entities = queryResult.getEntities();
			if (entities.size() == 0) {
				return null;
			} else {
				final IEntity entity = entities.get(0);
				return (T) entity;
			}

		} catch (FMSException e) {
			throw new RuntimeException("Failed to execute an entity query: " + query, e);
		}
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

    /**
     * Append Line Items to a sales receipt based on a shopping cart.
     * This is the bulk of the effort in creating a sales receipt.
     * @param salesReceipt - the receipt to append line items to
     * @param cart - the cart to use as a basis for the receipt (represents what was purchased)
     */
	private void appendLineItems(SalesReceipt salesReceipt, ShoppingCart cart) {
		// Make a list of lines
		List<Line> lineItems = new ArrayList<>();

		// For each cart item there will be a line item
		for (CartItem cartItem : cart.getCartItems()) {
			Line line = new Line();

            // This line corresponds to a the _sale_ of an _item_
            line.setDetailType(LineDetailTypeEnum.SALES_ITEM_LINE_DETAIL);

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

			// Add the line item to the list
			lineItems.add(line);
		}

		// Create a line to contain the discount
		Line discountLine = new Line();

        // This line corresponds to a _discout_
		discountLine.setDetailType(LineDetailTypeEnum.DISCOUNT_LINE_DETAIL);

        // Creating an attaching a 'detail' object
        DiscountLineDetail discountLineDetail = new DiscountLineDetail();

        /**
         * There are two ways to set the value of the discount which are mutually exclusive
         * 1) Set an explicit amount
         * 2) Set a percentage of total.
         *
         * We will use the _percentage_ approach.
         */

        // Explicit amount approach
        // discountLine.setAmount(cart.getPromotionSavings().getAmount());

        // Percentage approach
        discountLineDetail.setDiscountPercent(new BigDecimal(ShoppingCart.PROMOTION_MULTIPLIER * 100));
        discountLineDetail.setPercentBased(true);

        // Add the discount details to the line
        discountLine.setDiscountLineDetail(discountLineDetail);

        // Add the discount line to the sales recept
		lineItems.add(discountLine);

        // Add the line items to the receipt
        salesReceipt.setLine(lineItems);
	}
}
