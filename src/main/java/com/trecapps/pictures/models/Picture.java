package com.trecapps.pictures.models;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Data
@Table
@Entity
@javax.persistence.Entity
public class Picture {

    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    String id;

    String name;

    String userId;

    byte contentRestriction;

    String extension;
}
