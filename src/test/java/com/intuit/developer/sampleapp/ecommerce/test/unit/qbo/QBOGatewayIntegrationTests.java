package com.intuit.developer.sampleapp.ecommerce.test.unit.qbo;

import com.intuit.developer.sampleapp.ecommerce.domain.Company;
import com.intuit.developer.sampleapp.ecommerce.domain.Customer;
import com.intuit.developer.sampleapp.ecommerce.domain.SalesItem;
import com.intuit.developer.sampleapp.ecommerce.qbo.DataServiceFactory;
import com.intuit.developer.sampleapp.ecommerce.qbo.QBOGateway;
import com.intuit.developer.sampleapp.ecommerce.repository.CustomerRepository;
import com.intuit.developer.sampleapp.ecommerce.repository.SalesItemRepository;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Created by akuchta on 8/27/14.
 */
@RunWith(JMockit.class)
public class
        QBOGatewayIntegrationTests {
    @Tested
    QBOGateway gateway;

    @Mocked
    DataService dataService;

    @Injectable
    DataServiceFactory dataServiceFactory;

    @Injectable
    CustomerRepository customerRepository;

    @Injectable
    SalesItemRepository salesItemRepository;


    @Test
    public void testCreateItemInQBO() throws Exception {
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

        //Create an income account
        final Account incomeAccount = new Account();
        incomeAccount.setId("1234");
        incomeAccount.setName("Foo");
        incomeAccount.setDescription("Bar");
        incomeAccount.setAccountType(AccountTypeEnum.INCOME);
        final ReferenceType incomeAccountRef = new ReferenceType();
        incomeAccountRef.setValue(incomeAccount.getId());

        //Create an asset account
        final Account assetAccount = new Account();
        assetAccount.setName("Foo");
        assetAccount.setDescription("Bar");
        assetAccount.setAccountType(AccountTypeEnum.OTHER_CURRENT_ASSET);
        assetAccount.setAccountSubType(AccountSubTypeEnum.INVENTORY.value());
        final ReferenceType assetAccountRef = new ReferenceType();
        assetAccountRef.setValue(assetAccount.getId());

        //Create a cost of goods sold account
        final Account cogAccount = new Account();
        cogAccount.setName("Foo");
        cogAccount.setDescription("Bar");
        cogAccount.setAccountType(AccountTypeEnum.OTHER_CURRENT_ASSET);
        cogAccount.setAccountSubType(AccountSubTypeEnum.INVENTORY.value());
        final ReferenceType cogAccountRef = new ReferenceType();
        cogAccountRef.setValue(cogAccount.getId());

        // Create fake income Account query results
        final QueryResult incomeAccountQueryResult = new QueryResult();
        List<Account> incomeAccounts = new ArrayList<>();
        incomeAccounts.add(incomeAccount);
        incomeAccountQueryResult.setEntities(incomeAccounts);

        // Create fake asset Account query results
        final QueryResult assetAccountQueryResult = new QueryResult();
        List<Account> assetAccounts = new ArrayList<>();
        assetAccounts.add(assetAccount);
        assetAccountQueryResult.setEntities(assetAccounts);

        // Create fake cost of goods sold Account to query result
        final QueryResult cogAccountQueryResult = new QueryResult();
        List<Account> cogAccounts = new ArrayList<>();
        cogAccounts.add(cogAccount);
        cogAccountQueryResult.setEntities(cogAccounts);

        final Item qboItem = new Item();
        qboItem.setId(qboItemID);


        new NonStrictExpectations() {{
            // The QBO gateway will try to acquire a data service instance
            dataServiceFactory.getDataService(salesItem.getCompany());
            result = dataService;

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
            dataService.add(withArgThat(new SalesItemQBOItemMatcher(salesItem, incomeAccountRef, assetAccountRef, cogAccountRef)));
            result = qboItem;

            // The salesItemRepository should save the sales item
            salesItemRepository.save(salesItem);
        }};

        // Perform the actual operation under test
        gateway.createItemInQBO(salesItem);

        //Check that the qbo item ID was correctly assigned to the sales Item
        assertEquals(salesItem.getQboId(), qboItemID);
    }

    @Test
    public void testCreateCustomerInQBO() {
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


        // Establish a sequence of expectations.
        new Expectations() {{
            // The QBO gateway will try to acquire a data service instance
            dataServiceFactory.getDataService(customer.getCompany());
            result = dataService;

            // The data service will try to add an Item
            try {
                dataService.add(withArgThat(new CustomerQBOCustomerMatcher(customer)));
            } catch (Exception e) {
            }
            com.intuit.ipp.data.Customer qboCustomer = new com.intuit.ipp.data.Customer();
            qboCustomer.setId(qboCustomerID);
            result = qboCustomer;

            // The salesItemRepository should save the sales item
            customerRepository.save(customer);
        }};

        // Perform the actual operation under test
        gateway.createCustomerInQBO(customer);

        //Check that the qbo item ID was correctly assigned to the sales Item
        assertEquals(customer.getQboId(), qboCustomerID);
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
}
