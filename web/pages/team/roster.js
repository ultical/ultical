'use strict';

angular.module('ultical.team')

.controller('RosterCtrl', ['$scope', 'storage', '$filter', '$rootScope', 'CONFIG', 'alerter',
  function($scope, storage, $filter, $rootScope, CONFIG, alerter) {

    $scope.addContext = function(chosenContext) {
      if (isEmpty(chosenContext)) {
        chosenContext = $scope.chosenContext;
      }
      if (isEmpty(chosenContext)) {
        return;
      }
      var alreadyIn = false;
      angular.forEach($scope.rosterToEdit.contexts, function(context) {
        if (context.id == chosenContext.id) {
          alreadyIn = true;
        }
      });

      if (!alreadyIn) {
        $scope.rosterToEdit.contexts.push(chosenContext);
      }

      $scope.chosenContext = {};
    };

    $scope.removeContext = function(context) {
      $scope.rosterToEdit.contexts = $filter('removeByProperty')($scope.rosterToEdit.contexts, 'id', context.id);
    }

    $rootScope.saveRoster = function() {
      $scope.rosterCreationPending = true;
      $rootScope.rosterToEditDisabled = true;
      var isUpdate = $scope.rosterToEdit.id != -1;
      storage.saveRoster($scope.rosterToEdit, $scope.addRosterToThisTeam, function(updatedRoster) {
        if (isUpdate) {
          var indexToReplace = -1;
          angular.forEach($scope.addRosterToThisTeam.rosters, function(roster, idx) {
            if (roster.id == updatedRoster.id) {
              indexToReplace = idx;
            }
          });
          if (indexToReplace >= 0) {
            $scope.addRosterToThisTeam.rosters.splice(indexToReplace, 1);
            $scope.addRosterToThisTeam.rosters.push($scope.rosterToEdit);
          }
        } else {
          $scope.addRosterToThisTeam.rosters.push(updatedRoster);
          updatedRoster.players = [];
        }

        if ($scope.modal) {
          $scope.$hide();
        }

      }, function(errorResponse) {
        if (errorResponse.status = 409) {
          // a roster with the same season, division and identifier already exists for this team
          alerter.error('', 'team.roster.rosterDuplicated', {
            container: '#rosterError',
            duration: 10
          });
          $rootScope.rosterToEditDisabled = false;
          $scope.rosterCreationPending = false;
        }
      });
    };

    $scope.deleteRoster = function(roster) {
      alerter.confirm('team.roster.confirmDelete', function(userResponse) {
        if (userResponse == true) {
          storage.deleteRoster(roster, $scope.addRosterToThisTeam, function() {
              $scope.$hide()
            },
            function(errorResponse) {
              if (errorResponse.status = 403) {
                // this roster was active during an official tournament - cannot be deleted
                alerter.error('', 'team.roster.rosterBlocked', {
                  container: '#rosterError',
                  duration: 10,
                });
              }
            });
        }
      });
    };

    // init
    (function() {
      $scope.rosterCreationPending = false;
      $rootScope.rosterToEditDisabled = false;
      if ($scope.rosterToEdit == null) {
        $scope.rosterToEdit = {
          id: -1,
          divisionAge: 'regular',
          divisionType: 'open',
          season: {},
          nameAddition: '',
          contexts: [],
        }
      } else {
        $scope.rosterToEdit = angular.copy($scope.rosterToEdit);
        $scope.rosterToEdit.divisionAge = $scope.rosterToEdit.divisionAge.toLowerCase();
        $scope.rosterToEdit.divisionType = $scope.rosterToEdit.divisionType.toLowerCase();
      }

      // prepare selects for roster creation
      $scope.selects = {
        divisionAges: CONFIG.division.ages,
        divisionTypes: CONFIG.division.types,
        seasons: [],
        contexts: [],
      };

      storage.getSeasons(function(seasons) {
        $scope.selects.seasons = seasons;
        angular.forEach(seasons, function(season) {
          if (season.year == '2016') {
            $scope.rosterToEdit.season = season;
          }
        });
      });

      storage.getContexts(function(contexts) {
        $scope.selects.contexts = contexts;
        angular.forEach(contexts, function(context) {
          if (context.acronym == 'DFV') {
            if ($scope.rosterToEdit.id == -1) {
              $scope.addContext(context);
            }
          }
        });
      });

      $scope.getSelectContexts = function() {
        console.log("what we got", $scope.selects.contexts, $scope.rosterToEdit.contexts);
        if ($scope.rosterToEdit == null) {
          return [];
        }
        return $filter('arrayDiff')($scope.selects.contexts, $scope.rosterToEdit.contexts, 'id');
      }

    })();
  }
]);
