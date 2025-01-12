package ru.neustupov.advpost.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.util.DigestUtils;

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
    @Column(name = "s3Uri")
    private String s3Uri;
    @Transient
    private byte[] data;
    private String hash;

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

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
        setHash(getHashAsDigest(data));
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getHashAsDigest(byte[] data) {
        return DigestUtils.appendMd5DigestAsHex(data, new StringBuilder()).toString();
    }
}
