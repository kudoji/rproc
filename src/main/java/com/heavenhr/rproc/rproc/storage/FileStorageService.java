/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService implements StorageService{
    private final Path location;

    @Autowired
    public FileStorageService(StorageProperties storageProperties){
        this.location = Paths.get(storageProperties.getLocation());
    }

    @Override
    public void init(){
        try{
            Files.createDirectories(this.location);
        }catch (IOException e){
            throw new StorageException("Couldn't initialize file storage", e);
        }
    }

    @Override
    public void store(MultipartFile file){
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try{
            if (filename.isEmpty()){
                throw new StorageException(String.format("file '%s' is empty", filename));
            }

            if (filename.contains("..")){
                throw new StorageException(String.format("file name '%s' has invalid characters", filename));
            }

            try (InputStream inputStream = file.getInputStream()){
                Files.copy(inputStream, this.location.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            }
        }catch (IOException e){
            throw new StorageException(String.format("failed to store file '%s'", filename), e);
        }
    }
}
