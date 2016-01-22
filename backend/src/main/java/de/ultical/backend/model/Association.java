package de.ultical.backend.model;

import java.util.List;

import lombok.Data;

@Data
public class Association {
    private String name;
    private int id;
    private String acronym;

    private Contact contact;

    private List<User> admins;
}
