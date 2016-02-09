'use strict';

angular.module('ultical.events')

.controller('EventShowCtrl', ['$scope', '$stateParams', 'storage', '$state', '$filter', 'moment', 'authorizer', '$window', '$timeout',
                              function($scope, $stateParams, storage, $state, $filter, moment, authorizer, $window, $timeout) {

	$scope.event = {};
	$scope.mainLocation = null;
	$scope.curEvent = {};
	$scope.now = new Date();

	$scope.loggedIn = function() {
		return authorizer.loggedIn();
	}

	storage.getFormatForEvent($stateParams.eventId, function(format) {
		$scope.format = format;

		angular.forEach(format.editions, function(edition) {
			angular.forEach(edition.events, function(event) {
				if (event.id == $stateParams.eventId) {
					// this is the right event
					$scope.event = event;
					$scope.edition = edition;
				}
			});
		});
	});

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
		// find relevant roster
		var relevantRoster = null;

		angular.forEach(regTeam.team.rosters, function(roster) {
			if (roster.divisionAge == division.divisionAge && roster.divisionType == division.divisionType && roster.season.id == season.id) {
				relevantRoster = roster;
			}
		});
		if (relevantRoster == null) {
			return [];
		} else {
			// remove players that have been added after the event started
			var relevantPlayers = [];
			var startDate = moment(event.startDate);
			angular.forEach(relevantRoster.players, function(rosterPlayer) {
				if (moment(rosterPlayer.dateAdded).isBefore(startDate)) {
					relevantPlayers.push(rosterPlayer);
				}
			});

			return relevantPlayers;
		}
	};

	$scope.teamOrder = function(regTeam) {
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
		return orderString + regTeam.team.name;
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

