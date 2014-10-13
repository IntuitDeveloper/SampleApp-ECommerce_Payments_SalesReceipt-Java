'use strict';

/* Services */


var ecommerceServices = angular.module('myApp.services', ['ngResource']);

ecommerceServices.value('version', '0.1');

ecommerceServices.factory('InitializerSvc', ['$rootScope', 'RootUrlSvc', 'CompanySvc', 'SalesItemSvc', 'CustomerSvc', 'ShoppingCartSvc', 'CartItemSvc', 'SystemPropertySvc',
    function ($rootScope, RootUrlSvc, CompanySvc, SalesItemSvc, CustomerSvc, ShoppingCartSvc, CartItemSvc, SystemPropertySvc) {

        var initialized = false;

        var initialize = function () {

            $rootScope.$on('api.loaded', function() {
                SystemPropertySvc.initializeModel();
                CompanySvc.initialize();
                SalesItemSvc.initialize();
                CustomerSvc.initialize();
                ShoppingCartSvc.initialize();
                CartItemSvc.initialize();

                CompanySvc.initializeModel();
            });

            $rootScope.$on('model.company.change', function() {
                SalesItemSvc.initializeModel();
                CustomerSvc.initializeModel();
            });

            RootUrlSvc.initialize();

            $rootScope.$on('$viewContentLoaded', function (scope, next, current) {
                /*
                 Every time we load a new view, we need to reinitialize the intuit anywhere library
                 so that the connect to quickbooks button is rendered properly
                 */
                if (initialized) { //only reinitialize from the 2nd time onwards
                    intuit.ipp.anywhere.init();
                }
                initialized = true;
            });
        };

        return {
            initialize: initialize
        }
    }]);

//A service which contains the current model (e.g. companies, items, etc)
ecommerceServices.factory('ModelSvc', ['$rootScope',
    function ($rootScope) {

        var model = {};
        model.company = {};

        var broadcastCompanyChange = function () {
            $rootScope.$broadcast('model.company.change');
        };

        var onCompanyChange = function ($scope, callback) {
            $scope.$on('model.company.change', function () {
                callback(model);
            });
        };

        return {
            model: model,
            onCompanyChange: onCompanyChange,
            broadcastCompanyChange: broadcastCompanyChange
        }
    }]);

//a service which reads the root of the API and stores all the resource urls
ecommerceServices.factory('RootUrlSvc', ['$resource', '$rootScope', '$location',
    function ($resource, $rootScope, $location) {

        var rootUrls = {};
        var apiRoot = function() {
            return $location.protocol() +"://" + $location.host() + ":9001";
        };

        var initialize = function () {
            $resource(apiRoot()).get(function (data) {
                var links = data._links;
                for (var link in  links) {
                    var href = links[link].href;
//                    console.log("Discovered the URL for " + link + ": " + href);
                    rootUrls[link] = href.split(/\{/)[0]; //chop off the template stuff
                }
                rootUrls['syncRequest'] = apiRoot() + "/syncrequest";  // non-discoverable
                rootUrls['orders'] = apiRoot() + "/orders";  // non-discoverable
                $rootScope.$broadcast('api.loaded');  //broadcast an event so that the CompanySvc can know to load the companies
            });
        };

        var oauthGrantUrl = function() {
            return apiRoot() + "/request_token";
        }

        var onApiLoaded = function ($scope, callback) {
            $scope.$on('api.loaded', function () {
                callback();
            });
        };

        return {
            initialize: initialize,
            rootUrls: rootUrls,
            onApiLoaded: onApiLoaded,
            oauthGrantUrl : oauthGrantUrl
        }
    }]);

//A service which deals with CRUD operations for companies
ecommerceServices.factory('CompanySvc', ['$resource', '$rootScope', 'RootUrlSvc', 'ModelSvc',
    function ($resource, $rootScope, RootUrlSvc, ModelSvc) {

        var Company;

        var initialize = function () {
            Company = $resource(RootUrlSvc.rootUrls.companies + ':companyId', {}, { query: {method: 'GET', isArray: false} });
        };

        var initializeModel = function() {
            Company.query(function (data) {
                var companies = data._embedded.companies;
                ModelSvc.model.companies = companies;
                ModelSvc.model.company = companies[0]; //select the first company for now
                ModelSvc.broadcastCompanyChange();

                var grantUrl = RootUrlSvc.oauthGrantUrl() + '?appCompanyId=' + ModelSvc.model.company.id;
                intuit.ipp.anywhere.setup({grantUrl: grantUrl});
            });
        };

        return {
            initialize: initialize,
            initializeModel: initializeModel
        }

    }]);


ecommerceServices.factory('SalesItemSvc', ['$resource', '$rootScope', 'RootUrlSvc', 'ModelSvc',
    function ($resource, $rootScope, RootUrlSvc, ModelSvc) {

        var SalesItem;

        var initialize = function() {
            SalesItem = $resource(RootUrlSvc.rootUrls.salesItems, {}, { query: {method: 'GET', isArray: false} });
        };

        var initializeModel = function() {
            SalesItem.query(function (data) {
                var salesItems = data._embedded.salesItems;
                ModelSvc.model.company.salesItems = salesItems;
            });
        }

        return {
            initialize: initialize,
            initializeModel: initializeModel
        }
    }]);


ecommerceServices.factory('CustomerSvc', ['$resource', '$rootScope', 'RootUrlSvc', 'ModelSvc', 'ShoppingCartSvc',
    function ($resource, $rootScope, RootUrlSvc, ModelSvc, ShoppingCartSvc) {

        var Customer;

        var initialize = function() {
            Customer  = $resource(RootUrlSvc.rootUrls.customers, {}, { query: {method: 'GET', isArray: false} });
        };

        var initializeModel = function() {
            Customer.query(function(data) {
                var customers = data._embedded.customers;
                ModelSvc.model.company.customers = customers;
                ModelSvc.model.customer = customers[0];  // auto-set the 'logged in' customer

                //TODO: move to an event handler for customer changed
                ShoppingCartSvc.initializeModel();
            });
        }

        return {
            initialize: initialize,
            initializeModel: initializeModel
        }
    }]);


ecommerceServices.factory('ShoppingCartSvc', ['$resource', '$rootScope', 'RootUrlSvc', 'ModelSvc',
    function ($resource, $rootScope, RootUrlSvc, ModelSvc) {

        var ShoppingCart;

        var initialize = function() {
            ShoppingCart = $resource(RootUrlSvc.rootUrls.shoppingCarts, {},
                {
                    forCustomer: {  method: 'GET',
                        url: RootUrlSvc.rootUrls.shoppingCarts + '/search/findByCustomerId',
                        params: {projection: 'order'},
                        isArray: false},

                    query: { method: 'GET', isArray: false}
                });
        };

        var initializeModel = function() {
            refreshShoppingCart();
        };

        var refreshShoppingCart = function() {
            var customerShoppingCart = ShoppingCart.forCustomer({customerId: ModelSvc.model.customer.id}, function() {
                ModelSvc.model.shoppingCart = customerShoppingCart._embedded.shoppingCarts[0];
            });
        };

        return {
            initialize: initialize,
            initializeModel: initializeModel,
            refreshShoppingCart: refreshShoppingCart
        }
    }]);


ecommerceServices.factory('CartItemSvc', ['$resource', '$rootScope', 'RootUrlSvc', 'ModelSvc',
    function ($resource, $rootScope, RootUrlSvc, ModelSvc) {

        var CartItem;

        var initialize = function() {
            CartItem = $resource(RootUrlSvc.rootUrls.cartItems, {},
                        {   query: { method: 'GET', isArray: false },
                            forShoppingCart: {
                                method: 'GET',
                                url: RootUrlSvc.rootUrls.cartItems + '/search/findByShoppingCartId',
                                params: {projection: 'summary'},
                                isArray: false}
                        });

            ModelSvc.model.shoppingCartItems = [];
        };

        var getCartItems = function() {
            if (ModelSvc.model.shoppingCart != 'undefined') {
                var shoppingCartItems = CartItem.forShoppingCart({shoppingCartId: ModelSvc.model.shoppingCart.id}, function (data) {
                    ModelSvc.model.shoppingCartItems =
                        shoppingCartItems._embedded ?
                            ModelSvc.model.shoppingCartItems = shoppingCartItems._embedded.cartItems : {};
                })
            }
        };

        var addCartItem = function(salesItem, shoppingCart) {
            var cartItem = new CartItem();
            cartItem.shoppingCart = shoppingCart._links.self.href.split(/\{/)[0];
            cartItem.salesItem = salesItem._links.self.href.split(/\{/)[0];
            cartItem.quantity = 1;
            cartItem.$save();
        };

        return {
            initialize: initialize,
            getCartItems: getCartItems,
            addCartItem: addCartItem
        }
    }]);

ecommerceServices.factory('OrderSvc', ['$http', '$rootScope', 'RootUrlSvc', 'ModelSvc',
    function ($http, $rootScope, RootUrlSvc, ModelSvc) {

        var sendOrder = function (creditCard, billingInfo, successCallback, errorCallback) {
            // step 1 - tokenize credit card info
            var request = {};

            request.card = {};
            var card = request.card;
            card.number = creditCard.number;
            card.expMonth = creditCard.expMonth;
            card.expYear = creditCard.expYear;
            card.cvc = creditCard.CVC;
            card.address = {};
            card.address.streetAddress = billingInfo.address;
            card.address.city = billingInfo.cityStateZip.split(",")[0];
            card.address.region = billingInfo.cityStateZip.split(",")[1].trim().split(" ")[0];
            card.address.country = "US";
            card.address.postalCode = billingInfo.cityStateZip.split(",")[1].trim().split(" ")[1];;

            tokenize(request, successCallback, errorCallback);
        };

        var tokenize = function(card, successCallback, errorCallback) {
            intuit.ipp.payments.tokenize(ModelSvc.model.systemProperties.appToken, card, function(token, response) {
                if (token) {
                    // step 2 - place order to backend
                    console.log('placing order to: ' + RootUrlSvc.rootUrls.orders + ' with args: ' + token + ", " + ModelSvc.model.shoppingCart.id);
                    $http.post(
                        RootUrlSvc.rootUrls.orders,
                        { shoppingCartId: ModelSvc.model.shoppingCart.id, paymentToken: token })
                        .success(successCallback)
                        .error(errorCallback);
                }
                else {
                    console.log("Error during tokenization " + response.code +"<br/>" + response.message + "<br/>" + response.detail + "<br/>" + response.moreinfo);
                    errorCallback(response);
                }
            });
         };

        var initialize = function () {

        };

        return {
            initialize: initialize,
            sendOrder: sendOrder
        }
    }]);

ecommerceServices.factory('SyncRequestSvc', ['$http', '$rootScope', 'RootUrlSvc', 'ModelSvc',
    function ($http, $rootScope, RootUrlSvc, ModelSvc) {

        var sendSyncRequest = function (entityType, successCallback, errorCallback) {
            $http.post(RootUrlSvc.rootUrls.syncRequest, {type: entityType, companyId: ModelSvc.model.company.id})
                    .success(successCallback);
        };

        var initialize = function () {

        };

        return {
            initialize: initialize,
            sendCustomerSyncRequest: function (callback) { sendSyncRequest('Customer', callback); },
            sendSalesItemSyncRequest: function (callback) { sendSyncRequest('SalesItem', callback) }
        }
    }]);

ecommerceServices.factory('DeepLinkSvc', ['ModelSvc',
    function (ModelSvc) {

        var getQboDeepLinkURLRoot = function () {
            return "https://" + ModelSvc.model.systemProperties.qboUiHostname + "/login?";
        };

        var getMultipleEntitiesUrl = function (entityType) {
            return getQboDeepLinkURLRoot() + "deeplinkcompanyid=" + ModelSvc.model.company.qboId + "&pagereq=" + entityType;
        };

        var getSingleEntityUrl = function (entityType, entityId) {
            return getQboDeepLinkURLRoot() + "pagereq=" + entityType + "?txnId=" + entityId + "&deeplinkcompanyid=" + ModelSvc.model.company.qboId;
        };

        var getCustomersLink = function () {
            return getMultipleEntitiesUrl("customers");
        };

        var getSalesReceiptLink = function (entityId) {
            return getSingleEntityUrl("salesreceipt", entityId);
        };

        var getItemsLink = function () {
            return getMultipleEntitiesUrl("items");
        };

        return {
            getCustomersLink: getCustomersLink,
            getItemsLink: getItemsLink,
            getSalesReceiptLink: getSalesReceiptLink
        }
    }
]);

ecommerceServices.factory('SystemPropertySvc', [ '$resource', 'RootUrlSvc', 'ModelSvc',
    function ($resource, RootUrlSvc, ModelSvc) {

        var SystemProperty;

        var initializeModel = function () {
            SystemProperty = $resource(RootUrlSvc.rootUrls.systemProperties, {},
                {
                    query: {
                        isArray: false
                    }
                });
            SystemProperty.query(function (data) {
                ModelSvc.model.systemProperties = {};

                if (data._embedded) {
                    angular.forEach(data._embedded.systemProperties, function (systemProperty) {
                        ModelSvc.model.systemProperties[systemProperty.key] = systemProperty.value;
                    });
                }
            });
        }

        return {
            initializeModel: initializeModel
        }
}]);

ecommerceServices.factory('TrackingSvc', ['$resource', function ($resource) {


    var NAME_OF_USER_ID_TRACKING_COOKIE = "tracking_user_id";

    var getUserIdFromCookie = function (properties) {

        var cookie = document.cookie;

        if (cookie) {
            var cStart = cookie.indexOf(NAME_OF_USER_ID_TRACKING_COOKIE + "=");

            if (cStart > -1) {
                cStart += (NAME_OF_USER_ID_TRACKING_COOKIE + "=").length;
                var cEnd = cookie.indexOf(";", cStart);

                if (cEnd == -1) {
                    cEnd = cookie.length;
                }
                cookie = cookie.substring(cStart, cEnd);

                if (!properties) {
                    properties = { "user_id": cookie };
                } else {
                    properties["user_id"] = cookie;
                }
            }
        }

        return properties;

    };

    return {

        trackPage: function (pageName, event, properties) {

            properties = getUserIdFromCookie(properties);

            wa.trackPage(pageName, event, properties);
        },

        trackEvent: function (event, properties) {

            if (properties && typeof properties.user_id !== 'undefined') {
                document.cookie = "tracking_user_id=" + properties.user_id;
            } else {
                properties = getUserIdFromCookie(properties);
            }

            wa.trackEvent(event, properties);
        }

    };
}]);
