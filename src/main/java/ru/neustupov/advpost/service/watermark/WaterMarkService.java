package ru.neustupov.advpost.service.watermark;

import ru.neustupov.advpost.model.Attachment;

public interface WaterMarkService {

    byte[] addImageWatermarkToPhoto(Attachment photo);
}
