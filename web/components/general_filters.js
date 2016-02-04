'use strict';

app.filter('isEmpty', [function() {
	return function(obj) {
		return isEmpty(obj);
	};
}]);

app.filter('notEmpty', [function() {
	return function(obj) {
		return !isEmpty(obj);
	};
}]);

app.filter('url', [function() {
	return function(url) {
		if (isEmpty(url)) {
			return '';
		}
		if (url.indexOf('http') != 0) {
			url = 'http://' + url; 
		}
		return url;
	};
}]);

app.filter('range', function() {
	return function(input, total) {
		var from = 0, to = 0;

		if (angular.isObject(total)) {
			from = parseInt(total.from);
			to = parseInt(total.to) + 5;
		} else {
			to = parseInt(total);
		}

		for (var i = from; i < to; i++) {
			input.push(i);
		}

		return input;
	};
});

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

app.filter('urlEncode', function($window) {
	  return $window.encodeURIComponent;
	});