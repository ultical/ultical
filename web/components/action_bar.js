'use strict';

//map api service
app.factory('actionBar', ['authorizer', '$rootScope', function(authorizer, $rootScope) {

  $rootScope.$on('$stateChangeStart', function(event, next, current) {
      clearActions();
  });

  function clearActions() {
    actions.splice(0, actions.length);
  }

  // HEAD section
  var head = {};

  head.loggedIn = function() {
    return authorizer.loggedIn();
  }

  head.getUser = function() {
    return authorizer.getUser();
  }

  // ACTIONS section
  var actions = [];

  return {
    getHead: function() {
      return head;
    },
    getActions: function() {
      return actions;
    },
    addSeparator: function(id) {
      actions.push({id:id, separator: true, show: function() { return true;}});
    },
    addAction: function(action) {
      actions.push(action);
    },
    removeAction: function(actionId) {
      var indexToRemove = -1;
      angular.forEach(actions, function(action, idx) {
        if (action.id == actionId) {
          indexToRemove = idx;
        }
      });
      if (indexToRemove != -1) {
        actions.splice(indexToRemove, 1);
      }
    },
    clearActions: function() {
      clearActions();
    }
  }
}]);
