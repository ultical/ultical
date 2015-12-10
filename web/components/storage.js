'use strict';

app.factory('storage', ['$filter', 'serverApi',
                        function($filter, serverApi) {

	return  {
		// init structures
		selected: {
			event: {},
		},

		allEvents: [],

		getEvent: function(eventId, callback) {
			this.getEvents(function(events) {
				var foundEvent = null;
				angular.forEach(events, function(event, idx) {
					if (event.id == eventId)
						foundEvent = event;
				});
				callback(foundEvent);
			});
		},

		getEvents: function(callback) {
			var that = this;
			if (isEmpty(this.allEvents)) {
				// make API call
				serverApi.getEvents(function(data) {
					that.allEvents = data.data;

					// add some fields
					var todayDateString = $filter('date')(new Date(), 'yyyy-MM-dd');

					angular.forEach(data.data, function(event) {
						console.log("event.te", event.tournamentEdition);
						event.tournamentEdition.a = 1;
						event.tournamentEdition.registrationIsOpen = !isEmpty(event.tournamentEdition.registrationStart) && event.tournamentEdition.registrationStart.string <= todayDateString && event.tournamentEdition.registrationEnd.string >= todayDateString;
						event.tournamentEdition.registrationTime = isEmpty(event.tournamentEdition.registrationStart) ? 'never' : (event.tournamentEdition.registrationStart.string > todayDateString ? 'future' : 'past');

						var hasEditionFee = false;
						var hasEventFee = false;
						angular.forEach(['Player', 'Team', 'Guest'], function(feeType) {
							if (event.tournamentEdition['feePer' + feeType] != 0) {
								hasEditionFee = true;
							}
							if (event['feePer' + feeType] != 0) {
								hasEventFee = true;
							}
						});
						angular.forEach(['Breakfast', 'Lunch', 'Dinner', 'Night'], function(feeType) {
							if (event['feePer' + feeType] != 0) {
								hasEventFee = true;
							}
						});

						event.tournamentEdition.hasFees = hasEditionFee;
						event.hasFees = hasEventFee;
					
					});
					
					console.log("events", data.data);

					callback(data.data);
				});
			} else {
				callback(this.allEvents);
			}
		},


	};
}]);
