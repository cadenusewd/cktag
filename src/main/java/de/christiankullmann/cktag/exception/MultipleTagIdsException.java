package de.christiankullmann.cktag.exception;

public class MultipleTagIdsException extends RuntimeException {
  public MultipleTagIdsException(String id) {
    super("Multiple tag-entries found for id [" + id + "].");
  }
}
