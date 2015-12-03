'use strict';

angular.module('ultical.user', [])

.controller('RegistrationCtrl', ['$scope', 'serverApi',
                                 function($scope, serverApi) {

	$scope.user = {
			dob: {day: '01', month: '01', year: '1990' },
			getDob: function() {
				return this.dob.year + '-' + this.dob.month + '-' + this.dob.day;
			}
	};

	$scope.doRegister = function() {
		if (!$scope.registrationForm.$valid) {
			console.log("not valid");
			return;
		}
		
		var userRequest = {
				'firstname': $scope.user.firstname,
				'lastname': $scope.user.lastname,
				'dob': $scope.user.getDob(),
				'email': $scope.user.email,
		};
		
		console.log("user request", $scope.user);
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
