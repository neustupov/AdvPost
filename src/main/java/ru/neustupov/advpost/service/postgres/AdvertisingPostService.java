package ru.neustupov.advpost.service.postgres;

import ru.neustupov.advpost.model.AdvertisingPhoto;
import ru.neustupov.advpost.model.AdvertisingPost;
import ru.neustupov.advpost.model.dto.AdvertisingPhotoDTO;
import ru.neustupov.advpost.model.dto.AdvertisingPostDTO;

import java.util.List;

public interface AdvertisingPostService {

    AdvertisingPost processAdvertisingDocument(AdvertisingPostDTO dto);

    List<AdvertisingPhoto> processAdvertisingPhotos(List<AdvertisingPhotoDTO> dtos);
}
