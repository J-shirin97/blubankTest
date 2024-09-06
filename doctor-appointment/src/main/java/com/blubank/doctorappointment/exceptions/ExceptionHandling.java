package com.blubank.doctorappointment.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ExceptionHandling {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class AppointmentNotFoundException extends RuntimeException {
        public AppointmentNotFoundException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class AppointmentAlreadyTakenException extends RuntimeException {
        public AppointmentAlreadyTakenException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public static class AppointmentTakenException extends RuntimeException {
        public AppointmentTakenException(String message) {
            super(message);
        }
    }


}
