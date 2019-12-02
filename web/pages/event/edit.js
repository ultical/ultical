'use strict';

angular.module('ultical.events')

.controller('EventEditCtrl', ['$scope', 'storage', '$stateParams', '$filter', '$state', 'mapService', 'alerter', 'serverApi', 'authorizer', 'headService', '$translate', 'moment',
                      	  function($scope, storage, $stateParams, $filter, $state, mapService, alerter, serverApi, authorizer, headService, $translate, moment) {

  $scope.newAdmin = {obj:""};
  $scope.loaded = false;

  $scope.createEvent = false;
  $scope.editionContacts = [];
  $scope.allContexts = [];

  storage.getSeasons(function(seasons) {
    $scope.seasons = seasons;
  });

  $scope.action = {
    formatIdChosen: -1,
    editionIdChosen: -1,
    locationToEdit: { city: ''},
  };

  var feeIdCounter = 0;

  function initNewFee() {
    feeIdCounter--;
    $scope.newFee = {
      id: feeIdCounter,
      multiple: '0',
      type: 'PLAYER',
      amount: 0,
      currency: 'EUR',
      perPerson: '0',
      tournamentEdition: null,
    };
  }
  initNewFee();

  function initNewEdition() {
    $scope.newEdition = {
      id: -1,
      alternativeMatchdayName: '',
      context: null,
      divisionRegistrations: [],
      fees: [],
      name: '',
      organizer: {},
      registrationStart: moment().format('YYYY-MM-DD'),
      registrationEnd: moment().add(1, 'months').format('YYYY-MM-DD'),
      season: {},
      tournamentFormat: $scope.format,
    };
  }

  function clearNewDivision() {
    $scope.newDivision = {
      divisionType: 'open',
      divisionAge: 'regular',
      divisionIdentifier: '',
      numberSpots: '0',
    }
  }

  clearNewDivision();

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
    $scope.editionCreate = false;
    $scope.formatChosen = false;

    storage.getFormatList(function(formats) {
      $scope.formatList = formats;
      $scope.loaded = true;
      $scope.createEvent = true;
    });
  } else {
    storage.getEvent($stateParams.eventId, function(event) {
      headService.setTitle($translate.instant('event.edit.editTitle') + event.name);
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

      countNumberOfRegisteredTeams();

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
      initNewEdition();
      $scope.editionList = editions;
      $scope.formatChosen = true;
    });
  };

  $scope.chooseEdition = function() {
    angular.forEach($scope.editionList, function(edition) {
      if (edition.id == $scope.action.editionIdChosen) {
        edition.tournamentFormat = $scope.format;
        $scope.edition = edition;
        $scope.event.tournamentEdition = $scope.edition;
      }
    });
    $scope.editionChosen = true;
  };

  $scope.createEdition = function() {
    $scope.editionCreate = true;
    $scope.season = {
      surface: 'TURF',
      yearObject: $scope.seasonYears[2],
    };

    $scope.edition.registrationStart = moment().format('DD-MM-YYYY');
    $scope.edition.registrationEnd = moment().add(2, 'month').format('DD-MM-YYYY');

    var newOrganizer = {id:-1, name: $translate.instant('event.edit.createNewEditionOrganizer'), phone: '', email: ''};
    $scope.editionContacts.push(newOrganizer);
    $scope.edition.organizer = newOrganizer;

    storage.getContactsForEdition(function(contacts) {
      angular.forEach(contacts, function(contact) {
        $scope.editionContacts.push(contact);
      });
    });

    storage.getContexts(function(contexts) {
      $scope.allContexts = contexts;
      $scope.edition.context = contexts[0];
    });
  };

  $scope.getSeasonSurfaces = function() {
    return ['TURF', 'GYM', 'BEACH'];
  };

  $scope.seasonYears = getSeasonYears();
  function getSeasonYears() {
    var lastYear = parseInt(moment().subtract(1, 'years').format('YYYY'));
    var listOfYearObjects = [];

    for (var year = lastYear; year < lastYear + 5; year++) {
      var yearString = year + '';
    	listOfYearObjects.push({year: year, plusOneYear: false, yearString: yearString});
    	var yearPlusString = '' + (year + 1);
      yearPlusString = yearString + '/' + yearPlusString.substring(2,4);
    	listOfYearObjects.push({year: year, plusOneYear: true, yearString: yearPlusString});
    }
    return listOfYearObjects;
  };

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
    if (isEmpty(event.name)) {
      alerter.error('', 'event.edit.titleMissing', {
              container: '#event-edit-error',
              duration: 10
            });
      return;
    }
    if (isEmpty(event.x.divisionIds)) {
      alerter.error('', 'event.edit.divisionsMissing', {
              container: '#event-edit-error',
              duration: 10
            });
      return;
    }
    if (!angular.isObject($scope.action.locationToEdit) || isEmpty($scope.action.locationToEdit)) {
      event.locations = null;
    } else {
      event.locations[0] = $scope.action.locationToEdit;
    }

    if (isEmpty(event.localOrganizer) || isEmpty(event.localOrganizer.name)) {
      event.localOrganizer = null;
    }

    $scope.addFee($scope.newFee);

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

  $scope.addDivision = function(newDivision) {
    storage.createDivisionRegistration(newDivision, $scope.edition.id, function(createdDivision) {
      $scope.edition.divisionRegistrations.push(createdDivision);
      clearNewDivision();
    });
  };

  $scope.getDivisionTypes = function() {
    return ['open', 'women', 'mixed'];
  };

  $scope.getDivisionAges = function() {
    return ['u14', 'u17', 'u20', 'u23', 'regular', 'masters', 'grandmasters', 'greatgrand'];
  };

  $scope.cancel = function() {
    if ($scope.event.id == -1) {
      $state.go('app.eventsList');
    } else {
      $state.go('app.eventShow', {eventId: $scope.event.id, eventSlug: 'slug'});
    }
  }

  var numberOfRegisteredTeams = 0;

  function countNumberOfRegisteredTeams() {
    var divisionRegistrations = $scope.event.tournamentEdition.divisionRegistrations;

    angular.forEach(divisionRegistrations, function(div) {
      numberOfRegisteredTeams += div.registeredTeams.length;
    });
  }

  $scope.checkDelete = function() {
    var alertText = $translate.instant('event.edit.confirmDelete');
    if (numberOfRegisteredTeams > 0) {
      alertText += "\n\n" + $translate.instant('event.edit.confirmDeleteRegisteredTeams', {numRegisteredTeams : numberOfRegisteredTeams});
    }
    alerter.confirm(alertText, function(shouldBeDeleted) {
      if (shouldBeDeleted) {
        deleteEvent();
      }
    });
  }

  function deleteEvent() {
    if ($scope.event.id == -1) return;

    storage.deleteEvent($scope.event, function() {
      $state.go('app.eventsList');
    });
  }

  $scope.addFee = function(newFee) {
    if (newFee.amount == 0) {
      return;
    }

    newFee.multiple = newFee.multiple == '1';
    newFee.perPerson = newFee.perPerson == '1';

    $scope.event.fees.push(newFee);

    initNewFee();
  }

  $scope.removeFee = function(feeIdToDelete) {
    var feeIndexToDelete = 0;
    angular.forEach($scope.event.fees, function(fee, idx) {
      if (fee.id == feeIdToDelete) {
        feeIndexToDelete = idx;
      }
    });
    if (feeIndexToDelete >= 0) {
      $scope.event.fees.splice(feeIndexToDelete, 1);
    }
  };

  $scope.getFeeTypes = function() {
    return ['PLAYER', 'GUEST', 'TEAM', 'BREAKFAST', 'LUNCH', 'DINNER', 'NIGHT', 'OTHER'];
  }

  $scope.saveEdition = function() {
    $scope.edition.season = {
      year: $scope.season.yearObject.year,
      plusOneYear: $scope.season.yearObject.plusOneYear,
      surface: $scope.season.surface,
    }




  }

}]);
