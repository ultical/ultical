'use strict';

//Declare app level module which depends on views, and components
var app = angular.module('ultical', 
		['ui.router',
		 'mgcrea.ngStrap',		
		 'ngAnimate',
		 'pascalprecht.translate',
		 'ngSanitize',
		 'ultical.start',
		 ]);




//router ui route
app.config(function($stateProvider, $urlRouterProvider, $compileProvider) {
	// For any unmatched url, redirect to /start
	$urlRouterProvider.otherwise("/tournaments");

	var version = '1';

	$stateProvider
	.state('start', {
		url: "/start",
		templateUrl: "pages/start/start.html?v="+version,
	})
	.state('tournaments', {
		url: "/tournaments",
		templateUrl: "pages/tournaments/list.html?v="+version,
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

//config element service
app.factory('CONFIG', function() {
	return CONFIG_OBJECT;
});
