package ru.neustupov.advpost.service.postgres;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.neustupov.advpost.repository.AdvertisingPhotoRepository;

@Slf4j
@Service
public class AdvertisingPhotoServiceImpl {

    private final AdvertisingPhotoRepository repository;

    public AdvertisingPhotoServiceImpl(AdvertisingPhotoRepository repository) {
        this.repository = repository;
    }
}
