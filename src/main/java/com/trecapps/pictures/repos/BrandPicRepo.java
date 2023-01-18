package com.trecapps.pictures.repos;

import com.trecapps.pictures.models.BrandProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandPicRepo extends JpaRepository<BrandProfile, String> {
}
