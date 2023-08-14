package com.demo.filemanager.dto;

import lombok.Data;

@Data
public class FileMetaDataDTO {
    private Long id;
    private String fileName;
    private String filePath;
    private String fileType;
    private long fileSize;
}