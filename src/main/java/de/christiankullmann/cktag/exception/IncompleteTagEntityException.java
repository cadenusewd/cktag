package de.christiankullmann.cktag.exception;

public class IncompleteTagEntityException extends RuntimeException {
  public IncompleteTagEntityException(String id, String name, String path) {
    super("Entity with id [" + id + "] is missing either name [" + name + "] or path [" + path + "].");
  }
}
