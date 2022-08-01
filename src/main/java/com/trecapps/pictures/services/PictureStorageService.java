package com.trecapps.pictures.services;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class PictureStorageService {

    BlobServiceClient client;

    @Autowired
    PictureStorageService(@Value("${picture.storage.account-name}") String name,
                       @Value("${picture.storage.account-key}") String key,
                       @Value("${picture.storage.blob-endpoint}") String endpoint,
                       Jackson2ObjectMapperBuilder objectMapperBuilder)
    {
        AzureNamedKeyCredential credential = new AzureNamedKeyCredential(name, key);
        client = new BlobServiceClientBuilder().credential(credential).endpoint(endpoint).buildClient();
//        objectMapper = objectMapperBuilder.createXmlMapper(false).build();
//        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    void uploadPicture(String base64Value, String name, String extension)
    {
        BlobContainerClient containerClient = client.getBlobContainerClient("trec-apps-pictures");

        BlobClient client = containerClient.getBlobClient(String.format("%s.%s", name, extension));

        byte[] picture = Base64.getDecoder().decode(base64Value);

        client.upload(BinaryData.fromBytes(picture));
    }

    String retrieveImage(String name, String extension)
    {
        BlobContainerClient containerClient = client.getBlobContainerClient("trec-apps-pictures");

        BlobClient client = containerClient.getBlobClient(String.format("%s.%s", name, extension));

        return Base64.getEncoder().encodeToString(client.downloadContent().toBytes());
    }
}
