package ru.neustupov.advpost.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AdvertisingPostDTO {

    @NotNull
    private String text;
    @NotNull
    private String token;
    @NotNull
    private LocalDate periodFrom;
    @NotNull
    private LocalDate periodTo;
    @NotNull
    private LocalTime time;
    private boolean comments;
    private Long repostId;
}
