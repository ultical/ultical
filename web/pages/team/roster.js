'use strict';

angular.module('ultical.team')

.controller('RosterCtrl', ['$scope', 'storage', '$filter', '$rootScope', 'CONFIG', 'alerter', '$translate', 'moment',
  function($scope, storage, $filter, $rootScope, CONFIG, alerter, $translate, moment) {

    $rootScope.saveRoster = function() {
      $scope.rosterCreationPending = true;
      $rootScope.rosterToEditDisabled = true;

      if ($scope.rosterToEdit.context.id == -1 || isEmpty($scope.rosterToEdit.context)) {
        $scope.rosterToEdit.context = null;
      }

      if (CONFIG.general.rosterNeedsContext && $scope.rosterToEdit.context == null) {
        // error - we need a context
        alerter.error('', 'team.roster.rosterNeedsContext', {
          container: '#rosterError',
          duration: 10
        });
      }

      storage.saveRoster($scope.rosterToEdit, $scope.addRosterToThisTeam, function(updatedRoster) {
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
      $scope.rosterNeedsContext = CONFIG.general.rosterNeedsContext;

      if ($scope.rosterToEdit == null) {
        $scope.isEdit = false;
        $scope.rosterToEdit = {
          id: -1,
          divisionAge: 'regular',
          divisionType: 'open',
          season: {},
          nameAddition: '',
          context: {},
        }
      } else {
        $scope.isEdit = true;
        $scope.rosterToEdit = angular.copy($scope.rosterToEdit);
        $scope.rosterToEdit.divisionAge = $scope.rosterToEdit.divisionAge.toLowerCase();
        $scope.rosterToEdit.divisionType = $scope.rosterToEdit.divisionType.toLowerCase();
      }

      // prepare selects for roster creation
      $scope.selects = {
        divisionAges: CONFIG.division.ages,
        divisionTypes: CONFIG.division.types,
        seasons: [],
        context: null,
      };

      storage.getSeasons(function(seasons) {
        $scope.selects.seasons = seasons;
        if (!$scope.isEdit) {
          var thisYear = moment().format('YYYY');
          var thisMonth = parseInt(moment().format('M'));
          var isGymTime = thisMonth < 4 || thisMonth > 9;

          angular.forEach(seasons, function(season) {
            if (season.year == thisYear && ((season.surface == 'GYM') == isGymTime)) {
              $scope.rosterToEdit.season = season;
            }
          });
        }
      });

      storage.getContexts(function(contexts) {
        $scope.selects.contexts = contexts;

        if (!CONFIG.general.rosterNeedsContext) {
          // introduce (and set) empty option
          var noContext = {
            id: -1,
            acronym: 'AAA', // for sorting purposes
            name: $translate.instant('team.roster.noContext')
          };
          $scope.selects.contexts = [noContext].concat($scope.selects.contexts);
        }

        if ($scope.rosterToEdit.context == null) {
          // preset first context
          $scope.rosterToEdit.context = contexts[0];
        } else {
          // preset chosen context
          angular.forEach($scope.selects.contexts, function(selContext) {
            if ($scope.rosterToEdit.context.id == selContext.id) {
              $scope.rosterToEdit.context = selContext;
            }
          });
        }

        if (contexts.length > 0 && !(contexts.length == 1 && CONFIG.general.rosterNeedsContext)) {
          $scope.chooseContexts = true;
        }
      });

    })();

    $scope.getSelectText = function(context) {
      var selText = '';
      if (context.id != -1) {
        selText += context.acronym + ' - ';
      }
      selText += context.name;
      return selText;
    };
  }
]);
