'use strict';

angular.module('ultical.services', [])

.controller('EmailServiceCtrl', ['$scope', 'CONFIG', 'serverApi', 'authorizer', '$rootScope', 'alerter', '$translate', '$filter',
  function($scope, CONFIG, serverApi, authorizer, $rootScope, alerter, $translate, $filter) {

    $scope.emailSendPending = false;
    $rootScope.emailSendDisabled = false;

    // initialize with default values
    $scope.email = {};
    var authorName = '';
    if (authorizer.loggedIn()) {
      $scope.email.replyTo = authorizer.getUser().email;
      $scope.notLoggedIn = false;
      authorName = authorizer.getUser().fullName;
    } else {
      $scope.email.replayTo = '';
      $scope.notLoggedIn = true;
      $scope.captchaKey = CONFIG.captcha.publicKey;
    }

    var receiverName = '';

    if ($scope.mailToEvent) {
      var eventName = $filter('eventname')($scope.event);
      $scope.descriptionText = $translate.instant('email.descriptionEvent', {eventName: eventName } );
      $scope.email.eventId = $scope.event.id;
      $scope.email.subject = $translate.instant('email.subjectTo', { receiverName: eventName });
    } else if ($scope.mailToTeam) {
      var teamName = $scope.team.name;
      $scope.descriptionText = $translate.instant('email.descriptionTeam', {teamName: teamName } );
      $scope.email.teamId = $scope.team.id;
      $scope.email.subject = $translate.instant('email.subjectTo', { receiverName: teamName });
    }

    $rootScope.sendEmail = function() {
      if (!authorizer.loggedIn()) {
        if (isEmpty($scope.email.name)) {
          alerter.error('email.emptyName', '', {container: '#email-modal-error', duration: 5});
          return;
        }
        if (isEmpty($scope.email.captcha)) {
          alerter.error('email.emptyCaptcha', '', {container: '#email-modal-error', duration: 5});
          return;
        }
        authorName = $scope.email.name;
      }

      $scope.email.authorDescriptionText = $translate.instant('email.authorDescriptionText', { authorName: authorName });

      if (isEmpty($scope.email.replyTo)) {
        alerter.error('email.emptyReplyTo', '', {container: '#email-modal-error', duration: 5});
        return;
      }

      if (isEmpty($scope.email.body)) {
        alerter.error('email.empty', '', {container: '#email-modal-error', duration: 5});
        return;
      }

      serverApi.sendEmail($scope.email, successCallback, errorCallback);
    }

    var successCallback = function() {
      alerter.success('email.successTitle', 'email.successText', {duration: 10});
      $scope.$hide();
    };

    var errorCallback = function(errorType) {
      if (errorType == 'captcha') {
        alerter.error('email.errorCaptcha', 'email.failure', {container: '#email-modal-error'});
      } else {
        alerter.error('general.error', 'email.failure', {container: '#email-modal-error'});
      }
    }

  }
]);
