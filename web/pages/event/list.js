'use strict';

angular.module('ultical.events', [])

.controller('EventListCtrl', ['$scope', 'storage',
                      	  function($scope, storage) {
		
	storage.getEvents(function(data) {

		$scope.events = data;
	});
	
	
}]);

