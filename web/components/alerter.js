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
		options.title = $translate.instant(title, options);
		options.content = $translate.instant(content, options);
		options.placement = 'top';
		options.show = true;
		options.type = type;

		$alert(options);
	}

}]);
