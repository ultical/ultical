'use strict';

app.directive('basActionBar', ['actionBar', function (actionBar) {
  return {
    restrict: 'E',
    template: `
    <div class="col-lg-offset-1 col-lg-push-8 col-sm-3 col-sm-push-9 hidden-xs">
      <div class="action-bar ani-vslide margin-bottom">
      <div ng-include="'components/actionbar/bas_actionbar.html?v=8'"></div>
      </div></div>`,
    scope: {},
    link: function($scope, element, attr) {
      $scope.head = actionBar.getHead();
      $scope.actions = actionBar.getActions();
      $scope.showAction = actionBar.showAction;
    },
  };
}]);
