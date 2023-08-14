package com.demo.filemanager.repository;

import com.demo.filemanager.model.FileMetaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<FileMetaData, Long> {

}
