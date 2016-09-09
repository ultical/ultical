'use strict';

angular.module('ultical.events', [])

.controller('EventListCtrl', ['$scope', 'storage', '$state', '$filter', 'headService', 'moment', '$stateParams',
                              function($scope, storage, $state, $filter, headService, moment, $stateParams) {

  headService.setTitle('event.list.title', {});

	$scope.sortKey = ['startDate', 'name'];
	$scope.sortOrderDesc = false;

  if ('year' in $stateParams) {
    // TODO: start with this year
  }

	storage.getEvents(true, function(data) {
		$scope.events = data;
	});

  $scope.show = {
    beginningOfYear: false,
    year: {},
  };

  $scope.current = {
    january: moment().date(1).month(0),
    now: moment(),
    nextYear: moment().add(1, 'years').month(0).day(0),
    lastMonth: moment().date(1).subtract(1, 'months'),
    dayMinus3: moment().subtract(3, 'days'),
  };

  $scope.eventFilter = function(event, index, array) {
    var eventStartDate = moment(event.startDate);
    if (eventStartDate.isSameOrAfter($scope.current.dayMinus3, 'month') && eventStartDate.isBefore($scope.current.nextYear)) {
        return true;
    }
    if (eventStartDate.isSame($scope.current.now, 'year') && $scope.show.beginningOfYear) {
      return true;
    }
    if (eventStartDate.format('YYYY') in $scope.show.year) {
      return true;
    }
    return false;
  };

}]);
