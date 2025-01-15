package ru.neustupov.advpost.service.watermark;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.neustupov.advpost.model.Attachment;
import ru.neustupov.advpost.service.s3.S3Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

import static ru.neustupov.advpost.service.postgres.AttachmentServiceImpl.PHOTO_TYPE;

@Slf4j
@Service
public class WaterMarkServiceImpl implements WaterMarkService {

    private final S3Util s3Util;

    public WaterMarkServiceImpl(S3Util s3Util) {
        this.s3Util = s3Util;
    }

    @Override
    public byte[] addImageWatermarkToPhoto(Attachment photo) {
        String s3Uri = photo.getS3Uri();
        try(InputStream inputStream = s3Util.download(s3Uri)) {
            byte[] imageWatermark = addImageWatermark(inputStream);
            //После того как наложили вотермарку - заливаем в S3 и проставляем ссылку в attachment
            uploadToS3(photo, imageWatermark);
            return imageWatermark;
        } catch (IOException e) {
            log.error("Can`t download file with uri = {} from S3", s3Uri);
        }
        return null;
    }

    private byte[] addImageWatermark(InputStream sourceImageFile) {
        try {
            BufferedImage sourceImage = ImageIO.read(sourceImageFile);
            int sourceImageWidth = sourceImage.getWidth();
            int sourceImageHeight = sourceImage.getHeight();
            String wmSource = "water_mark_image_500.png";
            if (sourceImageWidth < 500 || sourceImageHeight < 500) {
                wmSource = "water_mark_image_100.png";
            } else if (sourceImageWidth < 1000 || sourceImageHeight < 1000) {
                wmSource = "water_mark_image_200.png";
            } else if (sourceImageWidth < 1500 || sourceImageHeight < 1500) {
                wmSource = "water_mark_image_300.png";
            }
            URL resource = getClass().getClassLoader().getResource(wmSource);
            File watermarkImageFile;
            try {
                watermarkImageFile = new File(resource.toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            BufferedImage watermarkImage = ImageIO.read(watermarkImageFile);
            Graphics2D g2d = (Graphics2D) sourceImage.getGraphics();
            AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
            g2d.setComposite(alphaChannel);

            int watermarkImageWidth = watermarkImage.getWidth();
            int watermarkImageHeight = watermarkImage.getHeight();
            int topLeftX = (sourceImageWidth - watermarkImageWidth) / 40;
            int topLeftY = (sourceImageHeight - watermarkImageHeight) - (watermarkImageHeight / 100 * 5);

            log.info("source W -> {} source H -> {} waterMark W -> {} waterMark H -> {} total X -> {} total Y -> {}",
                    sourceImageWidth, sourceImageHeight, watermarkImageWidth, watermarkImageHeight, topLeftX, topLeftY);

            g2d.drawImage(watermarkImage, topLeftX, topLeftY, null);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(sourceImage, "jpg", byteArrayOutputStream);
            g2d.dispose();
            log.info("The image watermark is added to the image.");
            byte[] bytes = byteArrayOutputStream.toByteArray();
            log.info("Image with watermark size = {}", bytes.length);
            return bytes;
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
        return null;
    }

    private void uploadToS3(Attachment photo, byte[] imageWatermark) {
        String photoNameWithWatermark = "watermark_" + photo.getName();
        String upload = s3Util.upload(photoNameWithWatermark, new ByteArrayInputStream(imageWatermark), PHOTO_TYPE);
        photo.setS3Uri(upload);
        log.info("Photo with watermark and name = {} is uploaded to S3", photoNameWithWatermark);
    }
}
