package com.sausagemountain.processing;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("process")
public class FileProcessingController {
    private final FileProcessingService fileProcessingService;

    public FileProcessingController(FileProcessingService fileProcessingService) {
        this.fileProcessingService = fileProcessingService;
    }

    @PostMapping(value = "{consumers}", consumes = {MediaType.ALL_VALUE})
    public void processFile(@RequestPart MultipartFile file, @PathVariable int consumers) throws Throwable {
        fileProcessingService.processFile(file.getInputStream(), file.getOriginalFilename(), consumers);
    }
}
