package com.intuit.developer.sampleapp.ecommerce.oauth.controllers;

import com.intuit.developer.sampleapp.ecommerce.domain.Company;
import com.intuit.developer.sampleapp.ecommerce.qbo.QBOGateway;
import com.intuit.developer.sampleapp.ecommerce.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 */
@RestController
@RequestMapping(value = "/syncrequest", consumes = "application/json", produces = "application/json")
public class SyncRequestController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SyncRequestController.class);

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private QBOGateway qboGateway;

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public SyncRequest createSyncRequest(@RequestBody final SyncRequest syncRequest) {

		final Company company = companyRepository.findOne(Long.parseLong(syncRequest.getCompanyId()));

		int successfulSyncs = 0;

		switch (syncRequest.getType()) {
			case Customer:
                qboGateway.createCustomersInQBO(company.getCustomers());
				company.setCustomersSynced(true);
                successfulSyncs = company.getCustomers().size();
				break;
			case SalesItem:
                qboGateway.createItemsInQBO(company.getSalesItems());
				company.setSalesItemSynced(true);
                successfulSyncs = company.getSalesItems().size();
				break;
		}

		companyRepository.save(company);

		syncRequest.setMessage("Synced " + successfulSyncs + " " + syncRequest.getType().name() + " objects to QBO");
		syncRequest.setSuccessful(true);

		return syncRequest;
	}

}