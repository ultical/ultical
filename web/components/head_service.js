/*
 * ultical Copyright (C) 2016 ultical developers
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

'use strict';

var app = angular.module('ultical');

app.factory('headService', ['$translate', function($translate) {
  var baseTitle = $translate.instant('general.pageTitle');
  var pageTitle = '';
  var pageTitleOptions = {};
  return {
  	getTitle: function() {
  			var returnTitle = '';
  			if (!isEmpty(pageTitle)) {
  				returnTitle += $translate.instant(pageTitle, pageTitleOptions) + ' - ';
  			}
  			return returnTitle + baseTitle;
  	},
    setTitle: function(newTitle, newTitleOptions) {
      pageTitle = newTitle;
      pageTitleOptions = newTitleOptions;
    },
  };
}]);
