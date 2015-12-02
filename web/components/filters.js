'use strict';

app.filter('eventname', ['$translate', 'matchdaynameFilter', function ($translate, matchdaynameFilter) {
	return function (event) {
		if (isEmpty(event)) {
			return '';
		}

		var eventName;

		if (isEmptyString(event.tournamentEdition.alternativeName)) {
			// use tournament format name
			eventName = event.tournamentEdition.tournamentFormat.name;
		} else {
			// this tournament edition uses a different name than the tournament format
			eventName = event.tournamentEdition.alternativeName;
		}

		// this is a multi-matchday-tournament
		if (event.matchdayNumber != -1) {
			var matchday = '';
			eventName += ' ' + event.matchdayNumber + '. ';

			eventName += matchdaynameFilter(event);

		}

		return eventName;
	};
}]);

app.filter('matchdayname', ['$translate', function ($translate) {
	return function (event) {
		if (isEmpty(event)) {
			return '';
		}

		var matchdayName = '';

		if (!isEmptyString(event.tournamentEdition.alternativeMatchdayName)) {
			// this league uses a different name to mark each matchday
			matchdayName += event.tournamentEdition.alternativeMatchdayName;
		} else {
			// this league uses the default matchday translation
			matchdayName += $translate.instant('event.matchday');
		}

		return matchdayName;
	};
}]);

app.filter('season', ['$translate', function($translate) {
	return function(season) {
		if (isEmpty(season)) {
			return '';
		}

		var seasonString = '';
		if (season.surface == 'GYM') {
			seasonString += $translate.instant('season.indoor');
		} else {
			seasonString += $translate.instant('season.outdoor');
		}
		seasonString += ' ' + season.year;

		if (season.plusOneYear) {
			var yearString = ''+(season.year + 1);
			seasonString += '/' + yearString.substring(2,4);
		}

		return seasonString;
	}
}]);

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