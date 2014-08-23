package com.intuit.developer.sampleapp.ecommerce.controllers;

import com.intuit.developer.sampleapp.ecommerce.domain.Company;
import com.intuit.developer.sampleapp.ecommerce.oauth.CompanyRequestTokenSecret;
import com.intuit.developer.sampleapp.ecommerce.oauth.OAuthException;
import com.intuit.developer.sampleapp.ecommerce.oauth.OAuthInfoProvider;
import com.intuit.developer.sampleapp.ecommerce.repository.AppInfoRepository;
import com.intuit.developer.sampleapp.ecommerce.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created with IntelliJ IDEA.
 * User: russellb337
 * Date: 7/17/14
 * Time: 4:17 PM
 */
public class OAuthInfoProviderImpl implements OAuthInfoProvider {

    @Autowired
    private AppInfoRepository appInfoRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Override
    public String getAppToken() {
        return appInfoRepository.getFirst().getAppToken();
    }

    @Override
    public String getConsumerKey() {
        return appInfoRepository.getFirst().getConsumerKey();
    }

    @Override
    public String getConsumerSecret() {
        return appInfoRepository.getFirst().getConsumerSecret();
    }

    @Override
    public CompanyRequestTokenSecret getCompanyRequestTokenSecret(String requestToken) {
        final Company company = companyRepository.findByRequestToken(requestToken);

        if (company == null) {
            throw new OAuthException("Could not find a company with an request token of " + requestToken);
        }

        return new CompanyRequestTokenSecret("" + company.getId(), company.getRequestTokenSecret());
    }


    @Override
    public void setRequestTokenValuesForCompany(String appCompanyId, String requestToken, String requestTokenSecret) {
        final Company company = companyRepository.findOne(Long.parseLong(appCompanyId));

        if (company == null) {
            throw new OAuthException("Could not find a company with an id of " + appCompanyId);
        }

        company.setRequestToken(requestToken);
        company.setRequestTokenSecret(requestTokenSecret);

        companyRepository.save(company);

    }

    @Override
    public void setAccessTokenForCompany(String appCompanyId, String realmId, String accessToken, String accessTokenSecret) {
        final Company company = companyRepository.findOne(Long.parseLong(appCompanyId));

        if (company == null) {
            throw new OAuthException("Could not find a company with an id of " + appCompanyId);
        }

        company.setQboId(realmId);
        company.setAccessToken(accessToken);
        company.setAccessTokenSecret(accessTokenSecret);
        company.setConnectedToQbo(true);

        companyRepository.save(company);

    }


}
