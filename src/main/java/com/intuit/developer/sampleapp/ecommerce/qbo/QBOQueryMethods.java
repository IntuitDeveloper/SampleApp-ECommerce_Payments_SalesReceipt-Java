package com.intuit.developer.sampleapp.ecommerce.qbo;

import com.intuit.developer.sampleapp.ecommerce.domain.SalesItem;
import com.intuit.ipp.core.IEntity;
import com.intuit.ipp.data.*;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.services.QueryResult;

import java.lang.Class;
import java.util.List;

/**
 * This class contains convenience wrappers around the basic DataService#executeQuery() method to query for various entities
 * These wrapper encapsulate some error handling, query formation, and type casting to make other code more readable.
 * Created by akuchta on 9/12/14.
 */
public class QBOQueryMethods {
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
     * Get a ReferenceType wrapper for a QBO PaymentMethod
     *
     * ReferenceType values are used to associate different QBO entities to each other.
     */
    public static ReferenceType findPaymentMethodReference(DataService dataService, String paymentMethodName) {
        PaymentMethod paymentMethod = findPaymentMethod(dataService, paymentMethodName);
        if (paymentMethod == null) {
            throw new RuntimeException(String.format("Could not find a payment method with name: %s", paymentMethodName));
        }
        ReferenceType referenceType = new ReferenceType();
        referenceType.setValue(paymentMethod.getId());
        referenceType.setName(paymentMethod.getName());
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
     * Finds a QBO item where the item's name equals the passed in salesItem's name.
     */
    public static final String ITEM_QUERY = "select * from item where active = true and name = '%s'";
    public static com.intuit.ipp.data.Item findItem(DataService dataService, SalesItem salesItem) {
        String query = String.format(ITEM_QUERY, salesItem.getName());
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
}
