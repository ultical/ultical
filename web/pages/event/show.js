'use strict';

angular.module('ultical.events')

.controller('EventShowCtrl', ['$scope', '$stateParams', 'storage', '$state', '$filter', 'moment', 'authorizer', '$window', '$timeout', 'headService', 'actionBar', '$modal',
                              function($scope, $stateParams, storage, $state, $filter, moment, authorizer, $window, $timeout, headService, actionBar, $modal) {

	$scope.event = {};
  $scope.edition = {};
  $scope.format = {};

	$scope.now = new Date();

  $scope.editStandings = false;

	$scope.loggedIn = function() {
		return authorizer.loggedIn();
	}

  $scope.tabs = {
    activeTab: 'events',
  };

  // get own teams to determine if this user may register a team
  $scope.ownTeams = null;
  if (authorizer.loggedIn()) {
    $scope.ownTeams = storage.getOwnTeamsCache(function(cachedOwnTeams) {
      // only called if own teams were not cached
      $scope.ownTeams = cachedOwnTeams;
      updateActions();
    });
  }


  // check if we show an event or an edition
  if ($stateParams.eventId !== undefined) {
    $scope.show = {
      event: true,
      date: true,
      location: true,
      organizer: true,
      localOrganizer: true,
      formatInfo: true,
      eventInfo: true,
      formatUrl: true,
      editionFees: true,
      eventFees: true,
      feesLabel: true,
      registration: true,
      divisions: true,
    };
    storage.getFormatForEvent($stateParams.eventId, init);
  } else if ($stateParams.editionId !== undefined) {
    $scope.show = {
      edition: true,
      organizer: true,
      formatInfo: true,
      formatUrl: true,
      editionFees: true,
      registration: true,
      divisions: true,
    };
    storage.getFormatForEdition($stateParams.editionId, init);
  } else if ($stateParams.formatId !== undefined) {
    $scope.show = {
      format: true,
      organizer: true,
      formatInfo: true,
      formatUrl: true,
    };
    storage.getFormat($stateParams.formatId, init);
  }

  $scope.eventIsFuture = false;


  // init function
  function init(format) {
		$scope.format = format;

    angular.forEach(format.editions, function(edition) {
      if ($scope.show.edition) {
        if (edition.id == $stateParams.editionId) {
          $scope.edition = edition;
        }
      } else {
  			angular.forEach(edition.events, function(event) {
  				if (event.id == $stateParams.eventId) {
  					// this is the right event
  					$scope.event = event;
            $scope.edition = edition;
  				}
  			});
      }
		});

    // check if url name is correct - else modify it
    if ($scope.show.event) {
      var shouldBeEventSlug = $filter('slugify')($filter('eventname')($scope.event));
      if ($stateParams.eventSlug != shouldBeEventSlug || $state.current.name != 'app.eventShow') {
        $state.go('app.eventShow', {eventSlug: shouldBeEventSlug, eventId: $scope.event.id }, {notify: false});
      }
    } else if ($scope.show.edition) {
      var shouldBeEditionSlug = $filter('slugify')($filter('editionname')($scope.edition));
      if ($stateParams.editionSlug != shouldBeEditionSlug || $state.current.name != 'app.editionShow') {
        $state.go('app.editionShow', {editionSlug: shouldBeEditionSlug, editionId: $scope.edition.id }, {notify: false});
      }
    } else {
      var shouldBeFormatSlug = $filter('slugify')($scope.format.name);
      if ($stateParams.formatSlug != shouldBeFormatSlug || $state.current.name != 'app.formatShow') {
        $state.go('app.formatShow', {formatSlug: shouldBeFormatSlug, formatId: $scope.format.id }, {notify: false});
      }
    }

    if ($scope.show.event) {
      headService.setTitle($filter('eventname')($scope.event), {});
      // define which divisions to show
      $scope.divisionsToShow = $scope.event.x.divisions;
      $scope.getPlayingTeams = function(division) {
        return division.playingTeams;
      }
      $scope.showStandings = function(divisionId) {
        $scope.event.x.timing != 'future' && hasStandings[divisionId]
      }
    } else if ($scope.show.edition) {
      headService.setTitle($filter('editionname')($scope.edition), {});
      $scope.divisionsToShow = $scope.edition.divisionRegistrations;
      $scope.getPlayingTeams = function(division) {
        return division.registeredTeams;
      }
      // calculate if edition has events in different years
      var editionYears = {};
      angular.forEach($scope.edition.events, function(event) {
        editionYears[event.startDate.substring(0,4)] = true;
      });
      $scope.showYearSeparator = Object.keys(editionYears).length > 1;
    } else if ($scope.show.format) {
      headService.setTitle($scope.format.name, {});
    }

    $scope.show.linkToEdition = $scope.show.event && !$scope.event.x.isSingleEvent;

    if (!$scope.show.format) {
      // find out if this event is the last one of this edition (for this division)
      $scope.latestEvents = $scope.edition.x.lastestEventPerDivision;

      // find earliest and latest event and set it to use in calculations
      var lastEvent = null;
      var firstEvent = null;
      if ($scope.show.edition) {
        firstEvent = { startDate: '2900-01-01'};
        lastEvent = { endDate: '1900-01-01'};

        angular.forEach($scope.edition.events, function(event) {
          if (event.endDate > lastEvent.endDate) {
            lastEvent = event;
          }
          if (event.startDate < firstEvent.startDate) {
            firstEvent = event;
          }
        });

        if (lastEvent.endDate == '1900-01-01') {
          lastEvent = null;
          // directly show divisions tab if no event is present
          $scope.tabs.activeTab = 'divisions';
        }

        if (firstEvent.startDate == '2900-01-01') {
          firstEvent = null;
          $scope.editionHasStarted = false;
        } else {
          $scope.editionHasStarted = moment().isSameOrAfter(firstEvent.startDate, 'day');
        }
      }

      // determine if event (or the last event of the series in case of editions) is in the future
      $scope.eventIsFuture = ($scope.show.event && $scope.event.x.timing == 'future')
      || ($scope.show.edition && !isEmpty(lastEvent) && lastEvent.x.timing == 'future');

      $scope.enableTeamRegistrationManagement = (($scope.show.event && $scope.eventIsFuture) || ($scope.show.edition && !$scope.editionHasStarted)) && ($scope.format.x.own || ($scope.show.event && $scope.event.x.own && $scope.edition.allowEventTeamRegManagement));
      $scope.enableTeamStandingManagement = (($scope.show.event && !$scope.eventIsFuture) || ($scope.show.edition && $scope.editionHasStarted)) && ($scope.format.x.own || ($scope.show.event && $scope.event.x.own && $scope.edition.allowEventTeamRegManagement));

      $scope.show.registration = $scope.show.registration && $scope.edition.x.registrationTime != 'never' &&
      ((!isEmpty($scope.event) && $scope.event.x.timing == 'future') || ($scope.show.edition && !$scope.editionHasStarted));

      // if this event is not in the future any more the team lists are different
      $scope.teamOrderReverse = false;
      $scope.teamListOrder = {text:'name'};

      if ($scope.eventIsFuture) {
        $scope.teamFilter = function() { return true; }
      } else {
        $scope.teamFilter = {status: 'CONFIRMED'};
        $scope.teamListOrder.text = 'standing';
      }

      $scope.hasStandings = {};
      $scope.hasSpiritScores = {};
      $scope.hasOwnSpiritScores = {};

      angular.forEach($scope.divisionsToShow, function(division) {
        division.registrationComplete = false;

        // get number of confirmed teams
        division.numTeamsConfirmed = 0;
        division.numTeamsPending = 0;
        angular.forEach($scope.getPlayingTeams(division), function(teamReg) {
          if (teamReg.status == 'CONFIRMED') {
            division.numTeamsConfirmed++;
          } else if (teamReg.status == 'PENDING') {
            division.numTeamsPending++;
          }

          checkForStandings(division);
        });

        if ($scope.edition.x.registrationTime == 'never' || !$scope.eventIsFuture) {
          division.registrationComplete = true;
        } else {
          // if registration is yet to come or still open, it's obviously not complete
          if ($scope.edition.x.registrationTime == 'past' && !$scope.edition.x.registrationIsOpen) {
            // ...but if it's closed we have to check whether or not teams are still pending
            if (division.numTeamsPending == 0) {
              division.registrationComplete = true;
            }
          }
        }
      });
    }

    // define whether or not the info texts should be shown
    $scope.info = {
      showFormatInfo: $scope.show.formatInfo && !isEmpty($scope.format.description),
      showEventInfo: $scope.show.eventInfo && !isEmpty($scope.event.info),
    };

    updateActions();
  };

  function updateActions() {
    actionBar.clearActions();
    // Action bar actions
    if ($scope.show.registration && !$scope.show.format && $scope.edition.x.registrationIsOpen) {
      actionBar.addSeparator('event-registration');
      actionBar.addAction({
        group: 'event-registration',
        needLogIn: false,
        text: 'event.register.notLoggedIn',
      });
      if (!isEmpty($scope.ownTeams)) {
        // has own teams
        actionBar.addAction({
          group: 'event-registration',
          needLogIn: true,
          button: {
            text: 'event.register.title',
            click: function() {
              $scope.openRegistrationModal();
            }
          },
        });
      } else {
        // has not yet own teams
        actionBar.addAction({
          group: 'event-registration',
          needLogIn: true,
          text: 'event.register.noOwnTeam',
        });
      }
    }

    if ($scope.show.event && !(isEmpty($scope.event.localOrganizer) || isEmpty($scope.event.localOrganizer.email))) {
      actionBar.addAction({
        group: 'contact-event',
        needLogIn: true,
        button: {
          text: 'event.contactButton',
          click: function() {
            openEmailToEventModal();
          },
        }
      });
    }

    if ($scope.show.event && $scope.event.x.own && !$scope.format.x.own) {
      actionBar.addAction({
        group: 'event-admin',
        needLogIn: true,
        text: 'event.youAreEventAdmin',
        separator: true,
      });
    }
    if ($scope.format.x.own) {
      actionBar.addAction({
        group: 'event-admin',
        needLogIn: true,
        text: 'event.youAreFormatAdmin',
        separator: true,
      });
    }
    if (!$scope.show.format && ($scope.format.x.own || ($scope.show.event && $scope.event.x.own))) {
      actionBar.addAction({
        group: 'event-admin',
        needLogIn: true,
        button: {
          text: 'event.email.buttonLabel',
          click: function() {
            openEmailToTeamsModal();
          },
        },
      });
    }
    if ($scope.show.event && !$scope.editEvent && ($scope.format.x.own || $scope.event.x.own)) {
      actionBar.addAction({
        group: 'event-admin',
        needLogIn: true,
        button: {
          text: 'event.edit.buttonLabel',
          click: function() {
            toggleEventEdit();
          },
        },
      });
    }
  }

  $scope.openRegistrationModal = function() {
    var modal = $modal({
      animation: 'am-fade-and-slide-top',
      templateUrl: 'pages/event/registration_modal.html?v=15',
      show: true,
      scope: $scope,
    });
  };

  function openEmailToTeamsModal() {
    var modal = $modal({
      animation: 'am-fade-and-slide-top',
      templateUrl: 'pages/event/email_teams_modal.html?v=4',
      show: true,
      scope: $scope,
    });
  };

  function openEmailToEventModal() {
    var newScope = $scope.$new();
    newScope.mailToEvent = true;
    newScope.event = $scope.event;

    var modal = $modal({
      animation: 'am-fade-and-slide-top',
      templateUrl: 'components/email_service/email_modal.html?v=4',
      show: true,
      scope: newScope,
    });
  };

  $scope.editEvent = false;
  function toggleEventEdit() {
    $scope.editEvent = !$scope.editEvent;
    updateActions();
  }

  function checkForStandings(division) {
    // check if there are standings / spirit scores
    if ($scope.eventIsFuture) {
      $scope.hasStandings[division.id] = false;
      $scope.hasSpiritScores[division.id] = false;
      $scope.hasOwnSpiritScores[division.id] = false;
    } else {
      angular.forEach($scope.getPlayingTeams(division), function(playingTeam) {
        if (playingTeam.status == 'CONFIRMED') {
          $scope.hasStandings[division.id] = !isEmpty(playingTeam.standing) && playingTeam.standing != -1;
          $scope.hasSpiritScores[division.id] = !isEmpty(playingTeam.spiritScore) && playingTeam.spiritScore != -1;
          $scope.hasOwnSpiritScores[division.id] = !isEmpty(playingTeam.ownSpiritScore) && playingTeam.ownSpiritScore != -1;
        }
      });
    }
  }

  $scope.teamRegConfirm = function(teamRegistration) {
    if (teamRegistration.status == 'CONFIRMED') {
      return;
    }
    storage.updateTeamRegStatus($scope.event, teamRegistration, 'CONFIRMED');
  }
  $scope.teamRegToWaitingList = function(teamRegistration) {
    if (teamRegistration.status == 'WAITING_LIST') {
      return;
    }
    storage.updateTeamRegStatus($scope.event, teamRegistration, 'WAITING_LIST');
  }
  $scope.teamRegDecline = function(teamRegistration) {
    if (teamRegistration.status == 'DECLINED') {
      return;
    }
    storage.updateTeamRegStatus($scope.event, teamRegistration, 'DECLINED');
  }


	// collapses
	$scope.panels = {
			activeLocationPanel: 1,
			divisionPanel: -1,
			teamPanel: -1,
	};

	$scope.toggleLocationPanel = function() {
		$scope.panels.activeLocationPanel = $scope.panels.activeLocationPanel == 1 ? 0 : 1;
	}

	// printing
	$scope.printAll = false;
	$scope.printing = false;

	$scope.print = function() {
		$scope.printAll = false;
		$scope.printing = true;
		$timeout(function() {
			$window.print();
			$scope.printing = false;
		}, 0);
	};

	$scope.doPrintAll = function() {
		$scope.printAll = true;
		$scope.printing = true;
		$timeout(function() {
			$window.print();
			$scope.printing = false;
		}, 0);
	}

	$scope.getAllFees = function() {
		$scope.editionFeeEndIndex = 0;
		var fees = [];
		angular.forEach($scope.edition.fees, function(fee) {
			$scope.editionFeeEndIndex++;
			if (!('x' in fee)) {
				fee.x = {};
			}
			fee.x.fromEdition = true;
			fee.x.order = getFeeTypeOrder(fee.type);
			fees.push(fee);
		});
		angular.forEach($scope.event.fees, function(fee) {
			if (!('x' in fee)) {
				fee.x = {};
			}
			fee.x.fromEdition = false;
			fee.x.order = getFeeTypeOrder(fee.type) + 100;
			fees.push(fee);
		});
		return fees;
	};

	function getFeeTypeOrder(feeType) {
		var feeOrder = 0;
		switch (feeType) {
		case 'PLAYER':
			feeOrder = 5;
			break;
		case 'TEAM':
			feeOrder = 2;
			break;
		case 'GUEST':
			feeOrder = 7;
			break;
		case 'BREAKFAST':
			feeOrder = 11;
			break;
		case 'LUNCH':
			feeOrder = 14;
			break;
		case 'DINNER':
			feeOrder = 17;
			break;
		case 'NIGHT':
			feeOrder = 20;
			break;
		case 'OTHER':
			feeOrder = 23;
			break;
		}
		return feeOrder;
	}

	$scope.getRelevantPlayers = function(regTeam, division, season, event) {
		if (regTeam.roster == null) {
			return [];
		} else {
			// remove players that have been added after the event started
			var relevantPlayers = [];
			var startDate = moment(event.startDate);
			angular.forEach(regTeam.roster.players, function(rosterPlayer) {
				if (moment(rosterPlayer.dateAdded).isBefore(startDate)) {
					relevantPlayers.push(rosterPlayer);
				}
			});

			return relevantPlayers;
		}
	};

	$scope.teamOrder = function(regTeam) {

    if ($scope.eventIsFuture) {
      // the order for registration
  		var orderString = '';
  		switch (regTeam.status) {
  		case 'CONFIRMED':
  			orderString += '1';
  			break;
  		case 'PENDING':
  			orderString += '2';
  			break;
  		case 'WAITING_LIST':
  			orderString += '3';
  			break;
  		case 'DECLINED':
  			orderString += '4';
  			break;
  		}
      $scope.teamOrderReverse = false;
  		return orderString + regTeam.roster.team.name;
    } else {
      if ($scope.editStandings) {
        return regTeam.roster.team.name;
      }

      // the order for the time after the tournament took playerBlocked
      switch ($scope.teamListOrder.text) {
      case 'name':
        $scope.teamOrderReverse = false;
        return regTeam.roster.team.name;
        break;
      case 'standing':
        $scope.teamOrderReverse = false;
        return regTeam['standing'];
        break;
      case 'spirit':
        $scope.teamOrderReverse = true;
        return regTeam['spiritScore'];
        break;
      }
    }
	};

	$scope.divisionOrder = function(division) {
		return $filter('division')(division);
	}

	var teamHeadings = {};
	$scope.needTeamHeading = function(index, divisionId, teamStatus) {
		if (index == 0 || !(divisionId in teamHeadings)) {
			teamHeadings[divisionId] = teamStatus;
			return true;
		} else if (teamHeadings[divisionId] != teamStatus) {
			teamHeadings[divisionId] = teamStatus;
			return true;
		}
		return false;
	};

  $scope.toggleEditStandings = function(division) {
    angular.forEach($scope.getPlayingTeams(division), function(teamReg) {
      if (teamReg.standing == -1) {
        teamReg.standing = '';
      }
      if (teamReg.spiritScore == -1) {
        teamReg.spiritScore = '';
      }
      if (teamReg.ownSpiritScore == -1) {
        teamReg.ownSpiritScore = '';
      }
    });
    $scope.editStandings = !$scope.editStandings;
  }

  $scope.saveStandings = function(division) {
    angular.forEach($scope.getPlayingTeams(division), function(teamReg) {
      if (teamReg.standing == null || isNaN(teamReg.standing) || teamReg.standing <= 0) {
        teamReg.standing = null;
      }

      teamReg.spiritScore = readFloat(teamReg.spiritScore);
      teamReg.ownSpiritScore = readFloat(teamReg.ownSpiritScore);
    });

    storage.updateStandings($scope.event, $scope.getPlayingTeams(division), function() {
      $scope.editStandings = false;
      checkForStandings(division);
    }, function() {
      // TODO: error
    });

  }

  function readFloat(item) {
    if (undefined === item || item == null) {
      item = null;
    } else {
      if (isNaN(item)) {
        item = parseFloat(item.replace(',', '.'));
      }
      if (isNaN(item) || item < 0) {
        item = null;
      }
    }
    return item;
  }

}]);
