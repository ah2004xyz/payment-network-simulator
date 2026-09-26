package org.example.bankA.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends BaseException{

    public BadRequestException(String message) {
        super(message);
    }
}
