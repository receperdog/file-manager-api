package com.demo.filemanager.repository;

import com.demo.filemanager.model.FileMetaData;
import com.demo.filemanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
