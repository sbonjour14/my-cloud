package com.github.sbonjour.my_cloud.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.sbonjour.my_cloud.entity.StoredFile;
import com.github.sbonjour.my_cloud.repository.StoredFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoredFileService {    
    private final StoredFileRepository storedFileRepository;


    public StoredFile save(StoredFile sf) {
        return storedFileRepository.save(sf);
    }

    public StoredFile findByChecksum(String checksum) {
        return storedFileRepository.findByChecksum(checksum).orElse(null);
    }
    
    public StoredFile findById(UUID id) {
        return storedFileRepository.findById(id).orElse(null);
    }

    public void delete(StoredFile sf) {
        storedFileRepository.delete(sf);
    }


}
