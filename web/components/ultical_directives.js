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

app.directive('basObjectTypeahead', ['$filter', function($filter) {

	function link(scope, element, attrs) {

		var whatToWatch = 'teamToEdit.' + attrs.basObjectTypeahead;
		if (attrs.basObjectTypeahead == 'admins') {
			whatToWatch = 'newAdmin.obj';
		}

		scope.$watch(whatToWatch, function(newValue, oldValue) {

			// check if new value is no object anymore, but the last one was
			// this happens in some browsers after blurring the typeahead
			if (!angular.isObject(newValue) && angular.isObject(oldValue)) {

				switch (attrs.basObjectTypeahead) {
				case 'location':
					if ($filter('location')(oldValue, true) == newValue) {
						scope.teamToEdit.location = oldValue;
					}
					break;
				case 'club':
					if (oldValue.name == newValue) {
						scope.teamToEdit.club = oldValue;
					}
					break;
				case 'admins':
					if (oldValue.fullName == newValue) {
						scope.newAdmin.obj = oldValue;
					}
					break;
				}
			}
		});
	}

	return {
		link: link,
	};
}]);
