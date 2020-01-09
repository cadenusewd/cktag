package de.christiankullmann.cktag.controller;

import com.dropbox.core.DbxException;
import de.christiankullmann.cktag.service.DropboxConnectionService;
import de.christiankullmann.cktag.service.DropboxTagSolrService;
import de.christiankullmann.cktag.solr.DropboxTag;
import de.christiankullmann.cktag.solr.DropboxTagAssembler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("api/v1")
public class DropboxTagController {

  @Value("${solr.tag.collection}")
  private String collectionName;

  @Value("${dropbox.access.token}")
  private String accessToken;

  private final DropboxTagSolrService dropboxTagSolrService;

  private final DropboxConnectionService dropboxConnectionService;

  @Autowired
  public DropboxTagController(DropboxTagSolrService dropboxTagSolrService, DropboxTagAssembler assembler, DropboxConnectionService dropboxConnectionService) {
    Assert.notNull(dropboxTagSolrService, "dropboxTagService must not be null");
    Assert.notNull(dropboxConnectionService, "dropboxConnectionService must not be null");
    this.dropboxTagSolrService = dropboxTagSolrService;
    this.dropboxConnectionService = dropboxConnectionService;
  }

  /**
   * Get all entries found in Solr as {@link Resources} containing the tags
   *
   * @param tags the tags to be searched for
   * @return all entries returned by Solr matching the search-tags
   */
  @GetMapping(path = "dropboxtags", produces = {MediaType.APPLICATION_JSON_VALUE})
  public Resources<Resource<DropboxTag>> getAllDropboxTagsResourcesByTags(@RequestParam(value = "tags", defaultValue = "*") String tags,
                                                                          @RequestParam(value = "offset", defaultValue = "0") String start,
                                                                          @RequestParam(value = "limit", defaultValue = "10") String rows) {
    log.debug("Calling getAllDropboxTagsResourcesByTags with tags [{}], start [{}], rows [{}]", tags, start, rows);
    return dropboxTagSolrService.getAllByTags(tags, start, rows);
  }

  /**
   * Look for a single entry in Solr using the entry's id
   *
   * @param id the id to be searched for
   * @return a {@link Resource}
   */
  @GetMapping("dropboxtags/{id}")
  public Resource<DropboxTag> getOneDropboxTagResourceById(@PathVariable String id) {
    return dropboxTagSolrService.getDropboxTagById(id);

  }

  /**
   * Add a new DropboxItem to Solr by adding a randomly generated ID.
   * This is a workaround as it allows to add the same document multiple times.
   *
   * @param newDropboxTagItem a {@link DropboxTag} in JSON format
   * @return a {@link ResponseEntity}
   */
  @PostMapping("dropboxtags")
  ResponseEntity<?> addTagEntry(@RequestBody DropboxTag newDropboxTagItem) {
    return dropboxTagSolrService.addDropboxTagEntry(newDropboxTagItem);
  }

  /**
   * Patch/Update a {@link DropboxTag} by id
   *
   * @param id         the id of the dropboxtag to update
   * @param dropBoxTag the new dropboxtag
   * @return a {@link ResponseEntity}
   */
  @PatchMapping("dropboxtags/{id}")
  ResponseEntity<?> updateTags(@PathVariable String id, @RequestBody DropboxTag dropBoxTag) {
    return dropboxTagSolrService.patchUpdateDropboxTag(id, dropBoxTag);
  }

  /**
   * Delete the {@link DropboxTag} entry in Solr by id
   *
   * @param id id of the {@link DropboxTag} entry to be removed from the Solr index
   * @return a {@link ResponseEntity}
   */
  @DeleteMapping("dropboxtags/{id}")
  ResponseEntity<?> deleteDropboxTag(@PathVariable String id) {
    return dropboxTagSolrService.deleteDropboxTag(id);
  }

  /**
   * Download all files that are tagged with <b>tags</b> in Solr from Dropbox as a zipfile to the provided download location
   *
   * @param tags the tags
   * @return a {@link ResponseEntity} when finished
   */
  @GetMapping(path = "dropboxtags/zipped")
  public ResponseEntity<?> zipFilesByTag(@RequestParam(value = "tags", defaultValue = "*") String tags) {

    List<DropboxTag> dropboxTagList = dropboxTagSolrService.getAllByTagsAsList(tags, "0", Integer.toString(Integer.MAX_VALUE));
    return dropboxConnectionService.downloadTaggedFilesToZipFileFromDropbox(tags, dropboxTagList);
  }
}
