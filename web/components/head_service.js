'use strict';

app.factory('headService', ['$translate', function($translate) {
  var baseTitle = $translate.instant('general.pageTitle');
  var pageTitle = '';
  var pageTitleOptions = {};
  return {
  	getTitle: function() {
  			var returnTitle = '';
  			if (!isEmpty(pageTitle)) {
  				returnTitle += $translate.instant(pageTitle, pageTitleOptions) + ' - ';
  			}
  			return returnTitle + baseTitle;
  	},
    setTitle: function(newTitle, newTitleOptions) {
      pageTitle = newTitle;
      pageTitleOptions = newTitleOptions;
    },
  };
}]);
