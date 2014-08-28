'use strict';

/* Services */


var ecommerceServices = angular.module('myApp.services', ['ngResource']);

ecommerceServices.value('version', '0.1');

ecommerceServices.factory('InitializerSvc', ['$rootScope', 'RootUrlSvc', 'CompanySvc', 'SalesItemSvc', 'CustomerSvc', 'ShoppingCartSvc', 'CartItemSvc',
    function ($rootScope, RootUrlSvc, CompanySvc, SalesItemSvc, CustomerSvc, ShoppingCartSvc, CartItemSvc) {

        var initialized = false;

        var initialize = function () {

            $rootScope.$on('api.loaded', function() {
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
            return $location.protocol() +"://" + $location.host() + ":8080";
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
                    ModelSvc.model.shoppingCartItems = shoppingCartItems._embedded.cartItems;
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
