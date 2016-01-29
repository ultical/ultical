'use strict';

angular.module('ultical.events')

.controller('EventShowCtrl', ['$scope', '$stateParams', 'storage', '$state', '$filter', 'moment',
                              function($scope, $stateParams, storage, $state, $filter, moment) {

	$scope.event = {};
	$scope.mainLocation = null;
	$scope.curEvent = {};

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

	$scope.panels = {
			activeLocationPanel: 1,
	};

	$scope.toggleLocationPanel = function() {
		$scope.panels.activeLocationPanel = $scope.panels.activeLocationPanel == 1 ? 0 : 1;
	}

	$scope.downloadTeamlist = function() {

		var newLine = '\n';
		var output = '';

		output += "Spielerliste " + $filter('eventname')($scope.event, true) + newLine;
		output += "Stand vom: " + moment().format("DD.MM.YYYY - HH:mm") + newLine;

		output += newLine + newLine;

		angular.forEach($scope.event.tournamentEdition.divisionRegistrations, function(divReg) {
			output += $filter('division')(divReg) + newLine + newLine;

			angular.forEach(divReg.registeredTeams, function(regTeam) {
				output += regTeam.team.name + newLine + newLine;

				var validRoster = null;
				angular.forEach(regTeam.team.rosters, function(roster) {
					if (id($scope.edition.season) == id(roster.season) && divReg.divisionType == roster.divisionType && divReg.divisionAge == roster.divisionAge) {
						validRoster = roster;
					}
				});

				if (validRoster != null) {
					angular.forEach(validRoster.players, function(regPlayer) {
//						output += moment(regPlayer.dateAdded).format("DD.MM.YYYY") + "  -  ";
						output += regPlayer.player.firstName + ' ' + regPlayer.player.lastName + newLine;
					});
				}

				output += newLine;
			});
		});

		console.log("output", output);
		return;
		downloader.performDownload(output, "Reisekostenumlage.csv");
	};

	function id(element) {
		if (angular.isObject(element)) {
			return element.id;
		} else {
			return element;
		}
	}

}]);

