package ru.neustupov.advpost.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdvertisingPostDTO {

    private List<String> image;
    private String text;
    private String token;
    private LocalDateTime periodFrom;
    private LocalDateTime periodTo;
    private boolean comments;
    private Long repostId;
}
