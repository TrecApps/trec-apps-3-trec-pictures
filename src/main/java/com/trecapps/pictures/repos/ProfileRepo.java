package com.trecapps.pictures.repos;

import com.trecapps.pictures.models.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepo extends JpaRepository<Profile, String> {
}
