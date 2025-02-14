package ru.neustupov.advpost.model.dto;

import jakarta.validation.constraints.NotNull;

public class AdvertisingPhotoDTO {

    @NotNull
    private String url;
}
