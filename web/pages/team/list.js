'use strict';

angular.module('ultical.team', [])

.controller('TeamListCtrl', ['CONFIG', '$scope', '$stateParams', 'storage', '$state', '$filter', 'authorizer', 'serverApi', 'mapService', 'alerter', '$timeout', 'moment', 'headService', '$modal',
  function(CONFIG, $scope, $stateParams, storage, $state, $filter, authorizer, serverApi, mapService, alerter, $timeout, moment, headService, $modal) {

    $scope.currentLocale = $stateParams.locale;
    $scope.showContexts = false;

    storage.getContexts(function(contexts) {
        $scope.showContexts = contexts.length > 1;
    });

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
    $scope.tabs = {
      activeTab: $stateParams.activeTab ? $stateParams.activeTab : 'all'
    };

    headService.setTitle('team.list.' + $stateParams.activeTab, {});

    $scope.newEmail = {
      text: ''
    };
    $scope.newAdmin = {obj:null};

    $scope.teams = [];
    getTeams();

    $scope.$watch('tabs.activeTab', function() {
      var newState = 'app.teams' + ($scope.tabs.activeTab == 'all' ? 'List' : 'Own');
      $state.go(newState);
    });

    $scope.teamOrder = 'name';

    $scope.createNewRoster = function(team) {
      $scope.addRosterToThisTeam = team;
      $scope.rosterToEdit = null;
      var myModal = $modal({
        scope: $scope,
        animation: "am-fade-and-slide-top",
        templateUrl: "pages/team/roster_modal.html?v=15",
        show: true
      });
    }

    $scope.editRoster = function(roster, team) {
      $scope.addRosterToThisTeam = team;
      $scope.rosterToEdit = roster;
      var myModal = $modal({
        scope: $scope,
        animation: "am-fade-and-slide-top",
        templateUrl: "pages/team/roster_modal.html?v=15",
        show: true
      });
    };

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
        location: null,
        foundingDate: '',
        rosters: [],
        url: '',
        facebookUrl: '',
        twitterName: '',
        contactEmail: '',
      });
    };

    $scope.deleteTeam = function(team) {
      alerter.confirm('team.edit.deletionConfirm', function(userResponse) {
        if (userResponse == true) {
          storage.deleteTeam(team, function() {
            $scope.cancel();
          }, function(response) {
            if (response.message.indexOf('c17') == 0) {
              alerter.error('', 'team.edit.deletionFailed', {
                container: '#team-edit-error' + team.id,
                duration: 10
              });
            }
          });
        }
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
        alerter.error('', 'team.edit.locationMissing', {
          container: '#team-edit-error' + team.id,
          duration: 10
        });
        return;
      }

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
      if (isEmpty(newAdmin)) {
        return;
      }

      if (!angular.isObject(newAdmin)) {
        return;
      }

      // check if admin is already in the list
      var alreadyAdmin = false;
      angular.forEach($scope.teamToEdit.admins, function(admin) {
        if (admin.id == newAdmin.id) {
          alreadyAdmin = true;
        }
      });

      if (!alreadyAdmin) {
        $scope.teamToEdit.admins.push(newAdmin);
      }

      $scope.newAdmin.obj = null;
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
        });
        $scope.oldPlayerNames = result;
        return result;
      });
    };

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
          if ($scope.locationToEdit) {
          location.id = $scope.locationToEdit.id;
          location.version = $scope.locationToEdit.version;
        } else {
          location.id = 0;
          location.version = 0;
        }
        });
        $scope.oldLocations = locations;
        return locations;
      });
    };

    $scope.addPlayerToRoster = function(newPlayer, roster) {
      if (!angular.isObject(newPlayer)) {
        return;
      }
      if ($scope.editRosterBlock) {
        return;
      }

      $scope.editRosterBlock = true;

      var alreadyInRoster = false;
      angular.forEach(roster.players, function(rosterPlayer) {
        if (newPlayer.dfvNumber == rosterPlayer.player.dfvNumber) {
          alreadyInRoster = true;
        }
      });

      if (alreadyInRoster) {
        $scope.editRosterBlock = false;
        return;
      }

      storage.addPlayerToRoster(newPlayer, roster, function() {
        $scope.newPlayer = {};
        $scope.editRosterBlock = false;
      }, function(errorResponse) {
        if (errorResponse.status = 409) {
          var errorParts = errorResponse.message.split('-');
          switch (errorParts[0]) {
            case 'e101':
              // this player is already part of another roster in this season and division
              alerter.error('', 'team.roster.playerAlreadyInRoster', {
                container: '#add-player-error',
                duration: 10,
                teamName: errorParts[1],
              });
              break;
            case 'e102':
              // this player has the wrong gender
              alerter.error('', 'team.roster.playerWrongGender', {
                container: '#add-player-error',
                duration: 10
              });
              break;
            case 'e103':
              // this player has not the right age for this division
              alerter.error('', 'team.roster.playerWrongAge', {
                container: '#add-player-error',
                duration: 10
              });
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

    $scope.editRosterPlayers = function(roster, team, collapseIndex) {
      roster.x.blocked = false;

      serverApi.getRosterBlockingDate(roster.id, function(blockingDates) {
        roster.x.blockingDates = blockingDates;

        // get the relevant blocking date
        var today = moment();
        var lastBlockingDateBeforeToday = moment('1900-01-01');

        angular.forEach(blockingDates, function(blockingDateString) {
          var blockingDate = moment(blockingDateString.string);
          if (!blockingDate.isAfter(today)) {
            roster.x.blocked = true;

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
      }, 100);
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
            alerter.error('', 'team.roster.playerBlocked', {
              container: '#add-player-error',
              duration: 10
            });
          }
        });
    };

  }
]);
