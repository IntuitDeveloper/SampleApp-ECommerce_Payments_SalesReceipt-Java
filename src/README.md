/src
====

In this directory you will find the most pertinent files for integrating with the QuickBooks Online Accounting and Payments APIs using the QBO V3 Java SDK.

By contrast, the code in the [`src-general`](../src-general) directory is not specific or pertinent to QuickBooks Online integration.

For OAuth implementation see:
  - [`OAuthController.java`](main/java/com/intuit/developer/sampleapp/ecommerce/controllers/OAuthController.java)
  - [`OAuthInfoProvider.java`](main/java/com/intuit/developer/sampleapp/ecommerce/oauth/OAuthInfoProvider.java)
  - [`OAuthInfoProviderImpl.java`](main/java/com/intuit/developer/sampleapp/ecommerce/controllers/OAuthInfoProviderImpl.java)

For QBO V3 Java SDK usage see:
  - [`QBOGateway.java`](main/java/com/intuit/developer/sampleapp/ecommerce/qbo/QBOGateway.java)
  - [`PaymentGateway.java`](main/java/com/intuit/developer/sampleapp/ecommerce/qbo/PaymentGateway.java)
  - [`QBOServiceFactory.java`](main/java/com/intuit/developer/sampleapp/ecommerce/qbo/QBOServiceFactory.java)
