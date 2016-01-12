'use strict';

angular.module('ultical.user', [])
.controller('LoginCtrl', ['$scope', 'serverApi', 'authorizer', '$state', 'alerter',
                          function($scope, serverApi, authorizer, $state, alerter) {

	$scope.loginFail = false;

	$scope.doLogin = function(loginData) {

		// TODO: DEBUG
		loginData = {};
		loginData.email = "johanna@knallbude.de";
		loginData.password = "asdasdasda";
		// END DEBUG

		serverApi.login(loginData, function(authResponse) {
			if (authResponse.status == 'SUCCESS') {
				$scope.loginFail = false;
				$scope.password = '';
				authorizer.setUser(authResponse.user);
				$scope.$hide();
				$state.reload();
			} else {
				$scope.loginFail = {};
				switch (authResponse.status) {
				case 'WRONG_CREDENTIALS':
					$scope.loginFail.text = 'wrongCredentials';
					$scope.loginFail.actionText = 'wrongCredentialsAction';
					$scope.loginFail.action = function(loginData) {
						authorizer.sendForgotPasswortMail(loginData, function() {
							alerter.success('nav.loginEmailActions.successTitle', 'nav.loginEmailActions.passwordReset');
							$scope.$hide();
						});
					};
					break;
				case 'EMAIL_NOT_CONFIRMED':
					$scope.loginFail.text = 'emailNotConfirmed';
					$scope.loginFail.actionText = 'emailNotConfirmedAction';
					$scope.loginFail.action = function(loginData) {
						authorizer.sendConfirmationEmail(loginData, function() {
							console.log("success");
							alerter.success('nav.loginEmailActions.successTitle', 'nav.loginEmailActions.confirmationEmailSendContent');
							$scope.$hide();
						});
					};
					break;
				case 'DFV_EMAIL_NOT_OPT_IN':
					$scope.loginFail.text = 'dfvEmailNotOptIn';
					$scope.loginFail.actionText = 'dfvEmailNotOptInAction';
					$scope.loginFail.action = function(loginData) {
						authorizer.sendDfvOptInEmail(loginData, function() {
							alerter.success('nav.loginEmailActions.successTitle', 'nav.loginEmailActions.confirmationEmailSendContent');
							$scope.$hide();
						});
					};
					break;
				default:
					$scope.loginFail.text = 'loginFail';
					$scope.loginFail.actionText = null;
				}
			}
		});
	}

}]);
