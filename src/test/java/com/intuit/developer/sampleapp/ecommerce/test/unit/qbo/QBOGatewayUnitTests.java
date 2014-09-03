package com.intuit.developer.sampleapp.ecommerce.test.unit.qbo;

import com.intuit.developer.sampleapp.ecommerce.domain.*;
import com.intuit.developer.sampleapp.ecommerce.domain.Company;
import com.intuit.developer.sampleapp.ecommerce.domain.Customer;
import com.intuit.developer.sampleapp.ecommerce.qbo.QBOGateway;
import com.intuit.developer.sampleapp.ecommerce.qbo.QBOServiceFactory;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    public void testCreateItemInQBO() throws Exception {
        //
        // Establish a set of test data
        //

        // Create a sales item
        final SalesItem salesItem = new SalesItem("Shirt", "It's a shirt", Money.of(CurrencyUnit.USD, 5.00), "");
        salesItem.setQtyOnHand(new BigDecimal(5.0));
        final String qboItemID = "1";

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
            salesItemRepository.save(salesItem);
        }};
    }

    @Test
    public void testCreateCustomerInQBO() throws Exception {
        final String qboCustomerID = "1";
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
    public void testCreateSalesReceiptInQBO() throws Exception {
        // Create a a shopping cart
        final ShoppingCart shoppingCart = new ShoppingCart(customer);

        // Create a list of items for the cart
        List<CartItem> cartItems = new ArrayList<>();
        CartItem cartItem = new CartItem();
        cartItem.setSalesItem(new SalesItem("ItemType1","It's an item", Money.of(CurrencyUnit.USD, 5.00), ""));
        cartItem.setQuantity(2);
        cartItem.setShoppingCart(shoppingCart);
        cartItems.add(cartItem);
        cartItem = new CartItem();
        cartItem.setSalesItem(new SalesItem("ItemType2","It's another item", Money.of(CurrencyUnit.USD, 3.50), ""));
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
            assertEquals(4, lines.size());
            List<CartItem> cartItems = shoppingCart.getCartItems();

            // The first two lines items of the sales receipt should be the items added
            verifyLineForCartItem(lines.get(0), cartItems.get(0));
            verifyLineForCartItem(lines.get(1), cartItems.get(1));

            // The next line should be a discount
            verifyLineForDiscount(lines.get(2), shoppingCart);

            // The next line should be taxes
            verifyLineForTaxes(lines.get(3), shoppingCart);
        }};
    }

    private void verifyLineForCartItem(Line line, CartItem cartItem) {
        assertEquals(LineDetailTypeEnum.SALES_ITEM_LINE_DETAIL, line.getDetailType());
        SalesItemLineDetail lineDetail = line.getSalesItemLineDetail();
        assertEquals(
                "The unit prices should match",
                cartItem.getSalesItem().getUnitPrice().getAmount(),
                lineDetail.getUnitPrice());
        assertEquals("The item quantities should match",
                new BigDecimal(cartItem.getQuantity()),
                lineDetail.getQty());
        assertEquals(
                "The line total should be cart item qty * unit price",
                cartItem.getSalesItem().getUnitPrice().multipliedBy(cartItem.getQuantity()).getAmount(),
                line.getAmount());
    }

    private void verifyLineForDiscount(Line line, ShoppingCart shoppingCart) {
        assertEquals(LineDetailTypeEnum.DISCOUNT_LINE_DETAIL, line.getDetailType());
        DiscountLineDetail lineDetail = line.getDiscountLineDetail();
        assertTrue(lineDetail.isPercentBased());
        assertEquals(new BigDecimal(ShoppingCart.PROMOTION_PERCENTAGE), lineDetail.getDiscountPercent());
    }

    private void verifyLineForTaxes(Line line, ShoppingCart shoppingCart) {
        assertEquals(LineDetailTypeEnum.TAX_LINE_DETAIL, line.getDetailType());
        TaxLineDetail lineDetail = line.getTaxLineDetail();
        assertEquals(shoppingCart.getTax().getAmount(), line.getAmount());
        assertEquals(new BigDecimal(ShoppingCart.TAX_PERCENTAGE), lineDetail.getTaxPercent());
    }
}
