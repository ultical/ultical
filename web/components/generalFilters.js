'use strict';

app.filter('capitalize', function() {
  return function(input, all) {
    var reg = (all) ? /([^\W_]+[^\s-]*) */g : /([^\W_]+[^\s-]*)/;
    return (!!input) ? input.replace(reg, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();}) : '';
  }
});

app.filter('numberFixedLen', function () {
	return function (n, len) {
		var num = parseInt(n, 10);
		len = parseInt(len, 10);
		if (isNaN(num) || isNaN(len)) {
			return n;
		}
		num = ''+num;
		while (num.length < len) {
			num = '0'+num;
		}
		return num;
	};
});

app.filter('customDate', ['$filter', '$translate', function($filter, $translate) {
	return function(input, type) {
		if (isEmpty(input)) {
			return null;
		}

		if (angular.isObject(input)) {
			input = input.string;
		}

		switch(type) {
		case 'datetime_medium':
			return $filter('date')(input, $translate.instant('general.dateTimeFormat'));
			break;
		case 'date_medium':
			return $filter('date')(input, $translate.instant('general.dateFormat'));
			break;
		case 'date_internal':
			return $filter('date')(input, 'yyyy-MM-dd');
			break;
		}  

		return input;
	};
}]);

app.filter('currencySymbol', [function() {
	return function(currencyString) {
		if (isEmpty(currencyString)) {
			return '';
		}

		var output = '';

		switch(currencyString) {
		case 'EUR':
			output = '€';
			break;
		default:
			output = '$';
		}

		return output;
	};
}]);