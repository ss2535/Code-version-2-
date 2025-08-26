package com.scheduleNetwork_version2.scheduleNetwork_version2.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s با %s: %s یافت نشد", resourceName, fieldName, fieldValue));
    }
}

//@ResponseStatus(value = HttpStatus.NOT_FOUND)
//public class ResourceNotFoundException extends RuntimeException {
//    private String resourceName;
//    private String fieldName;
//    private Long fieldValue;
//
//    public ResourceNotFoundException(String resourceName, String fieldName, Long fieldValue) {
//        super(String.format("%s با %s: %s یافت نشد", resourceName, fieldName, fieldValue));
//        this.resourceName = resourceName;
//        this.fieldName = fieldName;
//        this.fieldValue = fieldValue;
//    }
//}
//
//
