'use strict';

app.directive('basObjectTypeahead', ['$filter', function($filter) {

	function link(scope, element, attrs) {

		var whatToWatch = 'teamToEdit.' + attrs.basObjectTypeahead;
		if (attrs.basObjectTypeahead == 'admins') {
			whatToWatch = 'newAdmin.obj';
		}

		scope.$watch(whatToWatch, function(newValue, oldValue) {

			// check if new value is no object anymore, but the last one was
			// this happens in some browsers after blurring the typeahead
			if (!angular.isObject(newValue) && angular.isObject(oldValue)) {

				switch (attrs.basObjectTypeahead) {
				case 'location':
					if ($filter('location')(oldValue, true) == newValue) {
						scope.teamToEdit.location = oldValue;
					}
					break;
				case 'club':
					if (oldValue.name == newValue) {
						scope.teamToEdit.club = oldValue;
					}
					break;
				case 'admins':
					if (oldValue.fullName == newValue) {
						scope.newAdmin.obj = oldValue;
					}
					break;
				}
			}
		});
	}

	return {
		link: link,
	};
}]);