package com.intuit.developer.sampleapp.ecommerce.repository;

import com.intuit.developer.sampleapp.ecommerce.domain.AppInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RestResource;

/**
 * Created with IntelliJ IDEA.
 * User: russellb337
 * Date: 6/24/14
 * Time: 1:01 PM
 */
@RestResource(exported = true)
public interface AppInfoRepository extends PagingAndSortingRepository<AppInfo, Long> {

    @Query("select ai from AppInfo ai where ai.id = '1'")
        //wish there was a way in JPA to put a limit here
    AppInfo getFirst();

}
