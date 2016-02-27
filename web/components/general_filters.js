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

app.filter('orderLocaleBy',function() {
    return function (items, property) {
        items.sort(function (a, b) {
            return a[property].localeCompare(b[property]);
        });
        return items;
      };
});

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

app.filter('decimal', ['$translate', function($translate) {
	return function(amount, numDecimals) {
		if (isEmpty(amount)) {
			return'';
		}
		var parts = (''+amount).split('.');
		if (parts.length == 1) {
			parts[1] = '0';
		}
		while (parts[1].length < numDecimals) {
			parts[1] += '0';
		}
		if (parts[1].length > numDecimals) {
			parts[1] = parts[1].substring(0, numDecimals);
		}
		return parts[0] + $translate.instant('general.decimalSeparator') + parts[1];
	}
}]);

app.filter('money', ['$translate', 'decimalFilter', function($translate, decimalFilter) {
	return function(currencyCode, amount) {
		if (isEmpty(amount)) {
			return '';
		}
		return $translate.instant('general.currencyFormat', {amount: decimalFilter(amount, 2), currencySymbol: $translate.instant('currencySymbol.' + currencyCode) });
	};
}]);

app.filter('urlEncode', function($window) {
  return $window.encodeURIComponent;
});
