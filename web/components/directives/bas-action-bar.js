'use strict';

app.directive('basActionBar', ['actionBar', function (actionBar) {
  return {
    restrict: 'E',
    templateUrl: 'components/directives/bas-action-bar.html?v=5',
    scope: {},
    link: function($scope, element, attr) {
      $scope.head = actionBar.getHead();
      $scope.actions = actionBar.getActions();
    },
  };
}]);
