/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    void init();

    void store(MultipartFile file);

//    Stream<Path> loadAll();
//
//    Path load(String file);
//
//    Resource loadAsResource(String file);
//
//    void deleteAll();
}
