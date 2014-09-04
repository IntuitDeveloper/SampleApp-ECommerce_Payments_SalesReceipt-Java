package com.intuit.developer.sampleapp.ecommerce.test.unit.qbo;

import com.intuit.developer.sampleapp.ecommerce.domain.Company;
import com.intuit.developer.sampleapp.ecommerce.domain.Customer;
import com.intuit.developer.sampleapp.ecommerce.domain.SalesItem;
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
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.management.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.*;

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


    @Test
    public void testCreateItemInQBO_NoMatchFound() throws Exception {
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
        salesItem.setQtyOnHand(new BigDecimal(5.0));
        String qboItemID = "1";

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

	        dataService.executeQuery(String.format(QBOGateway.SALES_ITEM_QUERY, salesItem.getName()));
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
            dataService.add(withArgThat(new SalesItemQBOItemMatcher(salesItem, incomeAccountRef, assetAccountRef, cogAccountRef)));

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
            // Explicitly verify that dataService.add must be called with a paramter that matches certain requirements
            dataService.add(withArgThat(new CustomerQBOCustomerMatcher(customer)));

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


    // Defining a matcher class to check that the correct fields get mapped over to qbo Item from the Sales Item
    public class SalesItemQBOItemMatcher extends TypeSafeMatcher<Item> {

        SalesItem salesItemToMatch;
        ReferenceType incomeAccountRefToMatch;
        ReferenceType assetAccountRefToMatch;
        ReferenceType expenseAccountRefToMatch;

        public SalesItemQBOItemMatcher(SalesItem salesItem, ReferenceType incomeAccountRefToMatch, ReferenceType assetAccountRefToMatch, ReferenceType expenseAccountRefToMatch) {
            this.salesItemToMatch = salesItem;
            this.incomeAccountRefToMatch = incomeAccountRefToMatch;
            this.assetAccountRefToMatch = assetAccountRefToMatch;
            this.expenseAccountRefToMatch = expenseAccountRefToMatch;
        }

        @Override
        public boolean matchesSafely(Item item) {
            // Compare Values of Domain Object and QBO entity
            assertEquals(salesItemToMatch.getName(), item.getName());
            assertEquals(salesItemToMatch.getDescription(), item.getDescription());
            assertEquals(salesItemToMatch.getQtyOnHand(), item.getQtyOnHand());
            assertEquals(salesItemToMatch.getUnitPrice().getAmount(), item.getUnitPrice());
            // Check for explicit / special fields on the _QBO ENTITY_ that are not part of domain object but should be set
            // as part of mapping
            assertTrue(item.isTaxable());
            assertTrue(item.isActive());
            assertTrue(item.isTrackQtyOnHand());
            assertFalse(item.isSalesTaxIncluded());
            assertEquals(ItemTypeEnum.INVENTORY, item.getType());
            assertNotNull(item.getInvStartDate());
            assertNotNull(item.getQtyOnHand());

            //Check that the correct account references were made
            assertEquals(assetAccountRefToMatch, item.getAssetAccountRef());
            assertEquals(incomeAccountRefToMatch, item.getIncomeAccountRef());
            assertEquals(expenseAccountRefToMatch, item.getExpenseAccountRef());

            // If we reach this return statement all assertions have passed.
            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("The QBO Item did not match the SalesItem");
        }
    }

    // Defining a matcher class to check the the correct fields get mapped over to qbo Customer from app customer
    public class CustomerQBOCustomerMatcher extends TypeSafeMatcher<com.intuit.ipp.data.Customer> {
        Customer customerToMatch;

        public CustomerQBOCustomerMatcher(Customer customerToMatch) {
            this.customerToMatch = customerToMatch;
        }

        @Override
        public boolean matchesSafely(com.intuit.ipp.data.Customer qboCustomer) {
            assertEquals(customerToMatch.getFirstName(), qboCustomer.getGivenName());
            assertEquals(customerToMatch.getLastName(), qboCustomer.getFamilyName());
            assertEquals(customerToMatch.getEmailAddress(), qboCustomer.getPrimaryEmailAddr().getAddress());
            assertEquals(customerToMatch.getPhoneNumber(), qboCustomer.getPrimaryPhone().getFreeFormNumber());
            // If we reach this return statement all assertions have passed.
            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("The QBO Customer Did not match the Customer");
        }
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
