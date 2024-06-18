package exception.terminal;

import exception.FrameworkException;

public class DuplicateGetMappingException extends FrameworkException {
    public DuplicateGetMappingException(String message) {
        super(message);
    }
}