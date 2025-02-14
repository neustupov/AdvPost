package ru.neustupov.advpost.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.neustupov.advpost.model.AdvertisingPhoto;

public interface AdvertisingPhotoRepository extends JpaRepository<AdvertisingPhoto, Long> {
}
