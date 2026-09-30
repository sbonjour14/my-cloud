package com.github.sbonjour.my_cloud.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.github.sbonjour.my_cloud.entity.Upload;
import com.github.sbonjour.my_cloud.entity.UploadSession;
import com.github.sbonjour.my_cloud.entity.User;
import com.github.sbonjour.my_cloud.repository.UploadRepository;

import lombok.AllArgsConstructor;

@Service 
@AllArgsConstructor 
public class UploadService {
    private final UploadRepository repository;
    
    public Upload create(UploadSession us) {
        Upload upload = Upload.builder()
            .id(us.getId())
            .filename(us.getFilename())
            .totalSize(us.getTotalSize())
            .user(us.getUser())
            .build(); 
        
        return repository.save(upload);
    }
    public List<Upload> findByUser(User user) {
        return repository.findByUser(user);
    }
}
