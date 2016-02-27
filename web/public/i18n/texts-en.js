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

if (undefined === TRANSLATIONS) {
	var TRANSLATIONS = {};
}
TRANSLATIONS['en'] = {

		general: {
			pageTitle: 'DFV-Tournaments',
			dateFormat : "dd.MM.yyyy",
			dateFormatShort: "dd.MM.yy",
			datetimeFormat : "dd.MM.yy - HH:mm",
			na: 'Noch keine Informationen',
			close: 'schließen',
			email: 'Email',
			phone: 'Telefon',
			optional: 'optional',
			save: 'Speichern',
			cancel: 'Abbrechen',
			create: 'Erstellen',
			done: 'Fertig',
			currencyFormat: '{{ currencySymbol }} {{ amount }}',
			decimalSeparator: '.',
	},

		nav: {
			titleFlipText: 'Deutscher Frisbeesport Verband e.V.',
			login: 'Login',
			loginFail: {
				wrongCredentials: 'Email oder Passwort fehlerhaft!',
				wrongCredentialsAction: 'Passwort vergessen?',
				emailNotConfirmed: 'Email Adresse noch nicht bestätigt!',
				emailNotConfirmedAction: 'Bestätigungsemail erneut senden?',
				dfvEmailNotOptIn: 'Die Mail an die beim DFV hinterlegte Emailadresse wurde noch nicht bestätigt!',
				dfvEmailNotOptInAction: 'Mail erneut senden?',
				loginFail: 'Felher bei der Anmeldung',
			},
			loginEmailActions: {
				successTitle: 'Email erfolgreich gesendet!',
				passwordReset: 'Weitere Anweisungen, wie das Passwort zurückgesetzt werden kann findest du in der Email',
				confirmationEmailSendContent: 'Bitte bestätige den Erhalt der Email mit dem enthaltenen Link. Danach kannst du dich einloggen.',
			},
			register: 'Registrieren',
			eventDropdown: {
				label: 'Tournaments',
				newEvent: 'Neues Turnier',
				listEvents: 'Turniere anzeigen',
			},
			teams: 'Teams',
			profileDropdown: {
				ownTeams: 'My Teams',
				ownEvents: 'My Tournaments',
				logout: 'Log out',
			},
		},
};
