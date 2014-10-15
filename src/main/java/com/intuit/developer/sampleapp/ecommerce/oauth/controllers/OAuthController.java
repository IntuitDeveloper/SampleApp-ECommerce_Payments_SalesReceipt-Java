package com.intuit.developer.sampleapp.ecommerce.oauth.controllers;

import com.intuit.developer.sampleapp.ecommerce.oauth.CompanyRequestTokenSecret;
import com.intuit.developer.sampleapp.ecommerce.oauth.OAuthInfoProvider;
import com.intuit.ia.connection.IAPlatformClient;
import com.intuit.ia.exception.OAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * For a full walk through of this class and what is required for implementing OAuth, please visit our HowTo Guide:
 * https://github.com/IntuitDeveloper/SampleApp-TimeTracking_Invoicing-Java/wiki/OAuth-How-To-Guide-(Java)
 *
 * TODO: add description of OAuth
 */
@RestController
public class OAuthController {

    @Autowired
    private OAuthInfoProvider oAuthInfoProvider;

    /**
     *
     * This is the first REST endpoint invoked in the OAuth 1.0 flow.
	 *
	 * It is redirected to by Intuit, when the user clicks on the 'Connect
	 * to Quick Books' button from the Setup Page to get the 'Request Token'.
	 *
	 * In other words, the URL required to invoke this method should be put in the 'Connect To QuickBooks' button's
	 * "grantUrl" parameter.
	 *
	 * For example in your Javascript you would do the following:
	 *
	 * intuit.ipp.anywhere.setup({grantUrl: https://myawesomeapp.com/request_token?companyId=<companyIdInYourApp>});
	 */

    @RequestMapping(value = "/request_token", method = RequestMethod.GET)
    public void requestOAuthToken(final HttpServletResponse response,
                                  @RequestParam(value = "appCompanyId", required = true) String companyId) throws IOException {

        //Instantiate the QuickBook SDK's IAPlatformClient object
        IAPlatformClient client = new IAPlatformClient();
        try {
            //Use the IAPlatformClient to get a Request Token and Request Token Secret from Intuit
            final Map<String, String> requestTokenAndSecret =
		            client.getRequestTokenAndSecret(oAuthInfoProvider.getConsumerKey(), oAuthInfoProvider.getConsumerSecret());

            if (requestTokenAndSecret == null) {
                throw new RuntimeException("The system does not recognize the provided consumer key.  The development consumer key is only recognized in the sandbox environment; the production consumer key is only recognized in the 'published' environment.");
            }

            //Pull the values out of the map
            final String requestToken = requestTokenAndSecret.get("requestToken");
            final String requestTokenSecret = requestTokenAndSecret.get("requestTokenSecret");

            //Persist the request token and request token secret in the app database on the given company, we will need the
            //Request Token Secret to make the final request to Intuit for the Access Tokens
            oAuthInfoProvider.setRequestTokenValuesForCompany(companyId, requestToken, requestTokenSecret);

            // Retrieve the Authorize URL
            final String authURL = client.getOauthAuthorizeUrl(requestToken);

            // Redirect to the authorized URL page and retrieve the verifier code.
            response.sendRedirect(authURL);

        } catch (OAuthException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * This is the second REST endpoint invoked in the OAuth 1.0 flow.
     *
     * Using information received during the first endpoint (e.g. RequestToken and RequestTokenSecret), it makes a final request to Intuit's OAuth servers to
     * get the AccessToken and AccessTokenSecret. It then persists the AccessToken And AccessTokenSecret on the the sample app's
     * company object using the OAuthInfoProvider.
     *
     * Finally it redirects to the "close.html" page so that the popup, in which the OAuth 1.0 flow took place in, is closed
     * and the parent page is refreshed.
     *
     */
    @RequestMapping(value = "/request_token_ready", method = RequestMethod.GET)
    public void requestTokenReady(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        IAPlatformClient client = new IAPlatformClient();

        final String verifierCode = request.getParameter("oauth_verifier");
        final String realmID = request.getParameter("realmId");
        final String requestToken = request.getParameter("oauth_token");

        final CompanyRequestTokenSecret companyRequestTokenSecret = oAuthInfoProvider.getCompanyRequestTokenSecret(requestToken);

        try {
            final Map<String, String> oAuthAccessToken = client.getOAuthAccessToken(verifierCode, requestToken, companyRequestTokenSecret.getRequestTokenSecret(),
                    oAuthInfoProvider.getConsumerKey(), oAuthInfoProvider.getConsumerSecret());

            final String accessToken = oAuthAccessToken.get("accessToken");
            final String accessTokenSecret = oAuthAccessToken.get("accessTokenSecret");

            oAuthInfoProvider.setAccessTokenForCompany(companyRequestTokenSecret.getAppCompanyId(), realmID,
                   accessToken, accessTokenSecret);


            response.sendRedirect(getProtocolHostnameAndPort(request) + "/app/close.html");


        } catch (OAuthException e) {
            throw new RuntimeException(e);
        }

    }

    public static String getProtocolHostnameAndPort(final HttpServletRequest request) {
        String protocol = request.getProtocol().split("/")[0].toLowerCase();
        String hostname = request.getServerName();
        int port = request.getServerPort();

        StringBuilder result = new StringBuilder(protocol + "://" + hostname);
        if (port != 80) {
            result.append(":").append(port);
        }

        return result.toString();
    }
}
