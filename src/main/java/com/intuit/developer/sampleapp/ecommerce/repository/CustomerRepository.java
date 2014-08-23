package com.intuit.developer.sampleapp.ecommerce.repository;

import com.intuit.developer.sampleapp.ecommerce.domain.Customer;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Created with IntelliJ IDEA.
 * User: russellb337
 * Date: 8/20/14
 * Time: 3:41 PM
 */
@RepositoryRestResource
public interface CustomerRepository extends PagingAndSortingRepository<Customer, Long> {

}
