'use strict';

//nav bar controller
app.controller('NavBarCtrl', ['$scope', 'CONFIG', '$filter', '$translate',
                              function($scope, CONFIG, $filter, $translate) {

	$scope.version = '0.1';

	$scope.loggedIn = false;

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
