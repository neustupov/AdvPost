package ru.neustupov.advpost.service.s3;

import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
public class S3ServiceImpl implements S3Service {

    @Value("${minio.bucket.name}")
    private String s3bucket;

    private final MinioClient minioClient;

    public S3ServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public String uploadFile(String objectName, InputStream inputStream, String contentType) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(s3bucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(s3bucket).build());
            }
            String uploadedAttachment = minioClient.putObject(
                    PutObjectArgs.builder().bucket(s3bucket).object(objectName).stream(
                                    inputStream, inputStream.available(), -1)
                            .contentType(contentType)
                            .build()).object();
            log.info("Attachment with name = {} is uploaded to S3", objectName);
            return uploadedAttachment;
        } catch (Exception e) {
            throw new RuntimeException("Error occurred: " + e.getMessage());
        }
    }

    @Override
    public InputStream getFileAsInputStream(String filename) {
        try {
            return minioClient.getObject(GetObjectArgs
                                 .builder()
                                 .bucket(s3bucket)
                                 .object(filename)
                                 .build());
        } catch (ErrorResponseException | XmlParserException | ServerException | NoSuchAlgorithmException |
                 IOException | InvalidResponseException | InvalidKeyException | InternalException |
                 InsufficientDataException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
