'use strict';

angular.module('ultical.events')

.controller('EventEditCtrl', ['$scope', 'storage', '$stateParams', '$filter', '$state', 'mapService', 'alerter', 'serverApi', 'authorizer', 'headService',
                      	  function($scope, storage, $stateParams, $filter, $state, mapService, alerter, serverApi, authorizer, headService) {

  $scope.newAdmin = {obj:""};
  $scope.loaded = false;

  $scope.createEvent = false;

  $scope.action = {
    formatIdChosen: -1,
    editionIdChosen: -1,
    locationToEdit: {},
  };

  if (!authorizer.loggedIn() || (isNaN($stateParams.eventId) && $stateParams.eventId != 'new')) {
    $state.go('app.eventsList');
    return;
  }

  if ($stateParams.eventId == 'new') {
    headService.setTitle('event.edit.createTitle');
    $scope.event = storage.getEmptyEvent();
    $scope.edition = {};
    $scope.format = {};
    $scope.editionChosen = false;
    $scope.formatChosen = false;

    storage.getFormatList(function(formats) {
      $scope.formatList = formats;
      $scope.loaded = true;
      $scope.createEvent = true;
    });
  } else {
    storage.getEvent($stateParams.eventId, function(event) {
      headService.setTitle('event.edit.editTitle' + event.name);
      prepareDateDiff(event);
      $scope.locationClone = angular.copy(event.locations[0]);
      $scope.action.locationToEdit = angular.copy(event.locations[0]);
      event.matchdayNumber += '';

      $scope.event = event;
      $scope.edition = event.tournamentEdition;
      $scope.format = event.tournamentEdition.tournamentFormat;

      $scope.editionChosen = true;
      $scope.formatChosen = true;
      $scope.loaded = true;

      if (!$scope.event.x.own && !$scope.format.x.own) {
        $state.go('app.eventShow', {eventId: $scope.event.id, eventSlug: 'slug'});
      }
    });
  }

  $scope.chooseFormat = function() {
    angular.forEach($scope.formatList, function(format) {
      if (format.id == $scope.action.formatIdChosen) {
        format.x.own = true;
        $scope.format = format;
      }
    });
    storage.getEditionListingForFormat($scope.format.id, function(editions) {
      $scope.editionList = editions;
      $scope.formatChosen = true;
    });
  }

  $scope.chooseEdition = function() {
    angular.forEach($scope.editionList, function(edition) {
      if (edition.id == $scope.action.editionIdChosen) {
        edition.tournamentFormat = $scope.format;
        $scope.edition = edition;
        $scope.event.tournamentEdition = $scope.edition;
      }
    });
    $scope.editionChosen = true;
  }

  function isNumber(input) {
      return typeof input === 'number' || Object.prototype.toString.call(input) === '[object Number]';
  }

  var prepareDateDiff = function(event) {
      var timeDiff = moment.duration(moment(event.endDate).diff(moment(event.startDate)));
      event.x.eventNumOfDays = Math.ceil(timeDiff / (1000 * 3600 * 24)) + 1;
      event.x.eventNumOfDays += '';
  }

	$scope.divisionOrder = function(division) {
		return $filter('division')(division);
	}

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
      });
      $scope.oldLocations = locations;
      return locations;
    });
  };


	$scope.saveEvent = function(event) {
    if (isEmpty(event.localOrganizer) || isEmpty(event.localOrganizer.name)) {
      event.localOrganizer = null;
    }

    if (!angular.isObject($scope.action.locationToEdit) || isEmpty($scope.action.locationToEdit)) {
      $scope.locationIsMissing = true;
      alerter.error('', 'event.edit.locationMissing', {
        container: '#team-edit-error' + event.id,
        duration: 10
      });
      return;
    }
    event.locations[0] = $scope.action.locationToEdit;

    event.divisionConfirmations = event.x.divisionIds;

    // calculate end-date from start-date
    var endDateMoment = moment(event.startDate).add(event.x.eventNumOfDays - 1, 'days');
    event.endDate = endDateMoment.format('YYYY-MM-DD');

    event.tournamentEdition = { id: event.tournamentEdition.id };
    storage.saveEvent(event, function(newEvent) {
      $scope.showEvent(newEvent);
    }, function() {
    });
	}

  $scope.showEvent = function(event) {
    $state.go('app.eventShow', {eventId: event.id, eventSlug: 'slug'});
  }

  $scope.addAdmin = function(newAdmin) {
    if (isEmpty(newAdmin)) {
      return;
    }

    if (!angular.isObject(newAdmin)) {
      return;
    }

    if (newAdmin.obj == "") {
      return;
    }

    // check if admin is already in the list
    var alreadyAdmin = false;
    angular.forEach($scope.event.admins, function(admin) {
      if (admin.id == newAdmin.id) {
        alreadyAdmin = true;
      }
    });

    if (!alreadyAdmin) {
      $scope.event.admins.push(newAdmin);
    }

    $scope.newAdmin.obj = "";
  }

  // return user proposals
  $scope.getUsers = function(userName) {
    if (userName.length < 4) {
      return [];
    }

    return serverApi.getUserProposals(userName, function(result) {
      angular.forEach(result, function(user) {
        if (angular.isObject(user.dfvPlayer)) {
          user.fullName = user.dfvPlayer.firstName + ' ' + user.dfvPlayer.lastName;
        } else {
          user.fullName = user.email;
        }
      });
      return result;
    });
  };

  $scope.removeAdmin = function(adminId) {
    var indexToRemove = -1;
    angular.forEach($scope.event.admins, function(admin, idx) {
      if (admin.id == adminId) {
        indexToRemove = idx;
      }
    });

    if (indexToRemove >= 0) {
      $scope.event.admins.splice(indexToRemove, 1);
    }
  };

}]);