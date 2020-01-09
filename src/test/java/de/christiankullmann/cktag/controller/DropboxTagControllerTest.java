package de.christiankullmann.cktag.controller;

import de.christiankullmann.cktag.service.DropboxConnectionService;
import de.christiankullmann.cktag.service.DropboxTagSolrService;
import de.christiankullmann.cktag.service.SolrClientService;
import de.christiankullmann.cktag.solr.DropboxTag;
import de.christiankullmann.cktag.solr.DropboxTagAssembler;
import de.christiankullmann.cktag.util.ApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.MapSolrParams;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(DropboxTagController.class)
@DirtiesContext
public class DropboxTagControllerTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private DropboxTagController controller;

  @TestConfiguration
  @Slf4j
  static class ControllerTestConfiguration {

    private String collectionName = "cktagtest";
    private String tags = "tag1, tag2, tag3";
    private String start = "0";
    private String rows = "20";

    public DropboxTag dropboxTag = new DropboxTag("1", "name1", "/path1", tags);

    public static SolrClientService solrClientService = mock(SolrClientService.class);

    @Bean
    DropboxTagAssembler getAssembler() {
      return new DropboxTagAssembler();
    }

    @Bean
    DropboxConnectionService getConnectionService() {
      return mock(DropboxConnectionService.class);
    }

    @Bean
    DropboxTagSolrService getDropboxTagSolrService() {
      DropboxTagAssembler assembler = new DropboxTagAssembler();
      DropboxTagSolrService dropboxTagSolrService = new DropboxTagSolrService(solrClientService, assembler, new ApiUtils(), collectionName);
      return dropboxTagSolrService;
    }
  }

  /**
   * Test the getAll with one DropboxTag in Solr
   *
   * @throws Exception
   */
  @Test
  public void getterShouldReturnAllEntriesWithOffset0AndLimit10() throws Exception {
    doAnswer(initQuerySolrClientForTag("1", "name1", "/path1", "tag1, tag2, tag3")).when(ControllerTestConfiguration.solrClientService).queryClient(anyString(), any(MapSolrParams.class));
    this.mvc.perform(get("/api/v1/dropboxtags")).andExpect(status().isOk())
        .andExpect(content().string("{\"_embedded\":{\"dropboxTagList\":[{\"id\":\"1\",\"name\":\"name1\",\"path\":\"/path1\",\"tags\":\"tag1, tag2, tag3\",\"_links\":{\"self\":{\"href\":\"http://localhost/api/v1/dropboxtags/1\"},\"dropboxtags\":{\"href\":\"http://localhost/api/v1/dropboxtags?tags=*&offset=0&limit=10\"}}}]},\"_links\":{\"self\":{\"href\":\"http://localhost/api/v1/dropboxtags?tags=*&offset=0&limit=10\"}}}"));

  }

  /**
   * Test the getById with one DropboxTag in Solr
   *
   * @throws Exception
   */
  @Test
  public void getByIdShouldReturnOneEntry() throws Exception {
    doAnswer(initQuerySolrClientForTag("1", "name1", "/path1", "tag1, tag2, tag3")).when(ControllerTestConfiguration.solrClientService).queryClient(anyString(), any(MapSolrParams.class));
    this.mvc.perform(get("/api/v1/dropboxtags/1")).andExpect(status().isOk())
        .andExpect(content().string("{\"id\":\"1\",\"name\":\"name1\",\"path\":\"/path1\",\"tags\":\"tag1, tag2, tag3\",\"_links\":{\"self\":{\"href\":\"http://localhost/api/v1/dropboxtags/1\"},\"dropboxtags\":{\"href\":\"http://localhost/api/v1/dropboxtags?tags=*&offset=0&limit=10\"}}}"));
  }

  /**
   * Test the delete of one entry
   *
   * @throws Exception
   */
  @Test
  public void deleteByIdOnlyOnce() throws Exception {
    doAnswer(initQuerySolrClientForTag("1", "name1", "/path1", "tag1, tag2, tag3")).when(ControllerTestConfiguration.solrClientService).queryClient(anyString(), any(MapSolrParams.class));

    this.mvc.perform(delete("/api/v1/dropboxtags/1")).andExpect(status().isOk())
        .andExpect((content().string("DropboxTag with id [1] has been deleted.")));
    doReturn(null).when(ControllerTestConfiguration.solrClientService).queryClient(anyString(), any(MapSolrParams.class));
    this.mvc.perform(delete("/api/v1/dropboxtags/1")).andExpect(status().isNotFound())
        .andExpect((content().string("{\"code\":\"DROPBOX_TAG_ENTRY_NOT_FOUND\",\"message\":\"No DropboxTag Entry with id [1] found in Solr.\"}")));

  }

  /**
   * Test deleting an entry that is not there
   * @throws Exception
   */
  @Test
  public void deleteByIdNotFound() throws Exception {
    doReturn(null).when(ControllerTestConfiguration.solrClientService).queryClient(anyString(), any(MapSolrParams.class));
    this.mvc.perform(delete("/api/v1/dropboxtags/99")).andExpect(status().isNotFound())
        .andExpect((content().string("{\"code\":\"DROPBOX_TAG_ENTRY_NOT_FOUND\",\"message\":\"No DropboxTag Entry with id [99] found in Solr.\"}")));
  }

  /**
   * Test adding a DropboxTag
   * @throws Exception
   */
  @Test
  public void addDropboxTag() throws Exception {
    MvcResult result = this.mvc.perform(post("/api/v1/dropboxtags")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"name\": \"myCookbook.doc\", \"path\": \"/myCookbook.doc\", \"tags\": \"cooking,book\"}"))
        .andExpect(status().isCreated()).andReturn();

    String content = result.getResponse().getContentAsString();
    assertThat(content.endsWith(",\"name\":\"myCookbook.doc\",\"path\":\"/myCookbook.doc\",\"tags\":\"cooking,book\",\"_links\":{\"self\":{\"href\":\"http://localhost/api/v1/dropboxtags/2\"},\"dropboxtags\":{\"href\":\"http://localhost/api/v1/dropboxtags?tags=*&offset=0&limit=10\"}}}"));
    assertThat(content.startsWith("{\"id\":"));
    int idStart = 7; // position of cursor after '{"id":"
    int idEnd = content.indexOf(",\"name\":");
    String idStringFromContent = content.substring(idStart, idEnd);
    assertThat(idStringFromContent.contains(",")).isFalse();
  }

  private Answer initQuerySolrClientForTag(String id, String name, String path, String tags) {

    return new Answer() {
      @Override
      public QueryResponse answer(InvocationOnMock invocationOnMock) throws Throwable {
        QueryResponse response = mock(QueryResponse.class);

        SolrDocument document = mock(SolrDocument.class);
        when(document.getFirstValue("id")).thenReturn(id);
        when(document.getFirstValue("name")).thenReturn(name);
        when(document.getFirstValue("path")).thenReturn(path);
        when(document.getFirstValue("tags")).thenReturn(tags);

        SolrDocumentList solrDocumentList = new SolrDocumentList();
        solrDocumentList.add(document);
        when(response.getResults()).thenReturn(solrDocumentList);
        return response;
      }
    };
  }
}