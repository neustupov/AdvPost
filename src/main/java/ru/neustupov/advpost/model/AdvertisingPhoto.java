package ru.neustupov.advpost.model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvertisingPhoto extends AbstractEntity {

    private String name;
    private String s3Uri;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getS3Uri() {
        return s3Uri;
    }

    public void setS3Uri(String s3Uri) {
        this.s3Uri = s3Uri;
    }
}
