package de.christiankullmann.cktag.service;

import de.christiankullmann.cktag.solr.DropboxTag;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * This test class currently tests against mocks for the Solr client.
 * This should be replaced in the future with calls to an embedded Solr server (or similar) to test that the
 * Solr-API correctly answers incoming requests within the confines of this service.
 *
 * Within the scope of this task the setting up and evaluating how to realize such an embedded server would have taken too much time.
 * For a real world service however this test should be implemented.
 *
 */
@ExtendWith(MockitoExtension.class)
class SolrClientServiceTest {

  @Mock
  SolrClient clientMock;

  @Mock
  QueryResponse queryResponseMock;

  @Mock
  UpdateResponse updateResponseMock;

  private SolrClientService solrClientServiceSpy;

  @BeforeEach
  void setup() {
    solrClientServiceSpy = spy(new SolrClientService(""));
    when(solrClientServiceSpy.getSolrClient()).thenReturn(clientMock);
  }

  /**
   * Happy Path Test for the queryClient - method
   *
   * @throws Exception
   */
  @Test
  void queryClient() throws Exception {
    when(clientMock.query(anyString(), any(SolrParams.class))).thenReturn(queryResponseMock);

    MapSolrParams queryParams = mock(MapSolrParams.class);
    String collectionName = "cktag";

    QueryResponse response = solrClientServiceSpy.queryClient(collectionName, queryParams);

    assertThat(response).isNotNull();
    verify(clientMock, times(1)).query(collectionName,queryParams);

  }

  /**
   * Happy Path Test for the commitDropboxTag - method
   *
   * @throws Exception
   */
  @Test
  void commitDropboxTag() throws Exception {
    when(clientMock.addBean(anyString(), any(DropboxTag.class))).thenReturn(updateResponseMock);

    DropboxTag inputTag = new DropboxTag("1", "name", "path", "tag1, tag2, tag3");
    String collectionName = "cktag";


    UpdateResponse response = solrClientServiceSpy.commitDropboxTag(collectionName, inputTag);

    assertThat(response).isNotNull();
    verify(clientMock, times(1)).addBean(collectionName, inputTag);
    verify(clientMock, times(1)).commit(collectionName);
  }

  /**
   * Happy Path Test for the deleteDropboxTag - method
   *
   * @throws Exception
   */
  @Test
  void deleteDropboxTag() throws Exception {
    when(clientMock.deleteById(anyString(), anyString())).thenReturn(updateResponseMock);

    String collectionName = "cktag";
    String id = "1";

    UpdateResponse response = solrClientServiceSpy.deleteDropboxTag(collectionName, id);

    assertThat(response).isNotNull();
    verify(clientMock, times(1)).deleteById(collectionName, id);
    verify(clientMock, times(1)).commit(collectionName);

  }
}