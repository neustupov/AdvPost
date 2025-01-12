package ru.neustupov.advpost.s3;

import io.minio.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;

@Slf4j
@Service
public class S3Service {

    @Value("${minio.bucket.name}")
    private String s3bucket;

    private final MinioClient minioClient;

    public S3Service(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String uploadFile(String objectName, InputStream inputStream, String contentType) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(s3bucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(s3bucket).build());
            }
            ObjectWriteResponse response = minioClient.putObject(
                    PutObjectArgs.builder().bucket(s3bucket).object(objectName).stream(
                                    inputStream, inputStream.available(), -1)
                            .contentType(contentType)
                            .build());
            return response.object();
        } catch (Exception e) {
            throw new RuntimeException("Error occurred: " + e.getMessage());
        }
    }

    public File getFile(String filename) {
        File file = new File(filename);
        try (InputStream stream =
                     minioClient.getObject(GetObjectArgs
                             .builder()
                             .bucket(s3bucket)
                             .object(filename)
                             .build())) {
            FileUtils.copyInputStreamToFile(stream, file);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return file;
    }
}
