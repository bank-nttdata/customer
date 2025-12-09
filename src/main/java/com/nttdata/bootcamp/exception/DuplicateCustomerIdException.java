package com.nttdata.bootcamp.exception;

import static com.nttdata.bootcamp.util.Constant.CLIENT_EXIST;

public class DuplicateCustomerIdException extends RuntimeException{

    public DuplicateCustomerIdException(String dni) {
        super(CLIENT_EXIST + dni);
    }

}
