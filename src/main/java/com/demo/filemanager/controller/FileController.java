package com.demo.filemanager.controller;

import com.demo.filemanager.model.FileMetaData;
import com.demo.filemanager.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Value("${file.storage.location}")
    private String fileStorageLocation;


    @GetMapping
    public ResponseEntity<List<FileMetaData>> getAllFiles() {
        List<FileMetaData> files = fileService.getAllFiles();
        return ResponseEntity.ok(files);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileMetaData> getFileById(@PathVariable Long id) {
        Optional<FileMetaData> file = fileService.getFileById(id);
        if (file.isPresent()) {
            return ResponseEntity.ok(file.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (!isValidFile(file)) {
            return ResponseEntity.badRequest().body("Invalid file type or size!");
        }

        String fileName = storeFile(file);
        String fileType = file.getContentType();
        long fileSize = file.getSize();

        FileMetaData metaData = new FileMetaData();
        metaData.setFileName(fileName);
        metaData.setFileType(fileType);
        metaData.setFileSize(fileSize);
        metaData.setFilePath(Paths.get(fileStorageLocation, fileName).toString());

        FileMetaData savedFile = fileService.saveFile(metaData);
        return ResponseEntity.ok(savedFile);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        Optional<FileMetaData> fileMetaDataOptional = fileService.getFileById(id);

        if (!fileMetaDataOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        FileMetaData fileMetaData = fileMetaDataOptional.get();
        Path filePath = Paths.get(fileMetaData.getFilePath());

        try {
            byte[] fileContent = Files.readAllBytes(filePath);
            return ResponseEntity.ok()
                    .header("Content-Type", fileMetaData.getFileType())
                    .header("Content-Disposition", "attachment; filename=\"" + fileMetaData.getFileName() + "\"")
                    .body(fileContent);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isValidFile(MultipartFile file) {
        String[] allowedExtensions = { "png", "jpeg", "jpg", "docx", "pdf", "xlsx" };
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        boolean validExtension = Arrays.stream(allowedExtensions).anyMatch(ext -> ext.equalsIgnoreCase(fileExtension));

        boolean validSize = file.getSize() <= 5 * 1024 * 1024;

        return validExtension && validSize;
    }

    private String storeFile(MultipartFile file) {
        try {
            Path fileStoragePath = Paths.get(fileStorageLocation).toAbsolutePath().normalize();
            Files.createDirectories(fileStoragePath);

            Path targetPath = fileStoragePath.resolve(file.getOriginalFilename());
            file.transferTo(targetPath);

            return file.getOriginalFilename();
        } catch (IOException ex) {
            throw new RuntimeException("Error saving file", ex);
        }
    }
}
