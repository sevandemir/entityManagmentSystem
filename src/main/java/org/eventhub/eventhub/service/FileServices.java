package org.eventhub.eventhub.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileServices {
    String saveImage(MultipartFile file) throws IOException;
}
