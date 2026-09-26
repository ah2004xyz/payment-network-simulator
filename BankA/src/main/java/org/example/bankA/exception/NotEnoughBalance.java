package org.example.bankA.exception;

import org.example.bankA.Constants.ErrorConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NotEnoughBalance extends BaseException {

    public NotEnoughBalance(String role) {
        super(String.format(ErrorConstants.NOT_ENOUGH_BALANCE, role));
    }

}
