'use strict';

angular.module('ultical.user')

.controller('RegistrationCtrl', ['$scope', 'serverApi', 'CONFIG', '$translate', 'alterter',
                                 function($scope, serverApi, CONFIG, $translate, alterter) {

	$scope.minPasswordLength = CONFIG.registration.minPasswordLength;
	$scope.error = {};
	$scope.registrationPending = false;

	$scope.user = {
			dob: {day: '01', month: '01', year: '1990' },
			getDob: function() {
				return this.dob.year + '-' + this.dob.month + '-' + this.dob.day;
			}
	};

	$scope.doRegister = function() {
		if (!$scope.registrationForm.$valid) {
			$scope.error.personals = true;
			return;
		}

		$scope.registrationPending = true;

		if ($scope.user.password != $scope.user.passwordCheck ||
				$scope.user.password.length < CONFIG.general.minPasswordLength) {
			createError('validation_error');
			return;
		}

		var userRequest = {
				email: $scope.user.email,
				password: $scope.user.password,
				firstName: $scope.user.firstname,
				lastName: $scope.user.lastname,
				birthDate: $scope.user.getDob(),
		};

		if ($scope.error.email_not_found) {
			userRequest.dfvEmail = $scope.user.dfvEmail;
		}

		$scope.registrationPending = true;

		serverApi.registerUser(userRequest, function(userResponse) {
			if (userResponse.status != 'SUCCESS') {
				// registration was not successful
				createError(userResponse.status.toLowerCase());
			} else {
				var alertContent = $translate.instant('user.registration.success.confirmationEmail', { email: userRequest.email });
				if (!isEmpty(userResponse.dfvEmail)) {
					alertContent += ' ' + $translate.instant('user.registration.success.dfvEmail', { dfvEmail: userResponse.dfvEmail });
				}
				alerter.success($translate.instant('user.registration.success.title'), alertContent);

				// overwrite plain text passwords
				$scope.user.password = '';
				$scope.user.passwordCheck = '';
				console.log("userResponse", userResponse);
				$scope.$hide();
			}

			$scope.registrationPending = false;
		});
	};

	function createError(errorType) {
		$scope.error[errorType] = true;
		$alert({title: $translate.instant('user.registration.error.title'), content: $translate.instant('user.registration.error.' + errorType), container: '#registrationModalError', placement: 'top', type: 'danger', show: true});
	}

}]);
