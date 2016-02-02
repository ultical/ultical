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