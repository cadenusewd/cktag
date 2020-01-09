package de.christiankullmann.cktag.solr;

import de.christiankullmann.cktag.controller.DropboxTagController;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class DropboxTagAssembler implements ResourceAssembler<DropboxTag, Resource<DropboxTag>> {

  @Override
  public Resource<DropboxTag> toResource(DropboxTag dropboxTag) {

    return new Resource<>(dropboxTag,
        linkTo(methodOn(DropboxTagController.class).getOneDropboxTagResourceById(dropboxTag.id)).withSelfRel(),
        linkTo(methodOn(DropboxTagController.class).getAllDropboxTagsResourcesByTags("*", "0", "10")).withRel("dropboxtags"));
  }
}
