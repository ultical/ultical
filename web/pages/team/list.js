'use strict';

angular.module('ultical.team', [])

.controller('TeamListCtrl', ['$scope', '$stateParams', 'storage', '$state', '$filter',
                              function($scope, $stateParams, storage, $state, $filter) {

	$scope.tabs = { activeTab: 'own' };

	
//	if ($stateParams.teamId == 'new') {
//		$scope.create = true;
//		$scope.teamToEdit = {};
//	} else {
//		$scope.create = false;
//		// get team
//		storage.getTeam($stateParams.teamId, function(team) {
//			$scope.teamToEdit = team;
//		});
//	}


}]);
