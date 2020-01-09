package de.christiankullmann.cktag.exception;

/**
 * An exception to handle all errors occurring during Download from Dropbox
 */
public class DropboxDownloadException extends RuntimeException {

  public DropboxDownloadException(String message) {
    super(message);
  }

  public DropboxDownloadException(String message, Throwable e) {
    super(message, e);
  }
}
