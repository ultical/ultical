'use strict';

app.controller('FooterCtrl', ['$scope', 'CONFIG',
                              function($scope, CONFIG) {

	$scope.emailSupport = CONFIG.brand.emailSupport;
	$scope.bugLink = CONFIG.brand.bugLink;
	$scope.emailContact = CONFIG.brand.emailContact;
	$scope.codeLink = CONFIG.brand.codeLink;

  $scope.qaLink = CONFIG.brand.qaLink;
}]);
