'use strict';

angular.module('ultical.team')

.controller('TeamShowCtrl', ['CONFIG', '$scope', '$stateParams', 'storage', 'headService', 'actionBar', '$filter', '$state', 'serverApi', 'alerter', '$timeout', '$modal', 'authorizer', 'mapService',
function(CONFIG, $scope, $stateParams, storage, headService, actionBar, $filter, $state, serverApi, alerter, $timeout, $modal, authorizer, mapService) {

  $scope.team = {x:{}};

  $scope.bigButtons = CONFIG.general.actionButtonsBig;

  $scope.editRosterBlock = false;

  $scope.oldPlayerNames = [];
  $scope.oldLocations = [];

  $scope.newEmail = {
    text: ''
  };
  $scope.newAdmin = {obj:null};

  $scope.activeUserId = authorizer.getUser() != null ? authorizer.getUser().id : -1;

  $scope.currentLocale = $stateParams.locale;

  function doRun() {
    if ($stateParams.createNew) {
      if (!authorizer.loggedIn()) {
        $state.go('app.teamsList');
      }

      $scope.team = {
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
        x: {},
      };

      headService.setTitle('team.action.create');
      $scope.editTeam();
    } else {
      storage.getTeam($stateParams.teamId, init);
    }
  }

  function init(teamData) {
    $scope.team = teamData;

    // check if url slug is correcty - else correct it
    var shouldBeTeamSlug = $filter('slugify')($scope.team.name);
    if ($stateParams.teamSlug != shouldBeTeamSlug) {
      $state.go('app.teamShow', {teamId: $scope.team.id, teamSlug: shouldBeTeamSlug}, {notify: false});
    }

    headService.setTitle($scope.team.name);
    updateActions();
  }

  function updateActions() {
    actionBar.clearActions();

    if (!$scope.editing) {
      actionBar.addAction({
        group: 'team-new',
        needLogIn: true,
        button: {
          text: 'team.action.create',
          click: function() {
            $state.go('app.teamNew');
          }
        },
        separator: true,
      });
    }

    actionBar.addAction({
      group: 'team-contact',
      needLogIn: null,
      button: {
          text: 'team.contactButton',
          click: function() {
            openEmailToTeamModal();
          },
      },
      separator: true,
    });

    if ($scope.team.x.own) {
      actionBar.addAction({
        group: 'team-admin',
        text: 'team.action.youAreTeamAdmin',
        separator: true,
      });
      if (!$scope.editing) {
        actionBar.addAction({
          group: 'team-admin',
          button: {
            text: 'team.roster.newRoster',
            click: function() {
              $scope.createNewRoster();
            }
          },
        });
        actionBar.addAction({
          group: 'team-admin',
          button: {
            text: 'team.action.edit',
            click: function() {
              $scope.editTeam();
            }
          },
        });
      }
    }
  }

  function openEmailToTeamModal() {
    var newScope = $scope.$new();
    newScope.mailToTeam = true;
    newScope.team = $scope.team;

    var modal = $modal({
      animation: 'am-fade-and-slide-top',
      templateUrl: 'components/email_service/email_modal.html?v=4',
      show: true,
      scope: newScope,
    });
  };

  $scope.$watch('editing', updateActions);

  // TEAM MANIPULATION
  $scope.editTeam = function() {

    if ($scope.team.foundingDate == 0) {
      $scope.team.foundingDate = '';
    }

    $scope.editing = true;
    $scope.teamToEdit = angular.copy($scope.team);
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

    storage.saveTeam(team, function(savedTeam) {
        $scope.team = savedTeam;
        $scope.editing = false;
        $scope.locationIsMissing = false;
        if ($stateParams.createNew) {
          $state.go('app.teamShow', {teamId: savedTeam.id, teamSlug: ""})
        }
      }, function(errorResponse) {
        // probably a validation error
        if (errorResponse.status == 417) {
          $scope.locationIsMissing = true;
        }
      });
  };

  $scope.cancel = function() {
    $scope.locationIsMissing = false;
    $scope.teamToEdit = {};
    $scope.editing = false;
    if ($stateParams.createNew) {
      $state.go('app.teamsList');
    }
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


  // ROSTER MANIPULATION
  $scope.createNewRoster = function() {
    $scope.addRosterToThisTeam = $scope.team;
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

  $scope.rosterEditEnd = function() {
    $scope.editingRosterPlayers = -1;
    $scope.actualRosterEditedPanelIdx = -1
  };

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

  $scope.editRosterPlayers = function(roster, team, collapseIndex, $event) {
    $event.stopPropagation();
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

    // adjust collapseIndex (needed for newly create rosters)
    angular.forEach(team.rosters, function(rr, idx) {
      if (roster.id == rr.id) {
        // collapseIndex = idx;
      }
    });

    $scope.actualRosterEditedPanelIdx = collapseIndex;
    $scope.editRosterBlock = false;
    $scope.newPlayer = {};
    $scope.editingRosterPlayers = roster.id;

    $timeout(function() {
      $scope.teamPanels.activePanel = collapseIndex;
    }, 0);
  };

  $scope.teamPanels = {};

  // we use this value to let the input fields disappear, when a different roster is un-collapsed
  $scope.actualRosterEditedPanelIdx = -1;

  $scope.$watch('teamPanels.activePanel', function() {
    $timeout(function() {
      if ($scope.actualRosterEditedPanelIdx != $scope.teamPanels.activePanel) {
        $scope.rosterEditEnd();
      }
    }, 100);
  });
  
  $scope.stringToDate = function(date) {
	  return moment(date).toDate();
  }

  doRun();


}
]);
