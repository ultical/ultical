'use strict';

/**
 * possible options:
 * disabled - true/false
 * model - ng-model of typeahead - chosen value will be stored in property 'chosen'
 * placeholder - placeholder text for input field
 * class - class(es) for input field
 * pre-filtered-input - true/false - set true if input values are pre-filtered
 * comparator - set comparator function (as variable) to decide which values to show in dropdown
 * on-choose - set function to trigger when a selection is made - the currenct selection is the function's parameter
 * input-list - list of objects or function to return list of objects to select from
 * input-list-feedback - function given as a variable (e.g.: input-list-feedback="getValues") to be called with a
 *     parameter containing the input value of the user (for api calls, ...)
 * options - ngOptions expression referring to option-list without the 'in...' part
 * order-by - order by string referencing the options expression
 * track-by - track by string referencing the options expression
 */
app.directive('basTypeahead', ['$timeout', function($timeout) {

  var modelIsolated = null;
  var inputListArray = [];

  var preLink = function($scope, element, attrs) {
    console.log("*******************");

    $scope.orderByString = '';
    if ($scope.orderBy !== undefined) {
      $scope.orderByString = ' | orderBy : ' + $scope.orderBy;
    } else {
      $scope.orderByString = '';
    }

    $scope.trackByString = '';
    if ($scope.trackBy !== undefined) {
      $scope.trackByString = ' track by ' + $scope.trackBy;
    } else {
      $scope.trackByString = '';
    }
  };

  // link function
  var postLink = function($scope, element, attrs) {

    if (isTrue($scope.preFilteredInput)) {
      // return true for every input
      $scope.comparator = function() {
        return true;
      };
    } else {
      if ('comparator' in attrs) {
        console.log("comp", $scope.comparator);
      }
    }

    $scope.onChange = function() {
      if (isTrue($scope.delayInput)) {

      } else {
        refreshInputList();
        console.log("change directly", $scope.modelIsolated, $scope.model);

      }
    }

    $scope.getInputList = function() {
      return inputListArray;
    }

    function refreshInputList() {
      if ($scope.inputListFn !== undefined && typeof $scope.inputListFn == 'function') {
        inputListArray = $scope.inputListFn($scope.modelIsolated);
      } else {
        inputListArray = $scope.inputList({
          a: 'ff'
        });
      }
    }

    refreshInputList();

    $scope.doChoose = function() {
      // timeout needed to get model updated before call
      $timeout(function() {
        if (angular.isObject($scope.modelIsolated)) {
          $scope.model.selection = $scope.modelIsolated;
          $scope.onChoose();
        }
      }, 0);
    }

  };

  function isTrue(attrValue) {
    return attrValue !== undefined && ((typeof attrValue == 'string' && attrValue.toLowerCase() === true) || attrValue == '1');
  }

  return {
    restrict: 'E',
    templateUrl: 'bas-typeahead-tpl.html',
    scope: {
      'disabled': '=',
      'model': '=',
      'onChoose': '&',
      'options': '@',
      'inputList': '&',
      'inputListFn': '=inputListFeedback',
      'placeholder': '@',
      'class': '@',
      'preFilteredInput': '=',
      'delayInput': '@',
      'orderBy': '@',
      'trackBy': '@',
      'comparator': '=',
    },
    compile: function compile() {
      return {
        pre: preLink,
        post: postLink,
      };
    },
  };
}]);
