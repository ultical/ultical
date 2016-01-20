'use strict';

angular.module('ultical.events', [])

.controller('EventListCtrl', ['$scope', 'storage', '$state',
                              function($scope, storage, $state) {

	$scope.sortKey = 'startDate';
	$scope.sortOrderDesc = false;

	$scope.goToEvent = function(eventId) {
		$state.go('eventShow', {eventId: eventId});
	};

	storage.getEvents(function(data) {

		$scope.events = data;
	});


}]);

