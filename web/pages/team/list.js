'use strict';

angular.module('ultical.team', [])

.controller('TeamListCtrl', ['$scope', '$stateParams', 'storage', '$state', '$filter', 'authorizer',
                             function($scope, $stateParams, storage, $state, $filter, authorizer) {

	$scope.loggedIn = function() {
		return authorizer.loggedIn();
	}

	// make sure we are directly at the right tab ('own' or 'all')
	$scope.tabs = { activeTab: $stateParams.activeTab ? $stateParams.activeTab: 'all' };

	$scope.teams = [];

	$scope.$watch('tabs.activeTab', function() {
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


}]);
