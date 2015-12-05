'use strict';

angular.module('ultical.user', [])

.controller('RegistrationCtrl', ['$scope', 'serverApi', 'CONFIG',
                                 function($scope, serverApi, CONFIG) {

	$scope.minPasswordLength = CONFIG.general.minPasswordLength;
	$scope.formError = false;
	
	$scope.user = {
			dob: {day: '01', month: '01', year: '1990' },
			getDob: function() {
				return this.dob.year + '-' + this.dob.month + '-' + this.dob.day;
			}
	};

	$scope.doRegister = function() {
		if (!$scope.registrationForm.$valid) {
			$scope.formError = true;
			return;
		}
		
		if ($scope.user.password != $scope.user.passwordCheck ||
				$scope.user.password.length < CONFIG.general.minPasswordLength) {
			$scope.formError = true;
			return;
		}

		var userRequest = {
				email: $scope.user.email,
				password: $scope.user.password,
				firstName: $scope.user.firstname,
				lastName: $scope.user.lastname,
				birthDate: $scope.user.getDob(),
		};

		console.log("user request", userRequest);
		serverApi.registerUser(userRequest, function(userResponse) {
			console.log("user response", userResponse);

			if (userResponse.error) {
				// registration was not successful
				if (userResponse.error == 'notfound') {
					// user not in dfv db
				}
			}
		});
	};

}]);
