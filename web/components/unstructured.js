// ultical Copyright (C) 2016 ultical developers
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published
// the Free Software Foundation; either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful
// but WITHOUT ANY WARRANTY; without even the implied warranty
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public
// along with this program; if not, write to the Free Software Foundation,
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
// 

function isEmpty(obj) {
	if (undefined === obj || obj == null) {
		return true;
	}
	if (!angular.isObject(obj)) {
		switch (typeof obj) {
		case 'string':
			return isEmptyString(obj);
			break;
		case 'number':
			return obj == 0;
			break;
		}
	}
	if (angular.isArray(obj)) {
		return obj.length == 0;
	} else {
		for (var prop in obj) {
			return false;
		}
	}
	return true;
}

function isEmptyString(str) {
	return undefined === str || null === str || str.length == 0 || str.trim() == '';
}

function stringStartsWith (string, prefix) {
    return string.slice(0, prefix.length) == prefix;
}

if (!Date.now) {
    Date.now = function() { return new Date().getTime(); }
}

var allCurrencies = ['ALL','AFN','ARS','AWG','AUD','AZN','BSD','BBD','BYR','BZD','BMD','BOB','BAM','BWP','BGN','BRL','BND','KHR','CAD','KYD','CLP','CNY','COP','CRC','HRK','CUP','CZK','DKK','DOP','XCD','EGP','SVC','EUR','FKP','FJD','GHS','GIP','GTQ','GGP','GYD','HNL','HKD','HUF','ISK','INR','IDR','IRR','IMP','ILS','JMD','JPY','JEP','KZT','KPW','KRW','KGS','LAK','LBP','LRD','MKD','MYR','MUR','MXN','MNT','MZN','NAD','NPR','ANG','NZD','NIO','NGN','KPW','NOK','OMR','PKR','PAB','PYG','PEN','PHP','PLN','QAR','RON','RUB','SHP','SAR','RSD','SCR','SGD','SBD','SOS','ZAR','KRW','LKR','SEK','CHF','SRD','SYP','TWD','THB','TTD','TRY','TVD','UAH','GBP','USD','UYU','UZS','VEF','VND','YER','ZWD'];
var prioCurrencies = ['EUR', 'USD', 'GBP', 'CHF', 'DKK', 'PLN', 'CAD', 'AUD', 'NZD', 'RUB', 'TRY', 'CNY'];
