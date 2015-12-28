'use strict';

app.factory('storage', ['$filter', 'serverApi',
                        function($filter, serverApi) {

	return  {
		// init structures
		selected: {
			event: {},
		},

		events: {},
		tournamentEditions: {},
		tournamentFormats: {},

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

					// add some fields
					var todayDateString = $filter('date')(new Date(), 'yyyy-MM-dd');

					angular.forEach(data, function(event) {

						storeEvent(that, event);

						console.log("event.te", event.tournamentEdition);

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

					console.log("events", that.events);

					callback(that.events);
				});
			} else {
				callback(this.events);
			}
		}

	};

	function storeEvent(that, event) {
		that.events[event.id] = event;

		if (angular.isObject(event.tournamentEdition)) {
			storeTournamentEdition(that, event.tournamentEdition);
		} else {
			if (event.tournamentEdition in that.tournamentEditions) {
				event.tournamentEdition = that.tournamentEditions[event.tournamentEdition];
			} else {
				alert("Missing an edition: " + event.tournamentEdition);
			}

		}
	}

	function storeTournamentEdition(that, edition) {
		that.tournamentEditions[edition.id] = edition;

		if (angular.isObject(edition.tournamentFormat)) {
			storeTournamentFormat(that, edition.tournamentFormat);
		} else {
			if (edition.tournamentFormat in that.tournamentFormats) {
				edition.tournamentFormat = that.tournamentFormats[edition.tournamentFormat];
			} else {
				alert("Missing a format: " + edition.tournamentFormat);
			}
		}
	}

	function storeTournamentFormat(that, format) {
		that.tournamentFormats[format.id] = format;
	}

}]);
