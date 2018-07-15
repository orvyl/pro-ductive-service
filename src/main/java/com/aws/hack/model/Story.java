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
public class Story {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String title;

    @Column(length = 2000)
    private String content;
    private String link;

    @Column(length = 2000)
    private String image;
    private String localImagePath;
    private int vote;
    private String source;

    @ElementCollection
    private List<String> tags = new ArrayList<>();

    public Story() {
    }

    public Story(String title, String link) {
        this.title = title;
        this.link = link;
    }

    public Story(String title, String link, String image) {
        this.title = title;
        this.link = link;
        this.image = image;
    }
}
