package com.intuit.developer.sampleapp.ecommerce.repository;

import com.intuit.developer.sampleapp.ecommerce.domain.ShoppingCart;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface ShoppingCartRepository extends PagingAndSortingRepository<ShoppingCart, Long> {
    ShoppingCart findByCustomerId(@Param("customerId") long customerId);
    ShoppingCart findByCustomerEmailAddress(@Param("emailAddress") String emailAddress);
}
