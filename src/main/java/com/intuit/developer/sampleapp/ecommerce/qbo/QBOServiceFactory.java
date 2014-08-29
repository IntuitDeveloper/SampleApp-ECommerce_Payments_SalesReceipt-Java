package com.intuit.developer.sampleapp.ecommerce.qbo;

import com.intuit.developer.sampleapp.ecommerce.domain.AppInfo;
import com.intuit.developer.sampleapp.ecommerce.domain.Company;
import com.intuit.developer.sampleapp.ecommerce.repository.AppInfoRepository;
import com.intuit.ipp.core.Context;
import com.intuit.ipp.core.ServiceType;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.security.IAuthorizer;
import com.intuit.ipp.security.OAuthAuthorizer;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.services.payment.ChargeService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Created with IntelliJ IDEA.
 * User: russellb337
 * Date: 8/21/14
 * Time: 5:59 PM
 */
public class QBOServiceFactory {
    @Autowired
    private AppInfoRepository appInfoRepository;

    public DataService getDataService(Company domainCompany) {
        return new DataService(buildContext(domainCompany, ServiceType.QBD));
    }

    public ChargeService getChargeService(Company domainCompany) {
        return new ChargeService(buildContext(domainCompany, ServiceType.QBO));
    }

    private Context buildContext(Company domainCompany, ServiceType serviceType) {
        verifyCompanyConnectedToQBO(domainCompany);

        final AppInfo appInfo = appInfoRepository.getFirst();

        IAuthorizer authorizer = new OAuthAuthorizer(appInfo.getConsumerKey(),
                appInfo.getConsumerSecret(),
                domainCompany.getAccessToken(),
                domainCompany.getAccessTokenSecret());

        Context context;
        try {
            context = new Context(authorizer, serviceType, domainCompany.getQboId());
        } catch (FMSException e) {
            throw new RuntimeException("Could not initialize Intuit context object", e);
        }

        return context;
    }

    private void verifyCompanyConnectedToQBO(Company company) {
        if (!company.isConnectedToQbo()) {
            throw new RuntimeException("Company is not connected to QBO: " + company.getName());
        }
    }
}
