package com.trecapps.pictures.models;

import lombok.Data;

import java.util.List;

@Data
public class PicturePermissions {

    boolean isPublic;

    // List of entities whom have access to this picture
    List<String> userId, brandId;
}
