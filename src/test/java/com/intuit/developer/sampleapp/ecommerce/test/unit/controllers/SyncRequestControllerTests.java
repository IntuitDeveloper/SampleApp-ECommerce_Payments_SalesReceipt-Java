package com.intuit.developer.sampleapp.ecommerce.test.unit.controllers;

import com.intuit.developer.sampleapp.ecommerce.controllers.SyncRequest;
import com.intuit.developer.sampleapp.ecommerce.controllers.SyncRequestController;
import com.intuit.developer.sampleapp.ecommerce.domain.Company;
import com.intuit.developer.sampleapp.ecommerce.domain.Customer;
import com.intuit.developer.sampleapp.ecommerce.domain.SalesItem;
import com.intuit.developer.sampleapp.ecommerce.qbo.QBOGateway;
import com.intuit.developer.sampleapp.ecommerce.repository.CompanyRepository;
import mockit.*;
import org.joda.money.Money;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static junit.framework.Assert.*;
/**
 * Created by connorm659 on 8/27/14.
 */
public class SyncRequestControllerTests {
    @Tested
    SyncRequestController controller;

    @Injectable
    QBOGateway mockedQBOGateway;

    @Injectable
    CompanyRepository companyRepository;

    @Test
    public void testCustomerSync() {
        final Customer customer = new Customer("firstName", "lastName", "emailAddress", "phoneNumber");
        final Company company = new Company("accessToken", "accessTokenSecret", "1234567");
        company.addCustomer(customer);

        SyncRequest syncRequest = new SyncRequest();
        syncRequest.setCompanyId("1234");
        syncRequest.setType(SyncRequest.EntityType.Customer);

        new NonStrictExpectations(){{
            companyRepository.findOne(anyLong);
            result = company;
        }};

        SyncRequest syncRequestReturn = controller.createSyncRequest(syncRequest);
        assertTrue(syncRequestReturn.isSuccessful());
        assertTrue(company.isCustomersSynced());
        assertFalse(company.isSalesItemSynced());

        new Verifications() {{
            mockedQBOGateway.createCustomerInQBO(withSameInstance(customer)); times = 1;
            mockedQBOGateway.createItemInQBO(withInstanceOf(SalesItem.class)); times = 0;
            companyRepository.save(withSameInstance(company)); times = 1;
        }};
    }

    @Test
    public void testSalesItemSync() {
        final SalesItem salesItem = new SalesItem("name", "description", Money.parse("USD 1"), "imageFile");
        final Company company = new Company("accessToken", "accessTokenSecret", "1234567");
        company.addServiceItem(salesItem);

        SyncRequest syncRequest = new SyncRequest();
        syncRequest.setCompanyId("1234");
        syncRequest.setType(SyncRequest.EntityType.SalesItem);

        new NonStrictExpectations(){{
            companyRepository.findOne(anyLong);
            result = company;
        }};

        SyncRequest syncRequestReturn = controller.createSyncRequest(syncRequest);
        assertTrue(syncRequestReturn.isSuccessful());
        assertTrue(company.isSalesItemSynced());
        assertFalse(company.isCustomersSynced());

        new Verifications() {{
            mockedQBOGateway.createItemInQBO(withSameInstance(salesItem)); times = 1;
            mockedQBOGateway.createCustomerInQBO(withInstanceOf(Customer.class)); times = 0;
            companyRepository.save(withSameInstance(company)); times = 1;
        }};
    }
}
