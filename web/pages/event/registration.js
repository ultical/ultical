'use strict';

angular.module('ultical.events')

.controller('EventRegistrationCtrl', ['$scope', 'storage', '$filter', '$rootScope', 'alerter',
  function($scope, storage, $filter, $rootScope, alerter) {

    var ownTeams = [];
    var ownTeamsByDivision = [];

    $scope.teamRegistrationPending = false;
    $rootScope.teamRegistrationDisabled = false;

    $scope.register = {};
    $scope.register.division = $scope.divisionsToShow[0];

    $scope.eventName = $filter('editionname')($scope.edition);

    $scope.changeDivision = function() {
      ownTeamsByDivision = angular.copy(ownTeams);

      // add sub-teams
      angular.forEach(ownTeams, function(team) {
        angular.forEach(team.rosters, function(roster) {

          if (roster.season.id == $scope.edition.season.id && roster.divisionAge == $scope.register.division.divisionAge && roster.divisionType == $scope.register.division.divisionType) {
            // check for context equality (or null)
            if ((isEmpty(roster.context) && isEmpty($scope.edition.context)) || (!isEmpty(roster.context) && !isEmpty($scope.edition.context) && roster.context.id == $scope.edition.context.id)) {
              // only add subteams (the main teams have already be added above)
              if (!isEmpty(roster.nameAddition)) {
                ownTeamsByDivision.push({
                  team: team,
                  roster: roster,
                  name: team.name + ' ' + roster.nameAddition
                });
              }
            }
          }
        });
      });

      // sort
      ownTeamsByDivision = $filter('orderLocaleBy')(ownTeamsByDivision, 'name');

      // preset team
      if (ownTeamsByDivision.length > 0) {
        $scope.chosenTeam = ownTeamsByDivision[0];
      }
    };

    $scope.getOwnTeams = function() {
      return ownTeamsByDivision;
    };

    storage.getOwnTeams(function(teams) {
      ownTeams = teams;
      $scope.changeDivision();
    });

    $scope.newRosterCreation = false;
    $scope.startNewRosterCreation = function() {
      $scope.newRosterCreation = true;
    };
    $scope.cancelNewRosterCreation = function() {
      $scope.newRosterCreation = false;
      $scope.newNameAddition = '';
    };

    $scope.getTranslateParams = function(team) {
      return { teamName: $scope.getTeam(team) ? $scope.getTeam(team).name : ''};
    }

    $scope.getTeam = function(team) {
      if (isEmpty(team)) {
        return null;
      }
      if ('id' in team) {
        return team;
      } else {
        return team.team;
      }
    };

    $scope.newNameAddition = {text: ''};

    $rootScope.doTeamRegister = function() {
      $scope.teamRegistrationPending = true;
      $rootScope.teamRegistrationDisabled = true;

      var existingRosterForRegistration = null;

      if ($scope.newRosterCreation) {
        // a new roster ought to be created
        if (isEmpty($scope.newNameAddition.text)) {
          // new name addition request but not put into place - error
          alerter.error('', 'event.register.nameAdditionEmpty', {
            container: '#event-registration-error',
            duration: 10,
          });
          $scope.teamRegistrationPending = false;
          $rootScope.teamRegistrationDisabled = false;
          return;
        }
      } else {
        // check if the chosen team already has a roster
        if ('id' in $scope.chosenTeam) {
          // this is a base-team - let's check if a roster is present
          angular.forEach($scope.chosenTeam.rosters, function(roster) {
            if (roster.season.id == $scope.edition.season.id && roster.divisionAge == $scope.register.division.divisionAge && roster.divisionType == $scope.register.division.divisionType) {
              // check for context equality (or null)
              if ((isEmpty(roster.context) && isEmpty($scope.edition.context)) || (!isEmpty(roster.context) && !isEmpty($scope.edition.context) && roster.context.id == $scope.edition.context.id)) {
                // only look for rosters for the 'base' team
                if (isEmpty(roster.nameAddition)) {
                  existingRosterForRegistration = roster;
                }
              }
            }
          });
        } else {
          existingRosterForRegistration = $scope.chosenTeam.roster;
        }
      }

      if (existingRosterForRegistration == null) {

        // save roster
        var rosterToCreate = {
          id: -1,
          divisionType: $scope.register.division.divisionType,
          divisionAge: $scope.register.division.divisionAge,
          season: $scope.edition.season,
          context: $scope.edition.context,
          nameAddition: $scope.newNameAddition.text,
          team: $scope.getTeam($scope.chosenTeam),
        }

        storage.saveRoster(rosterToCreate, rosterToCreate.team, function(newRoster) {
          doRegister(newRoster, $scope.getTeam($scope.chosenTeam));
        }, function(errorResponse) {
          if (errorResponse.status = 409) {
            alerter.error('', 'event.register.rosterDuplicated', {
              container: '#event-registration-error',
              duration: 10,
            });
          }
          $scope.teamRegistrationPending = false;
          $rootScope.teamRegistrationDisabled = false;
        });
      } else {
        // choose roster to register
        doRegister(existingRosterForRegistration, $scope.getTeam($scope.chosenTeam));
      }
    };

    function doRegister(roster, team) {
      var registration = {};
      registration.comment = $scope.register.comment ? $scope.register.comment : null;
      registration.roster = { id: roster.id};
      registration.teamName = team.name + (!isEmpty(roster.nameAddition) ? ' ' + roster.nameAddition : '');

      var division = {};
      angular.forEach($scope.divisionsToShow, function(div) {
        if (div.id == $scope.register.division.id) {
          division = div;
        }
      });

      storage.registerTeamForEdition(registration, division, function(newTeamReg) {
        newTeamReg.roster = roster;
        var teamCopy = angular.copy($scope.getTeam($scope.chosenTeam));
        teamCopy.rosters  = [];
        newTeamReg.roster.team = teamCopy;
        $scope.changeDivision();
        $scope.$hide();
        $scope.teamRegistrationPending = false;
        $rootScope.teamRegistrationDisabled = false;
        alerter.success('event.register.teamSuccessfullyRegisteredTitle', 'event.register.teamSuccessfullyRegistered', {
          duration: 10,
        });
      }, function(errorResponse) {
        alerter.error('', 'event.register.teamAlreadyInDivision', {
          container: '#event-registration-error',
          duration: 10,
        });

        $scope.teamRegistrationPending = false;
        $rootScope.teamRegistrationDisabled = false;
      });
    };

  }
]);
