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
