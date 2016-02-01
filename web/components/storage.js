'use strict';

app.factory('storage', ['$filter', 'serverApi', 'authorizer',
                        function($filter, serverApi, authorizer) {

	var returnObject = {
			// init structures
			selected: {
				event: {},
			},

			own: {
				teams: [],
			},
			events: [],
			teams: [],
			seasons: [],

			editionIndexed: {},
			formatIndexed: {},

			eventsIndexed: {},
			teamsIndexed: {},
			userIndexed: {},
			playerIndexed: {},
			clubIndexed: {},
			associationIndexed: {},
			contactIndexed: {},
			rosterIndexed: {},
			seasonIndexed: {},

			getEmptyEvent: function() {
				return createEmptyEvent();
			},

			getEmptyEdition: function() {
				return createEmptyEdition();
			},

			getEmptyFormat: function() {
				return createEmptyFormat();
			},

			getTeam: function(teamId, callback) {
				var that = this;
				serverApi.getTeam(teamId, function(team) {
					storeTeam(that, team, newLoopIndex());
					callback(team);
				});
			},

			getAllTeams: function(callback) {
				var that = this;
				callback(that.teams);
				serverApi.getAllTeams(function(teams) {
					that.teams = teams;
					angular.forEach(teams, function(team) {
						storeTeam(that, team, newLoopIndex());
					});
					callback(that.teams);
				});
			},

			getOwnTeams: function(callback) {
				var that = this;
				callback(that.own.teams);
				serverApi.getOwnTeams(function(teams) {
					that.own.teams = teams;
					angular.forEach(teams, function(team) {
						storeTeam(that, team, newLoopIndex());
					});
					callback(that.own.teams);
				});
			},

			saveRoster: function(roster, team, callback, errorCallback) {
				serverApi.postRoster(roster, team.id, callback, errorCallback);
			},

			deleteRoster: function(roster, team, callback, errorCallback) {
				serverApi.deleteRoster(roster, function() {
					var rosterDeleteIdx = -1;
					angular.forEach(team.rosters, function(teamRoster, idx) {
						if (teamRoster.id == roster.id) {
							rosterDeleteIdx = idx;
						}
					});
					if (rosterDeleteIdx >= 0) {
						team.rosters.splice(rosterDeleteIdx, 1);
					}
					callback();
				}, errorCallback);
			},

			removePlayerFromRoster: function(player, roster, callback, errorCallback) {
				var that = this;
				serverApi.removePlayerFromRoster(player, roster, function() {
					var idxToRemove = -1;
					angular.forEach(roster.players, function(rosterPlayer, idx) {
						if (rosterPlayer.player.id == player.id) {
							idxToRemove = idx;
						}
					});
					if (idxToRemove != -1) {
						roster.players.splice(idxToRemove, 1);
					}
					callback();
				}, errorCallback);
			},

			addPlayerToRoster: function(player, roster, callback, errorCallback) {
				var that = this;
				serverApi.addPlayerToRoster(player, roster, function(newPlayer) {
					that.playerIndexed[newPlayer.id] = newPlayer;
					var today = new Date();
					var rosterPlayer = {
							player: newPlayer,
							dateAdded: moment().format('YYYY-MM-DD'),
					};
					roster.players.push(rosterPlayer);
					callback(rosterPlayer);
				}, errorCallback);
			},

			getSeasons: function(callback) {
				var that = this;
				if (isEmpty(this.seasons)) {
					serverApi.getSeasons(function(seasons) {
						that.seasons = seasons;
						angular.forEach(seasons, function(season) {
							storeSeason(that, season, newLoopIndex());
						});
					});
				} else {
					callback(this.seasons);
				}
			},

			getFormatForEvent: function(eventId, callback) {
				var that = this;

				serverApi.getFormatByEvent(eventId, function(data) {
					storeTournamentFormat(that, data, newLoopIndex());
					callback(data);
				});
			},

			getEvent: function(eventId, callback) {
				var that = this;

				serverApi.getEvent(eventId, function(data) {
					storeEvent(that, data, newLoopIndex());
					callback(data);
				});
			},

			getEvents: function(callback) {
				var that = this;
				if (isEmpty(this.allEvents)) {
					// make API call
					serverApi.getEvents(function(data) {
						that.events = data;

						// add some fields
						angular.forEach(data, function(event) {
							storeEvent(that, event, newLoopIndex());
						});

						callback(that.events);
					});
				} else {
					callback(this.events);
				}
			},

			saveTeam: function(team, callback, activeList) {
				var that = this;
				var oldTeam;
				if (team.id == -1) {
					oldTeam = null;
				} else {
					oldTeam = this.teamsIndexed[team.id];
					if (!angular.isObject(team.location)) {
						team.location = {
								id: oldTeam.location.id,
								version: oldTeam.location.version,
						}
					}
				}
				serverApi.saveTeam(team, oldTeam, function(savedTeam) {
					that.teamsIndexed[team.id] = savedTeam;
					var index = -1;
					angular.forEach(that.own.teams, function(ownTeam, idx) {
						if (ownTeam.id == savedTeam.id) {
							index = idx;
						}
					});
					if (index >= 0) {
						that.own.teams.splice(index, 1);
					}
					that.own.teams.push(savedTeam);

					index = -1;
					angular.forEach(that.teams, function(existingTeam, idx) {
						if (existingTeam.id == savedTeam.id) {
							index = idx;
						}
					});
					if (index >= 0) {
						that.teams.splice(index, 1);
					}
					that.teams.push(savedTeam);

					storeTeam(that, savedTeam, newLoopIndex());

					if (activeList == 'own') {
						callback(that.own.teams);
					} else if (activeList == 'all') {
						callback(that.teams);
					}
				});
			},
	}

	function newLoopIndex() {
		return Math.floor(Math.random()*10000);
	}

	function storeWithoutLoops(element, elementIndexed, loopIndex) {
		// to avoid loops only process every element once
		if (element.id in elementIndexed && element.x in elementIndexed && elementIndexed[element.id].x.loopIndex == loopIndex) {
			return false;
		}

		element.x = {loopIndex: loopIndex};
		elementIndexed[element.id] = element;

		return true;
	}

	function storeTeam(that, team, loopIndex) {
		if (!storeWithoutLoops(team, that.teamsIndexed, loopIndex)) {
			return;
		}

		if (!isEmpty(team.emails)) {
			team.emails = team.emails.split(',');
		} else {
			team.emails = [];
		}

		angular.forEach(team.admins, function(admin, idx) {
			if (angular.isObject(admin)) {
				storeUser(that, admin, loopIndex);
			} else {
				team.admins[idx] = that.userIndexed[admin];
			}
			team.x.own = authorizer.getUser() != null && (team.x.own || admin.id == authorizer.getUser().id);
		});

		angular.forEach(team.rosters, function(roster, idx) {
			if (angular.isObject(roster)) {
				storeRoster(that, roster, loopIndex);
			} else {
				team.rosters[idx] = that.rosterIndexed[roster];
			}
		});

		if (angular.isObject(team.club)) {
			storeClub(that, team.club, loopIndex);
		} else {
			team.club = that.clubIndexed[team.club];
		}
	}

	function storeRoster(that, roster, loopIndex) {
		if (!storeWithoutLoops(roster, that.rosterIndexed, loopIndex)) {
			return;
		}

		if (angular.isObject(roster.season)) {
			storeSeason(that, roster.season, loopIndex);
		} else {
			roster.season = that.seasonIndexed[roster.season];
		}

		angular.forEach(roster.players, function(player, idx) {
			if (angular.isObject(player.player)) {
				storePlayer(that, player.player, loopIndex);
			} else {
				roster.players[idx].player = that.playerIndexed[player.player];
			}
		});
	}

	function storeSeason(that, season, loopIndex) {
		that.seasonIndexed[season.id] = season;
	}


	function storeUser(that, user, loopIndex) {
		if (!storeWithoutLoops(user, that.userIndexed, loopIndex)) {
			return;
		}

		if (angular.isObject(user.dfvPlayer)) {
			storePlayer(that, user.dfvPlayer, loopIndex);
		} else {
			user.dfvPlayer = that.playerIndexed[user.dfvPlayer];
		}
	}

	function storePlayer(that, player, loopIndex) {
		if (!storeWithoutLoops(player, that.playerIndexed, loopIndex)) {
			return;
		}

		if (angular.isObject(player.club)) {
			storeClub(that, player.club, loopIndex);
		} else {
			player.club = that.clubIndexed[player.club];
		}
	}

	function storeClub(that, club, loopIndex) {
		if (!storeWithoutLoops(club, that.clubIndexed, loopIndex)) {
			return;
		}

		if (angular.isObject(club.association)) {
			storeAssociation(that, club.association, loopIndex);
		} else {
			club.association = that.associationIndexed[club.association];
		}
	}

	function storeAssociation(that, association, loopIndex) {
		if (!storeWithoutLoops(association, that.associationIndexed, loopIndex)) {
			return;
		}

		association.x = {};

		angular.forEach(association.admins, function(admin, idx) {
			if (angular.isObject(admin)) {
				storeUser(that, admin, loopIndex);
			} else {
				association.admins[idx] = that.userIndexed[admin];
			}
			association.x.own = authorizer.getUser() != null && (association.x.own || admin.id == authorizer.getUser().id);
		});

		if (angular.isObject(association.contact)) {
			storeUser(that, association.contact, loopIndex);
		} else {
			association.contact = that.contactIndexed[association.contact];
		}
	}

	function storeContact(that, contact, loopIndex) {
		that.contactIndexed[contact.id] = contact;
	}

	function storeEvent(that, event, loopIndex) {
		if (!storeWithoutLoops(event, that.eventsIndexed, loopIndex)) {
			return;
		}

		if (angular.isObject(event.tournamentEdition)) {
			storeTournamentEdition(that, event.tournamentEdition, loopIndex);
		} else {
			if (event.tournamentEdition in that.editionIndexed) {
				event.tournamentEdition = that.editionIndexed[event.tournamentEdition];
			} else {
				alert("Missing an edition: " + event.tournamentEdition);
			}
		}

		angular.forEach(event.admins, function(admin, idx) {
			if (angular.isObject(admin)) {
				storeUser(that, admin, loopIndex);
			} else {
				event.admins[idx] = that.userIndexed[admin];
			}
			event.x.own = authorizer.getUser() != null && (event.x.own || admin.id == authorizer.getUser().id);
		});

		if (angular.isObject(event.localOrganizer)) {
			storeContact(that, event.localOrganizer, loopIndex);
		} else {
			event.localOrganizer = that.contactIndexed[event.localOrganizer];
		}

		var todayDateString = $filter('date')(new Date(), 'yyyy-MM-dd');

		event.tournamentEdition.registrationIsOpen = !isEmpty(event.tournamentEdition.registrationStart) && event.tournamentEdition.registrationStart.string <= todayDateString && event.tournamentEdition.registrationEnd.string >= todayDateString;
		event.tournamentEdition.registrationTime = isEmpty(event.tournamentEdition.registrationStart) ? 'never' : (event.tournamentEdition.registrationStart.string > todayDateString ? 'future' : 'past');

		event.x.isSingleEvent = event.tournamentEdition.isSingleEdition;
		event.x.hasLocalOrganizer = !isEmpty(event.localOrganizer) && !isEmpty(event.localOrganizer.name) && event.tournamentEdition.organizer.id != event.localOrganizer.id;
		event.x.hasFees = event.tournamentEdition.fees.length > 0 || event.fees.length > 0;

	}

	function storeTournamentEdition(that, edition, loopIndex) {
		if (!storeWithoutLoops(edition, that.editionIndexed, loopIndex)) {
			return;
		}

		angular.forEach(edition.events, function(event, idx) {
			if (angular.isObject(event)) {
				event.tournamentEdition = edition;
				storeEvent(that, event, loopIndex);
			} else {
				edition.events[idx] = event;
			}
		});

		if (angular.isObject(edition.tournamentFormat)) {
			storeTournamentFormat(that, edition.tournamentFormat, loopIndex);
		} else {
			if (edition.tournamentFormat in that.formatIndexed) {
				edition.tournamentFormat = that.formatIndexed[edition.tournamentFormat];
			} else {
				alert("Missing a format: " + edition.tournamentFormat);
			}
		}

		if (angular.isObject(edition.organizer)) {
			storeContact(that, edition.organizer, loopIndex);
		} else {
			edition.organizer = that.contactIndexed[edition.organizer];
		}

		edition.x.isSingleEdition = edition.events != null && edition.events.length == 1;		
	}

	function storeTournamentFormat(that, format, loopIndex) {
		if (!storeWithoutLoops(format, that.formatIndexed, loopIndex)) {
			return;
		}

		angular.forEach(format.editions, function(edition, idx) {
			if (angular.isObject(edition)) {
				edition.tournamentFormat = format;
				storeTournamentEdition(that, edition, loopIndex);
			} else {
				format.editions[idx] = that.editionIndexed[edition];
			}
		});

		angular.forEach(format.admins, function(admin, idx) {
			if (angular.isObject(admin)) {
				storeUser(that, admin, loopIndex);
			} else {
				format.admins[idx] = that.userIndexed[admin];
			}
			format.x.own = authorizer.getUser() != null && (format.x.own || admin.id == authorizer.getUser().id);
		});

		if (angular.isObject(format.association)) {
			storeAssociation(that, format.association, loopIndex);
		} else {
			format.association = that.associationIndexed[format.association];
		}
	}

	function createEmptyEvent() {
		return {
			matchdayNumber: -1,
			location: {},
			startDate: '2016-01-01',
			endDate: '2016-01-03',
			fees: {},
			admins: [],
			localOrganizerName: '',
			localOrganizerEmail: '',
			localOrganizerPhone: '',
			divisionConfirmations: {},
			tournamentEdition: {},
		};
	}

	function createEmptyEdition() {
		return {
			alternativeName: '',
			season: {
				year: 2016,
				surface: 'TURF',
				plusOneYear: false,
			},
			registrationStart: '',
			registrationEnd: '',
			fees: {},
			currency: 'EUR',
			organizerName: '',
			organizerEmail: '',
			organizerPhone: '',
			alternativeMatchdayName: '',
			divisionRegistrations: {},
			events: [],
			tournamentFormat: {},
		};
	};

	function createEmptyFormat() {
		return {
			name: '',
			description: '',
			admins: [],
			editions: [],
		};
	};

	return returnObject;

}]);
