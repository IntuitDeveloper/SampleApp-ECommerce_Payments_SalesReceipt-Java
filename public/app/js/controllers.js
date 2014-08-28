'use strict';

/* Controllers */

var controllersModule = angular.module('myApp.controllers', ['ngRoute', 'ui.bootstrap', 'ui.validate', 'myApp.services']);

controllersModule.controller('NavCtrl', ['$scope', '$routeParams', '$location', 'ModelSvc',
    function ($scope, $routeParams, $location, ModelSvc) {
        $scope.navClass = function (page) {
            var currentRoute = $location.path().substring(1);
            return page === currentRoute ? 'active' : '';
        };

        $scope.model = ModelSvc.model;
    }]);


controllersModule.controller('SettingsCtrl', ['$scope', 'SyncRequestSvc', 'ModelSvc', 'CompanySvc',
        function ($scope, SyncRequestSvc, ModelSvc, CompanySvc) {

            $scope.model = ModelSvc.model;
            $scope.syncCustomersMessage = '';
            $scope.syncSalesItemMessage = '';

            $scope.showConnectButton = function () {
                return $scope.model.company.connectedToQbo === false;
            }

            var connectedToQBO = function () {
                return $scope.model.company.connectedToQbo === true;
            }

            var disableSyncButton = function (entitySynced) {
                if (connectedToQBO()) {
                    //we can synced
                    if (entitySynced) {
                        //we have synced
                        return true;
                    } else {
                        //we have not synced
                        return false;
                    }
                } else {
                    //we can't sync
                    return true;
                }
            }

            $scope.disableCustomersSyncButton = function () {
                return disableSyncButton($scope.model.company.customersSynced);
            }

            $scope.disableSalesItemsSyncButton = function () {
                return disableSyncButton($scope.model.company.salesItemsSycned);
            }

            $scope.syncCustomers = function() {
                SyncRequestSvc.sendCustomerSyncRequest(syncCompleted);
            }

            $scope.syncSalesItems = function() {
                SyncRequestSvc.sendSalesItemSyncRequest(syncCompleted);
            }

            var syncCompleted = function(data, status, headers, config) {
                var message = data.successful ? data.message : 'Error: ' + data.message;
                if (data.type === 'Customer') {
                    $scope.syncCustomersMessage = message;
                } else if (data.type === 'SalesItem') {
                    $scope.syncSalesItemMessage = message;
                }
                CompanySvc.getCompanies();
            }
        }]);


controllersModule.controller('StoreFrontCtrl', ['$scope', 'ModelSvc', 'CartItemSvc',
    function ($scope, ModelSvc, CartItemSvc) {
        $scope.model = ModelSvc.model;

        // assume that login of customer has occurred - CustomerSvc automatically loads

        $scope.addToCart = function(salesItem) {
            CartItemSvc.addCartItem(salesItem, ModelSvc.model.shoppingCart);
        };
    }]);

controllersModule.controller('ShoppingCartCtrl', ['$scope', 'ModelSvc', 'CartItemSvc',
    function ($scope, ModelSvc, CartItemSvc) {
        CartItemSvc.getCartItems();
        $scope.model = ModelSvc.model;
    }]);