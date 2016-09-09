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
    removeActionGroup: function(groupId) {
      var indicesToRemove = [];
      angular.forEach(actions, function(action, idx) {
        if (action.group == groupId) {
          indicesToRemove.push(idx);
        }
      });
      for (var i = indicesToRemove.length - 1; i >= 0; i--) {
        actions.splice(indicesToRemove[i], 1);
      }
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
    },
    showAction: function(action) {
      if (action.needLogIn === undefined || action.needLogIn == null) {
        return true;
      } else {
        return (!action.needLogIn && !authorizer.loggedIn()) || (action.needLogIn && authorizer.loggedIn());
      }
    },
  }
}]);
