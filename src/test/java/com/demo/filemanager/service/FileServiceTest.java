package com.demo.filemanager.service;

import com.demo.filemanager.model.FileMetaData;
import com.demo.filemanager.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private FileService fileService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllFiles() {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setId(1L);

        when(fileRepository.findAll()).thenReturn(Collections.singletonList(fileMetaData));

        assertEquals(1, fileService.getAllFiles().size());
        verify(fileRepository, times(1)).findAll();
    }

    @Test
    public void testGetFileById() {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setId(1L);

        when(fileRepository.findById(1L)).thenReturn(Optional.of(fileMetaData));

        Optional<FileMetaData> result = fileService.getFileById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(fileRepository, times(1)).findById(1L);
    }

    @Test
    public void testSaveFile() {
        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setId(1L);

        when(fileRepository.save(fileMetaData)).thenReturn(fileMetaData);

        FileMetaData result = fileService.saveFile(fileMetaData);

        assertEquals(1L, result.getId());
        verify(fileRepository, times(1)).save(fileMetaData);
    }

    @Test
    public void testDeleteFile() {
        fileService.deleteFile(1L);

        verify(fileRepository, times(1)).deleteById(1L);
    }
}

