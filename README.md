eCommerce Java Sample App
===


For setup and run instructions, as well as topical guides, go to:
[Intuit Developer Documentation](https://developer.intuit.com/v2/docs/0000_about_intuit_developer/0060_sample_app_tutorials/ecommerce)


### Project Structure
* **The Java code for integrating with the QuickBooks Online Accounting and Payments APIs is located in the `src` directory.**
    *  For OAuth implementation see:
        - [`OAuthController.java`](src/main/java/com/intuit/developer/sampleapp/ecommerce/controller/OAuthController.java)
        - [`OAuthInfoProvider.java`](src/main/java/com/intuit/developer/sampleapp/ecommerce/oauth/OAuthInfoProvider.java)
        - [`OAuthInfoProviderImpl.java`](src/main/java/com/intuit/developer/sampleapp/ecommerce/controllers/OAuthInfoProviderImpl.java)
    *  For QBO V3 Java SDK usage see:
        - [`QBOGateway.java`](src/main/java/com/intuit/developer/sampleapp/ecommerce/qbo/QBOGateway.java)
        - [`PaymentGateway.java`](src/main/java/com/intuit/developer/sampleapp/ecommerce/qbo/PaymentGateway.java)
        - [`QBOServiceFactory.java`](src/main/java/com/intuit/developer/sampleapp/ecommerce/qbo/QBOServiceFactory.java)
* The Java code for the rest of the application is located in the `src-general` directory
* The HTML, CSS and JavaScript code for the web-based client are is located in the `public` directory
