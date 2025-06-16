package utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import annotation.error.Error;
import annotation.validation.Required;
import annotation.validation.Size;

public class ValidationUtils {
    public static Map<String, String> validateObject(Object object) throws IllegalAccessException {
        Map<String, String> errors = new HashMap<>();
        
        Class<?> clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true); // To access private fields
            
            Object value = field.get(object);
            if (field.isAnnotationPresent(Required.class)) {
                if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
                    String message = field.isAnnotationPresent(Error.class)
                        ? field.getAnnotation(Error.class).message()
                        : "This field is required.";
                    errors.put(field.getName(), message);
                }
            }
            
            if (field.isAnnotationPresent(Size.class) && value instanceof Integer) {
                Size size = field.getAnnotation(Size.class);
                int intValue = (int) value;
                if (intValue < size.minimum() || intValue > size.maximum()) {
                    String message = field.isAnnotationPresent(Error.class)
                        ? field.getAnnotation(Error.class).message()
                        : "Value is out of range.";
                    errors.put(field.getName(), message);
                }
            }
        }
        return errors;
    }
}
