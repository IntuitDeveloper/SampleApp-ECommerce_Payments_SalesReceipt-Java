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


controllersModule.controller('SettingsCtrl', ['$scope', 'SyncRequestSvc', 'ModelSvc',
        function ($scope, SyncRequestSvc, ModelSvc) {

            $scope.model = ModelSvc.model;

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

            $scope.disableEmployeeSyncButton = function () {
                return disableSyncButton($scope.model.company.employeesSynced);
            }

            $scope.disableCustomersSyncButton = function () {
                return disableSyncButton($scope.model.company.customersSynced);
            }

            $scope.disableSalesItemsSyncButton = function () {
                return disableSyncButton($scope.model.company.salesItemsSycned);
            }

            $scope.syncCustomers = function() {
                SyncRequestSvc.syncCustomers();
            }

            $scope.syncSalesItems = function() {
                SyncRequestSvc.syncSalesItems();
            }
        }]);
