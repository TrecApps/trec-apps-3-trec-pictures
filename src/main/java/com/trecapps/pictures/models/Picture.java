package com.trecapps.pictures.models;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table
@Entity
public class Picture {

    @Id
    String id;

    String name;

    String userId;

    byte contentRestriction;

    String extension;
}
