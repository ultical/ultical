/*
 * ultical Copyright (C) 2016 ultical developers
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

'use strict';

var app = angular.module('ultical');

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
