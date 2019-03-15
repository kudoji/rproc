/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.storage;

public class StorageException extends RuntimeException {
    public StorageException(String message, Throwable e){
        super(message, e);
    }

    public StorageException(String message){
        super(message);
    }
}
