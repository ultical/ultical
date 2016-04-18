'use strict';

angular.module('ultical.events')

.controller('EventShowCtrl', ['$scope', '$stateParams', 'storage', '$state', '$filter', 'moment', 'authorizer', '$window', '$timeout', 'headService',
                              function($scope, $stateParams, storage, $state, $filter, moment, authorizer, $window, $timeout, headService) {

	$scope.event = {};
  $scope.edition = {};
  $scope.format = {};

	$scope.now = new Date();

	$scope.loggedIn = function() {
		return authorizer.loggedIn();
	}

  $scope.tabs = {
    activeTab: 'events',
  };

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

    if ($scope.show.event) {
      headService.setTitle($filter('eventname')($scope.event), {});
    } else if ($scope.show.edition) {
      headService.setTitle($filter('editionname')($scope.edition), {});
    } else if ($scope.show.format) {
      headService.setTitle($scope.format.name, {});
    }

    $scope.show.linkToEdition = $scope.show.event && !$scope.event.x.isSingleEvent;

    // define which divisions to show
    if ($scope.show.event) {
      $scope.divisionsToShow = $scope.event.x.divisions;
      $scope.getPlayingTeams = function(division) {
        return division.playingTeams;
      }
    } else {
      $scope.divisionsToShow = $scope.edition.divisionRegistrations;
      $scope.getPlayingTeams = function(division) {
        return division.registeredTeams;
      }
    }

    if (!$scope.show.format) {

      // find out if this event is the last one of this edition (for this division)
      $scope.latestEvents = $scope.edition.x.lastestEventPerDivision;

      // find latest event and set it to use in calculations
      if ($scope.show.edition) {
        $scope.event = { endDate: '1900-01-01'};
        angular.forEach($scope.latestEvents, function(latestEvent) {
          if (latestEvent.endDate > $scope.event.endDate) {
            $scope.event = latestEvent;
          }
        });
      }

      // if this event is not in the future any more the team lists are different
      $scope.teamOrderReverse = false;
      if ($scope.event.x.timing == 'future') {
        $scope.teamFilter = function() { return true; }
      } else {
        $scope.teamFilter = {status: 'CONFIRMED'};
        $scope.teamListOrder = {text:'standing'};
      }

      $scope.hasStandings = {};
      $scope.hasSpiritScores = {};
      $scope.hasOwnSpiritScores = {};

      angular.forEach($scope.event.x.divisions, function(division) {
        division.registrationComplete = false;

        // get number of confirmed teams
        division.numTeamsConfirmed = 0;
        angular.forEach(division.playingTeams, function(teamReg) {
          if (teamReg.status == 'CONFIRMED') {
            division.numTeamsConfirmed++;
          }

          // check if there are standings / spirit scores
          if ($scope.event.x.timing == 'future') {
            $scope.hasStandings[division.id] = false;
            $scope.hasSpiritScores[division.id] = false;
            $scope.hasOwnSpiritScores[division.id] = false;
          } else {
            angular.forEach(division.playingTeams, function(playingTeam) {
              if (playingTeam.status == 'CONFIRMED') {
                $scope.hasStandings[division.id] = !isEmpty(playingTeam.standing) && playingTeam.standing != -1;
                $scope.hasSpiritScores[division.id] = !isEmpty(playingTeam.spiritScore) && playingTeam.spiritScore != -1;
                $scope.hasOwnSpiritScores[division.id] = !isEmpty(playingTeam.ownSpiritScore) && playingTeam.ownSpiritScore != -1;
              }
            });
          }
        });

        if ($scope.edition.x.registrationTime == 'never' || $scope.event.x.timing != 'future') {
          division.registrationComplete = true;
        } else {
          // if registration is yet to come or still open, it's obviously not complete
          if ($scope.edition.x.registrationTime == 'past' && !$scope.edition.x.registrationIsOpen) {
            // ...but if it's closed we have to check whether or not enough teams were selected
            if (division.numTeamsConfirmed == division.numberSpots || division.numTeamsConfirmed == division.playingTeams.length) {
              division.registrationComplete = true;
            }
          }
        }
      });
    }

    // define whether or not the info texts should be shown
    $scope.info = {};
    $scope.info.hasFormatInfo = !isEmpty($scope.format.description);
    $scope.info.hasEventInfo = !isEmpty($scope.event.info);
    $scope.info.showFormatInfo = $scope.show.formatInfo && $scope.info.hasFormatInfo;
    $scope.info.showEventInfo = $scope.show.eventInfo && $scope.info.hasEventInfo;
  };


  // get own teams to determine if this user may register a team
  $scope.ownTeams = null;
  if (authorizer.loggedIn()) {
    storage.getOwnTeamsCache(function(cachedOwnTeams) {
      $scope.ownTeams = cachedOwnTeams;
    });
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

    if ($scope.event.x.timing == 'future') {
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

}]);
