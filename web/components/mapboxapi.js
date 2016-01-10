'use strict';

//mapbox api service
app.factory('mapBox', ['CONFIG', '$http', '$httpParamSerializer',
                       function(CONFIG, $http, $httpParamSerializer) {

	var thisApi = {

			directions: function(type, point1, point2, optionsObject, callback) {

				if (optionsObject === undefined || optionsObject == null)
					optionsObject = {};

				optionsObject.access_token = CONFIG.mapBox.accessToken;
				var optionsString = $httpParamSerializer(optionsObject);

				var coordinates = point1.lon + ',' + point1.lat + ';' + point2.lon + ',' + point2.lat;

				$http({
					method: 'GET',
					url: 'https://api.mapbox.com/v4/directions/mapbox.' + type + '/' + coordinates + '.json?' + optionsString,
				}).then(callback);
			},

			geocoding: function(locationName, optionsObject, callback) {

				if (optionsObject === undefined || optionsObject == null)
					optionsObject = {};

				optionsObject.access_token = CONFIG.mapBox.accessToken;
				var optionsString = $httpParamSerializer(optionsObject);

				return $http.get('https://api.mapbox.com/v4/geocode/mapbox.places/' + locationName + '.json?' + optionsString)
				.then(callback);
			},

	};

	return thisApi;

}]);