package ru.neustupov.advpost.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attachment extends AbstractEntity {

    private static final String MD5_ALGORITHM = "MD5";
    private String name;
    private Integer originalId;
    @Column(columnDefinition = "text")
    private String originalUri;
    private String s3Uri;
    private String hash;
    private AttachmentType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOriginalId() {
        return originalId;
    }

    public void setOriginalId(Integer originalPhotoId) {
        this.originalId = originalPhotoId;
    }

    public String getOriginalUri() {
        return originalUri;
    }

    public void setOriginalUri(String originalUri) {
        this.originalUri = originalUri;
    }

    public String getS3Uri() {
        return s3Uri;
    }

    public void setS3Uri(String s3Uri) {
        this.s3Uri = s3Uri;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(InputStream inputStream) {
        this.hash = getHashAsDigest(inputStream);
    }

    public String getHashAsDigest(InputStream inputStream) {
        try {
            return DigestUtils.appendMd5DigestAsHex(inputStream, new StringBuilder()).toString();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public AttachmentType getType() {
        return type;
    }

    public void setType(AttachmentType type) {
        this.type = type;
    }
}
