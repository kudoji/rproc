package com.heavenhr.rproc.rproc;

import com.heavenhr.rproc.rproc.storage.StorageProperties;
import com.heavenhr.rproc.rproc.storage.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class RprocApplication {

	public static void main(String[] args) {
		SpringApplication.run(RprocApplication.class, args);
	}

	@Bean
    CommandLineRunner init(StorageService storageService){
	    return (args -> storageService.init());
    }
}

