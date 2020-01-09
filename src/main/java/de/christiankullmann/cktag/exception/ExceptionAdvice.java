package de.christiankullmann.cktag.exception;

import com.dropbox.core.DbxException;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.common.SolrException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
class ExceptionAdvice {

  @ResponseBody
  @ExceptionHandler(DropboxTagNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  ErrorResponse handleDropboxTagNotFoundException(DropboxTagNotFoundException ex) {
    log.error("A DropboxTagNotFoundException occurred: [{}]", ex.getMessage(), ex);
    return new ErrorResponse("DROPBOX_TAG_ENTRY_NOT_FOUND", ex.getMessage());
  }

  @ResponseBody
  @ExceptionHandler(MultipleTagIdsException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  ErrorResponse handleMultipleTagIdsException(MultipleTagIdsException ex) {
    log.error("A MultipleTagIdsException occurred: [{}]", ex.getMessage(), ex);
    return new ErrorResponse("MULTIPLE_TAG_IDS_FOUND", ex.getMessage());
  }

  @ResponseBody
  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  ErrorResponse handleResourceNotFoundException(ResourceNotFoundException ex) {
    log.error("A ResourceNotFoundException occurred: [{}]", ex.getMessage(), ex);
    return new ErrorResponse("RESOURCE_NOT_FOUND", ex.getMessage());
  }

  @ResponseBody
  @ExceptionHandler(NullPointerException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  ErrorResponse handleNullPointerException(NullPointerException ex) {
    log.error("A NullPointerException occurred: [{}]", ex.getMessage(), ex);
    return new ErrorResponse("NULL_POINTER_EXCEPTION", ex.getMessage());
  }

  @ResponseBody
  @ExceptionHandler(IncompleteTagEntityException.class)
  @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
  ErrorResponse handleIncompleteTagEntityException(IncompleteTagEntityException ex) {
    log.error("A IncompleteTagEntityException occurred: [{}]", ex.getMessage(), ex);
    return new ErrorResponse("INCOMPLETE_TAG_ENTITY", ex.getMessage());
  }

  @ResponseBody
  @ExceptionHandler(InternalServerException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  ErrorResponse handleInternalServerException(InternalServerException ex) {
    log.error("A InternalServerException occurred: [{}]", ex.getMessage(), ex);
    return new ErrorResponse("INTERNAL_SERVER_ERROR", "Please contact the administrator.");
  }

  @ResponseBody
  @ExceptionHandler(SolrException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  ErrorResponse handleSolrException(SolrException ex) {
    log.error("A SolrException occurred: [{}]", ex.getMessage(), ex);
    return new ErrorResponse("SolrException", ex.getMessage());
  }

  @ResponseBody
  @ExceptionHandler(DbxException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  ErrorResponse handleDbxException(DbxException ex) {
    log.error("A DbxException occurred: [{}]", ex.getMessage(), ex);
    return new ErrorResponse("DbxException", ex.getMessage());
  }

  @ResponseBody
  @ExceptionHandler(DropboxDownloadException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  ErrorResponse handleDropboxDownloadException(DropboxDownloadException ex) {
    log.error("A DropboxDownloadException occurred: [{}]", ex.getMessage(), ex);
    return new ErrorResponse("DropboxDownloadException", ex.getMessage());
  }

  @ResponseBody
  @ExceptionHandler(UpdateException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  ErrorResponse handleUpdateException(UpdateException ex) {
    log.error("A UpdateException occurred: [{}]", ex.getMessage(), ex);
    return new ErrorResponse("UpdateException", ex.getMessage());
  }

}