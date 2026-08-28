package com.sift.exceptions;

public class UserAlreadyExistException extends IllegalArgumentException {
    public UserAlreadyExistException() {
        super("User already exist");
    }
}
