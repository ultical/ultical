'use strict';

angular.module('ultical.events', [])

.controller('EventListCtrl', ['$scope', 'storage', '$state', '$filter', 'Slug', 'headService', 'moment',
                              function($scope, storage, $state, $filter, slug, headService, moment) {

  headService.setTitle('event.list.title', {});

	$scope.sortKey = 'startDate';
	$scope.sortOrderDesc = false;

	$scope.getEventUrl = function(event) {
		return $state.href('app.eventShow', {eventId: event.id, eventSlug: slug.slugify($filter('eventname')(event)) });
	}

	storage.getEvents(function(data) {
		$scope.events = data;
	});

  $scope.show = {
    beginningOfYear: false,
    year: {},
  };

  $scope.current = {
    january: moment().month(0),
    now: moment(),
    nextYear: moment().add(1, 'years').month(0).day(0),
    lastMonth: moment().subtract(1, 'months'),
    day: moment().subtract(3, 'days'),
  };

  $scope.eventFilter = function(event, index, array) {
    var eventStartDate = moment(event.startDate);
    if (eventStartDate.isAfter($scope.current.day) && eventStartDate.isBefore($scope.current.nextYear)) {
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
