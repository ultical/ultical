'use strict';

angular.module('ultical.events')

.controller('EventShowCtrl', ['$scope', '$stateParams', 'storage', '$state', '$filter',
                              function($scope, $stateParams, storage, $state, $filter) {

	$scope.event = {};
	
	storage.getEvent($stateParams.eventId, function(event) {
		$scope.event = event;
	});
	
	
}]);

