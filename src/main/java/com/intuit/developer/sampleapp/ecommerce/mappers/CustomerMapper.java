package com.intuit.developer.sampleapp.ecommerce.mappers;

import com.intuit.developer.sampleapp.ecommerce.domain.Customer;
import com.intuit.ipp.data.PhysicalAddress;
import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
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
                .field("city", "billAddr.city")
                .field("country", "billAddr.country")
                .field("postalCode", "billAddr.postalCode")
                .field("countrySubDivisionCode", "billAddr.countrySubDivisionCode")
                .field("line1", "billAddr.line1")
                .field("line2", "billAddr.line2")
                .field("city", "shipAddr.city")
                .field("country", "shipAddr.country")
                .field("postalCode", "shipAddr.postalCode")
                .field("countrySubDivisionCode", "shipAddr.countrySubDivisionCode")
                .field("line1", "shipAddr.line1")
                .field("line2", "shipAddr.line2")
                .field("emailAddress", "primaryEmailAddr.address")
                .field("phoneNumber", "primaryPhone.freeFormNumber")
                .customize( new CustomMapper<Customer, com.intuit.ipp.data.Customer>() {
                    @Override
                    public void mapAtoB(Customer customer, com.intuit.ipp.data.Customer customer2, MappingContext context) {
                        PhysicalAddress physicalAddress = new PhysicalAddress();
                        physicalAddress.setCountrySubDivisionCode(customer.getCountrySubDivisionCode());
                        physicalAddress.setCountry(customer.getCountry());
                        physicalAddress.setPostalCode(customer.getPostalCode());
                        physicalAddress.setCity(customer.getCity());
                        physicalAddress.setLine1(customer.getLine1());
                        physicalAddress.setLine2(customer.getLine2());
                        customer2.setBillAddr(physicalAddress);
                        customer2.setShipAddr(physicalAddress);
                    }
                })
                .exclude("id")
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
