package de.ultical.backend.services;

import de.ultical.backend.exception.IBANValidationException;

public interface IBANValidationService {

    void validateIBAN(final String iban) throws IBANValidationException;
}
