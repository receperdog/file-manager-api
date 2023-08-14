package com.demo.filemanager.service;

import com.demo.filemanager.model.FileMetaData;
import com.demo.filemanager.repository.FileRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class FileService {

    private FileRepository fileRepository;

    public List<FileMetaData> getAllFiles() {
        return fileRepository.findAll();
    }

    public Optional<FileMetaData> getFileById(Long id) {
        return fileRepository.findById(id);
    }

    public FileMetaData saveFile(FileMetaData fileMetaData) {
        return fileRepository.save(fileMetaData);
    }

    public void deleteFile(Long id) {
        fileRepository.deleteById(id);
    }

}
