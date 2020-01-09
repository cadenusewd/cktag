package de.christiankullmann.cktag.exception;

public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException(Throwable e) {
    super(e);
  }
}
