'use strict';

/* Services */


var ecommerceServices = angular.module('myApp.services', ['ngResource']);

ecommerceServices.value('version', '0.1');

ecommerceServices.factory('InitializerSvc', ['$rootScope', 'RootUrlSvc', 'CompanySvc',
    function ($rootScope, RootUrlSvc, CompanySvc) {

        var initialized = false;

        var initialize = function () {
            CompanySvc.initialize();
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

        var getCompanyResource = function () {
            return $resource(RootUrlSvc.rootUrls.companies + ':companyId', {}, {
                query: {method: 'GET', isArray: false}
            });
        };

        var getCompanies = function () {
            getCompanyResource().query(function (data) {
                var companies = data._embedded.companies;
                ModelSvc.model.companies = companies;
                ModelSvc.model.company = companies[0]; //select the first company for now
                ModelSvc.broadcastCompanyChange();

                var grantUrl = RootUrlSvc.oauthGrantUrl() + '?appCompanyId=' + ModelSvc.model.company.id;
                intuit.ipp.anywhere.setup({
                    grantUrl: grantUrl});


            });
        };

        var initialize = function () {
            RootUrlSvc.onApiLoaded($rootScope, getCompanies);
        };

        return {
            initialize: initialize,
            getCompanies: getCompanies
        }

    }]);


ecommerceServices.factory('SyncRequestSvc', ['$http', '$rootScope', 'RootUrlSvc', 'ModelSvc',
    function ($http, $rootScope, RootUrlSvc, ModelSvc) {

        var sendSyncRequest = function (entityType) {
            $http.post(RootUrlSvc.syncRequest, {type: entityType, companyId: ModelSvc.model.company.id});

        };

        var initialize = function () {

        };

        return {
            initialize: initialize,
            sendCustomerSyncRequest: sendSyncRequest('Customer'),
            sendSalesItemSyncRequest: sendSyncRequest('SalesItem')
        }

    }]);
