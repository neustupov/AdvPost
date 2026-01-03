package ru.neustupov.advpost.service.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.neustupov.advpost.exception.DownloadServiceException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

@Slf4j
@Service
public class DownloadServiceImpl implements DownloadService {
    @Override
    public InputStream downloadAsInputStream(URI uri) {
        try (InputStream is = uri.toURL().openStream()) {
            log.info("Open stream for uri = {}", uri);
            byte[] allBytes = is.readAllBytes();
            log.info("Download file with size = {}", allBytes.length);
            return new ByteArrayInputStream(allBytes);
        } catch (IOException e) {
            log.error("File with URI: {} is not found in VK", uri);
            //throw new DownloadServiceException(e.getMessage(), e);
        }
        return null;
    }
}
