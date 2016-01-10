'use strict';

//map api service
app.factory('mapService', ['CONFIG', 'mapBox',
                           function(CONFIG, mapBox) {

	var thisApi = {
			getDistance: function(point1, point2, callback) {
				var type = 'driving'; // walking, cycling, driving

				var options = {
						alternatives: false,
						geometry: false,
						steps: false,
				};

				mapBox.directions(type, point1, point2, options, function(response) {
					callback(response.data.routes[0].distance);
				});
			},

			getLocations: function(locationName, type, callback) {
				
				var options = {};

				// filter types
				if (type == 'city') {
				options.types = 'place,country';
				} else {
					options.types = 'address,place,country';
				}
				
				// e.g.: center of germany 51.163375, 10.447683
				options.proximity = CONFIG.mapBox.proximity;

				return mapBox.geocoding(locationName, options, function(response) {
					return callback(response.data.features);
				});
			},

			getCoordinates: function(locationObject) {
				return {lon: locationObject.center[0], lat: locationObject.center[1]};
			},

			// returns the linear distance between two coordinates (lat,lon) 'as the crow flies'
			getLinearDistance: function(point1, point2) {
				return distance(point1.lat, point1.lon, point2.lat, point2.lon, 'K');
			},
	};

	function distance(lat1, lon1, lat2, lon2, unit) {
		var radlat1 = Math.PI * lat1/180
		var radlat2 = Math.PI * lat2/180
		var radlon1 = Math.PI * lon1/180
		var radlon2 = Math.PI * lon2/180
		var theta = lon1-lon2
		var radtheta = Math.PI * theta/180
		var dist = Math.sin(radlat1) * Math.sin(radlat2) + Math.cos(radlat1) * Math.cos(radlat2) * Math.cos(radtheta);
		dist = Math.acos(dist)
		dist = dist * 180/Math.PI
		dist = dist * 60 * 1.1515
		if (unit=="K") { dist = dist * 1.609344 }
		if (unit=="N") { dist = dist * 0.8684 }
		return dist
	}

	return thisApi;

}]);