//Do not change values in this file! Use config.js (config.js.dist).

var CONFIG_OBJECT = {
		// put values that are to be used in the config (e.g. color definitions, ...)
};

angular.extend(CONFIG_OBJECT, {
	// put default values for the app
	general: {
		// list of two-letter country codes
		availableLanguages: ['DE'],
		// map availableLanguages to its name (in this language, so it can be found
		availableLanguagesFull: ['deutsch', 'spanisch'],
		// can be any two-letter country code or 'auto'
		defaultLanguage: 'de',
	},

	registration: {
		minPasswordLength: 10,
	},

	api: {
		hostname: 'http://localhost:8765',
	},

	debug: true,

	mapBox: {
		accessToken: '',
		// defines an anchor point (cooridnates) for location search (center of germany: 51.16, 10.45)
		proximity: '51.16,10.45',
	}


});
