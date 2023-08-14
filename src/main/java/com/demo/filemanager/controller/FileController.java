package com.demo.filemanager.controller;

import com.demo.filemanager.dto.FileMetaDataDTO;
import com.demo.filemanager.model.FileMetaData;
import com.demo.filemanager.model.response.ApiResponse;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Value("${file.storage.location}")
    private String fileStorageLocation;


    @GetMapping
    public ResponseEntity<ApiResponse<List<FileMetaDataDTO>>> getAllFiles() {
        List<FileMetaData> files = fileService.getAllFiles();
        List<FileMetaDataDTO> dtos = files.stream()
                .map(this::convertToFileMetaDataDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>("All files fetched successfully.", dtos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FileMetaDataDTO>> getFileById(@PathVariable Long id) {
        Optional<FileMetaData> file = fileService.getFileById(id);
        return file.map(metaData -> ResponseEntity.ok(new ApiResponse<>("File fetched successfully.", convertToFileMetaDataDTO(metaData))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileMetaDataDTO>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (!isValidFile(file)) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("Invalid file type or size!", null));
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

        return ResponseEntity.ok(new ApiResponse<>("File uploaded successfully.", convertToFileMetaDataDTO(savedFile)));
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
    public ResponseEntity<ApiResponse<String>> deleteFile(@PathVariable Long id) {
        fileService.deleteFile(id);
        return ResponseEntity.ok(new ApiResponse<>("File deleted successfully.", null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FileMetaDataDTO>> updateFile(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<FileMetaData> existingFileMetaData = fileService.getFileById(id);

        if (!existingFileMetaData.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        if (!isValidFile(file)) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("Invalid file type or size!", null));
        }

        FileMetaData updatedMetaData = existingFileMetaData.get();
        updatedMetaData.setFileName(file.getOriginalFilename());
        updatedMetaData.setFileSize(file.getSize());
        updatedMetaData.setFileType(file.getContentType());

        Path targetLocation = Paths.get(fileStorageLocation).resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), targetLocation);

        FileMetaData savedMetaData = fileService.saveFile(updatedMetaData);

        return ResponseEntity.ok(new ApiResponse<>("File updated successfully.", convertToFileMetaDataDTO(savedMetaData)));
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

    private FileMetaDataDTO convertToFileMetaDataDTO(FileMetaData metaData) {
        FileMetaDataDTO dto = new FileMetaDataDTO();
        dto.setId(metaData.getId());
        dto.setFileName(metaData.getFileName());
        dto.setFileType(metaData.getFileType());
        dto.setFileSize(metaData.getFileSize());
        dto.setFilePath(metaData.getFilePath());
        return dto;
    }

    private FileMetaData convertToEntity(FileMetaDataDTO dto) {
        FileMetaData metaData = new FileMetaData();
        metaData.setId(dto.getId());
        metaData.setFileName(dto.getFileName());
        metaData.setFileType(dto.getFileType());
        metaData.setFileSize(dto.getFileSize());
        metaData.setFilePath(dto.getFilePath());
        return metaData;
    }

}
