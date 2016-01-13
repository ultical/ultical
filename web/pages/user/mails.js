'use strict';

angular.module('ultical.user')
.controller('MailsCtrl', ['$scope', 'serverApi', 'authorizer', '$state', 'alerter', '$stateParams',
                            function($scope, serverApi, authorizer, $state, alerter, $stateParams) {

	$scope.promptFor = '';
	$scope.validationError = false;
	
	$scope.doPasswordChange = function(passwords) {
		if (passwords.one != passwords.two) {
			$scope.validationError = true;
		}
		
		$scope.mailCode.user.password = passwords.one;
		
		serverApi.changePasswordWithMailCode($scope.mailCode.code, $scope.mailCode.user, function(user) {
			alerter.success('emailCode.successPasswordChangedTitle', 'emailCode.successPasswordChangedText');
			$state.go('start');
		});
	};
	
	serverApi.redeemMailCode($stateParams.code, function(mailCode) {
		// success callback
		if ($stateParams.emailCodeType == 'password' && mailCode.type == 'FORGOT_PASSWORD') {
			// change password
			$scope.promptFor = 'password';
			$scope.mailCode = mailCode;
		}
		if ($stateParams.emailCodeType == 'confirm') {
			var successText = '';
			if (mailCode.user.emailConfirmed == false) {
				successText = 'emailCode.missingEmailConfirm';
			} else if (mailCode.user.dfvEmailOptIn == false) {
				successText = 'emailCode.missingDfvOptIn';
			} else {
				successText = 'emailCode.successText';
			}
			alerter.success('emailCode.successTitle', successText);
			$state.go('start');
		}
	}, 
	function() {
		// error callback
		if ($stateParams.emailCodeType == 'password') {
			alerter.error('emailCode.noCodeTitle', 'emailCode.noCodeForgotPw');
		} else {
			alerter.error('emailCode.noCodeTitle', 'emailCode.noCodeConfirm');
		}
		$state.go('start');
	});

	
	
}]);
