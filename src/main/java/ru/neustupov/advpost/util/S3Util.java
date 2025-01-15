package ru.neustupov.advpost.util;

import org.springframework.stereotype.Component;
import ru.neustupov.advpost.service.s3.S3Service;

import java.io.InputStream;

@Component
public class S3Util {

    private final S3Service s3Service;

    public S3Util(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    public String upload(String objectName, InputStream inputStream, String contentType) {
        return s3Service.uploadFile(objectName, inputStream, contentType);
    }

    public InputStream download(String objectName) {
        return s3Service.getFileAsInputStream(objectName);
    }
}
