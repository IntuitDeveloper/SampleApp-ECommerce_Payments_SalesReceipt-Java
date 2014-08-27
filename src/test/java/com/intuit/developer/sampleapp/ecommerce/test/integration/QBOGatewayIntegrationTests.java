package com.intuit.developer.sampleapp.ecommerce.test.integration;

import com.intuit.developer.sampleapp.ecommerce.domain.Company;
import com.intuit.developer.sampleapp.ecommerce.domain.Customer;
import com.intuit.developer.sampleapp.ecommerce.domain.SalesItem;
import com.intuit.developer.sampleapp.ecommerce.qbo.DataServiceFactory;
import com.intuit.developer.sampleapp.ecommerce.qbo.QBOGateway;
import com.intuit.developer.sampleapp.ecommerce.repository.CustomerRepository;
import com.intuit.developer.sampleapp.ecommerce.repository.SalesItemRepository;
import com.intuit.ipp.data.Account;
import com.intuit.ipp.data.AccountTypeEnum;
import com.intuit.ipp.data.Item;
import com.intuit.ipp.data.ItemTypeEnum;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.services.QueryResult;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by akuchta on 8/27/14.
 */
@RunWith(JMockit.class)
public class QBOGatewayIntegrationTests {
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
    public void testCreateItemInQBO() {
        //
        // Establish a set of test data
        //

        // Create a sales item
        final SalesItem salesItem = new SalesItem("Shirt", "It's a shirt", Money.of(CurrencyUnit.USD, 5.00), "");
        final String qboItemID = "1";
        salesItem.setCompany(new Company());
        Account account = new Account();
        account.setName("Foo");
        account.setDescription("Bar");
        account.setAccountType(AccountTypeEnum.INCOME);

        // Create fake query results to return
        final QueryResult queryResult = new QueryResult();
        List<Account> accounts = new ArrayList<>();
        accounts.add(account);
        queryResult.setEntities(accounts);

        // Establish a sequence of expectations.
        new Expectations() {{
            // The QBO gateway will try to acquire a data service instance
            dataServiceFactory.getDataService(salesItem.getCompany());
            result = dataService;

            // The dataService will try to execute a query
            try {
                dataService.executeQuery(anyString);
            } catch (FMSException e) {
            }
            result = queryResult;

            // The data service will try to add an Item
            try {
                dataService.add(withArgThat(new SalesItemQBOItemMatcher(salesItem)));
            } catch (Exception e) {
            }
            Item item = new Item();
            item.setId(qboItemID);
            result = item;

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

        // Create a sales item
        final Customer customer = new Customer("Bob","Shmob","a@a.com","");
        final String qboCustomerID = "1";
        customer.setCompany(new Company());
        Account account = new Account();
        account.setName("Foo");
        account.setDescription("Bar");
        account.setAccountType(AccountTypeEnum.INCOME);

        // Create fake query results to return
        final QueryResult queryResult = new QueryResult();
        List<Account> accounts = new ArrayList<>();
        accounts.add(account);
        queryResult.setEntities(accounts);

        // Establish a sequence of expectations.
        new Expectations() {{
            // The QBO gateway will try to acquire a data service instance
            dataServiceFactory.getDataService(customer.getCompany());
            result = dataService;

//            // The dataService will try to execute a query
//            try {
//                dataService.executeQuery(anyString);
//            } catch (FMSException e) {
//            }
//            result = queryResult;

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

        public SalesItemQBOItemMatcher(SalesItem salesItem) {
            this.salesItemToMatch = salesItem;
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

            // If we reach this return statement all assertions have passed.
            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("The QBO Item did not match the SalesItem");
        }
    }

    public class CustomerQBOCustomerMatcher extends  TypeSafeMatcher<com.intuit.ipp.data.Customer> {
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
