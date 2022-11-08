package com.trecapps.pictures.services;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.trecapps.pictures.models.PicturePermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class PictureStorageService {

    BlobServiceClient client;

    ObjectMapper objectMapper;

    @Autowired
    PictureStorageService(@Value("${picture.storage.account-name}") String name,
                       @Value("${picture.storage.account-key}") String key,
                       @Value("${picture.storage.blob-endpoint}") String endpoint,
                       Jackson2ObjectMapperBuilder objectMapperBuilder)
    {
        AzureNamedKeyCredential credential = new AzureNamedKeyCredential(name, key);
        client = new BlobServiceClientBuilder().credential(credential).endpoint(endpoint).buildClient();
        objectMapper = objectMapperBuilder.createXmlMapper(false).build();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    void uploadPicture(String base64Value, String name, String extension)
    {
        uploadPicture(base64Value, name, extension, true);
    }

    void uploadPicture(String base64Value, String name, String extension, boolean isPublic)
    {
        BlobContainerClient containerClient = client.getBlobContainerClient("trec-apps-pictures");

        BlobClient client = containerClient.getBlobClient(String.format("%s.%s", name, extension));

        byte[] picture = Base64.getDecoder().decode(base64Value);

        client.upload(BinaryData.fromBytes(picture));



        PicturePermissions permissions = new PicturePermissions();
        permissions.setPublic(true);
        uploadPermissions(permissions, name);

    }

    void uploadPermissions(PicturePermissions permissions, String name)
    {
        BlobContainerClient containerClient = client.getBlobContainerClient("trec-apps-pictures");
        BlobClient client = containerClient.getBlobClient(String.format("%s.json", name));
        try {
            client.upload(BinaryData.fromString(objectMapper.writeValueAsString(permissions)), true);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    String retrieveImage(String name, String extension)
    {
        BlobContainerClient containerClient = client.getBlobContainerClient("trec-apps-pictures");

        BlobClient client = containerClient.getBlobClient(String.format("%s.%s", name, extension));

        return Base64.getEncoder().encodeToString(client.downloadContent().toBytes());
    }

    byte[] retrieveImage(String file)
    {
        BlobContainerClient containerClient = client.getBlobContainerClient("trec-apps-pictures");

        BlobClient client = containerClient.getBlobClient(file);
        return client.downloadContent().toBytes();
    }

    PicturePermissions retrievePermissions(String name)
    {
        BlobContainerClient containerClient = client.getBlobContainerClient("trec-apps-pictures");

        BlobClient client = containerClient.getBlobClient(String.format("%s.json", name));

        try {
            return objectMapper.readValue(client.downloadContent().toString(), PicturePermissions.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
