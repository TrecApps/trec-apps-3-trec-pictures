package com.trecapps.pictures.repos;

import com.trecapps.pictures.models.Picture;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface PictureRepo extends JpaRepository<Picture, String>{
}
