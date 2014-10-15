package com.intuit.developer.sampleapp.ecommerce.repository;

import com.intuit.developer.sampleapp.ecommerce.domain.Company;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: russellb337
 * Date: 6/17/14
 * Time: 5:45 PM
 */

@RepositoryRestResource
public interface CompanyRepository extends PagingAndSortingRepository<Company, Long> {

	List<Company> findById(@Param("id") String id);
    List<Company> findByQboId(@Param("qboId") String qboId);

    Company findByRequestToken(@Param("requestToken") String requestToken);

    /*
    Used in End To End Tests
     */
    @Modifying
    @Transactional
    @Query("delete from Company")
    void deleteCompanies();

}
