'use strict';

angular.module('ultical.events')

.controller('EventEditCtrl', ['$scope', 'storage', '$stateParams', '$filter', '$state', 'mapService', 'alerter',
                      	  function($scope, storage, $stateParams, $filter, $state, mapService, alerter) {

  storage.getEvent($stateParams.eventId, function(event) {
    prepareDateDiff(event);
    $scope.locationClone = angular.copy(event.locations[0]);
    $scope.locationToEdit = angular.copy(event.locations[0]);

    $scope.event = event;
    $scope.edition = event.tournamentEdition;
    $scope.format = event.tournamentEdition.tournamentFormat;
    console.log("event", event);
    console.log("edition", $scope.edition);
    console.log("format", $scope.format);

    if (!$scope.event.x.own || !$scope.format.x.own) {
      // TODO - $state.go('app.eventShow', {eventId: $scope.event.id, eventSlug: 'slug'});
    }

  });

  var prepareDateDiff = function(event) {
      var timeDiff = moment.duration(moment(event.endDate).diff(moment(event.startDate)));
      event.x.eventNumOfDays = Math.ceil(timeDiff / (1000 * 3600 * 24)) + 1;
      event.x.eventNumOfDays += '';
  }

	$scope.divisionOrder = function(division) {
		return $filter('division')(division);
	}

	// if format(s) exist - create select field to choose from - else create new
//	$scope.formats = getUsersFormats...
	
	
//	if ($stateParams.formatId == 'new') {
//		$scope.edit = {format = storage.getEmptyEvent();
//	}
//	$scope.edition = 

  // return location-proposals from mapbox api
  $scope.getLocations = function(locationName) {
    if (locationName.length < 4) {
      return [];
    }
    if (angular.isObject(locationName)) {
      return $scope.oldLocations;
    }

    return mapService.getLocations(locationName, 'street', function(locations) {
      angular.forEach(locations, function(location) {
        location.mapBoxId = location.id;
        if ($scope.locationClone) {
          location.id = $scope.locationClone.id;
          location.version = $scope.locationClone.version;
        } else {
          location.id = 0;
          location.version = 0;
        }
        console.log("setting version", location.id, location.version);
      });
      $scope.oldLocations = locations;
      return locations;
    });
  };


	$scope.saveEvent = function(event) {
	  console.log("Saving event", event);

    if (!angular.isObject($scope.locationToEdit) || isEmpty($scope.locationToEdit)) {
      $scope.locationIsMissing = true;
      alerter.error('', 'team.edit.locationMissing', {
        container: '#team-edit-error' + team.id,
        duration: 10
      });
      return;
    }
    event.locations[0] = $scope.locationToEdit;

    // calculate end-date from start-date
    var endDateMoment = moment(event.startDate).add(event.x.eventNumOfDays - 1, 'days');
    event.endDate = endDateMoment.format('YYYY-MM-DD');

    storage.saveEvent(event, function(newEvent) {
      prepareDateDiff(newEvent);
      $scope.event = newEvent;
    }, function() {
      console.log("ERRORROROROROR");
    });


	}

}]);