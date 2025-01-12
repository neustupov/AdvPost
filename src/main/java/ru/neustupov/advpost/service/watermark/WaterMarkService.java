package ru.neustupov.advpost.service.watermark;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.neustupov.advpost.model.Attachment;
import ru.neustupov.advpost.model.Post;
import ru.neustupov.advpost.s3.S3Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WaterMarkService {

    @Value("${files.root}")
    private String root;

    private final S3Service s3Service;

    public WaterMarkService(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    public Map<Long, List<File>> processPhoto(List<Post> posts) {
        Map<Long, List<File>> fileMap = downloadAndGetPhoto(posts);
        return addImageWatermarkToPhotos(posts, fileMap);
    }

    public Map<Long, List<File>> downloadAndGetPhoto(List<Post> attachments) {
        Map<Long, List<File>> fileMap = new HashMap<>();
        attachments.forEach(p -> {
            List<Attachment> photos = p.getAttachments();
            List<File> totalPhotos = new ArrayList<>();
            //Фото уже в s3 - нужно достать оттуда
            photos.forEach(photo -> {
                String s3Uri = photo.getS3Uri();
                File file = s3Service.getFile(s3Uri);
                totalPhotos.add(file);
            });
            fileMap.put(p.getId(), totalPhotos);
        });
        return fileMap;
    }

    private Map<Long, List<File>> addImageWatermarkToPhotos(List<Post> posts, Map<Long, List<File>> attachments) {
        Map<Long, List<File>> imageWithWatermarkMap = new HashMap<>();

        for (Map.Entry<Long, List<File>> entry : attachments.entrySet()) {
            Long k = entry.getKey();
            List<File> v = entry.getValue();
            List<File> precessedFiles = new ArrayList<>();
            for (File f : v) {
                String destFileName = "withWaterMark_" + f.getName();
                File destinationFile = new File(root + destFileName);
                File watermark = addImageWatermark(f, destinationFile);
                precessedFiles.add(watermark);
                posts.stream()
                        .filter(post -> post.getId().equals(k))
                        .findFirst()
                        .flatMap(p -> p.getAttachments().stream()
                                .filter(photo -> photo.getName().equals(f.getName()))
                                .findFirst())
                        .ifPresent(ph -> {
                            try {
                                byte[] data = Files.readAllBytes(watermark.toPath());
                                ph.setData(data);
                            } catch (IOException e) {
                                log.error("Can`t read byte array from file with name = {}", f.getName());
                            }
                        });
            }
            imageWithWatermarkMap.put(k, precessedFiles);
        }
        return imageWithWatermarkMap;
    }

    private File addImageWatermark(File sourceImageFile, File destImageFile) {
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
            ImageIO.write(sourceImage, "jpg", destImageFile);
            g2d.dispose();

            log.info("The image watermark is added to the image.");
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
        return destImageFile;
    }
}
