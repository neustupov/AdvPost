package ru.neustupov.advpost.service.s3;

import java.io.InputStream;
import java.util.List;

public interface S3Service {

    String uploadFile(String objectName, InputStream inputStream, String contentType);

    InputStream getFileAsInputStream(String filename);

    void deleteUnusedImage(List<String> fileList);
}
