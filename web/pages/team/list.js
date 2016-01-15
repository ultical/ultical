'use strict';

angular.module('ultical.team', [])

.controller('TeamListCtrl', ['CONFIG', '$scope', '$stateParams', 'storage', '$state', '$filter', 'authorizer', 'serverApi', '$http', 'mapService', 'alerter', '$timeout',
                             function(CONFIG, $scope, $stateParams, storage, $state, $filter, authorizer, serverApi, $http, mapService, alerter, $timeout) {

	$scope.loggedIn = function() {
		return authorizer.loggedIn();
	}

	$scope.bigButtons = CONFIG.general.actionButtonsBig;

	$scope.activeUserId = authorizer.getUser() != null ? authorizer.getUser().id : -1;

	// make sure we are directly at the right tab ('own' or 'all')
	$scope.tabs = { activeTab: $stateParams.activeTab ? $stateParams.activeTab: 'all' };

	$scope.newEmail = { text: ''};
	$scope.newAdmin = { obj: {}};
	$scope.teams = [];

	$scope.$watch('tabs.activeTab', function() {
		$scope.teamPanels.activePanel = -1;
		$scope.editingRoster = -1;
		$scope.editing = false;
		$scope.teams = [];
		getTeams();
	});

	$scope.teamOrder = 'name';

	// get teams
	function getTeams() {
		if ($scope.tabs.activeTab == 'all') {
			storage.getAllTeams(function(teams) {
				$scope.teams = teams;
			});
		} else {
			storage.getOwnTeams(function(teams) {
				$scope.teams = teams;
			});
		};
	}

	$scope.createNewTeam = function() {
		$scope.editTeam({
			id: -1,
			name: '',
			description: '',
			admins: [authorizer.getUser()],
			emails: [],
			location: {},
			foundingDate: '',
			rosters: [],
			url: '',
			facebookUrl: '',
			twitterName: '',
			contactEmail: '',
		});
	};

	$scope.editTeam = function(team) {
		$scope.editing = true;
		$scope.teamToEdit = angular.copy(team);
		$scope.locationToEdit = angular.copy($scope.teamToEdit.location);
	};

	$scope.saveTeam = function(team) {
		$scope.addAdmin($scope.newAdmin.obj);
		$scope.addEmail($scope.newEmail);

		storage.saveTeam(team, function(ownTeams) {
			$scope.teams = ownTeams;
			$scope.editing = false;
			$scope.panel.activePanel = -1;
		}, $scope.tabs.activeTab);
	};

	$scope.cancel = function() {
		$scope.teamToEdit = {};
		$scope.editing = false;
		$scope.panel.activePanel = -1;
	}

	$scope.addAdmin = function(newAdmin) {
		if (isEmpty(newAdmin) || isEmpty(newAdmin.obj)) {
			return;
		}

		if (!angular.isObject(newAdmin.obj)) {
			return;
		}

		// check if admin is already in the list
		var alreadyAdmin = false;
		angular.forEach($scope.teamToEdit.admins, function(admin) {
			if (admin.id == newAdmin.obj.id) {
				alreadyAdmin = true;
			}
		});

		if (!alreadyAdmin) {
			$scope.teamToEdit.admins.push(newAdmin.obj);
		}

		$scope.newAdmin = {};
	}

	$scope.removeAdmin = function(adminId) {
		var indexToRemove = -1;
		angular.forEach($scope.teamToEdit.admins, function(admin, idx) {
			if (admin.id == adminId) {
				indexToRemove = idx;
			}
		});

		if (indexToRemove >= 0) {
			$scope.teamToEdit.admins.splice(indexToRemove, 1);
		}
	}

	$scope.addEmail = function(emailForm, newEmail) {
		if (isEmpty(newEmail) || isEmpty(newEmail.text)) {
			return;
		}
		newEmail = newEmail.text;

		if (!emailForm.$valid) {
			return;
		}

		var alreadyEmail = false;
		angular.forEach($scope.teamToEdit.emails, function(email) {
			if (email == newEmail) {
				alreadyEmail = true;
			}
		});

		if (!alreadyEmail) {
			$scope.teamToEdit.emails.push(newEmail);
		}

		$scope.newEmail = {};
	}

	$scope.removeEmail = function(emailToRemove) {
		var indexToRemove = -1;
		angular.forEach($scope.teamToEdit.emails, function(email, idx) {
			if (email == emailToRemove) {
				indexToRemove = idx;
			}
		});

		if (indexToRemove >= 0) {
			$scope.teamToEdit.emails.splice(indexToRemove, 1);
		}
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

	// return player proposals
	$scope.getPlayers = function(playerName) {
		if (playerName.length < 4) {
			return [];
		}

		return serverApi.getPlayerProposals(playerName, function(result) {
			angular.forEach(result, function(player) {
				player.fullName = player.firstName + ' ' + player.lastName;
				if (player.club != null) {
					player.fullName += ' <i><small>(' + player.club.name + ')</small></i>';
				}
			});
			return result;
		});
	};

	$scope.deleteTeam = function(team) {
		alerter.confirm('team.confirmDelete', function(userResponse) {
			if (userResponse == true) {
				// not yet implemented
				// storage.deleteTeam(team);
			}
		});
	}

	// return location-proposals from mapbox api
	$scope.getLocations = function(locationName) {
		if (locationName.length < 4) {
			return [];
		}

		return mapService.getLocations(locationName, 'city', function(locations) {
			angular.forEach(locations, function(location) {
				location.mapBoxId = location.id;
				location.id = $scope.locationToEdit.id;
				location.version = $scope.locationToEdit.version;
			});
			return locations;
		});
	};

	$scope.locationMatching = function() {
		return true;
	};

	$scope.createNewRoster = function(team) {
		$scope.editingRoster = team.id;
		$scope.newRoster = {
				divisionAge: 'regular',
				divisionType: 'open',
				season: {},
		}
		storage.getSeasons(function(seasons) {
			angular.forEach(seasons, function(season) {
				if (season.year == '2016') {
					$scope.newRoster.season = season;
				}
			});
		});
	}

	$scope.cancelRosterEdit = function() {
		$scope.editingRoster = -1;
	}

	// prepare selects for roster creation
	$scope.selects = {
			divisionAges: CONFIG.division.ages,
			divisionTypes: CONFIG.division.types,
			seasons: [],
	};

	storage.getSeasons(function(seasons) {
		$scope.selects.seasons = seasons;
	});

	$scope.saveRoster = function(newRoster, team) {
		storage.saveRoster(newRoster, team, function(updatedRoster) {
			storage.getTeam(team.id, function(updatedTeam) {
				team.rosters = updatedTeam.rosters;
			});
			$scope.editingRoster = -1;
		});
	};

	$scope.addPlayerToRoster = function(newPlayer, roster) {
		if (!angular.isObject(newPlayer.obj)) {
			return;
		}
		if ($scope.editRosterBlock) {
			return;
		}

		$scope.editRosterBlock = true;

		var alreadyInRoster = false;
		angular.forEach(roster.players, function(rosterPlayer) {
			if (newPlayer.obj.dfvNumber == rosterPlayer.player.dfvNumber) {
				alreadyInRoster = true;
			}
		});

		if (alreadyInRoster) {
			$scope.editRosterBlock = false;
			return;
		}

		storage.addPlayerToRoster(newPlayer.obj, roster, function() {
			$scope.newPlayer = {};
			$scope.editRosterBlock = false;
		}, function(errorResponse) {
			if (errorResponse.status = 409) {
				switch(errorResponse.message.substring(0,4)) {
				case 'e101':
					// this player is already part of another roster in this season and division
					alerter.error('', 'team.roster.playerAlreadyInRoster', {container: '#add-player-error' , duration: 5});
					break;
				case 'e102':
					// this player has the wrong gender
					alerter.error('', 'team.roster.playerWrongGender', { container: '#add-player-error', duration: 5});
				}
			}
			$scope.editRosterBlock = false;
		});
	};

	$scope.teamPanels = {};
	$scope.editRosterBlock = false;

	// we use this value to let the input fields disappear, when a different roster is un-collapsed
	$scope.actualRosterEditedPanelIdx = -1;

	$scope.editRoster = function(roster, team, collapseIndex) {
		$scope.actualRosterEditedPanelIdx = collapseIndex;
		$scope.editRosterBlock = false;
		$scope.newPlayer = {};
		$scope.editingRosterPlayers = roster.id;

		$timeout(function() {
			$scope.teamPanels.activePanel = collapseIndex;
		}, 0);
	};

	$scope.$watch('panel.activePanel', function() {
		$scope.teamPanels.activePanel = -1;
	});

	$scope.$watch('teamPanels.activePanel', function() {
		$timeout(function() {
			if ($scope.actualRosterEditedPanelIdx != $scope.teamPanels.activePanel) {
				$scope.rosterEditEnd();
			}
		}, 200);
	});

	$scope.rosterEditEnd = function() {
		$scope.editingRosterPlayers = -1;
		$scope.actualRosterEditedPanelIdx = -1
	};

	$scope.removePlayerFromRoster = function(player, roster) {
		storage.removePlayerFromRoster(player, roster, function() {});
	};

	$scope.deleteRoster = function(roster, team) {
		alerter.confirm('team.roster.confirmDelete', function(userResponse) {
			if (userResponse == true) {
				storage.deleteRoster(roster, team);
			}
		});
	};
}]);
