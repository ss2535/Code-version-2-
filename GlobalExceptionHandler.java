package com.scheduleNetwork_version2.scheduleNetwork_version2.exception;

import com.scheduleNetwork_version2.scheduleNetwork_version2.exception.ResourceNotFoundException;
import com.scheduleNetwork_version2.scheduleNetwork_version2.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ساختار پاسخ خطا
    private Map<String, Object> createErrorResponse(String message, String errorCode, HttpStatus status, WebRequest request) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("message", message);
        errorDetails.put("errorCode", errorCode);
        errorDetails.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());
        errorDetails.put("status", status.value());
        return errorDetails;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        logger.error("خطای منبع یافت نشد: {}", ex.getMessage(), ex);
        Map<String, Object> errorDetails = createErrorResponse(ex.getMessage(), "NOT_FOUND", HttpStatus.NOT_FOUND, request);
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityException(SecurityException ex, WebRequest request) {
        logger.error("خطای امنیتی: {}", ex.getMessage(), ex);
        Map<String, Object> errorDetails = createErrorResponse(ex.getMessage(), "FORBIDDEN", HttpStatus.FORBIDDEN, request);
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException ex, WebRequest request) {
        logger.error("خطای اعتبارسنجی: {}", ex.getMessage(), ex);
        Map<String, Object> errorDetails = createErrorResponse(ex.getMessage(), "BAD_REQUEST", HttpStatus.BAD_REQUEST, request);
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex, WebRequest request) {
        logger.error("خطای عمومی: {}", ex.getMessage(), ex);
        Map<String, Object> errorDetails = createErrorResponse("خطای غیرمنتظره رخ داد", "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, request);
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

//package com.scheduleNetwork_version2.scheduleNetwork_version2.exception;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import org.springframework.http.*;
//import org.springframework.validation.*;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.context.request.WebRequest;
//import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
//
//@ControllerAdvice
//public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
//
//    @ExceptionHandler(ResourceNotFoundException.class)
//    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(
//            ResourceNotFoundException exception, WebRequest webRequest) {
//        ErrorDetails errorDetails = new ErrorDetails(
//                LocalDateTime.now(),
//                exception.getMessage(),
//                webRequest.getDescription(false),
//                "منبع درخواستی یافت نشد"
//        );
//        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorDetails> handleGlobalException(
//            Exception exception, WebRequest webRequest) {
//        ErrorDetails errorDetails = new ErrorDetails(
//                LocalDateTime.now(),
//                "خطای سرور داخلی رخ داده است",
//                webRequest.getDescription(false),
//                "خطای_سرور_داخلی"
//        );
//        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//
//    @Override
//    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
//                                                                  HttpHeaders headers,
//                                                                  HttpStatusCode status,
//                                                                  WebRequest request) {
//        Map<String , String> errors= new HashMap<>();
//        List<ObjectError> errorList= ex.getBindingResult().getAllErrors();
//        errorList.forEach((error) -> {
//            String fieldName = ((FieldError) error).getField();
//            String message = error.getDefaultMessage();
//            errors.put(fieldName, message);
//        });
//        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
//
//    }
//
//}
