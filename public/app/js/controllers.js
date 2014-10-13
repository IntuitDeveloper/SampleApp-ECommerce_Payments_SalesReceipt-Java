'use strict';

/* Controllers */

var controllersModule = angular.module('myApp.controllers', ['ngRoute', 'ui.bootstrap', 'ui.validate', 'myApp.services']);

controllersModule.controller('NavCtrl', ['$scope', '$routeParams', '$location', 'ModelSvc', 'TrackingSvc',
    function ($scope, $routeParams, $location, ModelSvc, TrackingSvc) {
        $scope.navClass = function (page) {
            var currentRoute = $location.path().substring(1);
            TrackingSvc.trackPage(null, null, {"site_section" : page});
            return page === currentRoute ? 'active' : '';
        };

        $scope.isStorefront = function() {
            return $location.path() === '/storefront' || $location.path() === '/shoppingcart';
        };

        $scope.model = ModelSvc.model;
    }]);

controllersModule.controller('SettingsCtrl', ['$scope', 'SyncRequestSvc', 'ModelSvc', 'CompanySvc', 'DeepLinkSvc', '$window',
        function ($scope, SyncRequestSvc, ModelSvc, CompanySvc, DeepLinkSvc, $window) {

            $scope.model = ModelSvc.model;
            $scope.syncCustomersMessage = '';
            $scope.syncSalesItemMessage = '';
            $scope.loadingSalesItems = false;
            $scope.loadingCustomers = false;

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

            var disableViewInQBOButton = function (entitySynced) {
                if (connectedToQBO()) {
                    //we can synced
                    if (entitySynced) {
                        //don't disable the view button
                        return false;
                    } else {
                        //do disable the view button
                        return true;
                    }
                } else {
                    //we can't view, disable the button
                    return true;
                }
            };

            $scope.disableViewItemsInQBOButton = function () {
                return disableViewInQBOButton($scope.model.company.salesItemSynced);
            }

            $scope.disableViewCustomersInQBOButton = function () {
                return disableViewInQBOButton($scope.model.company.customersSynced);
            }

            $scope.disableCustomersSyncButton = function () {
                return disableSyncButton($scope.model.company.customersSynced);
            }

            $scope.disableSalesItemsSyncButton = function () {
                return disableSyncButton($scope.model.company.salesItemSynced);
            }

            $scope.syncCustomers = function() {
                $scope.loadingCustomers = true;
                SyncRequestSvc.sendCustomerSyncRequest(syncCompleted);
            }

            $scope.syncSalesItems = function() {
                $scope.loadingSalesItems = true;
                SyncRequestSvc.sendSalesItemSyncRequest(syncCompleted);
            }

            $scope.openCustomersScreenInQBO = function () {
                $window.open(DeepLinkSvc.getCustomersLink());
            };

            $scope.openItemsScreenInQBO = function () {
                $window.open(DeepLinkSvc.getItemsLink());
            };

            var syncCompleted = function(data, status, headers, config) {
                var message = data.successful ? data.message : 'Error: ' + data.message;
                if (data.type === 'Customer') {
                    $scope.syncCustomersMessage = message;
                    $scope.loadingCustomers = false;
                } else if (data.type === 'SalesItem') {
                    $scope.syncSalesItemMessage = message;
                    $scope.loadingSalesItems = false;
                }
                CompanySvc.initializeModel();
            }
        }]);


controllersModule.controller('StoreFrontCtrl', ['$scope', 'ModelSvc', 'CartItemSvc',
    function ($scope, ModelSvc, CartItemSvc) {
        $scope.model = ModelSvc.model;

        $scope.shoppingCartView = "CartReview";

        $scope.addToCart = function(salesItem) {
            CartItemSvc.addCartItem(salesItem, ModelSvc.model.shoppingCart);
        };
    }]);

controllersModule.controller('ShoppingCartCtrl', ['$scope', 'ModelSvc', 'ShoppingCartSvc', 'CartItemSvc', 'OrderSvc', 'DeepLinkSvc', 'TrackingSvc',
    function ($scope, ModelSvc, ShoppingCartSvc, CartItemSvc, OrderSvc, DeepLinkSvc, TrackingSvc) {
        var customer = ModelSvc.model.company.customers[0];
        ShoppingCartSvc.refreshShoppingCart();
        CartItemSvc.getCartItems();
        $scope.model = ModelSvc.model;

        $scope.shoppingCartView = "ShoppingCart-Review";
        $scope.orderResponse = {};
        $scope.creditCard = {};
        $scope.creditCard.number = '4111111111111111';
        $scope.creditCard.CVC = '123';
        $scope.creditCard.expMonth = '02';
        $scope.creditCard.expYear = '2020';

        $scope.billingInfo = {};
        $scope.billingInfo.name = customer.firstName + ' ' + customer.lastName;
        $scope.billingInfo.address = customer.line1;
        $scope.billingInfo.cityStateZip = customer.city + ", " + customer.countrySubDivisionCode + ", " + customer.postalCode;
        $scope.billingInfo.email = customer.emailAddress;
        $scope.billingInfo.phone = customer.phoneNumber;
        $scope.showView = function(viewName) {
            $scope.shoppingCartView = viewName;
            TrackingSvc.trackPage(null, null, {"site_section" : viewName});
        };
        $scope.placeOrder = function() {
            $('#loadingOrder').modal({show: true, keyboard: false, backdrop: 'static'});
            $scope.orderMessage = "";
            OrderSvc.sendOrder(
                $scope.creditCard,
                $scope.billingInfo,
                function(data) {
                    $scope.orderResponse = data;
                    $scope.showView("ShoppingCart-Confirmation");
                    $('#loadingOrder').modal('hide');

                },
                function(data) {
                    $scope.orderMessage = 'Unexpected error placing your order.'
                }
            );
        }

        $scope.getSalesReceiptLinkUrl = function () {
            return DeepLinkSvc.getSalesReceiptLink($scope.orderResponse.txnId);
        }


    }]);
