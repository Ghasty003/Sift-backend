package com.sift.exceptions;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String user) {
        super("User '" + user + "' was not found.");
    }
}
