'use strict';

app.factory('alerter', ['$translate', '$alert', function($translate, $alert) {

	return {
		confirm: function(text, callback) {
			callback(confirm($translate.instant(text)));
		},

		success: function(title, content) {
			$alert({title: $translate.instant(title), content: $translate.instant(content), duration: 10, container: '#pageAlertSpace', placement: 'top', type: 'success', show: true});
		},
	};

}]);