// ultical Copyright (C) 2016 ultical developers
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published
// the Free Software Foundation; either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful
// but WITHOUT ANY WARRANTY; without even the implied warranty
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public
// along with this program; if not, write to the Free Software Foundation,
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
// 

'use strict';

//nav bar controller
app.controller('NavBarCtrl', ['$scope', 'CONFIG', '$filter', '$translate', '$state', 'authorizer', 'amMoment', '$rootScope', '$stateParams', '$location',
                              function($scope, CONFIG, $filter, $translate, $state, authorizer, amMoment, $rootScope, $stateParams, $location) {

	$scope.logoSide = "front";

	$scope.goTo = function() {
		console.log("go");
		$state.go('app.eventsList');
	}
	
	$scope.toggleLogoFlip = function(){
		$scope.logoSide = toggle($scope.logoSide);
	}

	function toggle(value){
		return value === 'front' ? 'back' : 'front';
	}

	$scope.loggedIn = function() {
		return authorizer.loggedIn();
	}

	$scope.getUser = function() {
		return authorizer.getUser();
	}

	$scope.createEvent = function() {
		console.log("Create an event");
	};

	$scope.logOut = function() {
		authorizer.logOut();
		$state.reload();
	};

	/* *** Language Selector ****/
	$scope.selectedLanguage = $translate.use().toUpperCase();

	// initialize and fill language list
	$scope.languageSelector = [];
	angular.forEach(CONFIG.general.availableLanguages, function(languageCode, idx) {
		$scope.languageSelector.push(
				{
					'text': $filter('capitalize')(CONFIG.general.availableLanguagesFull[idx]),
					'click': "setLanguage('"+languageCode+"')",
				});
	});

	function changeLanguage(locale) {
		$translate.use(locale);
		$rootScope.activeLang = locale;
		amMoment.changeLocale(locale);
		$scope.selectedLanguage = locale.toUpperCase();
	}

	// change language when user chooses from dropdown
	$scope.setLanguage = function(languageCode) {
		var oldLanguage = $translate.use();
		var key = languageCode.toLowerCase();

		if (oldLanguage != key) {
			$rootScope.otherLangURL = $location.url().replace('/' + oldLanguage, '/' + key.toLowerCase());
			// changes url to fire $stateChangeSuccess
			$location.url($rootScope.otherLangURL);
			changeLanguage(key);
		}
	};

	// checks language selection through url
	$scope.$on('$stateChangeSuccess', function rootStateChangeSuccess(event, toState){
		if($stateParams.locale !== undefined) {
			changeLanguage($stateParams.locale);
		}
	});

}]);
