package com.trecapps.pictures.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table
@Entity
@javax.persistence.Entity
public class BrandProfile {
    @Id
    String userId;

    String pictureId;
}
