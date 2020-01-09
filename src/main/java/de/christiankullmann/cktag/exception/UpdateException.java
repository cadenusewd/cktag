package de.christiankullmann.cktag.exception;

public class UpdateException extends RuntimeException {
  public UpdateException(String message) {
    super(message);
  }

  public UpdateException(String message, Throwable e) {
    super(message, e);
  }
}
