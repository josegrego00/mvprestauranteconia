package com.mvprestaurante.mvp.exceptions;

public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String resource, String field) {
        super(resource + " duplicado por: " + field);
    }

}
