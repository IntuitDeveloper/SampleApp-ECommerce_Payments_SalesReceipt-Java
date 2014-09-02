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
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
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
        incomeAccount.setId("1");
        incomeAccount.setName("Income Account");
        incomeAccount.setDescription("The Income account");
        incomeAccount.setAccountType(AccountTypeEnum.INCOME);
        incomeAccount.setAccountSubType(AccountSubTypeEnum.SALES_OF_PRODUCT_INCOME.value());
        final ReferenceType incomeAccountRef = new ReferenceType();
        incomeAccountRef.setValue(incomeAccount.getId());

        //Create an asset account
        final Account assetAccount = new Account();
        assetAccount.setId("2");
        assetAccount.setName("Asset Account");
        assetAccount.setDescription("The Asset Account");
        assetAccount.setAccountType(AccountTypeEnum.OTHER_CURRENT_ASSET);
        assetAccount.setAccountSubType(AccountSubTypeEnum.INVENTORY.value());
        final ReferenceType assetAccountRef = new ReferenceType();
        assetAccountRef.setValue(assetAccount.getId());

        //Create a cost of goods sold account
        final Account cogAccount = new Account();
        cogAccount.setId("3");
        cogAccount.setName("Cost of Goods Sold Account");
        cogAccount.setDescription("THE Cogs account.");
        cogAccount.setAccountType(AccountTypeEnum.COST_OF_GOODS_SOLD);
        cogAccount.setAccountSubType(AccountSubTypeEnum.SUPPLIES_MATERIALS_COGS.value());
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

        // Non Strict expectations, generally just mocking collaborators
        new NonStrictExpectations() {{
            // The QBO gateway will try to acquire a data service instance
            qboServiceFactory.getDataService(salesItem.getCompany());
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
            dataService.add(withAny(new Item()));
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
            salesItemRepository.save(salesItem);
        }};
    }

    @Test
    public void testCreateCustomerInQBO() throws Exception {
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


        // Establish non-sequential expections
        // Mostly used for mocking collaborators
        new NonStrictExpectations() {{
            // The QBO gateway will try to acquire a data service instance
            qboServiceFactory.getDataService(customer.getCompany());
            result = dataService;

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
