'use strict';

//nav bar controller
app.controller('NavBarCtrl', ['$scope', 'CONFIG', '$filter', '$translate', '$state', 'authorizer', 'amMoment', '$rootScope', '$stateParams', '$location', 'storage', 'actionBar', '$aside',
                              function($scope, CONFIG, $filter, $translate, $state, authorizer, amMoment, $rootScope, $stateParams, $location, storage, actionBar, $aside) {

	$scope.logoSide = "front";

	$scope.goTo = function() {
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
    storage.resetUserSpecifics();
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

      if (menuAside) {
        menuAside.hide();
      }
		}
	};

	// checks language selection through url
	$scope.$on('$stateChangeSuccess', function rootStateChangeSuccess(event, toState){
    if($stateParams.locale !== undefined) {
			changeLanguage($stateParams.locale);
		}
    if (menuAside) {
      menuAside.hide();
    }
    if (actionAside) {
      actionAside.hide();
    }
	});

  var menuAside = null;
  var actionAside = null;

  var thisScope = $scope;

  $scope.openMenuAside = function() {
    menuAside = $aside({
      title: 'nav.menuLabel',
      show: true,
      container: 'body',
      animation: 'am-fade-and-slide-left',
      position: 'left',
      controller: function($scope) {
        $scope.getUser = thisScope.getUser;
        $scope.loggedIn = thisScope.loggedIn;
        $scope.logOut = thisScope.logOut;
        $scope.languageSelector = thisScope.languageSelector;
        $scope.selectedLanguage = thisScope.selectedLanguage;
        $scope.setLanguage = thisScope.setLanguage;
        $scope.isMenuAside = true;
      },
      html: true,
      backdrop: true,
      templateUrl: 'navbar/aside.html?v=3',
    });
  };

  $scope.openActionAside = function() {
    actionAside = $aside({
      title: 'nav.actionsLabel',
      show: true,
      container: 'body',
      animation: 'am-fade-and-slide-left',
      position: 'left',
      controller: function($scope) {
        $scope.head = actionBar.getHead();
        $scope.actions = actionBar.getActions();
        $scope.showAction = actionBar.showAction;
        $scope.isActionAside = true;
      },
      html: true,
      backdrop: true,
      templateUrl: 'navbar/aside.html?v=3',
    });
  };

}]);
