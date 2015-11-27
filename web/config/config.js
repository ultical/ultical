//Do not change values in this file! Use config.js (config.js.dist).

var CONFIG_OBJECT = {
		// put values that are to be used in the config (e.g. color definitions, ...)
};

angular.extend(CONFIG_OBJECT, {
	// put default values for the app
	general: {
		// list of two-letter country codes
		availableLanguages: ['DE'],
		// can be any two-letter country code or 'auto'
		defaultLanguage: 'de',
	},
	
	api: {
		hostname: 'http://localhost:8765',
	},


});
