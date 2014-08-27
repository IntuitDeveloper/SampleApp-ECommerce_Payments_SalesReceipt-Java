'use strict';


// Declare app level module which depends on filters, and services
angular.module('myApp', [
    'ngRoute',
    'ui.bootstrap',
    'ui.validate',
    'myApp.filters',
    'myApp.services',
    'myApp.directives',
    'myApp.controllers'
]).
    config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/settings', {templateUrl: 'partials/settings.html', controller: 'SettingsCtrl'});
        $routeProvider.when('/storefront', {templateUrl: 'partials/storefront.html', controller: 'StoreFrontCtrl'});
        $routeProvider.otherwise({redirectTo: '/settings'});
    }])
    .run(['InitializerSvc', function(InitializerSvc) {
        InitializerSvc.initialize();
    }]);

