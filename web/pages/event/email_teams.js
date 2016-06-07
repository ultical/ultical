'use strict';

angular.module('ultical.events')

.controller('EmailToUsersCtrl', ['$scope', 'serverApi', 'authorizer', '$rootScope', 'alerter', '$translate', '$filter',
  function($scope, serverApi, authorizer, $rootScope, alerter, $translate, $filter) {

    $scope.emailSendPending = false;
    $rootScope.emailSendDisabled = false;

    $scope.statusList = ['PENDING', 'CONFIRMED', 'WAITING_LIST', 'DECLINED', 'CANCELLED'];

    // initialize with default values
    $scope.email = {};
    $scope.email.divisions = $scope.divisionsToShow;
    $scope.email.status = ['CONFIRMED'];
    $scope.email.replyTo = authorizer.getUser().email;

    $rootScope.sendEmail = function() {
      if (isEmpty($scope.email.body)) {
        alerter.error('email.empty', '', {container: '#email-modal-error', duration: 5});
        return;
      }

      var dataToSend = angular.copy($scope.email);

      dataToSend.divisions = [];
      angular.forEach($scope.email.divisions, function(division) {
        dataToSend.divisions.push({id: division.id});
      });

      var tournamentName = '';
      if ($scope.show.edition) {
        dataToSend.editionId = $scope.edition.id;
        tournamentName = $filter('editionname')($scope.edition);
      } else if ($scope.show.event) {
        dataToSend.eventId = $scope.event.id;
        tournamentName = $filter('eventname')($scope.event);
      }
      dataToSend.subject = $translate.instant('email.subjectFrom', { senderName: tournamentName });
      dataToSend.authorDescriptionText = $translate.instant('email.authorDescriptionText', { authorName: authorizer.getUser().fullName });

      serverApi.sendEmailToTeams(dataToSend, function() {
        alerter.success('email.successTitle', 'email.successText', {duration: 10});
        $scope.$hide();
      }, function() {
        alerter.error('general.error', 'email.failure', {container: '#email-modal-error'});
      });
    }
  }
]);
