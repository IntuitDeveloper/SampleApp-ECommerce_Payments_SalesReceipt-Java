package com.intuit.developer.sampleapp.ecommerce;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.developer.sampleapp.ecommerce.domain.*;
import com.intuit.developer.sampleapp.ecommerce.repository.*;
import com.intuit.ipp.util.Config;
import org.apache.commons.io.FileUtils;
import org.joda.money.Money;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: russellb337
 * Date: 8/20/14
 * Time: 3:32 PM
 */
public class DataLoader {

	/**
     * Loads oauth information from oauth.json, which is expected to be in the project root
     *
     * @param context
     */
    public static void initializeData(ConfigurableApplicationContext context) {
        if (oauthInfoNeeded(context)) {
            try {
                final File file = new File("oauth.json");
                final String jsonStr = FileUtils.readFileToString(file);
                ObjectMapper mapper = new ObjectMapper();
                final JsonNode jsonNode = mapper.readTree(jsonStr);

                createAppInfo(jsonNode, context);
                createCompany(context);
                createSystemProperties(context);

            } catch (IOException e) {
                throw new RuntimeException("Failed to read oauth information from oauth.json. Please make sure" +
                        " oauth.json is in the root of the project directory. This file should contain your appToken," +
                        " consumerKey, and consumerSecret which can be copied from the intuit developer portal. See the" +
                        " readme for more information.");
            }
        }
    }

    private static void createSystemProperties(ConfigurableApplicationContext springContext) {
        final SystemPropertyRepository repository = springContext.getBean(SystemPropertyRepository.class);

        //create the QBO UI URL system property
        String qboUiHostname;

        //grab the API URL from the SDK configuration
        final String qboApiURL = Config.getProperty(Config.BASE_URL_QBO);

        switch (qboApiURL) {
            case "https://quickbooks.api.intuit.com/v3/company":
                qboUiHostname = "qbo.intuit.com";
                break;
            case "https://sandbox-quickbooks.api.intuit.com/v3/company":
                qboUiHostname = "sandbox.qbo.intuit.com";
                break;
            default:
                qboUiHostname = "qa.qbo.intuit.com";
        }

        SystemProperty systemProperty = new SystemProperty("qboUiHostname", qboUiHostname);
        repository.save(systemProperty);

    }

	private static void createCompany(ConfigurableApplicationContext springContext) {
        final CompanyRepository repository = springContext.getBean(CompanyRepository.class);

        if (repository.count() == 0) {
	        System.out.println("No company data in the app, creating data");

            Company company = new Company("SBO eCommerce Account");
            repository.save(company);

            createSalesItems(company, springContext);
            createCustomers(company, springContext);
        }
    }

    private static void createSalesItems(Company company, ConfigurableApplicationContext springContext) {
        final SalesItemRepository repository = springContext.getBean(SalesItemRepository.class);

        final SalesItem salesItem1 = new SalesItem("Jersey", "League style", Money.parse("USD 75.00"), "IntuitWestBromAlbionJersey.jpg");
        salesItem1.setQtyOnHand(new BigDecimal(5));
        company.addSalesItem(salesItem1);
	    repository.save(salesItem1);

	    final SalesItem salesItem2 = new SalesItem("Men's Bike Jersey", "Tour de roads in style", Money.parse("USD 82.88"), "IntuitBikeJersey.jpg");
        salesItem2.setQtyOnHand(new BigDecimal(5));

	    company.addSalesItem(salesItem2);
        repository.save(salesItem2);

        final SalesItem salesItem3 = new SalesItem("Hoodie", "Silicon Valley poseur style", Money.parse("USD 24.50"), "IntuitHoodie.jpg");
        salesItem3.setQtyOnHand(new BigDecimal(5));
        company.addSalesItem(salesItem3);
        repository.save(salesItem3);

        final SalesItem salesItem4 = new SalesItem("Classic Polo", "Golf course style", Money.parse("USD 24.50"), "IntuitBlackPolo.jpg");
        salesItem4.setQtyOnHand(new BigDecimal(5));
        company.addSalesItem(salesItem4);
        repository.save(salesItem4);
    }

    private static void createCustomers(Company company, ConfigurableApplicationContext springContext) {
        final CustomerRepository repository = springContext.getBean(CustomerRepository.class);

        final Customer customer1 = new Customer("Aashi", "Mittal", "aashi.mittal@example.com", "555-555-5555");
        customer1.setCity("Middlefield");
        customer1.setPostalCode("98756");
        customer1.setCountry("United States");
        customer1.setLine1("333 Easy Street");
        customer1.setCountrySubDivisionCode("CA");
        company.addCustomer(customer1);
	    repository.save(customer1);
        createShoppingCart(customer1, springContext);
    }

    private static void createShoppingCart(Customer customer, ConfigurableApplicationContext springContext) {
        ShoppingCart shoppingCart = new ShoppingCart(customer);
        customer.setShoppingCart(shoppingCart);

        ShoppingCartRepository shoppingCartRepository = springContext.getBean(ShoppingCartRepository.class);
        shoppingCartRepository.save(shoppingCart);

        CustomerRepository customerRepository = springContext.getBean(CustomerRepository.class);
        customerRepository.save(customer);
    }

    private static boolean oauthInfoNeeded(ConfigurableApplicationContext context) {
        AppInfoRepository appInfoRepository = context.getBean(AppInfoRepository.class);
        return appInfoRepository.count() == 0;
    }


    private static AppInfo createAppInfo(JsonNode jsonNode, ConfigurableApplicationContext context) {
        AppInfoRepository repository = context.getBean(AppInfoRepository.class);
        SystemPropertyRepository systemPropertyRepository = context.getBean(SystemPropertyRepository.class);
        final JsonNode jsonAppInfo = jsonNode.get("appInfo");

        String appToken;
        String consumerKey;
        String consumerSecret;

        try {
            appToken = jsonAppInfo.get("appToken").asText();
            consumerKey = jsonAppInfo.get("consumerKey").asText();
            consumerSecret = jsonAppInfo.get("consumerSecret").asText();
        } catch (NullPointerException e) {
            RuntimeException rte = new RuntimeException("Exception occurred loading oauth.json verify that file contains valid json and that field names are correct.");
            rte.setStackTrace(e.getStackTrace());
            throw rte;
        }

        if (appToken.isEmpty()) {
            throw new RuntimeException("In 'oauth.json': 'appToken' property is empty");
        }
        if (consumerKey.isEmpty()) {
            throw new RuntimeException("In 'oauth.json': 'consumerKey' property is empty");
        }
        if (consumerSecret.isEmpty()) {
            throw new RuntimeException("In 'oauth.json': 'consumerSecret' property is empty");
        }

        AppInfo appInfo = new AppInfo(appToken,consumerKey, consumerSecret);
        repository.save(appInfo);

        SystemProperty systemProperty = new SystemProperty("appToken", appToken);
        systemPropertyRepository.save(systemProperty);

        return appInfo;
    }

}
