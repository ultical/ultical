'use strict';

var angular = require('angular');

angular.module('ultical.events').controller('EventEditCtrl', ['$scope', 'storage', '$stateParams',
                      	  function($scope, storage, $stateParams) {

	// if format(s) exist - create select field to choose from - else create new
//	$scope.formats = getUsersFormats...


//	if ($stateParams.formatId == 'new') {
//		$scope.edit = {format = storage.getEmptyEvent();
//	}
//	$scope.edition =
	console.log("edit event id", $stateParams.editionId, $stateParams.eventId);

}]);
