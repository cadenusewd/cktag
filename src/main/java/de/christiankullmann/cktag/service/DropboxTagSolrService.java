package de.christiankullmann.cktag.service;

import de.christiankullmann.cktag.controller.DropboxTagController;
import de.christiankullmann.cktag.exception.DropboxTagNotFoundException;
import de.christiankullmann.cktag.exception.IncompleteTagEntityException;
import de.christiankullmann.cktag.exception.InternalServerException;
import de.christiankullmann.cktag.exception.MultipleTagIdsException;
import de.christiankullmann.cktag.solr.DropboxTag;
import de.christiankullmann.cktag.solr.DropboxTagAssembler;
import de.christiankullmann.cktag.util.ApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.MapSolrParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Service
@Slf4j
public class DropboxTagSolrService {

  private final String collectionName;

  private final SolrClientService clientService;

  private final DropboxTagAssembler assembler;

  private final ApiUtils apiUtils;

  /**
   * Create a new {@link DropboxTagSolrService}
   *
   * @param clientService  the {@link SolrClientService}
   * @param assembler      the {@link DropboxTagAssembler}
   * @param apiUtils       an {@link ApiUtils}
   * @param collectionName the collectionName
   */
  @Autowired
  public DropboxTagSolrService(SolrClientService clientService, DropboxTagAssembler assembler, ApiUtils apiUtils, @Value("${solr.tag.collection}") String collectionName) {
    Assert.notNull(clientService, "SolrClientService must not be null");
    Assert.notNull(assembler, "DropboxTagAssembler must not be null");
    Assert.notNull(apiUtils, "ApiUtils must not be null");
    this.clientService = clientService;
    this.assembler = assembler;
    this.apiUtils = apiUtils;
    this.collectionName = collectionName;
  }

  /**
   * Find and return all DropboxTag-Resources that correspond with the provided tags-String
   *
   * @param tags the tags that are to be looked for
   * @return a {@link Resources} of {@link Resource} of {@link DropboxTag}
   */
  public Resources<Resource<DropboxTag>> getAllByTags(String tags, String start, String rows) {
    log.debug("Calling getAllByTags with tags [{}], start [{}], rows [{}]", tags, start, rows);
    final Map<String, String> queryParamMap = new HashMap<>();
    queryParamMap.put("q", "tags:(" + tags + ")");
    queryParamMap.put("fl", "id, name, path, tags");
    queryParamMap.put("sort", "id asc");
    queryParamMap.put("start", start);
    queryParamMap.put("rows", rows);
    MapSolrParams queryParams = new MapSolrParams(queryParamMap);

    List<Resource<DropboxTag>> result = querySolrServer(queryParams);

    return new Resources<>(result,
        linkTo(methodOn(DropboxTagController.class).getAllDropboxTagsResourcesByTags(tags, start, rows)).withSelfRel());
  }

  /**
   * Find and return all DropboxTag-Resources that correspond with the provided tags-String
   *
   * @param tags the tags that are to be looked for
   * @return a {@link Resources} of {@link Resource} of {@link DropboxTag}
   */
  public List<DropboxTag> getAllByTagsAsList(String tags, String start, String rows) {
    final Map<String, String> queryParamMap = new HashMap<>();
    queryParamMap.put("q", "tags:(" + tags + ")");
    queryParamMap.put("fl", "id, name, path, tags");
    queryParamMap.put("sort", "id asc");
    queryParamMap.put("start", start);
    queryParamMap.put("rows", rows);
    MapSolrParams queryParams = new MapSolrParams(queryParamMap);

    return querySolrServerAsDropboxTags(queryParams);
  }


  /**
   * Find and return one DropboxTag-Resource by id
   *
   * @param id the id of the wanted resource
   * @return {@link Resource} of {@link DropboxTag}
   */
  public Resource<DropboxTag> getDropboxTagById(String id) {
    Resource<DropboxTag> result = null;
    return assembler.toResource(findDropboxTagIfExists(id));
  }


  /**
   * Add a new DropboxItem to the Solr-Core/-Collection
   *
   * @param newDropboxTagItem the item to be added
   * @return a {@link ResponseEntity} conntaining the information about the item-creation
   */
  public ResponseEntity<?> addDropboxTagEntry(DropboxTag newDropboxTagItem) {
    newDropboxTagItem.id = UUID.randomUUID().toString();
    final UpdateResponse response = clientService.commitDropboxTag(collectionName, newDropboxTagItem);

    Resource<DropboxTag> resource = assembler.toResource(newDropboxTagItem);
    try {
      return ResponseEntity
          .created(new URI(resource.getId().expand().getHref()))
          .body(resource);
    } catch (URISyntaxException e) {
      throw new InternalServerException("A URISyntaxException occurred [{}]", e);
    }
  }

  /**
   * Update a {@link DropboxTag} entity in Solr
   *
   * @param id         the id of the DropboxTag-Entity to be updated
   * @param dropBoxTag the new entity
   * @return a {@link ResponseEntity} conntaining the information about the item-update
   * <p>
   * TODO: Created might not be the correct response, as the item was already existing and has merely been updated
   */
  public ResponseEntity<?> patchUpdateDropboxTag(String id, DropboxTag dropBoxTag) {
    DropboxTag existingEntry = findDropboxTagIfExists(id);

    apiUtils.merge(existingEntry, dropBoxTag);

    if (existingEntry.name.length() > 0 && existingEntry.path.length() > 0) {
      UpdateResponse response = clientService.commitDropboxTag(collectionName, dropBoxTag);
    } else {
      throw new IncompleteTagEntityException(existingEntry.id, existingEntry.name, existingEntry.path);
    }
    Resource<DropboxTag> resource = assembler.toResource(existingEntry);
    try {
      return ResponseEntity
          .created(new URI(resource.getId().expand().getHref()))
          .body(resource);
    } catch (URISyntaxException e) {
      throw new InternalServerException("A URISyntaxException occurred [{}]", e);
    }
  }


  /**
   * Delete the DropboxTag entry on Solr for the provided id
   *
   * @param id the id of the {@link DropboxTag} to be deleted
   * @return the {@link ResponseEntity}
   */
  public ResponseEntity<?> deleteDropboxTag(String id) {
    DropboxTag existingEntry = findDropboxTagIfExists(id);
    if (null == existingEntry) {
      return ResponseEntity.notFound()
          .build();
    }

    UpdateResponse response = clientService.deleteDropboxTag(collectionName, id);
    return ResponseEntity.ok().body("DropboxTag with id [" + id + "] has been deleted.");

  }

  // private

  /**
   * Find and return a {@link DropboxTag} by id if it exists
   *
   * @param id the id
   * @return a {@link DropboxTag}-object with the provided id and contents from Solr
   */
  private DropboxTag findDropboxTagIfExists(String id) {
    DropboxTag result;

    final Map<String, String> queryParamMap = new HashMap<>();
    queryParamMap.put("q", "id:" + id);
    queryParamMap.put("fl", "id, name, path, tags");
    MapSolrParams queryParams = new MapSolrParams(queryParamMap);

    final QueryResponse response = clientService.queryClient(collectionName,
        queryParams);
    if (null == response) {
      throw new DropboxTagNotFoundException(id);
    }

    final SolrDocumentList documents = response.getResults();

    if (documents.isEmpty()) {
      throw new DropboxTagNotFoundException(id);
    } else if (documents.size() > 1) {
      throw new MultipleTagIdsException(id);
    } else {
      SolrDocument document = documents.get(0);
      String name = (String) document.getFirstValue("name");
      String path = (String) document.getFirstValue("path");
      String tag = (String) document.getFirstValue("tags");
      result = new DropboxTag(id, name, path, tag);

    }
    return result;
  }

  /**
   * Query the SolrServer with the provided {@link MapSolrParams}
   *
   * @param queryParams the query params in {@link MapSolrParams} format
   * @return a {@link List} of {@link Resources}s
   */
  List<Resource<DropboxTag>> querySolrServer(MapSolrParams queryParams) {
    List<DropboxTag> dropboxTagList = querySolrServerAsDropboxTags(queryParams);
    return dropboxTagList.stream().map(dropboxTag -> assembler.toResource(dropboxTag)).collect(Collectors.toList());
  }

  /**
   * Query the SolrServer with the provided {@link MapSolrParams}
   *
   * @param queryParams the query params in {@link MapSolrParams} format
   * @return a {@link List} of {@link Resources}s
   */
  List<DropboxTag> querySolrServerAsDropboxTags(MapSolrParams queryParams) {
    List<DropboxTag> result = new ArrayList<>();

    final QueryResponse response = clientService.queryClient(collectionName, queryParams);
    final SolrDocumentList documents = response.getResults();

    for (SolrDocument document : documents) {
      String id = (String) document.getFirstValue("id");
      String name = (String) document.getFirstValue("name");
      String path = (String) document.getFirstValue("path");
      String tag = (String) document.getFirstValue("tags");
      result.add(new DropboxTag(id, name, path, tag));
    }

    return result;
  }
}
