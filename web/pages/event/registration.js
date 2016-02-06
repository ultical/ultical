'use strict';

angular.module('ultical.events')

.controller('EventRegistrationCtrl', ['$scope', 'storage', '$filter', '$rootScope',
                                      function($scope, storage, $filter, $rootScope) {

	$scope.ownTeams = [];
	$scope.teamRegistrationPending = false;

	$scope.register = {};
	$scope.register.division = $scope.event.x.divisions[0].id+'';

	$scope.eventName = $scope.event.x.isSingleEvent ? $filter('eventname')($scope.event) : $filter('editionname')($scope.event.tournamentEdition);

	storage.getOwnTeams(function(teams) {
		$scope.ownTeams = teams;
		if (teams.length > 0) {
			$scope.register.team = teams[0].id+'';
		}
	});

	$rootScope.teamRegistrationDisabled = false;

	$scope.isAlreadyRegistered = function(teamId, divisionId) {
		if ($scope.teamRegistrationPending) {
			return false;
		}
		
		var result = false;
		angular.forEach($scope.event.x.divisions, function(division) {
			if (division.id == divisionId) {
				angular.forEach(division.registeredTeams, function(regTeam) {
					if (regTeam.team.id == teamId) {
						result = true;
					}
				});
			}
		});
		$rootScope.teamRegistrationDisabled = result;
		return result;
	};

	$rootScope.doTeamRegister = function() {
		$scope.teamRegistrationPending = true;

		var registration = {};
		registration.comment = $scope.register.comment ? $scope.register.comment : null;
		angular.forEach($scope.ownTeams, function(team) {
			if (team.id == $scope.register.team) {
				registration.team = {id: team.id};
			}
		});
		var division = {};
		angular.forEach($scope.event.x.divisions, function(div) {
			if (div.id == $scope.register.division) {
				division = div;
			}
		});

		storage.registerTeamForEdition(registration, division, function(newTeamReg) {
			angular.forEach($scope.ownTeams, function(team) {
				if (team.id == $scope.register.team) {
					newTeamReg.team = team;
				}
			});
			$scope.$hide();
		});
	};

}]);

