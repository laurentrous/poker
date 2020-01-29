package com.wcs.poker.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Card {

    @Id
    private Long id;
    private String color;
    private String name;
    private int value;


    public Card() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
