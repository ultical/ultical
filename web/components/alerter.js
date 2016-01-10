'use strict';

app.factory('alerter', ['$translate', function($translate) {
	
	return {
		confirm: function(text, callback) {
			callback(confirm($translate.instant(text)));
		},
	};
	
}]);