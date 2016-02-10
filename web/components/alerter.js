// ultical Copyright (C) 2016 ultical developers
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published
// the Free Software Foundation; either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful
// but WITHOUT ANY WARRANTY; without even the implied warranty
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public
// along with this program; if not, write to the Free Software Foundation,
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
// 

'use strict';

app.factory('alerter', ['$translate', '$alert', function($translate, $alert) {

	return {
		confirm: function(text, callback) {
			callback(confirm($translate.instant(text)));
		},

		success: function(title, content, options) {
			doBootstrapAlert(title, content, 'success', options);
		},

		error: function(title, content, options) {
			doBootstrapAlert(title, content, 'danger', options);
		},
	};

	function doBootstrapAlert(title, content, type, options) {
		if (undefined === options || options == null) {
			options = {};
		}
		if (!('container' in options)) {
			options.container = '#pageAlertSpace';
		}
		if (!('duration' in options)) {
			options.duration = false;
		}
		options.title = $translate.instant(title);
		options.content = $translate.instant(content);
		options.placement = 'top';
		options.show = true;
		options.type = type;

		$alert(options);
	}

}]);