package com.intuit.developer.sampleapp.ecommerce.qbo;

import com.intuit.developer.sampleapp.ecommerce.domain.CartItem;
import com.intuit.developer.sampleapp.ecommerce.domain.Customer;
import com.intuit.developer.sampleapp.ecommerce.domain.SalesItem;
import com.intuit.developer.sampleapp.ecommerce.domain.ShoppingCart;
import com.intuit.developer.sampleapp.ecommerce.mappers.CustomerMapper;
import com.intuit.developer.sampleapp.ecommerce.mappers.SalesItemMapper;
import com.intuit.developer.sampleapp.ecommerce.repository.CustomerRepository;
import com.intuit.developer.sampleapp.ecommerce.repository.SalesItemRepository;
import com.intuit.ipp.core.IEntity;
import com.intuit.ipp.data.*;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.services.BatchOperation;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.services.QueryResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.map.MultiValueMap;
import org.springframework.beans.factory.annotation.Autowired;


import static com.intuit.ipp.query.GenerateQuery.select;
import static com.intuit.ipp.query.GenerateQuery.$;
import static com.intuit.ipp.query.GenerateQuery.createQueryEntity;


import java.lang.Class;
import java.math.BigDecimal;
import java.util.*;

/**
 * This class contains the needed code to interface with QBO via QBO v3 SDK.
 */
public class QBOGateway {

    @Autowired
    private QBOServiceFactory qboServiceFactory;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SalesItemRepository salesItemRepository;

    /**
     * Called when "Sync" on the Admin->Settings page is invoked in the UI
     *
     * Customers are pushed to QBO as QBO Customers.
     * QBO Customers can be viewed on the "Customers" page within QBO
     * @param customers - the Customers to create in QBO
     */
    public void createCustomersInQBO(List<Customer> customers) {
        if (customers == null) {
            throw new RuntimeException("No customers to create");
        }
        DataService dataService = qboServiceFactory.getDataService(customers.get(0).getCompany());

        // Determine which customers need to be pushed to QBO
        Set<Customer> customersToPush = new HashSet<>();
        Set<Customer> customersToSave = new HashSet<>();
        determineCustomersToPushAndSave(dataService, customers, customersToPush, customersToSave);
        // Save the customers which already exist in QBO, but have had their qboId's updated
        customerRepository.save(customersToSave);
        // Push the Sales Item which do not already exist in QBO to QBO
        pushCustomersToQBO(dataService, customersToPush);
        // Save the updated customers which were pushed to QBO
        customerRepository.save(customersToPush);
    }

    /**
     * Queries QBO to determine which Customers need to be pushed and places them in customersToPush.
     * Updates the local instances of those Sales Items which already exist in QBO with the correct qboId and
     * places them in customersToSave.
     * NOTE: This method implements a very weak matching approach based on first name and last name only.
     * A true sync operation would require more robust matching.
     * @param dataService - the data service to use for querying QBO.
     * @param allCustomers - the iterable of all Customers which are intended to be in QBO
     * @param customersToPush - Pass in empty set. Will be filled with Customers which do not already exist in qbo, subset of all Customers: qboId will be null
     * @param customersToSave - Pass in empty set. Will be filled with Customers which already exist in qbo, subset of all Customers: qboId will not be null
     */
    private void determineCustomersToPushAndSave(DataService dataService, Iterable<Customer> allCustomers, Set<Customer> customersToPush, Set<Customer> customersToSave) {
        // Build up a query to find customers whose have a last name matching one of the last names of _OUR_ customers.
        // This will limit the set of customers returned without excluding any possible matches
        com.intuit.ipp.data.Customer queryCustomer = createQueryEntity(com.intuit.ipp.data.Customer.class);
        List<String> names = new ArrayList<>();
        for (Customer customer : allCustomers) {
            names.add(customer.getLastName());
        }
        String[] namesArr = new String[names.size()];
        names.toArray(namesArr);

        // SELECT * FROM Customer WHERE familyName IN (...) ORDER BY familyName
        String query = select($(queryCustomer)).where($(queryCustomer.getFamilyName()).in(namesArr)).orderBy($(queryCustomer.getFamilyName())).generate();

        // Execute the query
        QueryResult queryResult;
        try {
            queryResult = dataService.executeQuery(query);
        } catch (FMSException e) {
            throw new RuntimeException(e);
        }

        // Build a hashmap to group customers by last name
        Map<String, List<com.intuit.ipp.data.Customer>> customerMap = new HashMap<>();
        for (IEntity iEntity : queryResult.getEntities()) {
            com.intuit.ipp.data.Customer customer = (com.intuit.ipp.data.Customer) iEntity;
            if (!customerMap.containsKey(customer.getFamilyName())) {
                customerMap.put(customer.getFamilyName(), new ArrayList<com.intuit.ipp.data.Customer>());
            }
            customerMap.get(customer.getFamilyName()).add(customer);
        }

        // For each customer we want to create in qbo, search through qbo customers with matching last name.
        for (Customer customer: allCustomers) {
            List<com.intuit.ipp.data.Customer> qboCustomersWithSameLastName = customerMap.get(customer.getLastName());

            // Create a qbo customer object to use a search key
            com.intuit.ipp.data.Customer searchCustomer = new com.intuit.ipp.data.Customer();
            searchCustomer.setGivenName(customer.getFirstName());
            if (qboCustomersWithSameLastName != null) {
                int pos = Collections.binarySearch(qboCustomersWithSameLastName, searchCustomer, new CustomerComparator());
                // If a match is found
                if (pos >= 0) {
                    // Grab the id of the qbo customer
                    customer.setQboId(qboCustomersWithSameLastName.get(pos).getId());
                    // Put this customer in the save set
                    customersToSave.add(customer);
                    // If a match is not found
                } else {
                    // Put this customer in the push sets
                    customersToPush.add(customer);
                }
            } else {
                customersToPush.add(customer);
            }
        }
    }

    /**
     * Push Customers (as Customers) to QBO using a Batch Operation which will allow multiple customers to be created with one
     * request. This method assumes all the customers should be created successfully and throws an exception if there are failures.
     * @param dataService - the data service to use for executing the BatchOperation.
     * @param customersToPush - the Customers to be pushed to qbo
     */
    private void pushCustomersToQBO(DataService dataService, Iterable<Customer> customersToPush) {

        // 1 Build the BatchOperation object
        BatchOperation batchOperation = new BatchOperation();
        for (Customer customer : customersToPush) {
            // We will use the "batchId" to associate request with response - to tell which entities were successfully
            // added.
            String bid = Long.toString(customer.getId());
            // Construct a qboItem from our Domain Object
            com.intuit.ipp.data.Customer qboCustomer = CustomerMapper.buildQBOObject(customer);
            // Add the item to the batch operation
            batchOperation.addEntity(qboCustomer, OperationEnum.CREATE, bid);
        }

        // 2 Execute the BatchOperation
        try {
            dataService.executeBatch(batchOperation);
        } catch (FMSException e) {
            throw new RuntimeException(e);
        }

        // 3 Process the BatchOperation (which now has response data)
        Map<String, IEntity> entityResults = batchOperation.getEntityResult();
        for (Customer customer : customersToPush) {
            // We will use the "batchId" to retrieve the entity result that corresponds to the current Customer
            String bid = Long.toString(customer.getId());
            /**
             * We expected all entities to be created because this method is called after
             * {@code determineCustomersToPushAndSave()}. In a real application, more robust, intelligent
             * error handling would be needed for situations when the service was unavailable or some validation failed,
             * etc.
             */
            if (batchOperation.getFault(bid) != null) {
                List<com.intuit.ipp.data.Error> errors = batchOperation.getFault(bid).getError();
                String errorString = "";
                for (com.intuit.ipp.data.Error error : errors) {
                    errorString = error.getDetail();
                }
                throw new RuntimeException(errorString);
            } else {
                // Grab the id of the qboCustomer and store it as the qboId on our domain model
                com.intuit.ipp.data.Customer qboCustomer = (com.intuit.ipp.data.Customer) entityResults.get(bid);
                customer.setQboId(qboCustomer.getId());
            }
        }
    }

    /**
     * Called when "Sync" on the Admin->Settings page is invoked in the UI
     *
     * SalesItems are pushed to QBO as QBO Items.
     * QBO Items can be viewed on the "Products and Services" page within QBO.
     * @param salesItems - the items to create in QBO
     */
    public void createItemsInQBO(List<SalesItem> salesItems) {
        if (salesItems == null) {
            throw new RuntimeException("No Sales Items to create");
        }

        DataService dataService = qboServiceFactory.getDataService(salesItems.get(0).getCompany());

        // Determine which items need to be pushed to QBO
        Set<SalesItem> salesItemsToPush = new HashSet<>();
        Set<SalesItem> salesItemsToSave = new HashSet<>();
        determineSalesItemsToPushAndSave(dataService, salesItems, salesItemsToPush, salesItemsToSave);
        // Save the Sales Items which already exist in QBO, but have had their qboId's updated
        salesItemRepository.save(salesItemsToSave);
        // Push the Sales Item which do not already exist in QBO to QBO
        pushSalesItemsToQBO(dataService, salesItemsToPush);
        // Save the updated Sales Items which were pushed to QBO
        salesItemRepository.save(salesItemsToPush);
    }

    /**
     * Queries QBO to determine which Sales Items need to be pushed and places them in customersToPush. The query keys off of name. This lookup is done to avoid
     * duplicate name errors when trying to push items.
     * Updates the local instances of those Sales Items which already exist in QBO with the correct qboId and places them in salesItemsToSave
     * @param dataService - the data service to use for querying QBO.
     * @param allSalesItems - the iterable of all SalesItems which are intended to be in QBO
     * @param salesItemsToPush - Pass in empty set. Will be filled with Sales Items which do not already exist in qbo, subset of allSalesItems: qboId will be null
     * @param salesItemsToSave - Pass in empty set. Will be filled with Sales Items which already exist in qbo, subset of allSalesItems: qboId will not be null
     */
    private void determineSalesItemsToPushAndSave(DataService dataService, Iterable<SalesItem> allSalesItems, Set<SalesItem> salesItemsToPush, Set<SalesItem> salesItemsToSave) {
        // Build up a query string to query for multiple named items
        Item queryItem = createQueryEntity(Item.class);
        List<String> names = new ArrayList<>();
        for (SalesItem salesItem : allSalesItems) {
            names.add(salesItem.getName().replace("'", "\\'"));
        }
        String[] namesArr = new String[names.size()];
        names.toArray(namesArr);

        String query = select($(queryItem)).where($(queryItem.getName()).in(namesArr)).generate();

        // Execute the query
        QueryResult queryResult;
        try {
            queryResult = dataService.executeQuery(query);
        } catch (FMSException e) {
            throw new RuntimeException(e);
        }

        // The query results may or may not contain an Item for each SalesItem name
        // build a hashmap of Item name and Item to allow better lookup
        HashMap<String, Item> itemMap = new HashMap<>();
        for (IEntity iEntity : queryResult.getEntities()) {
            Item item = (Item) iEntity;
            itemMap.put(item.getName(), item);
        }

        // Lookup each Sales Item from the master set in the map
        for (SalesItem salesItem: allSalesItems) {
            Item qboItem = itemMap.get(salesItem.getName());
            // If an item with matching name is not already in QBO
            if (qboItem == null) {
                // Add the corresponding SalesItem to the set of items to be pushed
                salesItemsToPush.add(salesItem);
            // If an Item with marching name already exists in QBO
            } else {
                // Grab the id of the matching Item and store it on the Sales Item
                salesItem.setQboId(qboItem.getId());
                salesItemsToSave.add(salesItem);
            }
        }
    }

    /**
     * Push SalesItems (as Items) to QBO using a Batch Operation which will allow multiple items to be created with one
     * request. This method assumes all the Items should be created successfully and throws an exception if there are failures.
     * @param dataService - the data service to use for executing the BatchOperation.
     * @param salesItemsToPush - the Sales Items to be pushed to qbo
     */
    private void pushSalesItemsToQBO(DataService dataService, Iterable<SalesItem> salesItemsToPush) {
        // find an Income Account to associate with QBO items
        ReferenceType accountRef = findAccountReference(dataService, AccountTypeEnum.INCOME, AccountSubTypeEnum.SALES_OF_PRODUCT_INCOME);
        // find an Asset Account to associate with QBO items
        ReferenceType assetAccountRef = findAccountReference(dataService, AccountTypeEnum.OTHER_CURRENT_ASSET, AccountSubTypeEnum.INVENTORY);
        // find a Cost of Goods Sold account to use as the expense account reference on the QBO Items
        ReferenceType cogAccountRef = findAccountReference(dataService, AccountTypeEnum.COST_OF_GOODS_SOLD, AccountSubTypeEnum.SUPPLIES_MATERIALS_COGS);

        // 1 Build the BatchOperation object
        BatchOperation batchOperation = new BatchOperation();
        for (SalesItem salesItem : salesItemsToPush) {
            // We will use the "batchId" to associate request with response - to tell which entities were successfully
            // added.
            String bid = Long.toString(salesItem.getId());
            // Construct a qboItem from our Domain Object
            Item qboItem = SalesItemMapper.buildQBOObject(salesItem);
            // Set the account references
            qboItem.setIncomeAccountRef(accountRef);
            qboItem.setExpenseAccountRef(cogAccountRef);
            qboItem.setAssetAccountRef(assetAccountRef);
            qboItem.setInvStartDate(new Date());
            // Add the item to the batch operation
            batchOperation.addEntity(qboItem, OperationEnum.CREATE, bid);
        }

        // 2 Execute the BatchOperation
        try {
            dataService.executeBatch(batchOperation);
        } catch (FMSException e) {
            throw new RuntimeException(e);
        }

        // 3 Process the BatchOperation (which now has response data)
        Map<String, IEntity> entityResults = batchOperation.getEntityResult();
        for (SalesItem salesItem : salesItemsToPush) {
            // We will use the "batchId" to retrieve the entity result that corresponds to the current SalesItem
            String bid = Long.toString(salesItem.getId());
            /**
             * We expected all entities to be created because this method is called after
             * {@code determineSalesItemsToPushAndSave()}. In a real application, more robust, intelligent
             * error handling would be needed for situations when the service was unavailable or some validation failed,
             * etc.
             */
            if (batchOperation.getFault(bid) != null) {
                List<com.intuit.ipp.data.Error> errors = batchOperation.getFault(bid).getError();
                String errorString = "";
                for (com.intuit.ipp.data.Error error : errors) {
                    errorString = error.getDetail();
                }
                throw new RuntimeException(errorString);
            } else {
                // Grab the id of the qboItem and store it as the qboId on our domain model
                Item qboItem = (Item) entityResults.get(bid);
                salesItem.setQboId(qboItem.getId());
            }
        }
    }
    /**
     * Called when "Place Order" is clicked in the UI
     * Creates a sales receipt based upon the passed in cart and makes all needed references
     * @param cart - the cart that was "ordered" which a sales receipt must be created for
     * @return - returns the created sales receipt as sent back from QBO API.
     */
    public SalesReceipt createSalesReceiptInQBO(ShoppingCart cart, String ccTransId) {
        Customer customer = cart.getCustomer();
        DataService dataService = qboServiceFactory.getDataService(customer.getCompany());
        // Make a sales receipt
        SalesReceipt receipt = new SalesReceipt();

        //Append the necessary lines items from the shopping cart
        appendLineItems(receipt, cart);

        /**
         * In a real application more judgement would be required to associate the correct tax code to a purchase.
         * Uncomment these lines to associate the receipt with a\
         * tax code in order to apply sales tax.
         * A Tax code must be correctly configured in the QBO Company, Sales Tax (As a feature) must be turned on,
         * and the sales item referenced must be have the taxable flag set.
         */
        TxnTaxDetail txnTaxDetail = new TxnTaxDetail();
        txnTaxDetail.setTxnTaxCodeRef(findTaxCodeReference(dataService, "California"));
        receipt.setApplyTaxAfterDiscount(false);
        receipt.setTxnTaxDetail(txnTaxDetail);

        // Set the reference to the customer
        ReferenceType customerRef = new ReferenceType();
        customerRef.setValue(customer.getQboId());
        receipt.setCustomerRef(customerRef);

        /**
         * txnSource must be set to "IntuitPayment" in order to trigger the Payments Reconciliation feature.
         * This feature allows the payments service to create a deposit in the QBO company for the payment
         * amount once the transaction has been processed.
         */
        receipt.setTxnSource("IntuitPayment");

        // processPayment must be true in order for Payment Reconciliation to happen.
        CreditChargeInfo creditChargeInfo = new CreditChargeInfo();
        creditChargeInfo.setProcessPayment(true);

        // ccTransId must be the ID of the charge object returned by the call to capture()
        CreditChargeResponse creditChargeResponse = new CreditChargeResponse();
        creditChargeResponse.setCCTransId(ccTransId);

        // The CreditChargeInfo and CreditChargeResponse objects are bundled together into a creditCardPayment
        CreditCardPayment creditCardPayment = new CreditCardPayment();
        creditCardPayment.setCreditChargeInfo(creditChargeInfo);
        creditCardPayment.setCreditChargeResponse(creditChargeResponse);
        receipt.setCreditCardPayment(creditCardPayment);

        //Create the receipt
        receipt = createObjectInQBO(dataService, receipt);

        // Construct an order confirmation from the
        return receipt;
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

            /**
             * These lines make the line item reference the TaxCode "TAX" so that
             * The sales receipt will include the amount of this line item in the tax calculation
             * "TAX" is a special reference value
             */
            ReferenceType taxRef = new ReferenceType();
            taxRef.setValue("TAX");
            lineDetail.setTaxCodeRef(taxRef);

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

    /**
     * Type generic wrapper for {@link com.intuit.ipp.services.DataService#add(com.intuit.ipp.core.IEntity)}
     * which translates a a somewhat confusing exception case.
     * @param dataService - the data service to use.
     * @param qboObject - the object to add to qbo
     * @param <T> - the type of the object to create
     * @return create object
     */
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
     * Get a ReferenceType wrapper for a QBO Account
     *
     * ReferenceType values are used to associate different QBO entities to each other.
     */
    public static ReferenceType findAccountReference(DataService dataService, AccountTypeEnum accountType, AccountSubTypeEnum accountSubType) {
        Account account = findAccount(dataService, accountType, accountSubType.value());
        if (account == null) {
            throw new RuntimeException(String.format("Could not find an account with account type: %s and account subtype: %s", accountType, accountSubType));
        }
        ReferenceType referenceType = new ReferenceType();
        referenceType.setValue(account.getId());
        return referenceType;
    }

    /**
     * Get a ReferenceType wrapper for a QBO TaxCode
     *
     * ReferenceType values are used to associate different QBO entities to each other.
     */
    public static ReferenceType findTaxCodeReference(DataService dataService, String taxCodeName) {
        TaxCode taxCode = findTaxCode(dataService, taxCodeName);
        ReferenceType referenceType = new ReferenceType();
        referenceType.setValue(taxCode.getId());
        return referenceType;
    }

    /**
     * Search for an account in QBO based on AccountType and AccountSubType
     */
    public static final String ACCOUNT_TYPE_QUERY = "select * from account where accounttype = '%s' and accountsubtype = '%s'";
    public static Account findAccount(DataService dataService, AccountTypeEnum accountType, String accountSubType) {
        String query = String.format(ACCOUNT_TYPE_QUERY, accountType.value(), accountSubType);
        return executeQuery(dataService, query, com.intuit.ipp.data.Account.class);
    }

    /**
     * Finds a QBO PaymentMethod by name
     */
    public static final String PAYMENT_METHOD_QUERY = "select * from paymentmethod where name= '%s'";
    public static PaymentMethod findPaymentMethod(DataService dataService, String paymentMethodName) {
        String query = String.format(PAYMENT_METHOD_QUERY, paymentMethodName);
        return executeQuery(dataService, query, com.intuit.ipp.data.PaymentMethod.class);
    }

    /**
     * Finds a QBO tax code by name
     */
    public static final String TAX_CODE_QUERY = "select * from taxcode where name= '%s'";
    public static TaxCode findTaxCode(DataService dataService, String taxCodeName) {
        String query = String.format(TAX_CODE_QUERY, taxCodeName);
        return executeQuery(dataService, query, com.intuit.ipp.data.TaxCode.class);
    }

    /**
     * Finds a QBO customer where the customer's first & last name equals the passed in application's customer's first & last name.
     */
    public static final String CUSTOMER_QUERY = "select * from customer where active = true and givenName = '%s' and familyName = '%s'";
    public static com.intuit.ipp.data.Customer findCustomer(DataService dataService, com.intuit.developer.sampleapp.ecommerce.domain.Customer customer) {
        String query = String.format(CUSTOMER_QUERY, customer.getFirstName(), customer.getLastName());
        return executeQuery(dataService, query, com.intuit.ipp.data.Customer.class);
    }

    /**
     * Finds a QBO item where the item's name equals the passed in customer's name.
     */
    public static final String ITEM_QUERY = "select * from item where active = true and name = '%s'";
    public static com.intuit.ipp.data.Item findItem(DataService dataService, SalesItem salesItem) {
        String query = String.format(ITEM_QUERY, salesItem.getName().replace("'", "\\'"));
        return executeQuery(dataService, query, com.intuit.ipp.data.Item.class);
    }

    /**
     * Type-generic query method which returns only the first result from a query and casts it to the desired type.
     */
    private static <T extends IEntity> T executeQuery(DataService dataService, String query, Class<T> qboType) {
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

    public class CustomerComparator implements Comparator<com.intuit.ipp.data.Customer> {
        @Override
        public int compare(com.intuit.ipp.data.Customer o1, com.intuit.ipp.data.Customer o2) {

            return ((o1.getGivenName().compareTo(o2.getGivenName()) == 0) ? 0 : 1);
        }
    }
}
