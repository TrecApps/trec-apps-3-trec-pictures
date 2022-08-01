package com.trecapps.pictures.models;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table
@Entity
public class Profile {
    @Id
    String userId;

    String pictureId;
}
