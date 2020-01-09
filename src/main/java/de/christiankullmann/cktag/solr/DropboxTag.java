package de.christiankullmann.cktag.solr;

import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.solr.client.solrj.beans.Field;

/**
 * A data bean for each entry to Solr
 */
@ToString
@NoArgsConstructor
public class DropboxTag {

  @Field public String id;
  @Field public String name;
  @Field public String path;
  @Field public String tags;

  public DropboxTag(String id, String name, String path, String tags) {
    this.id = id;
    this.name = name;
    this.path = path;
    this.tags = tags;
  }
}
