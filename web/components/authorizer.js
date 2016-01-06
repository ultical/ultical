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
	};
}]);
