package ru.neustupov.advpost.service.s3;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.neustupov.advpost.exception.S3ServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

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
            throw new S3ServiceException("Error occurred: " + e.getMessage(), e);
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
            throw new S3ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteUnusedImage(List<String> fileList) {
        List<DeleteObject> objects = fileList.stream().map(DeleteObject::new).toList();
        Iterable<Result<DeleteError>> results =
                minioClient.removeObjects(
                        RemoveObjectsArgs.builder().bucket(s3bucket).objects(objects).build());
        for (Result<DeleteError> result : results) {
            DeleteError error = null;
            try {
                error = result.get();
            } catch (ErrorResponseException | XmlParserException | ServerException | NoSuchAlgorithmException |
                     IOException | InvalidResponseException | InvalidKeyException | InternalException |
                     InsufficientDataException e) {
                throw new RuntimeException(e);
            }
            log.error("Error in deleting object {}; {}", error.objectName(), error.message());
        }
    }
}
