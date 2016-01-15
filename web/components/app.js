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
		 ]);

//router ui route
app.config(function($stateProvider, $urlRouterProvider, $compileProvider) {
	// For any unmatched url, redirect to:
	$urlRouterProvider.otherwise("/calendar");

	var version = '1';

	$stateProvider
	.state('start', {
		url: "/calendar",
		templateUrl: "pages/event/list.html?v="+version,
	})
	.state('eventsList', {
		url: "/calendar",
		templateUrl: "pages/event/list.html?v="+version,
	})
	.state('editionEdit', {
		url: "/tournament/edit/{editionId}/{eventId}",
		templateUrl: "pages/event/edit.html?v="+version,
	})
	.state('eventShow', {
		url: "/event/{eventId:int}/show",
		templateUrl: "pages/event/show.html?v="+version,
	})
	.state('teamsList', {
		url: "/teams",
		templateUrl: "pages/team/list.html?v="+version,
		params: {
			activeTab: 'all',
		},
	})
	.state('teamsOwn', {
		url: "/teams",
		templateUrl: "pages/team/list.html?v="+version,
		params: {
			activeTab: 'own',
		},
	})
	.state('confirmMails', {
		url: "/confirm/{code}",
		templateUrl: "pages/user/mails.html?v="+version,
		params: {
			emailCodeType: 'confirm',
		},
	})
	.state('forgotPassword', {
		url: "/forgot/password/{code}",
		templateUrl: "pages/user/mails.html?v="+version,
		params: {
			emailCodeType: 'password',
		},
	});

	$compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|blob):/);

});

//translation provider
app.config(function ($translateProvider) {
	angular.forEach(CONFIG_OBJECT.general.availableLanguages, function(language) {
		$translateProvider
		.translations(language.toLowerCase(), window['TRANSLATIONS_'+language.toUpperCase()]);
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
			'de_AT': 'de',
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

app.run(['storage', function(storage) {
	storage.getSeasons(function() {});
}]);