package com.intuit.developer.sampleapp.ecommerce.repository;

import com.intuit.developer.sampleapp.ecommerce.domain.CartItem;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * relationship that describes a SalesItem that has been added to a ShoppingCart
 */
@RepositoryRestResource
public interface CartItemRepository extends PagingAndSortingRepository<CartItem, Long> {
    List<CartItem> findByShoppingCartId(@Param("shoppingCartId") long shoppingCartId);
}
