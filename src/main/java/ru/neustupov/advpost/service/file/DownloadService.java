package ru.neustupov.advpost.service.file;

import java.io.InputStream;
import java.net.URI;

public interface DownloadService {

    InputStream downloadAsInputStream(URI uri);
}
