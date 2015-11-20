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
	$urlRouterProvider.otherwise("/start");

	var version = '1';

	$stateProvider
	.state('start', {
		url: "/start",
		templateUrl: "pages/start/start.html?v="+version,
	});
	
	$compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|blob):/);

});

//config element service
app.factory('CONFIG', function() {
	return CONFIG_OBJECT;
});
