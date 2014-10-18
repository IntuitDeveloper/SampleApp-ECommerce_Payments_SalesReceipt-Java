eCommerce Java Sample App
===

Welcome to the Intuit Developer's eCommerce Java Sample App. 

This sample app is meant to provide working examples of how to integrate your app with the Intuit Small Business ecosystem.  Specifically, this sample application demonstrates the following:

1. Implementing OAuth to connect an application to a customer's QuickBooks Online company.
2. Syncing customer and service item data from the app's local database to the QuickBooks Online company.
3. Processing a credit card payment to QuickBooks Online Payments.
4. Create a SalesReceipt in the QuickBooks Online company.

Please note that while these examples work, features not called out above are not intended to be taken and used in production business applications. In other words, this is not a seed project to be taken cart blanche and deployed to your production environment.  

For example, certain concerns are not addressed at all in our samples (e.g. security, privacy, scalability). In our sample apps, we strive to strike a balance between clarity, maintainability, and performance where we can. However, clarity is ultimately the most important quality in a sample app.

Therefore there are certain instances where we might forgo a more complicated implementation (e.g. caching a frequently used value, robust error handling, more generic domain model structure) in favor of code that is easier to read. In that light, we welcome any feedback that makes our samples apps easier to learn from.

## Table of Contents

* [Requirements](#requirements)
* [First Use Instructions](#first-use-instructions)
* [Running the code](#running-the-code)
* [Project Structure](#project-structure)
* [Importing into IntelliJ IDEA & Eclipse](#importing-into-intellij-idea--eclipse)
  * [IntelliJ IDEA](#intellij-idea)
  * [Eclipse](#eclipse)
* [How To Guides](#how-to-guides)
* [Cleaning up the database](#cleaning-up-the-database)
* [Testing the code](#testing-the-code)
  * [Java Junit Tests](#java-junit-tests)
  * [Javascript Karma tests](#javascript-karma-tests)


### Project Structure
* **The Java code for integrating with the QuickBooks Online Accounting and Payments APIs is located in the [`src`](src) directory.**
    *  For OAuth implementation see:
        - [`OAuthController.java`](src/main/java/com/intuit/developer/sampleapp/ecommerce/oauth/controllers/OAuthController.java)
        - [`OAuthInfoProvider.java`](src/main/java/com/intuit/developer/sampleapp/ecommerce/oauth/OAuthInfoProvider.java)
        - [`OAuthInfoProviderImpl.java`](src/main/java/com/intuit/developer/sampleapp/ecommerce/controllers/OAuthInfoProviderImpl.java)
    *  For QBO V3 Java SDK usage see:
        - [`QBOGateway.java`](src/main/java/com/intuit/developer/sampleapp/ecommerce/qbo/QBOGateway.java)
        - [`PaymentGateway.java`](src/main/java/com/intuit/developer/sampleapp/ecommerce/qbo/PaymentGateway.java)
        - [`QBOServiceFactory.java`](src/main/java/com/intuit/developer/sampleapp/ecommerce/qbo/QBOServiceFactory.java)
* The Java code for the rest of the application is located in the [`src-general`](src-general) directory
* The HTML, CSS and JavaScript code for the web-based client are is located in the [`public`](public) directory
