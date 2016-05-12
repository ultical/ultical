'use strict';

angular.module('ultical.services', [])

.controller('EmailServiceCtrl', ['$scope', 'serverApi', 'authorizer', '$rootScope', 'alerter', '$translate', '$filter',
  function($scope, serverApi, authorizer, $rootScope, alerter, $translate, $filter) {

    $scope.emailSendPending = false;
    $rootScope.emailSendDisabled = false;

    // initialize with default values
    $scope.email = {};
    $scope.email.replyTo = authorizer.getUser().email;

    var receiverName = '';

    if ($scope.mailToEvent) {
      var eventName = $filter('eventname')($scope.event);
      $scope.descriptionText = $translate.instant('email.descriptionEvent', {eventName: eventName } );
      $scope.email.eventId = $scope.event.id;
      $scope.email.subject = $translate.instant('email.subjectTo', { receiverName: eventName });
    }

    $scope.email.authorDescriptionText = $translate.instant('email.authorDescriptionText', { authorName: authorizer.getUser().fullName });

    $rootScope.sendEmail = function() {
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

    var errorCallback = function() {
      alerter.error('general.error', 'email.failure', {container: '#email-modal-error'});
    }

  }
]);
