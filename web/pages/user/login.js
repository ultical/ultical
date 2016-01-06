'use strict';

angular.module('ultical.user', [])
.controller('LoginCtrl', ['$scope', 'serverApi', 'authorizer', '$state',
                          function($scope, serverApi, authorizer, $state) {

	$scope.loginFail = false;

	$scope.doLogin = function(loginData) {
		serverApi.login(loginData, function(loginResponse) {
			if (isEmpty(loginResponse)) {
				$scope.loginFail = true;
			} else {
				$scope.password = '';
				authorizer.setUser(loginResponse);
				$scope.$hide();
				$state.reload();
			}
		});
	}

}]);
