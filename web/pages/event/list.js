'use strict';

angular.module('ultical.events', [])

.controller('EventListCtrl', ['$scope', 'storage', '$state', '$filter', 'headService', 'moment', '$stateParams',
                              function($scope, storage, $state, $filter, headService, moment, $stateParams) {

  headService.setTitle('event.list.title', {});

	$scope.sortKey = ['startDate', 'endDate', 'name'];
	$scope.sortOrderDesc = false;

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
    thisYear: moment().format('YYYY'),
    lastMonth: moment().date(1).subtract(1, 'months'),
    dayMinus3: moment().subtract(3, 'days'),
  };

  $scope.eventFilter = function(event, index, array) {
    var eventStartDate = moment(event.startDate);
    var eventEndDate = moment(event.endDate);
    // show all events - starting with events this month
    if (eventStartDate.isSameOrAfter($scope.current.dayMinus3, 'month')) {
        return true;
    }
    // show all events of this year if the corresponding button is clicked
    if (eventStartDate.isSame($scope.current.now, 'year') && $scope.show.beginningOfYear) {
      return true;
    }
    if (eventStartDate.format('YYYY') in $scope.show.year) {
      return true;
    }
    return false;
  };

}]);
