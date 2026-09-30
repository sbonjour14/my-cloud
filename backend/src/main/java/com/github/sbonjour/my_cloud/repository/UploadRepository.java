package com.github.sbonjour.my_cloud.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sbonjour.my_cloud.entity.Upload;
import com.github.sbonjour.my_cloud.entity.User;

public interface UploadRepository extends JpaRepository<Upload, UUID>{

    public List<Upload> findByUser(User user);    
    
}
