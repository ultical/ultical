'use strict';

app.factory('serverApi', ['CONFIG', '$http', function(CONFIG, $http) {

	function get(resource, successCallback, errorCallback) {
		if (undefined === errorCallback) {
			errorCallback = function() {};
		}
		$http.get(CONFIG.api.hostname + '/' + resource)
		.then(successCallback, errorCallback);
	}

	return {
		getEvent: function(eventId, callback) {
			this.get('event/' + eventId, callback);
		},
		
		getEvents: function(callback) {
			this.get('events', callback);
		},
	};

}]);