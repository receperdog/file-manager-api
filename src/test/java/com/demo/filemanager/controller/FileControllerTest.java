package com.demo.filemanager.controller;

import com.demo.filemanager.model.FileMetaData;
import com.demo.filemanager.service.FileService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RequiredArgsConstructor
public class FileControllerTest {

    @InjectMocks
    private FileController fileController;

    @Mock
    private FileService fileService;

    private static final String STORAGE_PATH = "uploads";


//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }

    @BeforeEach
    public void setUp() throws Exception {
        // Test öncesi dosyanın oluşturulmasını sağlayın
        MockitoAnnotations.openMocks(this);
        Path testFilePath = Paths.get(STORAGE_PATH, "sample.png");
        Files.write(testFilePath, "Sample content".getBytes());
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Test sonrası dosyanın silinmesini sağlayın
        Path testFilePath = Paths.get(STORAGE_PATH, "sample.png");
        Files.deleteIfExists(testFilePath);
    }

    @Test
    public void testGetAllFiles() {
        FileMetaData metaData = new FileMetaData();
        metaData.setId(1L);
        metaData.setFileName("sample.png");

        when(fileService.getAllFiles()).thenReturn(Collections.singletonList(metaData));

        ResponseEntity<?> response = fileController.getAllFiles();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testGetFileByIdFound() {
        FileMetaData metaData = new FileMetaData();
        metaData.setId(1L);
        metaData.setFileName("sample.png");

        when(fileService.getFileById(1L)).thenReturn(Optional.of(metaData));

        ResponseEntity<?> response = fileController.getFileById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testGetFileByIdNotFound() {
        when(fileService.getFileById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = fileController.getFileById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDownloadFileFound() throws Exception {
        FileMetaData metaData = new FileMetaData();
        metaData.setId(1L);
        metaData.setFileName("sample.png");
        metaData.setFilePath(Paths.get(STORAGE_PATH, "sample.png").toString());
        metaData.setFileType("text/plain");

        when(fileService.getFileById(1L)).thenReturn(Optional.of(metaData));

        ResponseEntity<?> response = fileController.downloadFile(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    public void testDownloadFileNotFound() {
        when(fileService.getFileById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = fileController.downloadFile(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDeleteFile() {
        ResponseEntity<?> response = fileController.deleteFile(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    public void testUpdateFileNotFound() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("file", "sample.png", "text/plain", "sample content".getBytes());

        when(fileService.getFileById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = fileController.updateFile(1L, mockFile);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

}
