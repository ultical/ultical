'use strict';

//Declare app level module which depends on views, and components
var app = angular.module('ultical', 
		['ui.router',
		 'mgcrea.ngStrap',		
		 'ngAnimate',
		 'pascalprecht.translate',
		 'ngSanitize',
		 'ultical.start',
		 'ultical.events',
		 'ultical.team',
		 'ultical.user',
		 'angularMoment',
		 'slugifier',
		 ]);

//router ui route
app.config(function($stateProvider, $urlRouterProvider, $compileProvider) {
	// For any unmatched url, redirect to:
	$urlRouterProvider.otherwise("/de/calendar");

	var version = '3';

	var availableLocales = '';
	angular.forEach(CONFIG_OBJECT.general.availableLanguages, function(localeCode) {
		availableLocales += localeCode.toLowerCase() + '|';
	});
	availableLocales = availableLocales.substring(0, availableLocales.length - 1);

	$stateProvider
	.state('app', {
		abstract: true,
		url: "/{locale:(?:"+availableLocales+")}",
		template: '<ui-view>',
	})
	.state('app.start', {
		url: "/calendar",
		templateUrl: "pages/event/list.html?v="+version,
	})
	.state('app.eventsList', {
		url: "/calendar",
		templateUrl: "pages/event/list.html?v="+version,
	})
	.state('app.editionEdit', {
		url: "/tournaments/edit/{editionId}/{eventId}",
		templateUrl: "pages/event/edit.html?v="+version,
	})
	.state('app.eventShow', {
		url: "/{eventSlug}-t{eventId:int}",
		templateUrl: "pages/event/show.html?v="+version,
	})
	.state('app.teamsList', {
		url: "/teams",
		templateUrl: "pages/team/list.html?v="+version,
		params: {
			activeTab: 'all',
		},
	})
	.state('app.teamsOwn', {
		url: "/teams",
		templateUrl: "pages/team/list.html?v="+version,
		params: {
			activeTab: 'own',
		},
	})
	.state('app.confirmMails', {
		url: "/confirm/{code}",
		templateUrl: "pages/user/mails.html?v="+version,
		params: {
			emailCodeType: 'confirm',
		},
	})
	.state('app.forgotPassword', {
		url: "/forgot/password/{code}",
		templateUrl: "pages/user/mails.html?v="+version,
		params: {
			emailCodeType: 'password',
		},
	});

	$compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|blob):/);

});

//eliminate the hash in the url
app.config(["$locationProvider", function($locationProvider) {
	$locationProvider.html5Mode(CONFIG_OBJECT.general.html5Mode);
}]);

//translation provider
app.config(function ($translateProvider) {
	angular.forEach(CONFIG_OBJECT.general.availableLanguages, function(language) {
		$translateProvider
		.translations(language.toLowerCase(), TRANSLATIONS[language.toLowerCase()]);
	});

	$translateProvider
	.fallbackLanguage('de')
	.useSanitizeValueStrategy('escaped');

	if (CONFIG_OBJECT.general.defaultLanguage.toLowerCase() == "auto") {
		$translateProvider
		.registerAvailableLanguageKeys(CONFIG_OBJECT.general.availableLanguages, {
			'en_US': 'en',
			'en_UK': 'en',
			'de_DE': 'de',
			'de_CH': 'de',
			'de_AT': 'de-at',
		})
		.determinePreferredLanguage();
	} else {
		$translateProvider
		.preferredLanguage(CONFIG_OBJECT.general.defaultLanguage);
	}
});

//make sure http(s) and mailto links are valid and not escaped for security reasons
app.config(function($compileProvider) {   
	$compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|mailto):/);
	// Angular before v1.2 uses $compileProvider.urlSanitizationWhitelist(...)
});

//config element service
app.factory('CONFIG', function() {
	return CONFIG_OBJECT;
});

app.config(function($modalProvider) {
	angular.extend($modalProvider.defaults, {
		animation: 'am-flip-x',
		container: 'body',
		keyboard: false,
	});
});

app.run(['storage', '$translate', 'amMoment', function(storage, $translate, amMoment) {
	storage.getSeasons(function() {});
	amMoment.changeLocale($translate.use().toLowerCase());
}]);

