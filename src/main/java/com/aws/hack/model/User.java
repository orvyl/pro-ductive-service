package com.aws.hack.model;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * author: vyl
 * date: 14/07/2018
 */
@Data
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;

    @OneToMany
    private List<Preference> preferences = new ArrayList<>();

    @ElementCollection
    private List<String> tags = new ArrayList<>();

    @OneToMany
    private List<Story> bookmarks = new ArrayList<>();

    public List<String> getStringPreference() {
        List<String> list = new ArrayList<>();
        preferences.forEach(preference -> list.add(preference.getPreference()));

        return list;
    }

    public User() {
    }

    public User(String name) {
        this.name = name;
    }
}
