package com.demo.filemanager.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomApiResponse<T> {
    private String message;
    private T data;

}

