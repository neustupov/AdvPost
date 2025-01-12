package ru.neustupov.advpost.service.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

@Slf4j
@Service
public class DownloadService {

    @Value("${timeout.connect}")
    private Integer connectTimeout;
    @Value("${timeout.read}")
    private Integer readTimeout;
    @Value("${files.root}")
    private String root;

    public File downloadFile(Integer photo_id, URI uri) {
        String fileName = root + "photo_" + photo_id + ".jpg";
        File file = new File(fileName);
        try {
            FileUtils.copyURLToFile(
                    new URL(uri.toString()),
                    file,
                    connectTimeout,
                    readTimeout);
            log.info("Download photo to file {} and size {}", fileName, file.length());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }
}
