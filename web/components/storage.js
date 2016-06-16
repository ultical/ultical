'use strict';

app.factory('storage', ['$filter', 'serverApi', 'authorizer', 'moment',
                        function($filter, serverApi, authorizer, moment) {

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
      formats: [],

      teamsIndexed: {},
			playerIndexed: {},

      requested: {
          own: {},
      },

      // fetch only once:
			seasons: [],
      clubs: [],
      contexts: [],

      resetUserSpecifics: function() {
          this.own.teams = [];
          this.requested.own = {};
      },

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
					storeTeam(team, newLoopIndex());
					callback(team);
				});
        angular.forEach(this.teams, function(team) {
          if (team.id == teamId) {
            callback(team);
          }
        });
			},

			getAllTeamBasics: function(callback) {
				var that = this;
				callback(that.teams);
				serverApi.getAllTeamBasics(function(teams) {
					that.teams = teams;

					var loopIndex = newLoopIndex();
					angular.forEach(teams, function(team) {
						storeTeam(team, loopIndex);
					});
					callback(that.teams);
				});
			},

      getOwnTeamsCache: function(callback) {
        var that = this;
        if (!that.requested.own.teams) {
          that.getOwnTeams(callback);
          return null;
        } else {
          // don't callback if we have a direct result
          return that.own.teams;
        }
      },

      getOwnTeamBasics: function(callback) {
        var that = this;
        if (that.requested.own.teams) {
          callback(that.own.teams);
        }
        serverApi.getOwnTeamBasics(function(teams) {
          that.own.teams = teams;
          that.requested.own.teams = true;
          var loopIndex = newLoopIndex();
          angular.forEach(teams, function(team) {
            storeTeam(team, loopIndex);
          });
          callback(that.own.teams);
        });
      },

			getOwnTeams: function(callback) {
				var that = this;
				if (that.requested.own.teams) {
          callback(that.own.teams);
				}
        serverApi.getOwnTeams(function(teams) {
					that.own.teams = teams;
          that.requested.own.teams = true;
					var loopIndex = newLoopIndex();
					angular.forEach(teams, function(team) {
						storeTeam(team, loopIndex);
					});
					callback(that.own.teams);
				});
			},

      deleteTeam: function(team, callback, errorCallback) {
        var that = this;
        serverApi.deleteTeam(team.id, function() {
          delete that.teamsIndexed[team.id];

          var teamIndexToDelete = -1;
          angular.forEach(that.teams, function(oneTeam, idx) {
            if (oneTeam.id == team.id) {
              teamIndexToDelete = idx;
            }
          });
          if (teamIndexToDelete >= 0) {
            that.teams.splice(teamIndexToDelete, 1);
          }
          var ownTeamIndexToDelete = -1;
          angular.forEach(that.own.teams, function(oneTeam, idx) {
            if (oneTeam.id == team.id) {
              ownTeamIndexToDelete = idx;
            }
          });
          if (ownTeamIndexToDelete >= 0) {
            that.own.teams.splice(ownTeamIndexToDelete, 1);
          }
          callback();
        }, errorCallback);
      },

			saveRoster: function(roster, team, callback, errorCallback) {
        if (roster.id != -1) {
          var rosterToSend = angular.copy(roster);
          rosterToSend.players = [];
          serverApi.putRoster(rosterToSend, team, function(updatedRoster) {
            updatedRoster.players = roster.players;
            updatedRoster.x = {};

            var indexToReplace = -1;
            angular.forEach(team.rosters, function(roster, idx) {
              if (roster.id == updatedRoster.id) {
                indexToReplace = idx;
              }
            });
            if (indexToReplace >= 0) {
              team.rosters.splice(indexToReplace, 1);
              team.rosters.push(updatedRoster);
            }

            callback(updatedRoster);
          }, errorCallback);
        } else {
				  serverApi.postRoster(roster, team.id, function(updatedRoster) {
            updatedRoster.x = {};
            updatedRoster.players = [];
            team.rosters.push(updatedRoster);
            callback(updatedRoster);
          }, errorCallback);
        }
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

      // fetch only once
			getSeasons: function(callback) {
				var that = this;
				if (isEmpty(this.seasons) && !this.gettingSeasons) {
          that.gettingSeasons = true;
					serverApi.getSeasons(function(seasons) {
						that.seasons = seasons;
            callback(seasons);
					});
				} else {
					callback(this.seasons);
				}
			},

      // fetch only once
      getContexts: function(callback) {
				var that = this;
				if (isEmpty(this.contexts) && !this.gettingContexts) {
          that.gettingContexts = true;
          serverApi.getContexts(function(contexts) {
						that.contexts = contexts;
            return callback(angular.copy(contexts));
					});
				} else {
					return callback(angular.copy(this.contexts));
				}
			},

      // fetch only once
      getClubs: function(callback) {
				var that = this;
				if (isEmpty(this.clubs) && !this.gettingClubs) {
          that.gettingClubs = true;
					serverApi.getAllClubs(function(clubs) {
						that.clubs = clubs;
            return callback(clubs);
					});
				} else {
					return callback(this.clubs);
				}
			},
      getFormatForEdition: function(editionId, callback) {
        var that = this;

        angular.forEach(this.formats, function(format) {
          angular.forEach(format.editions, function(edition) {
            if (edition.id == editionId) {
              callback(format);
            }
          });
        });

				serverApi.getFormatByEdition(editionId, function(data) {
          that.addFormat(that, data, callback);
				});
      },

			getFormatForEvent: function(eventId, callback) {
				var that = this;

        angular.forEach(this.formats, function(format) {
          angular.forEach(format.editions, function(edition) {
            angular.forEach(edition.events, function(event) {
              if (event.id == eventId) {
                callback(format);
              }
            });
          });
        });

				serverApi.getFormatByEvent(eventId, function(data) {
					that.addFormat(that, data, callback);
				});
			},

      getFormat: function(formatId, callback) {
        var that = this;
          serverApi.getFormat(formatId, function(data) {
            that.addFormat(that, data, callback);
          });
      },

      addFormat: function(that, data, callback) {
        storeTournamentFormat(data, newLoopIndex());
        var formatToReplace = -1;
        angular.forEach(that.formats, function(format, idx) {
          if (format.id == data.id) {
            formatToReplace = idx;
          }
        });
        if (formatToReplace != -1) {
          that.formats.splice(formatToReplace, 1);
        }
        that.formats.push(data);
        callback(data);
      },

			getEvent: function(eventId, callback) {
				var that = this;

				serverApi.getEvent(eventId, function(data) {
					storeEvent(data, newLoopIndex());
					callback(data);
				});
			},

			getEvents: function(basics, callback) {
				var that = this;
				callback(this.events);

				// make API call
				serverApi.getEvents(basics, function(data) {
					that.events = data;

					var loopIndex = newLoopIndex();

					// add some fields
					angular.forEach(data, function(event) {
						storeEvent(event, loopIndex);
					});

					callback(that.events);
				});
			},

			registerTeamForEdition: function(teamReg, divisionReg, callback, errorCallback) {
				serverApi.registerTeamForEdition(teamReg, divisionReg, function(newTeamReg) {
					divisionReg.registeredTeams.push(newTeamReg);
					callback(newTeamReg);
				}, errorCallback);
			},

      updateTeamRegStatus: function(event, teamReg, newStatus, callback, errorCallback) {
        var eventId = 0;
        if (!isEmpty(event)) {
          eventId = event.id;
        }
        var previousState = teamReg.status;
        teamReg.status = newStatus;
        serverApi.updateTeamRegistration(eventId, teamReg, function() {
          teamReg.version++;
          if (callback) {
            callback();
          }
        }, function() {
            teamReg.status = previousState;
            if (errorCallback) {
              errorCallback();
            }
        });
      },

      updateStandings: function(event, teamRegs, callback, errorCallback) {
        var eventId = 0;
        if (!isEmpty(event)) {
          eventId = event.id;
        }
        serverApi.updateTeamRegistrations(eventId, teamRegs, function() {
          angular.forEach(teamRegs, function(teamReg) {
            teamReg.version++;
          });
          callback();
        }, errorCallback);
      },

			saveTeam: function(team, callback, errorCallback) {
				var that = this;
				var oldTeam;
				if (team.id == -1) {
					oldTeam = null;
				} else {
					oldTeam = team;
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

					storeTeam(savedTeam, newLoopIndex());

          callback(savedTeam);
				}, errorCallback);
			},
	}

	function newLoopIndex() {
		return Math.floor(Math.random()*10000);
	}

	function storeWithoutLoops(element, loopIndex) {
		// to avoid loops only process every element once
		if (element == null || ('x' in element && element.x.loopIndex == loopIndex)) {
			return false;
		}

		element.x = {loopIndex: loopIndex};

		return true;
	}

	function storeTeam(team, loopIndex) {
		if (!storeWithoutLoops(team, loopIndex)) {
			return;
		}

		if (!isEmpty(team.emails)) {
			team.emails = team.emails.split(',');
		} else {
			team.emails = [];
		}

		angular.forEach(team.admins, function(admin, idx) {
			storeUser(admin, loopIndex);
			team.x.own = authorizer.getUser() != null && (team.x.own || admin.id == authorizer.getUser().id);
		});

		storeClub(team.club, loopIndex);

		angular.forEach(team.rosters, function(roster, idx) {
			storeRoster(roster, loopIndex);
		});
	}

	function storeRoster(roster, loopIndex) {
		if (!storeWithoutLoops(roster, loopIndex)) {
			return;
		}

		angular.forEach(roster.players, function(player, idx) {
			storePlayer(player.player, loopIndex);
		});
	}

	function storeUser(user, loopIndex) {
		if (!storeWithoutLoops(user, loopIndex)) {
			return;
		}

		storePlayer(user.dfvPlayer, loopIndex);
	}

	function storePlayer(player, loopIndex) {
		if (!storeWithoutLoops(player, loopIndex)) {
			return;
		}

		storeClub(player.club, loopIndex);

	}

	function storeClub(club, loopIndex) {
		if (!storeWithoutLoops(club, loopIndex)) {
			return;
		}

		storeAssociation(club.association, loopIndex);
	}

	function storeAssociation(association, loopIndex) {
		if (!storeWithoutLoops(association, loopIndex)) {
			return;
		}

		angular.forEach(association.admins, function(admin, idx) {
			storeUser(admin, loopIndex);
			association.x.own = authorizer.getUser() != null && (association.x.own || admin.id == authorizer.getUser().id);
		});

		storeUser(association.contact, loopIndex);
	}

	function storeContact(contact, loopIndex) {
		if (!storeWithoutLoops(contact, loopIndex)) {
			return;
		}
	}

	function storeEvent(event, loopIndex) {
		if (!storeWithoutLoops(event, loopIndex)) {
			return;
		}

		storeTournamentEdition(event.tournamentEdition, loopIndex);

		storeContact(event.localOrganizer, loopIndex);

		// get main location
		event.x.mainLocation = {};
		angular.forEach(event.locations, function(location) {
			if (location.main || isEmpty(event.x.mainLocation)) {
				event.x.mainLocation = location;
			}
		});

		angular.forEach(event.admins, function(admin, idx) {
			storeUser(admin, loopIndex);
			event.x.own = authorizer.getUser() != null && (event.x.own || admin.id == authorizer.getUser().id);
		});

		event.x.own = authorizer.getUser() != null && (event.x.own ||  event.tournamentEdition.tournamentFormat.x.own);

		event.x.isSingleEvent = event.tournamentEdition.isSingleEdition;
		event.x.hasLocalOrganizer = !isEmpty(event.localOrganizer) && !isEmpty(event.localOrganizer.name) && event.tournamentEdition.organizer.id != event.localOrganizer.id;
		event.x.hasFees = (event.tournamentEdition.fees && event.tournamentEdition.fees.length > 0) || (event.fees && event.fees.length > 0);

		var today = moment();
		if (moment(event.startDate).isAfter(today)) {
			event.x.timing = 'future';
		} else if (moment(event.endDate).isBefore(today)) {
			event.x.timing = 'past';
		} else {
			event.x.timing = 'running';
		}

		// assign the divisions (and maybe teams) that play this event
		event.x.divisions = [];

		if ('divisionConfirmations' in event && !isEmpty(event.divisionConfirmations)) {
      event.x.usesDivisionConfirmations = true;
			angular.forEach(event.divisionConfirmations, function(divisionConfirmation) {
				var division = angular.copy(divisionConfirmation.divisionRegistration);

				// if individual assignment is set to false, all teams/players of the registration will play
				if (divisionConfirmation.individualAssignment) {
					division.playingTeams = divisionConfirmation.teams;
				} else {
					// we take all teams of the divisionRegistration
					division.playingTeams = division.registeredTeams;
				}
				event.x.divisions.push(angular.copy(division));
			});
		} else {
			// this event gets all divisions and teams from the edition
      event.x.usesDivisionConfirmations = false;
			if (isEmpty(event.tournamentEdition.divisionRegistrations)) {
				event.x.divisions = [];
			} else {
				event.x.divisions = angular.copy(event.tournamentEdition.divisionRegistrations);
				angular.forEach(event.x.divisions, function(division) {
					division.playingTeams = division.registeredTeams;
				});

			}
		}
	}

	function storeTournamentEdition(edition, loopIndex) {
		if (!storeWithoutLoops(edition, loopIndex)) {
			return;
		}

		angular.forEach(edition.events, function(event, idx) {
			event.tournamentEdition = edition;
			storeEvent(event, loopIndex);
		});

		storeTournamentFormat(edition.tournamentFormat, loopIndex);

		storeContact(edition.organizer, loopIndex);

    edition.x.latestEvent = {};
    edition.x.eventsPerDivision = {};
		angular.forEach(edition.divisionRegistrations, function(divReg, idx) {
			storeDivReg(divReg, loopIndex);
      edition.x.latestEvent[divReg.id] = {};
      edition.x.eventsPerDivision[divReg.id] = [];
		});

		edition.x.isSingleEdition = edition.events != null && edition.events.length == 1;
    angular.forEach(edition.events, function(event) {
      event.x.isSingleEvent = edition.x.isSingleEdition;
    });

		var todayDateString = moment().format('YYYY-MM-DD');

		edition.x.registrationIsOpen = !isEmpty(edition.registrationStart) && edition.registrationStart  <= todayDateString && edition.registrationEnd >= todayDateString;
		edition.x.registrationTime = isEmpty(edition.registrationStart) ? 'never' : (edition.registrationStart > todayDateString ? 'future' : 'past');

    // find and store all events for each division
    angular.forEach(edition.events, function(event) {
      angular.forEach(event.x.divisions, function(div) {
        edition.x.eventsPerDivision[div.id].push(event);
      });
    });

    // find the last event of each division
    edition.x.lastestEventPerDivision = {};
    angular.forEach(edition.x.eventsPerDivision, function(div, divId) {
      edition.x.lastestEventPerDivision[divId] = {startDate: '1900-01-01'};
      angular.forEach(div, function(event) {
        if (event.startDate > edition.x.lastestEventPerDivision[divId].startDate) {
          edition.x.lastestEventPerDivision[divId] = event;
        }
      });
    });
  }

	function storeDivReg(divReg, loopIndex) {
		// divRegs are unique to editions

		angular.forEach(divReg.registeredTeams, function(teamReg, idx) {
			storeTeamRegistration(teamReg, loopIndex);
		});
	}

	function storeTeamRegistration(teamReg, loopIndex) {
		storeRoster(teamReg.roster, loopIndex);
	}

	function storeTournamentFormat(format, loopIndex) {
		if (!storeWithoutLoops(format, loopIndex)) {
			return;
		}

		angular.forEach(format.admins, function(admin, idx) {
			storeUser(admin, loopIndex);
			format.x.own = authorizer.getUser() != null && (format.x.own || admin.id == authorizer.getUser().id);
		});

		angular.forEach(format.editions, function(edition, idx) {
			edition.tournamentFormat = format;
			storeTournamentEdition(edition, loopIndex);
		});

		storeAssociation(format.association, loopIndex);


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
			name: '',
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
			divisionRegistrations: [],
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
