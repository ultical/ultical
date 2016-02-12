'use strict';

angular.module('ultical.events', [])

.controller('EventListCtrl', ['$scope', 'storage', '$state', '$filter', 'Slug',
                              function($scope, storage, $state, $filter, slug) {


	$scope.sortKey = 'startDate';
	$scope.sortOrderDesc = false;

	$scope.getEventUrl = function(event) {
		return $state.href('app.eventShow', {eventId: event.id, eventSlug: slug.slugify($filter('eventname')(event)) });
	}

	storage.getEvents(function(data) {
		$scope.events = data;
	});


}]);

