package com.andune.minecraft.hsp.command;

/**
 * Exception that can be thrown from commands during processing which
 * will be caught by the command execution to have a nice error printed
 * to the user and a detailed trace printed to the server log.
 *
 * @author andune
 */
public class CommandException extends Exception {

    public CommandException(String message) {
        super(message);
    }

    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandException(Throwable cause) {
        super(cause);
    }

    public CommandException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
