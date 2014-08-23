package com.intuit.developer.sampleapp.ecommerce.mappers;

import com.intuit.developer.sampleapp.ecommerce.domain.Customer;
import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

/**
 * Created with IntelliJ IDEA.
 * User: russellb337
 * Date: 8/21/14
 * Time: 8:58 AM
 */
public class CustomerMapper {

    private static BoundMapperFacade<Customer, com.intuit.ipp.data.Customer> domainToQBOMapper;

    static {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

        mapperFactory.classMap(Customer.class, com.intuit.ipp.data.Customer.class)
                .field("firstName", "givenName")
                .field("lastName", "familyName")
                .field("emailAddress", "primaryEmailAddr.address")
                .field("phoneNumber", "primaryPhone.freeFormNumber")
                .byDefault()
                .register();

        domainToQBOMapper = mapperFactory.getMapperFacade(Customer.class, com.intuit.ipp.data.Customer.class);

    }

    public static com.intuit.ipp.data.Customer buildQBOObject(Customer pSalesReceipt) {

        if (pSalesReceipt == null) {
            return null;
        }

        final com.intuit.ipp.data.Customer qboCustomer = domainToQBOMapper.map(pSalesReceipt);

        return qboCustomer;

    }
}
