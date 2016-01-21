'use strict';

//nav bar controller
app.controller('NavBarCtrl', ['$scope', 'CONFIG', '$filter', '$translate', '$state', 'authorizer', 'amMoment',
                              function($scope, CONFIG, $filter, $translate, $state, authorizer, amMoment) {

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
		$state.go(stateName);
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

	$scope.setLanguage = function(languageCode) {
		var oldLanguage = $translate.use();
		$translate.use(languageCode.toLowerCase())
		.then(function(key) {
			if (oldLanguage != key) {
				$scope.selectedLanguage = key.toUpperCase();
				amMoment.changeLocale(key.toLowerCase());
				$state.reload();
			}
		});
	}

}]);
