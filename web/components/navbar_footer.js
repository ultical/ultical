'use strict';

//nav bar controller
app.controller('NavBarCtrl', ['$scope', 'CONFIG', '$filter', '$translate', '$state', 'authorizer', 'amMoment', '$rootScope', '$stateParams', '$location',
                              function($scope, CONFIG, $filter, $translate, $state, authorizer, amMoment, $rootScope, $stateParams, $location) {

	$scope.logoSide = "front";

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

	$scope.goTo = function(stateName) {
		$state.go('app.' + stateName);
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
		}
	};

	// checks language selection through url
	$scope.$on('$stateChangeSuccess', function rootStateChangeSuccess(event, toState){
		if($stateParams.locale !== undefined) {
			changeLanguage($stateParams.locale);
		}
	});

}]);

app.controller('FooterCtrl', ['$scope', 'CONFIG',
                              function($scope, CONFIG) {

	$scope.emailSupport = CONFIG.brand.emailSupport;
	$scope.bugLink = CONFIG.brand.bugLink;
	$scope.emailContact = CONFIG.brand.emailContact;
	$scope.codeLink = CONFIG.brand.codeLink;

}]);
