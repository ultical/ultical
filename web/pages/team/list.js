'use strict';

angular.module('ultical.team', [])

.controller('TeamListCtrl', ['CONFIG', '$scope', '$stateParams', 'storage', '$state', 'headService', 'authorizer', 'actionBar',
  function(CONFIG, $scope, $stateParams, storage, $state, headService, authorizer, actionBar) {


    $scope.bigButtons = CONFIG.general.actionButtonsBig;

    // make sure that we only watch 'own' teams if we are logged in
    if ($stateParams.activeTab == 'own' && !authorizer.loggedIn()) {
      $stateParams.activeTab = 'all';
    }

    // make sure we are directly at the right tab ('own' or 'all')
    $scope.tabs = {
      activeTab: $stateParams.activeTab ? $stateParams.activeTab : 'all'
    };

    headService.setTitle('team.list.' + $stateParams.activeTab, {});

    $scope.teams = [];
    getTeams();

    $scope.$watch('tabs.activeTab', function(newValue, oldValue) {
      if (newValue != oldValue) {
        var newState = 'app.teams' + ($scope.tabs.activeTab == 'all' ? 'List' : 'Own');
        $state.go(newState);
      }
    });

    $scope.teamOrder = 'name';

    // get teams
    function getTeams() {
      if ($scope.tabs.activeTab == 'all') {
        storage.getAllTeamBasics(function(teams) {
          $scope.teams = teams;
        });
      } else {
        storage.getOwnTeamBasics(function(teams) {
          $scope.teams = teams;
        });
      };
    }

    actionBar.addAction({
      group: 'team-new',
      needLogIn: true,
      button: {
        text: 'team.action.create',
        click: function() {
          $scope.createNewTeam();
        }
      },
      separator: true,
    });

    $scope.createNewTeam = function() {
      $state.go('app.teamNew');
    };

  }
]);
