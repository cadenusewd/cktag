package de.christiankullmann.cktag.service;

import de.christiankullmann.cktag.exception.InternalServerException;
import de.christiankullmann.cktag.exception.ResourceNotFoundException;
import de.christiankullmann.cktag.solr.DropboxTag;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.MapSolrParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
@Slf4j
public class SolrClientService {

  private final String solrUrl;

  private SolrClient client;

  /**
   * Create a new SolrClientService with the url to an Apache Solr
   *
   * @param solrUrl the url to an Apache Solr as String
   */
  public SolrClientService(@Value("${solr.hosturl}") String solrUrl) {
    this.solrUrl = solrUrl;
  }

  /**
   * Send a Solr-Query to the client
   *
   * @param collectionName the name of the collection
   * @param queryParams the query parameters
   * @return a {@link QueryResponse}
   */
  public QueryResponse queryClient(String collectionName, MapSolrParams queryParams) {
    if (client == null) {
      client = getSolrClient();
    }
    QueryResponse response;
    try {
      response = client.query(collectionName, queryParams);
    } catch (SolrServerException e) {
      throw new InternalServerException(e.getMessage());
    } catch (IOException e) {
      if (e.getClass().getSimpleName().equals(FileNotFoundException.class.getSimpleName())) {
        throw new ResourceNotFoundException(e);
      }
      throw new RuntimeException(e);
    }
    return response;
  }

  /**
   * Add a {@link DropboxTag} to Apache Solr to a specific collection
   *
   * @param collectionName the name of the collection
   * @param dropboxTag the {@link DropboxTag}
   * @return an {@link UpdateResponse}
   * }
   */
  public UpdateResponse commitDropboxTag(String collectionName, DropboxTag dropboxTag) {
    if (client == null) {
      client = getSolrClient();
    }
    try {
      final UpdateResponse response = client.addBean(collectionName, dropboxTag);
      client.commit(collectionName);
      return response;
    } catch (SolrException e) {
      throw new InternalServerException(e.getMessage());
    } catch (SolrServerException e) {
      throw new InternalServerException("A SolrServerException occurred", e);
    } catch (IOException e) {
      if (e.getClass().getSimpleName().equals(FileNotFoundException.class.getSimpleName())) {
        throw new ResourceNotFoundException(e);
      }
      throw new RuntimeException(e);
    }
  }

  /**
   * Delete the {@link DropboxTag} with the provided id from the collection identified by the collectionName
   *
   * @param collectionName name of the collection in Apache Solr
   * @param id             the id of the {@link DropboxTag} to delete
   */
  public UpdateResponse deleteDropboxTag(String collectionName, String id) {
    if (client == null) {
      client = getSolrClient();
    }
    try {
      final UpdateResponse response = client.deleteById(collectionName, id);
      client.commit(collectionName);
      return response;
    } catch (SolrException e) {
      throw new InternalServerException(e.getMessage());
    } catch (SolrServerException e) {
      throw new InternalServerException("A SolrServerException occurred", e);
    } catch (IOException e) {
      if (e.getClass().getSimpleName().equals(FileNotFoundException.class.getSimpleName())) {
        throw new ResourceNotFoundException(e);
      }
      throw new RuntimeException(e);
    }
  }

  /**
   * Close the client prior to destroying this bean
   *
   * @throws IOException is thrown in case {@link #client} has a problem during closing
   */
  @PreDestroy
  public void preDestroy() throws IOException {
    if (null != client) {
      client.close();
    }
  }

  /**
   * Return a {@link SolrClient}
   *
   * @return a {@link SolrClient}
   */
  protected SolrClient getSolrClient() {
      return new HttpSolrClient.Builder(solrUrl)
          .withConnectionTimeout(10000)
          .withSocketTimeout(60000)
          .build();
  }
}
