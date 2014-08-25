'use strict';

/* Filters */

var timetrackingFilters = angular.module('myApp.filters', []);

timetrackingFilters.filter('interpolate', ['version', function(version) {
    return function(text) {
      return String(text).replace(/\%VERSION\%/mg, version);
    };
  }]);
