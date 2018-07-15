package com.aws.hack.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * author: vyl
 * date: 14/07/2018
 */
@Data
@Entity
public class Preference {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String preference;

    public Preference() {
    }

    public Preference(String preference) {
        this.preference = preference;
    }
}
