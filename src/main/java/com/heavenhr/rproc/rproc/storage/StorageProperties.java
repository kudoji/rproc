/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(value = "storage")
public class StorageProperties {
    private String location = "upload-dir";
}
