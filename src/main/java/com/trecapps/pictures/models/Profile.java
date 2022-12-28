package com.trecapps.pictures.models;

import lombok.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Data
@Table
@Entity
@javax.persistence.Entity
public class Profile {
    @Id
    String userId;

    String pictureId;
}
