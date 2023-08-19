package com.demo.filemanager.controller;

import com.demo.filemanager.dto.FileMetaDataDTO;
import com.demo.filemanager.model.FileMetaData;
import com.demo.filemanager.model.response.CustomApiResponse;
import com.demo.filemanager.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Tag(name = "File", description = "File management APIs")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Value("${file.storage.location}")
    private String fileStorageLocation;

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);


    @Operation(
            summary = "Retrieve all files",
            description = "Get a list of all File meta data.",
            tags = { "files" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = FileMetaDataDTO.class), mediaType = "application/json") })
    })
    @GetMapping
    public ResponseEntity<CustomApiResponse<List<FileMetaDataDTO>>> getAllFiles() {
        List<FileMetaData> files = fileService.getAllFiles();
        List<FileMetaDataDTO> dtos = files.stream()
                .map(this::convertToFileMetaDataDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new CustomApiResponse<>("All files fetched successfully.", dtos));
    }

    @Operation(
            summary = "Retrieve a File by Id",
            description = "Get a File meta data object by specifying its id.",
            tags = { "files", "get" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = FileMetaDataDTO.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) })
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomApiResponse<FileMetaDataDTO>> getFileById(@PathVariable Long id) {
        Optional<FileMetaData> file = fileService.getFileById(id);
        return file.map(metaData -> ResponseEntity.ok(new CustomApiResponse<>("File fetched successfully.", convertToFileMetaDataDTO(metaData))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Upload a file",
            description = "Upload a file and save its meta data.",
            tags = { "files", "upload" })
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = FileMetaDataDTO.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) })
    })
    @PostMapping("/upload")
    public ResponseEntity<CustomApiResponse<FileMetaDataDTO>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (!isValidFile(file)) {
            return ResponseEntity.badRequest().body(new CustomApiResponse<>("Invalid file type or size!", null));
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

        return ResponseEntity.ok(new CustomApiResponse<>("File uploaded successfully.", convertToFileMetaDataDTO(savedFile)));
    }

    @Operation(
            summary = "Download a file by its Id",
            description = "Retrieve the actual content of a file given its meta data ID. This endpoint will provide the file as a byte array, allowing for direct downloads.",
            tags = { "files", "download" }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful retrieval of file.",
                    content = @Content(schema = @Schema(type = "string", format = "binary"), mediaType = "*/*")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "File not found.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content
            )
    })
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

    @Operation(
            summary = "Delete a file meta data by its Id",
            description = "Delete the meta data information of a file given its ID. This will not delete the actual file but only its reference in the system.",
            tags = { "files", "delete" }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "File meta data deleted successfully.",
                    content = @Content(schema = @Schema(implementation = CustomApiResponse.class), mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "File not found.",
                    content = @Content
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomApiResponse<String>> deleteFile(@PathVariable Long id) {
        fileService.deleteFile(id);
        return ResponseEntity.ok(new CustomApiResponse<>("File deleted successfully.", null));
    }

    @Operation(
            summary = "Update file and its meta data by its Id",
            description = "Update the content and meta data of a file given its ID. This requires uploading a new file that will replace the old one.",
            tags = { "files", "update" }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "File updated successfully.",
                    content = @Content(schema = @Schema(implementation = CustomApiResponse.class), mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid file type or size.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "File not found.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomApiResponse<FileMetaDataDTO>> updateFile(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        Optional<FileMetaData> existingFileMetaData = fileService.getFileById(id);

        if (!existingFileMetaData.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        if (!isValidFile(file)) {
            return ResponseEntity.badRequest().body(new CustomApiResponse<>("Invalid file type or size!", null));
        }

        FileMetaData updatedMetaData = existingFileMetaData.get();
        updatedMetaData.setFileName(file.getOriginalFilename());
        updatedMetaData.setFileSize(file.getSize());
        updatedMetaData.setFileType(file.getContentType());

        Path targetLocation = Paths.get(fileStorageLocation).resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), targetLocation);

        FileMetaData savedMetaData = fileService.saveFile(updatedMetaData);

        return ResponseEntity.ok(new CustomApiResponse<>("File updated successfully.", convertToFileMetaDataDTO(savedMetaData)));
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
