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
