'use strict';

app.factory('authorizer', [function() {

	// init structures
	var activeUser = {};

	return  {
		setUser: function(user) {
			this.activeUser = user;
		},

		getUser: function() {
			return this.activeUser;
		},

		loggedIn: function() {
			return !isEmpty(this.activeUser);
		},

		logOut: function() {
			this.activeUser = {};
		},

		sendConfirmationEmail: function(loginData, callback) {
			callback(true);
		},

		sendDfvOptInEmail: function(loginData, callback) {
			callback(true);
		},

		sendForgotPasswortMail: function(loginData, callback) {
			callback(true);
		},
	};
}]);
