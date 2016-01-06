'use strict';

app.factory('serverApi', ['CONFIG', '$http', 'Base64', 'Authorizer', 
                          function(CONFIG, $http, Base64, Authorizer) {

	function get(resource, successCallback, errorCallback, includeHeader) {
		doHttp(resource, 'GET', null, successCallback, errorCallback, includeHeader);
	}

	function post(resource, data, successCallback, errorCallback, includeHeader) {
		doHttp(resource, 'POST', data, successCallback, errorCallback, includeHeader);
	}

	function doHttp(resource, method, data, successCallback, errorCallback, includeHeader) {
		var config = {
				method: method,
				url: CONFIG.api.hostname + '/' + resource,
		};

		if (authorizer.loggedIn()) {
			config.headers = { Authorization: 'Basic ' + Base64.encode(authorizer.getUser().email + ':' + authorizer.getUser().password)};
		}

		if (method != 'GET') {
			config.data = data;
		}

		$http(config).then(
				function (response) {
					// success callback
					callCallback(successCallback, response, includeHeader);
				}, function (response) {
					// error callback
					callCallback(errorCallback, response, includeHeader);
				}
		);
	}

	function callCallback(callback, response, includeHeader) {
		if (undefined === callback) {
			callback = function() {};
		}
		if (undefined === includeHeader) {
			includeHeader = false;
		}

		if (includeHeader) {
			// send complete response
			callback(response);
		} else {
			// only send payload data
			callback(response.data);
		}
	}

	return {
		getEvent: function(eventId, callback) {
			get('event/' + eventId, callback);
		},

		getEvents: function(callback) {
			get('events', callback);
		},

		getTeam: function(teamId, callback) {
			get('teams/' + teamId, callback);
		},

		getAllTeams: function(callback) {
			get('teams', callback);
		},

		getOwnTeams: function(callback) {
			get('teams/own', callback);
		},

		registerUser: function(user, callback) {
			post('register', user, callback);
		},

		login: function(user, callback) {
			post('auth', user, callback);
		}
	};

}]);