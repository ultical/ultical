'use strict';

app.factory('serverApi', ['CONFIG', '$http', 'Base64', 'authorizer', '$filter', 
                          function(CONFIG, $http, Base64, authorizer, $filter) {

	function get(resource, successCallback, errorCallback, includeHeader) {
		return doHttp(resource, 'GET', null, successCallback, errorCallback, includeHeader);
	}

	function post(resource, data, successCallback, errorCallback, includeHeader) {
		doHttp(resource, 'POST', data, successCallback, errorCallback, includeHeader);
	}

	function put(resource, data, successCallback, errorCallback, includeHeader) {
    var dataToSend = angular.copy(data);
    if ('x' in dataToSend) {
      delete dataToSend.x;
    }
		doHttp(resource, 'PUT', dataToSend, successCallback, errorCallback, includeHeader);
	}

	function del(resource, successCallback, errorCallback, includeHeader) {
		doHttp(resource, 'DELETE', null, successCallback, errorCallback, includeHeader);
	}

	function doHttp(resource, method, data, successCallback, errorCallback, includeHeader) {
		var config = {
				method: method,
				url: CONFIG.api.hostname + '/' + resource,
		};

		if (authorizer.loggedIn()) {
			config.headers = { Authorization: 'Basic ' + Base64.encode(authorizer.getUser().email + ':' + authorizer.getUser().password)};
		}

		if (method != 'GET' || method != 'DELETE') {
			config.data = data;
		}

		if (CONFIG.debug) {
			console.log("API Request", config.method, config.url, config.data);
		}

		var defaultTransform = angular.isArray($http.defaults.transformResponse) ? $http.defaults.transformResponse[0] : $http.defaults.transformResponse;

		var jsogTransform = function(data) {
			try {
				return JSOG.parse(data);
			} catch(e) {
				return data;
			}
		}

		config.transformResponse =  [jsogTransform, defaultTransform];

		return $http(config).then(
				function (response) {
					if (CONFIG.debug) {
						console.log("API Response", response.data);
					}
					// success callback
					return callCallback(successCallback, response, includeHeader);
				}, function (response) {
					if (CONFIG.debug) {
						console.log("API fail", response);
					}
					// error callback
					return callCallback(errorCallback, response, includeHeader);
				}
		);
	}

	function callCallback(callback, response, includeHeader) {
		if (undefined === callback) {
			callback = function() {};
		}
		if (undefined === includeHeader) {
			includeHeader = false;
		}

		if (includeHeader) {
			// send complete response
			return callback(response);
		} else {
			// only send payload data
			return callback(response.data);
		}
	}

	return {
		getEvent: function(eventId, callback) {
			get('events/' + eventId, callback);
		},

		getEvents: function(callback) {
			get('events', callback);
		},

		getSeasons: function(callback) {
			get('season', callback);
		},

    getContexts: function(callback) {
      get('context', callback);
    },

		postRoster: function(roster, teamId, callback, errorCallback) {
			roster.team = { id: teamId };
			post('roster', roster, callback, errorCallback);
		},

    putRoster: function(roster, team, callback, errorCallback) {
      roster.team = { id: team.id };
			put('roster', roster, callback, errorCallback);
		},

		deleteRoster: function(roster, callback, errorCallback) {
			del('roster/' + roster.id, callback, errorCallback);
		},

		getRosterBlockingDate: function(rosterId, callback) {
			get('roster/' + rosterId + '/blocking', callback);
		},

		getAllClubs: function(callback) {
			get('club/all', callback);
		},

		getTeam: function(teamId, callback) {
			get('teams/' + teamId, callback);
		},

		getAllTeams: function(callback) {
			get('teams', callback);
		},

		getOwnTeams: function(callback) {
			get('teams/own', callback);
		},

		getFormatByEvent: function(eventId, callback) {
			get('format/event/' + eventId, callback);
		},

		saveTeam: function(team, oldTeam, callback, errorCallback) {
			var teamToSend = angular.copy(team);

			teamToSend.rosters = [];

			// prevent bad requests if the backend tries to parse a string into a location objects
			if (!angular.isObject(team.club)) {
				teamToSend.club = null;
			} else {
				delete(teamToSend.club.x);
				delete(teamToSend.club.association);
			}

			var emailsString = '';
			angular.forEach(teamToSend.emails, function(email, idx) {
				emailsString += email;
				if (idx != team.emails.length - 1) {
					emailsString += ',';
				}
			});
			teamToSend.emails = emailsString;

			angular.forEach(teamToSend.admins, function(admin, idx) {
				teamToSend.admins[idx] = { id: admin.id };
			});

			var that = this;

			// delete properties added for frontend
			delete(teamToSend.x);

			if (teamToSend.id == -1) {
				// this is a team newly created
				post('teams', teamToSend, function(newTeam) {
					that.getTeam(newTeam.id, callback, errorCallback);
				});

			} else {
				put('teams/' + teamToSend.id, teamToSend, function() {
					that.getTeam(teamToSend.id, callback, errorCallback);
				});
			}
		},

		registerUser: function(user, callback) {
			post('register', user, callback);
		},

		changePasswordWithMailCode : function(code, user, callback) {
			var basicUser = { id: user.id, password: user.password};
			post('mail/' + code, basicUser, callback);
		},

		resendConfirmationEmail: function(loginData, callback) {
			post('mail/resend/confirmation', loginData, callback);
		},

		resendOptInEmail: function(loginData, callback) {
			post('mail/resend/optin', loginData, callback);
		},

		sendForgotPasswordEmail: function(loginData, callback) {
			post('mail/resend/password', loginData, callback);
		},

		redeemMailCode: function(code, callback, errorCallback) {
			get('mail/' + code, callback, errorCallback);
		},

		login: function(user, callback) {
			post('auth', user, callback);
		},

		getUserProposals: function(userName, callback) {
			return get('users?search=' + $filter('urlEncode')(userName), callback);
		},

		getPlayerProposals: function(playerName, callback) {
			return get('dfvmvname?search=' + $filter('urlEncode')(playerName), callback);
		},

		removePlayerFromRoster: function(player, roster, callback, errorCallback) {
			return del('roster/' + roster.id + '/player/' + player.id, callback, errorCallback);
		},

		addPlayerToRoster: function(player, roster, callback, errorCallback) {
			var requestPlayer = { lastName: player.lastName, firstName: player.firstName, dse: player.dse, dfvNumber: player.dfvNumber };
			return post('roster/' + roster.id, requestPlayer, callback, errorCallback);
		},

		registerTeamForEdition: function(registration, divisionRegistration, callback) {
			return post('tournaments/' + divisionRegistration.id + '/register/team', registration, callback);
		},
	};

	function addAdminsToTeam(teamId, addList, callback) {
		var postCounter = 0;
		angular.forEach(addList, function(adminToAdd) {
			post('teams/' + teamId + '/admin/' + adminToAdd.id, {}, function() {
				postCounter++;
				if (postCounter == addList.length) {
					callback();
				}
			});
		});
	}

	function deleteAdminsFromTeam(teamId, deleteList, callback) {
		var deleteCounter = 0;
		angular.forEach(deleteList, function(adminToDelete) {
			del('teams/' + teamId + '/admin/' + adminToDelete.id, function() {
				deleteCounter++;
				if (deleteCounter == deleteList.length) {
					callback();
				}
			});

		});
	}
}]);