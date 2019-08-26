'use strict';

angular.module('ultical.events')

.controller('EventEditCtrl', ['$scope', 'storage', '$stateParams', '$filter',
                      	  function($scope, storage, $stateParams, $filter) {

  console.log("stateParams", $stateParams);

  storage.getEvent($stateParams.eventId, function(event) {
    $scope.event = event;
    $scope.edition = event.tournamentEdition;
    $scope.format = event.tournamentEdition.tournamentFormat;
    console.log("event", event);
    console.log("edition", $scope.edition);
    console.log("format", $scope.format);


    var timeDiff = moment.duration(moment(event.endDate).diff(moment(event.startDate)));
    console.log("TIME DIFF", timeDiff);
    $scope.eventNumOfDays = Math.ceil(timeDiff / (1000 * 3600 * 24)) + 1;


  });

	$scope.divisionOrder = function(division) {
		return $filter('division')(division);
	}

	// if format(s) exist - create select field to choose from - else create new
//	$scope.formats = getUsersFormats...
	
	
//	if ($stateParams.formatId == 'new') {
//		$scope.edit = {format = storage.getEmptyEvent();
//	}
//	$scope.edition = 
	console.log("edit event id", $stateParams.editionId, $stateParams.eventId);
	console.log("")
}]);