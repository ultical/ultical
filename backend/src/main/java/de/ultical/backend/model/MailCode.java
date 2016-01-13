package de.ultical.backend.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MailCode {
    private MailCodeType type;
    private User user;
    private String code;
    private LocalDateTime timeCreated;
}
