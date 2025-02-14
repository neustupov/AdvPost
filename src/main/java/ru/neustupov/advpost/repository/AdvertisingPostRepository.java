package ru.neustupov.advpost.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.neustupov.advpost.model.AdvertisingPost;

public interface AdvertisingPostRepository extends JpaRepository<AdvertisingPost, Long> {
}
