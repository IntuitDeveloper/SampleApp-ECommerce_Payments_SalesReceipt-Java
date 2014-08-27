package com.intuit.developer.sampleapp.ecommerce.repository;

import com.intuit.developer.sampleapp.ecommerce.domain.CartItem;
import com.intuit.developer.sampleapp.ecommerce.domain.SalesItem;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * relationship that describes a SalesItem that has been added to a ShoppingCart
 */
@RepositoryRestResource
public interface CartItemRepository extends PagingAndSortingRepository<CartItem, Long> {
}
