package de.christiankullmann.cktag.exception;

public class DropboxTagNotFoundException extends RuntimeException {
  public DropboxTagNotFoundException(String id) {
    super("No DropboxTag Entry with id [" + id + "] found in Solr.");
  }
}
