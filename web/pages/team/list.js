'use strict';

angular.module('ultical.team', [])

.controller('TeamListCtrl', ['CONFIG', '$scope', '$stateParams', 'storage', '$state', '$filter', 'authorizer', 'serverApi', '$http', 'mapService', 'alerter', '$timeout', 'moment',
                             function(CONFIG, $scope, $stateParams, storage, $state, $filter, authorizer, serverApi, $http, mapService, alerter, $timeout, moment) {

	$scope.currentLocale = $stateParams.locale;

	$scope.loggedIn = function() {
		return authorizer.loggedIn();
	}

	$scope.allClubs = [];

	$scope.bigButtons = CONFIG.general.actionButtonsBig;

	$scope.activeUserId = authorizer.getUser() != null ? authorizer.getUser().id : -1;

	// make sure that we only watch 'own' teams if we are logged in
	if ($stateParams.activeTab == 'own' && !authorizer.loggedIn()) {
		$stateParams.activeTab = 'all';
	}

	// make sure we are directly at the right tab ('own' or 'all')
	$scope.tabs = { activeTab: $stateParams.activeTab ? $stateParams.activeTab: 'all' };

	$scope.newEmail = { text: ''};
	$scope.newAdmin = { obj: {}};

	$scope.teams = [];
	getTeams();

	$scope.$watch('tabs.activeTab', function() {
		var newState = 'app.teams' + ($scope.tabs.activeTab == 'all' ? 'List' : 'Own');
		$state.go(newState);
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
		serverApi.getAllClubs(function(clubs) {
			$scope.allClubs = clubs;
		});

		if (team.foundingDate == 0) {
			team.foundingDate = '';
		}

		$scope.editing = true;
		$scope.teamToEdit = angular.copy(team);
		$scope.locationToEdit = angular.copy($scope.teamToEdit.location);
	};

	$scope.locationIsMissing = false;

	$scope.saveTeam = function(team) {
		if (!angular.isObject(team.location) || isEmpty(team.location.city)) {
			$scope.locationIsMissing = true;
			return;
		}

		$scope.addAdmin($scope.newAdmin.obj);
		$scope.addEmail($scope.newEmail);

		storage.saveTeam(team, function(ownTeams) {
			$scope.teams = ownTeams;
			$scope.editing = false;
			$scope.panel.activePanel = -1;
			$scope.locationIsMissing = false;
		}, function(errorResponse) {
			// probably a validation error
			if (errorResponse.status == 417) {
				$scope.locationIsMissing = true;
			}
		},
		$scope.tabs.activeTab);
	};

	$scope.cancel = function() {
		$scope.locationIsMissing = false;
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

	$scope.oldPlayerNames = [];

	// return player proposals
	$scope.getPlayers = function(playerName) {
		if (playerName.length < 4) {
			return [];
		}
		if (angular.isObject(playerName)) {
			return $scope.oldPlayerNames;
		}

		return serverApi.getPlayerProposals(playerName, function(result) {
			angular.forEach(result, function(player) {
				player.fullName = player.firstName + ' ' + player.lastName;
				if (player.club != null) {
					player.fullName += ' <i><small>(' + player.club.name + ')</small></i>';
				}
			});
			$scope.oldPlayerNames = result;
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

	$scope.oldLocations = [];

	// return location-proposals from mapbox api
	$scope.getLocations = function(locationName) {
		if (locationName.length < 4) {
			return [];
		}
		if (angular.isObject(locationName)) {
			return $scope.oldLocations;
		}

		return mapService.getLocations(locationName, 'city', function(locations) {
			angular.forEach(locations, function(location) {
				location.mapBoxId = location.id;
				location.id = $scope.locationToEdit.id;
				location.version = $scope.locationToEdit.version;
			});
			$scope.oldLocations = locations;
			return locations;
		});
	};

	$scope.locationMatching = function() {
		return true;
	};

	$scope.editingRoster = -1;

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
			team.rosters.push(updatedRoster);
			updatedRoster.players = [];
			$scope.editingRoster = -1;
		}, function(errorResponse) {
			if (errorResponse.status = 409) {
				// a roster with the same season, division and identifier already exists for this team
				alerter.error('', 'team.roster.rosterDuplicated', {container: '#roster-error' + team.id, duration: 10});
			}
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
					alerter.error('', 'team.roster.playerAlreadyInRoster', {container: '#add-player-error' , duration: 10});
					break;
				case 'e102':
					// this player has the wrong gender
					alerter.error('', 'team.roster.playerWrongGender', { container: '#add-player-error', duration: 10});
					break;
				case 'e103':
					// this player has not the right age for this division
					alerter.error('', 'team.roster.playerWrongAge', { container: '#add-player-error', duration: 10});
					break;
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
		roster.blocked = false;

		serverApi.getRosterBlockingDate(roster.id, function(blockingDates) {
			roster.blockingDates = blockingDates;

			// get the relevant blocking date
			var today = moment();
			var lastBlockingDateBeforeToday = moment('1900-01-01');

			angular.forEach(blockingDates, function(blockingDateString) {
				var blockingDate = moment(blockingDateString.string);
				if (!blockingDate.isAfter(today)) {
					roster.blocked = true;

					if (lastBlockingDateBeforeToday.isBefore(blockingDate)) {
						lastBlockingDateBeforeToday = blockingDate;
					}
				}
			});
			angular.forEach(roster.players, function(player) {
				player.blocked = lastBlockingDateBeforeToday.isAfter(moment(player.dateAdded));
			});
		});

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
		storage.removePlayerFromRoster(player, roster, function() {}, 
				function(errorResponse) {
			if (errorResponse.status = 403) {
				// this player was part of a roster during an official tournament
				// thus: it cannot be removed at it would falsify the eligibility rules
				alerter.error('', 'team.roster.playerBlocked', {container: '#add-player-error' , duration: 10});
			}
		});
	};

	$scope.deleteRoster = function(roster, team) {
		alerter.confirm('team.roster.confirmDelete', function(userResponse) {
			if (userResponse == true) {
				storage.deleteRoster(roster, team, function() {},
						function(errorResponse) {
					if (errorResponse.status = 403) {
						// this roster was active during an official tournament - cannot be deleted
						alerter.error('', 'team.roster.rosterBlocked', {container: '#roster-error' + team.id, duration: 10});
					}
				});
			}
		});
	};
}]);
