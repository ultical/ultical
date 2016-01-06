'use strict';

//nav bar controller
app.controller('NavBarCtrl', ['$scope', 'CONFIG', '$filter', '$translate', '$state', 'authorizer',
                              function($scope, CONFIG, $filter, $translate, $state, authorizer) {

	$scope.version = '0.1';

	$scope.$state = $state;

	$scope.loggedIn = function() {
		return authorizer.loggedIn();
	}

	$scope.getUser = function() {
		return authorizer.getUser();
	}

	/* *** Menu ***/
	$scope.eventDropdown =
		[{
			'text': $translate.instant('nav.eventDropdown.newEvent'),
			'href': $state.href('editionEdit', { eventId: 'new' }),
		},
		{
			'divider': true
		},
		{
			'text': $translate.instant('nav.eventDropdown.listEvents'),
			'href': $state.href('eventsList'),
		},
		];

	$scope.createEvent = function() {
		console.log("Create an event");
	};

	$scope.profileDropdown =
		[{
			'text': $translate.instant('nav.profileDropdown.ownTeams'),
			'click': 'goTo("teamsOwn")',
		},
		{
			'text': $translate.instant('nav.profileDropdown.ownEvents'),
			'href': $state.href('eventsList'),
		},
		];

	$scope.goTo = function(stateName) {
		$state.go(stateName);
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
				$state.reload();
			}
		});
	}

}]);
