package de.christiankullmann.cktag.service;

import de.christiankullmann.cktag.solr.DropboxTag;
import de.christiankullmann.cktag.solr.DropboxTagAssembler;
import de.christiankullmann.cktag.util.ApiUtils;
import org.apache.solr.common.params.MapSolrParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DropboxTagSolrServiceTest {

  private static final String COLLECTION_NAME = "cktag";

  @Mock
  private SolrClientService clientService;

  private DropboxTagAssembler assembler = new DropboxTagAssembler();

  @Mock
  private ApiUtils apiUtils;
  
  private DropboxTagSolrService dropboxTagSolrServiceSpy;

  MapSolrParams queryParams = generateQueryParams();

  private static MapSolrParams generateQueryParams() {
    final Map<String, String> queryParamMap = new HashMap<>();
    queryParamMap.put("q", "tags:(testTag1 AND testTag2)");
    queryParamMap.put("fl", "id, name, path, tags");
    queryParamMap.put("sort", "id asc");
    queryParamMap.put("start", "0");
    queryParamMap.put("rows", "20");
    return new MapSolrParams(queryParamMap);
  }


  @BeforeEach
  void setUp() {
    dropboxTagSolrServiceSpy = spy(new DropboxTagSolrService(clientService, assembler, apiUtils, COLLECTION_NAME));
  }

  @Test
  void querySolrServerAsDropboxTags() {
    List<DropboxTag> resourceList = dropboxTagSolrServiceSpy.querySolrServerAsDropboxTags(queryParams);

    verify(clientService, times(1)).queryClient(COLLECTION_NAME, queryParams);
  }

  @Test
  void addDropboxTagEntry() {
    DropboxTag dropboxTag = new DropboxTag("","dropboxtagname","dropboxtagpath","dropboxtagtags");
    ResponseEntity responseEntity = dropboxTagSolrServiceSpy.addDropboxTagEntry(dropboxTag);

    assertNotNull(responseEntity);
  }
}