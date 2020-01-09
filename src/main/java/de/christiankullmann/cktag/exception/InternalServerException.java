package de.christiankullmann.cktag.exception;

/**
 * An Exception for this API if anything happens in communication with Solr.
 * The Solr-API only throws SolrException and SolrServerException, both not RuntimeExceptions and thusly not quite as easily handable using the ExceptionAdvice
 */
public class InternalServerException extends RuntimeException {

  public InternalServerException() {
    super("A problem with the SolrServer occurred. Please try again later.");
  }

  public InternalServerException(String message) {
    super(message);
  }

  public InternalServerException(String message, Throwable e) {
    super(message, e);
  }
}
