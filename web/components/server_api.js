'use strict';

app.factory('serverApi', ['CONFIG', '$http', function(CONFIG, $http) {

	function get(resource, successCallback, errorCallback) {
		if (undefined === errorCallback) {
			errorCallback = function() {};
		}
		$http.get(CONFIG.api.hostname + '/' + resource)
		.then(successCallback, errorCallback);
	}
	
	function post(recourse, data, successCallback, errorCallback) {
		if (undefined === errorCallback) {
			errorCallback = function() {};
		}
		$http.post(CONFIG.api.hostname + '/' + resource)
		.then(successCallback, errorCallback);
	}

	return {
		getEvent: function(eventId, callback) {
			get('event/' + eventId, callback);
		},
		
		getEvents: function(callback) {
			get('events', callback);
		},
		
		registerUser: function(user) {
			post('user', user, callback);
		},
	};

}]);