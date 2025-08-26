package com.scheduleNetwork_version2.scheduleNetwork_version2.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
//@AllArgsConstructor
//@NoArgsConstructor
public class ErrorDetails {
    private LocalDateTime timestamp;
    private String message;
    private String path;
    private String errorCode;

//    ErrorDetails errorDetails= new ErrorDetails(LocalDateTime.now(), "test" ,"/path","500");


    public ErrorDetails() {
    }

    public ErrorDetails(LocalDateTime timestamp, String massage, String path, String errorCode) {
        this.timestamp = timestamp;
        this.message = massage;
        this.path = path;
        this.errorCode = errorCode;
    }

}
