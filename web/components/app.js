'use strict';

//Declare app level module which depends on views, and components
var app = angular.module('ultical',
		['ui.router',
		 'ui.bootstrap',
		 'mgcrea.ngStrap',
		 'ngAnimate',
		 'pascalprecht.translate',
		 'ngSanitize',
		 'ultical.start',
		 'ultical.events',
		 'ultical.team',
		 'ultical.user',
		 'ultical.services',
		 'angularMoment',
		 'slugifier',
		 'ng.deviceDetector',
		 'angular.filter',
		 'vcRecaptcha',
		 ]);

//router ui route
app.config(function($stateProvider, $urlRouterProvider, $compileProvider) {
	// For any unmatched url, redirect to:
	$urlRouterProvider.otherwise('/de/calendar');

	var version = '3';

	var availableLocales = '';
	angular.forEach(CONFIG_OBJECT.general.availableLanguages, function(localeCode) {
		availableLocales += localeCode.toLowerCase() + '|';
	});
	availableLocales = availableLocales.substring(0, availableLocales.length - 1);

	$stateProvider
	.state('app', {
		abstract: true,
		url: '/{locale:(?:'+availableLocales+')}',
		params: {
			locale: 'de',
		},
		template: '<ui-view>',
	})
	.state('app.eventsList', {
		url: '/calendar',
		templateUrl: 'pages/event/list.html?v='+version,
	})
	.state('app.eventsListYear', {
		url: '/calendar/{year:int}',
		templateUrl: 'pages/event/list.html?v='+version,
	})
	.state('app.editionEdit', {
		url: '/tournaments/edit/{editionId}/{eventId}',
		templateUrl: 'pages/event/edit.html?v='+version,
	})
	.state('app.eventShow', {
		url: '/{eventSlug}--3{eventId:int}',
		templateUrl: 'pages/event/show.html?v='+version,
	})
	.state('app.editionShow', {
		url: '/{editionSlug}--2{editionId:int}',
		templateUrl: 'pages/event/show.html?v='+version,
	})
	.state('app.formatShow', {
		url: '/{formatSlug}--4{formatId:int}',
		templateUrl: 'pages/event/show.html?v='+version,
	})
	.state('app.eventShowOld', {
		url: '/{eventSlug}--t{eventId:int}',
		templateUrl: 'pages/event/show.html?v='+version,
	})
	.state('app.editionShowOld', {
		url: '/{editionSlug}--e{editionId:int}',
		templateUrl: 'pages/event/show.html?v='+version,
	})
	.state('app.formatShowOld', {
		url: '/{formatSlug}--f{formatId:int}',
		templateUrl: 'pages/event/show.html?v='+version,
	})
	.state('app.teamsList', {
		url: '/teams',
		templateUrl: 'pages/team/list.html?v='+version,
		params: {
			activeTab: 'all',
		},
	})
	.state('app.teamsOwn', {
		url: '/teams/own',
		templateUrl: 'pages/team/list.html?v='+version,
		params: {
			activeTab: 'own',
		},
	})
	.state('app.teamShow', {
		url: '/teams/{teamSlug}--7{teamId:int}',
		templateUrl: 'pages/team/show.html?v='+version,
	})
	.state('app.teamNew', {
		url: '/teams/new',
		templateUrl: 'pages/team/show.html?v='+version,
		params: {
			createNew: true,
		},
	})
	.state('app.confirmMails', {
		url: '/confirm/{code}',
		templateUrl: 'pages/user/mails.html?v='+version,
		params: {
			emailCodeType: 'confirm',
		},
	})
	.state('app.forgotPassword', {
		url: '/forgot/password/{code}',
		templateUrl: 'pages/user/mails.html?v='+version,
		params: {
			emailCodeType: 'password',
		},
	});

});

//eliminate the hash in the url
app.config(['$locationProvider', function($locationProvider) {
	$locationProvider.html5Mode(CONFIG_OBJECT.general.html5Mode);
}]);

//translation provider
app.config(function ($translateProvider) {
	angular.forEach(CONFIG_OBJECT.general.availableLanguages, function(language) {
		$translateProvider
		.translations(language.toLowerCase(), TRANSLATIONS[language.toLowerCase()]);
	});

	$translateProvider
	.addInterpolation('$translateMessageFormatInterpolation')
	.fallbackLanguage('de')
	.useSanitizeValueStrategy('escaped');

	if (CONFIG_OBJECT.general.defaultLanguage.toLowerCase() == 'auto') {
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
	$compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|mailto|blob):/);
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

app.config(function($tooltipProvider) {
  angular.extend($tooltipProvider.defaults, {
    animation: 'am-fade',
    trigger: 'hover',
		delay: {show: 500, hide: 100},
		placement: 'top',
		container: 'body',
  });
});

app.run(['$rootScope', 'storage', '$translate', 'amMoment', '$select', 'authorizer',
	function($rootScope, storage, $translate, amMoment, $select, authorizer) {
	storage.getSeasons(function() {});
	amMoment.changeLocale($translate.use().toLowerCase());
	angular.extend($select.defaults, {
    maxLengthHtml: $translate.instant('general.selectOverflow'),
		placeholder: $translate.instant('general.selectPlaceholder'),
  });

	// define globally accessible functions (especially useful for templates)
	$rootScope.loggedIn = function() {
	  return authorizer.loggedIn();
	};

	$rootScope.getAllClubs = function() {
		return storage.getClubs(function(clubs) {
      return clubs;
    });
	};

	$rootScope.getAllContexts = function() {
		return storage.getContexts(function(contexts) {
			return contexts;
		});
	};

	$rootScope.getAllSeasons = function() {
		return storage.getSeasons(function(seasons) {
		 	return seasons;
		});
	};
}]);

// <head> controller
app.controller('HeadCtrl', ['$scope', 'CONFIG', '$state', 'headService',
                            function($scope, CONFIG, $state, headService) {

	$scope.availableLanguages = CONFIG.general.availableLanguages;

	$scope.getTitle = headService.getTitle;

	$scope.getCurrentStateUrl = function(locale) {
		var params = $state.params;
		params.locale = locale.toLowerCase();
		return $state.href($state.current.name, params, {absolute: true});
	};


}]);
