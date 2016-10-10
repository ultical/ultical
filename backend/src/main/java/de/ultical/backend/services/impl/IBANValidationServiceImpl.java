package de.ultical.backend.services.impl;

import de.ultical.backend.exception.IBANValidationException;
import de.ultical.backend.services.IBANValidationService;

public class IBANValidationServiceImpl implements IBANValidationService {

    public static enum IBANCountryEnum {
	DE("DE",22),AT("AT",20),CH("CH",21),FR("FR",27),LU("LU",20),BE("BE",16),NL("NL",18),GB("GB",22),DK("DK",18),PL("PL",28),CZ("CZ",24);

	private final String countryCode;
	private final int length;
	
	IBANCountryEnum(final String cc, final int length) {
	    this.countryCode = cc;
	    this.length = length;
	}

	public String getCountryCode() {
	    return this.countryCode;
	}

	public int getLength() {
	    return this.length;
	}
    }
    
    private final static String IBAN_LENGTH_MISMATCH_MESSAGE = "IBAN-length does not match country code";
    
    public void validateIBAN(final String iban) {
	if (iban == null) {
	    throw new IBANValidationException("NULL is not a valid IBAN");
	}
	this.checkIbanLength(iban);
	//TODO validate checksum
    }

    private void checkIbanLength(final String iban) {
	boolean isValid = false;
	for (IBANCountryEnum country : IBANCountryEnum.values()) {
	    if (iban.startsWith(country.getCountryCode())) {
		if( iban.length() != country.getLength()) {
		    throw new IBANValidationException(IBAN_LENGTH_MISMATCH_MESSAGE);
		} else {
		    isValid = true;
		    break;
		}
	    }
	}
	if (!isValid && iban.length() < 15 || iban.length() > 34) {
	    throw new IBANValidationException(IBAN_LENGTH_MISMATCH_MESSAGE);
	} 
    }
}
