package com.intuit.developer.sampleapp.ecommerce.test.unit.qbo;

import com.intuit.developer.sampleapp.ecommerce.domain.*;
import com.intuit.developer.sampleapp.ecommerce.domain.Company;
import com.intuit.developer.sampleapp.ecommerce.domain.Customer;
import com.intuit.developer.sampleapp.ecommerce.qbo.QBOGateway;
import com.intuit.developer.sampleapp.ecommerce.qbo.QBOServiceFactory;
import com.intuit.developer.sampleapp.ecommerce.repository.CustomerRepository;
import com.intuit.developer.sampleapp.ecommerce.repository.SalesItemRepository;
import com.intuit.ipp.core.IEntity;
import com.intuit.ipp.data.*;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.services.QueryResult;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by akuchta on 8/27/14.
 */
@RunWith(JMockit.class)
public class QBOGatewayUnitTests {
    @Tested
    QBOGateway gateway;

    @Mocked
    DataService dataService;

    @Injectable
    QBOServiceFactory qboServiceFactory;

    @Injectable
    CustomerRepository customerRepository;

    @Injectable
    SalesItemRepository salesItemRepository;

    Company company;
    Customer customer;

    @Before
    public void before() {
        company = new Company();
        company.setName("Foo");
        company.setAccessToken("asdasdA");
        company.setAccessTokenSecret("sfsfsdfsdfsdfs");

        customer = new Customer("Bob", "Shmob", "a@a.com", "");
        customer.setCompany(company);
    }

    @Test
    public void testCreateItemInQBO_NoMatchFound() throws Exception {
        //
        // Establish a set of test data
        //

        // Create a sales item
        final SalesItem salesItem = new SalesItem("Shirt", "It's a shirt", Money.of(CurrencyUnit.USD, 5.00), "");
        salesItem.setQtyOnHand(new BigDecimal(5.0));
        final String qboItemID = "1";

	    //Create an income acount
	    final Account incomeAccount = createAccount("1", "IncomeAccount", "The Income account", AccountTypeEnum.INCOME, AccountSubTypeEnum.SALES_OF_PRODUCT_INCOME);
	    final ReferenceType incomeAccountRef = createAccountRef(incomeAccount);

	    //Create an asset account
	    final Account assetAccount = createAccount("2", "Asset Account", "The Asset Account", AccountTypeEnum.OTHER_CURRENT_ASSET, AccountSubTypeEnum.INVENTORY);
	    final ReferenceType assetAccountRef = createAccountRef(assetAccount);

	    //Create a cost of goods sold account
	    final Account cogAccount = createAccount("3", "Cost of Goods Sold Account", "THE Cogs account.", AccountTypeEnum.COST_OF_GOODS_SOLD, AccountSubTypeEnum.SUPPLIES_MATERIALS_COGS);
	    final ReferenceType cogAccountRef = createAccountRef(cogAccount);

        // Create fake income Account query results
        final QueryResult incomeAccountQueryResult = new QueryResult();
	    incomeAccountQueryResult.setEntities(Arrays.asList(incomeAccount));

        // Create fake asset Account query results
        final QueryResult assetAccountQueryResult = new QueryResult();
        assetAccountQueryResult.setEntities(Arrays.asList(assetAccount));

        // Create fake cost of goods sold Account to query result
        final QueryResult cogAccountQueryResult = new QueryResult();
	    cogAccountQueryResult.setEntities(Arrays.asList(cogAccount));

        final Item qboItem = new Item();
        qboItem.setId(qboItemID);
	    final QueryResult itemQueryResult = new QueryResult();
	    itemQueryResult.setEntities(new ArrayList<Item>());    // intentionally empty result list

        // Non Strict expectations, generally just mocking collaborators
        new NonStrictExpectations() {{
            // The QBO gateway will try to acquire a data service instance
            qboServiceFactory.getDataService(salesItem.getCompany());
            result = dataService;

	        dataService.executeQuery(String.format(QBOGateway.ITEM_QUERY, salesItem.getName()));
	        result = itemQueryResult;

            // The dataService will try to execute a query for income accounts
            dataService.executeQuery(String.format(
                            QBOGateway.ACCOUNT_TYPE_QUERY,
                            AccountTypeEnum.INCOME.value(),
                            AccountSubTypeEnum.SALES_OF_PRODUCT_INCOME.value())
            );
            result = incomeAccountQueryResult;

            // The dataService will try to execute a query for asset accounts
            dataService.executeQuery(String.format(
                            QBOGateway.ACCOUNT_TYPE_QUERY,
                            AccountTypeEnum.OTHER_CURRENT_ASSET.value(),
                            AccountSubTypeEnum.INVENTORY.value())
            );
            result = assetAccountQueryResult;

            // The dataService will try to execute a query for cost of goods sold accounts
            dataService.executeQuery(String.format(
                            QBOGateway.ACCOUNT_TYPE_QUERY,
                            AccountTypeEnum.COST_OF_GOODS_SOLD.value(),
                            AccountSubTypeEnum.SUPPLIES_MATERIALS_COGS.value())
            );
            result = cogAccountQueryResult;

            // The data service will try to add the item to QBO
            dataService.add((Item)any);
            result = qboItem;

        }};

        // Perform the actual operation under test
        gateway.createItemInQBO(salesItem);

        //Check that the qbo item ID was correctly assigned to the sales Item
        assertEquals(qboItemID, salesItem.getQboId());

        // Explicitly verify things that SHOULD HAVE happened.
        new Verifications() {{
            Item qboItemPassed;
            dataService.add(qboItemPassed = withCapture());

            // Compare Values of Domain Object and QBO entity
            assertEquals(salesItem.getName(), qboItemPassed.getName());
            assertEquals(salesItem.getDescription(), qboItemPassed.getDescription());
            assertEquals(salesItem.getQtyOnHand(), qboItemPassed.getQtyOnHand());
            assertEquals(salesItem.getUnitPrice().getAmount(), qboItemPassed.getUnitPrice());
            // Check for explicit / special fields on the _QBO ENTITY_ that are not part of domain object but should be set
            // as part of mapping
            assertTrue(qboItemPassed.isTaxable());
            assertTrue(qboItemPassed.isActive());
            assertTrue(qboItemPassed.isTrackQtyOnHand());
            assertFalse(qboItemPassed.isSalesTaxIncluded());
            assertEquals(ItemTypeEnum.INVENTORY, qboItemPassed.getType());
            assertNotNull(qboItemPassed.getInvStartDate());
            assertNotNull(qboItemPassed.getQtyOnHand());

            //Check that the correct account references were made
            assertEquals(assetAccountRef, qboItemPassed.getAssetAccountRef());
            assertEquals(incomeAccountRef, qboItemPassed.getIncomeAccountRef());
            assertEquals(cogAccountRef, qboItemPassed.getExpenseAccountRef());

            // The salesItemRepository should save the sales item
            salesItemRepository.save(salesItem); times = 1;
        }};
    }

	@Test
	public void testCreateItemInQBO_MatchFound() throws Exception {
		//
		// Establish a set of test data
		//

		// Create a company
		Company company = new Company();
		company.setName("Foo");
		company.setAccessToken("asdasdA");
		company.setAccessTokenSecret("sfsfsdfsdfsdfs");

		// Create a sales item
		final SalesItem salesItem = new SalesItem("Shirt", "It's a shirt", Money.of(CurrencyUnit.USD, 5.00), "");
		assertNull(salesItem.getQboId());
		salesItem.setQtyOnHand(new BigDecimal(5.0));

		// create a QBO item that matches on SalesItem name
		final String qboItemID = "1";
		final Item qboItem = new Item();
		qboItem.setId(qboItemID);
		qboItem.setName("Shirt");
		qboItem.setDescription("It's a shirt");

		// Create item query results
		final QueryResult itemQueryResult = new QueryResult();
		itemQueryResult.setEntities(Arrays.asList(qboItem));


		// Non Strict expectations, generally just mocking collaborators
		new NonStrictExpectations() {{
			// The QBO gateway will try to acquire a data service instance
			qboServiceFactory.getDataService(salesItem.getCompany());
			result = dataService;

			// The dataService will try to execute a query for income accounts
			dataService.executeQuery(anyString); times = 1;
			result = itemQueryResult;

			// The data service will not try to add the item to QBO
			dataService.add(withAny(new Item())); times = 0;
		}};

		// Perform the actual operation under test
		gateway.createItemInQBO(salesItem);

		// Explicitly verify things that SHOULD HAVE happened.
		new Verifications() {{
			// The salesItemRepository should save the sales item
			salesItemRepository.save(salesItem);

			//Check that the qbo item ID was correctly assigned to the sales Item
			assertEquals(qboItemID, salesItem.getQboId());
		}};
	}

    @Test
    public void testCreateCustomerInQBO_NoMatchFound() throws Exception {
        //
        // Establish a set of test data
        //

        // Create a company
        Company company = new Company();
        company.setName("Foo");
        company.setAccessToken("asdasdA");
        company.setAccessTokenSecret("sfsfsdfsdfsdfs");

        // Create a customer
        final Customer customer = new Customer("Bob", "Shmob", "a@a.com", "");
        final String qboCustomerID = "1";
        customer.setCompany(company);

	    final QueryResult customerQueryResult = new QueryResult();
	    customerQueryResult.setEntities(new ArrayList<IEntity>()); // intentionally empty

        // Establish non-sequential expectations
        // Mostly used for mocking collaborators
        new NonStrictExpectations() {{
            // The QBO gateway will try to acquire a data service instance
            qboServiceFactory.getDataService(customer.getCompany());
            result = dataService;

	        dataService.executeQuery(String.format(QBOGateway.CUSTOMER_QUERY, customer.getFirstName(), customer.getLastName()));
	        result = customerQueryResult;

            // The data service will try to add an Item
            dataService.add(withAny(new com.intuit.ipp.data.Customer()));
            com.intuit.ipp.data.Customer qboCustomer = new com.intuit.ipp.data.Customer();
            qboCustomer.setId(qboCustomerID);
            result = qboCustomer;

        }};

        // Perform the actual operation under test
        gateway.createCustomerInQBO(customer);

        //Check that the qbo item ID was correctly assigned to the sales Item
        assertEquals(customer.getQboId(), qboCustomerID);

        new Verifications() {{
            com.intuit.ipp.data.Customer passedInCustomer;

            // Explicitly verify that dataService.add must be called with a parameter that matches certain requirements
            dataService.add(passedInCustomer = withCapture());

            assertEquals(customer.getFirstName(), passedInCustomer.getGivenName());
            assertEquals(customer.getLastName(), passedInCustomer.getFamilyName());
            assertEquals(customer.getEmailAddress(), passedInCustomer.getPrimaryEmailAddr().getAddress());
            assertEquals(customer.getPhoneNumber(), passedInCustomer.getPrimaryPhone().getFreeFormNumber());

            // The salesItemRepository should save the sales item
            customerRepository.save(customer);
        }};
    }

	@Test
	public void testCreateCustomerInQBO_MatchFound() throws Exception {
		//
		// Establish a set of test data
		//

		// Create a company
		Company company = new Company();
		company.setName("Foo");
		company.setAccessToken("asdasdA");
		company.setAccessTokenSecret("sfsfsdfsdfsdfs");

		// Create a customer that has not been synced w/QBO
		final Customer customer = new Customer("Bob", "Shmob", "a@a.com", "");
		assertNull(customer.getQboId());

		final String qboCustomerID = "10"; // intentionally choose a number other than 1
		customer.setCompany(company);

		// create existing, matching QBO customers
		final com.intuit.ipp.data.Customer qboCustomer = new com.intuit.ipp.data.Customer();
		qboCustomer.setGivenName("Bob");
		qboCustomer.setFamilyName("Shmob");
		qboCustomer.setId(qboCustomerID);

		// Establish non-sequential expectations
		// Mostly used for mocking collaborators
		new NonStrictExpectations() {{
			// The QBO gateway will try to acquire a data service instance
			qboServiceFactory.getDataService(customer.getCompany());
			result = dataService;

			dataService.executeQuery(anyString);
			QueryResult queryResult = new QueryResult();
			queryResult.setEntities(Arrays.asList(qboCustomer));
			result = queryResult;
		}};

		// Perform the actual operation under test
		gateway.createCustomerInQBO(customer);

		new Verifications() {{
			// add should not be called
			dataService.add((IEntity)any); times = 0;

			// The customerRepository should save the customer that has been updated w/the QBO ID
			customerRepository.save(customer);

			//Check that the qbo item ID was correctly assigned to the customer
			assertEquals(qboCustomerID, customer.getQboId());
		}};
	}

	@Test
	public void testCreateSalesReceiptInQBO() throws Exception {
		// Create a a shopping cart
		final ShoppingCart shoppingCart = new ShoppingCart(customer);

		// Create a list of items for the cart
		List<CartItem> cartItems = new ArrayList<>();
		CartItem cartItem = new CartItem();
		SalesItem salesItem = new SalesItem("ItemType1","It's an item", Money.of(CurrencyUnit.USD, 5.00), "");
		salesItem.setQboId("1");
		cartItem.setSalesItem(salesItem);
		cartItem.setQuantity(2);
		cartItem.setShoppingCart(shoppingCart);
		cartItems.add(cartItem);

		cartItem = new CartItem();
		salesItem = new SalesItem("ItemType2","It's another item", Money.of(CurrencyUnit.USD, 3.50), "");
		salesItem.setQboId("2");
		cartItem.setSalesItem(salesItem);
		cartItem.setQuantity(1);
		cartItem.setShoppingCart(shoppingCart);
		cartItems.add(cartItem);
		shoppingCart.setCartItems(cartItems);

		new NonStrictExpectations() {{
			qboServiceFactory.getDataService(withAny(new Company()));
			result = dataService;
		}};

		gateway.createSalesReceiptInQBO(shoppingCart);

		new Verifications() {{
			SalesReceipt receiptPassed;
			dataService.add(receiptPassed = withCapture());
			List<Line> lines = receiptPassed.getLine();
			assertEquals(3, lines.size());
			List<CartItem> cartItems = shoppingCart.getCartItems();

			// The first two lines items of the sales receipt should be the items added
			verifyLineForCartItem(lines.get(0), cartItems.get(0));
			verifyLineForCartItem(lines.get(1), cartItems.get(1));

			// The next line should be a discount
			verifyLineForDiscount(lines.get(2), shoppingCart);

			// The next line should be taxes
			verifyTxnTaxDetail(receiptPassed.getTxnTaxDetail(), shoppingCart);

			assertEquals(shoppingCart.getCustomer().getQboId(), receiptPassed.getCustomerRef().getValue());
		}};
	}	

    private void verifyLineForCartItem(Line line, CartItem cartItem) {
        assertEquals(LineDetailTypeEnum.SALES_ITEM_LINE_DETAIL, line.getDetailType());
        SalesItemLineDetail lineDetail = line.getSalesItemLineDetail();
        assertNotNull(
                "There must be an item ref",
                lineDetail.getItemRef());
        assertNotNull(
                "The item ref must have a value",
                lineDetail.getItemRef().getValue());
        assertEquals(
                "The item ref should contain the Item QBO ID",
                cartItem.getSalesItem().getQboId(),
                lineDetail.getItemRef().getValue());
        assertEquals(
                "The unit prices should match",
                cartItem.getSalesItem().getUnitPrice().getAmount(),
                lineDetail.getUnitPrice());
        assertEquals(
                "The item quantities should match",
                new BigDecimal(cartItem.getQuantity()),
                lineDetail.getQty());
        assertEquals(
                "The line total should be cart item qty * unit price",
                cartItem.getSalesItem().getUnitPrice().multipliedBy(cartItem.getQuantity()).getAmount(),
                line.getAmount());
        assertEquals(
                "The item name should go on the sales receipt",
                cartItem.getSalesItem().getName(),
                line.getDescription());
    }

    private void verifyLineForDiscount(Line line, ShoppingCart shoppingCart) {
        assertEquals(LineDetailTypeEnum.DISCOUNT_LINE_DETAIL, line.getDetailType());
        DiscountLineDetail lineDetail = line.getDiscountLineDetail();
        assertTrue(lineDetail.isPercentBased());
        assertEquals(new BigDecimal(ShoppingCart.PROMOTION_PERCENTAGE), lineDetail.getDiscountPercent());
    }

    private void verifyTxnTaxDetail(TxnTaxDetail txnTaxDetail, ShoppingCart shoppingCart) {
        List<Line> taxLines = txnTaxDetail.getTaxLine();
        assertEquals(1, taxLines.size());
        Line taxLine = taxLines.get(0);
        assertEquals(LineDetailTypeEnum.TAX_LINE_DETAIL, taxLine.getDetailType());
        TaxLineDetail lineDetail = taxLine.getTaxLineDetail();
        assertEquals(shoppingCart.getTax().getAmount(), taxLine.getAmount());
        assertEquals(new BigDecimal(ShoppingCart.TAX_PERCENTAGE), lineDetail.getTaxPercent());
    }

	private Account createAccount(String id, String accountName, String description, AccountTypeEnum accountType, AccountSubTypeEnum accountSubType) {
		final Account cogAccount = new Account();
		cogAccount.setId(id);
		cogAccount.setName(accountName);
		cogAccount.setDescription(description);
		cogAccount.setAccountType(accountType);
		cogAccount.setAccountSubType(accountSubType.value());
		return cogAccount;
	}

	private ReferenceType createAccountRef(Account account) {
		final ReferenceType accountRef = new ReferenceType();
		accountRef.setValue(account.getId());
		return accountRef;
	}
}
