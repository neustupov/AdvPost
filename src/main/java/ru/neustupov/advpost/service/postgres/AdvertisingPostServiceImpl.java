package ru.neustupov.advpost.service.postgres;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.neustupov.advpost.model.AdvertisingPhoto;
import ru.neustupov.advpost.model.AdvertisingPost;
import ru.neustupov.advpost.model.dto.AdvertisingPhotoDTO;
import ru.neustupov.advpost.model.dto.AdvertisingPostDTO;
import ru.neustupov.advpost.repository.AdvertisingPostRepository;

import java.util.List;

@Slf4j
@Service
public class AdvertisingPostServiceImpl implements AdvertisingPostService{

    private final AdvertisingPostRepository repository;

    public AdvertisingPostServiceImpl(AdvertisingPostRepository repository) {
        this.repository = repository;
    }

    @Override
    public AdvertisingPost processAdvertisingDocument(AdvertisingPostDTO dto) {
        AdvertisingPost advPost = new AdvertisingPost(dto);
        AdvertisingPost advertisingPost = repository.save(advPost);
        log.info("Create AdvertisingPost entity with id = {}", advertisingPost.getId());
        return advertisingPost;
    }

    @Override
    public List<AdvertisingPhoto> processAdvertisingPhotos(List<AdvertisingPhotoDTO> dtos) {
        return null;
    }
}
