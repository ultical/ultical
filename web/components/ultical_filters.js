'use strict';


app.filter('location', [function () {
	return function (location) {
		if (isEmpty(location)) {
			return '';
		}

		var locationString = '';

		if (!isEmpty(location.city)) {
			locationString += location.city;
		}

		if (!isEmpty(location.country)) {
			locationString += ', ' + location.country;
		}

		return locationString
	};
}]);

app.filter('username', [function () {
	return function (user, fullName) {
		if (isEmpty(user)) {
			return '';
		}

		if (undefined === fullName) {
			fullName = false;
		}

		if (user.dfvPlayer == null) {
			return user.email;
		}

		var username = user.dfvPlayer.firstName;
		if (fullName) {
			username += ' ' + user.dfvPlayer.lastName;
		}

		return username;
	};
}]);

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
	return function(season, fullName) {
		if (isEmpty(season)) {
			return '';
		}
		if (undefined === fullName) {
			fullName = false;
		}

		var seasonString = '';
		if (season.surface == 'GYM') {
			seasonString += $translate.instant(fullName ? 'season.indoorFullName' : 'season.indoor');
		} else {
			seasonString += $translate.instant(fullName ? 'season.outdoorFullName' : 'season.outdoor');
		}
		seasonString += ' ' + season.year;

		if (season.plusOneYear) {
			var yearString = ''+(season.year + 1);
			seasonString += '/' + yearString.substring(2,4);
		}

		return seasonString;
	}
}]);

app.filter('division', ['$translate', function($translate) {
	return function(obj) {
		if (isEmpty(obj) || isEmpty(obj.divisionAge) || isEmpty(obj.divisionType)) {
			return '';
		}

		var divisionString = '';

		var divAgeStr = $translate.instant('division.' + obj.divisionAge.toLowerCase());
		if (divAgeStr.length > 0) {
			divAgeStr = ' ' + divAgeStr;
		}
		divisionString += $translate.instant('division.' + obj.divisionType.toLowerCase()) + divAgeStr;

		return divisionString;
	}
}]);