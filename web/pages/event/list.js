'use strict';

angular.module('ultical.events', [])

.controller('EventListCtrl', ['$scope', 'storage', '$state', '$filter', 'headService', 'moment', '$stateParams', 'actionBar', 'authorizer',
                              function($scope, storage, $state, $filter, headService, moment, $stateParams, actionBar, authorizer) {

  actionBar.clearActions();

  headService.setTitle('event.list.title', {});

	$scope.sortKey = ['startDate', 'endDate', 'name'];
	$scope.sortOrderDesc = false;

  storage.getEvents(true, function(data) {
    actionBar.clearActions();
		$scope.events = data;

    if ((data == null || data.length < 1) && authorizer.loggedIn()) {
      $state.go('app.eventEdit', {eventId: 'new'});
      return;
    }

    var first = null;
    var last = null;

    angular.forEach($scope.events, function(event) {
      if (first == null || moment(event.startDate).isBefore(first))
        first = moment(event.startDate);
      if (last == null || moment(event.endDate).isAfter(last))
        last = moment(event.endDate);
    });

    var noww = last == null ? moment() : (last.isBefore(moment()) ? last : (first.isAfter(moment()) ? first : moment()));
    $scope.current = {
      now: noww,
      first: first == null ? moment().year() : first.year(),
      january: noww.month(0).format('M'),
      thisYear: noww.format('YYYY'),
      lastMonth: noww.subtract(1, 'months'),
      dayMinus3: noww.subtract(3, 'days'),
    };


    if (authorizer.loggedIn()) {
    	var isFormatAdmin = false;
      var loggedInUserId = authorizer.getUser().id;

      angular.forEach($scope.events, function(event) {
        angular.forEach(event.tournamentEdition.tournamentFormat.admins, function(admin) {
          if (admin.id == loggedInUserId) {
            isFormatAdmin = true;
          }
        });
      });

      if (isFormatAdmin) {
        actionBar.addAction({
          group: 'event-new',
          needLogIn: true,
          button: {
            text: 'event.edit.createButtonLabel',
            click: function() {
              $scope.createEvent();
            }
          },
          separator: true,
        });
      }
    }
	});

  $scope.createEvent = function() {
    $state.go('app.eventEdit', {eventId: 'new'});
  }

  $scope.show = {
    beginningOfYear: false,
    year: {},
  };

  $scope.eventFilter = function(event, index, array) {
    var eventStartDate = moment(event.startDate);
    var eventEndDate = moment(event.endDate);
    // show all events - starting with events this month
    if (eventStartDate.isSameOrAfter($scope.current.dayMinus3, 'month')) {
        return true;
    }
    // show all events of this year if the corresponding button is clicked
    if (eventStartDate.isAfter(moment().year($scope.current.thisYear)) && $scope.show.beginningOfYear) {
      return true;
    }
    if (eventStartDate.format('YYYY') in $scope.show.year) {
      return true;
    }
    return false;
  };

}]);
