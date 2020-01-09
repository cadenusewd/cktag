package de.christiankullmann.cktag.service;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.Metadata;
import de.christiankullmann.cktag.exception.DropboxDownloadException;
import de.christiankullmann.cktag.solr.DropboxTag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The DroboxConnectionService provides all necessary functions to retrieve data from DropBox
 * To work properly it needs a Dropbox Access Token issued by Dropbox for an application.
 */
@Service
@Slf4j
public class DropboxConnectionService {

  private final String accessToken;


  private final int maxDownloadFilesize;

  private DropboxConnectionService(@Value("${dropbox.access.token}") String accessToken,
                                   @Value("${dropbox.max.downloadSize}") int maxDownloadFilesize) {
    this.accessToken = accessToken;
    this.maxDownloadFilesize = maxDownloadFilesize;
  }

  /**
   * Download the files referenced in the provided {@link List}&lt;{@link DropboxTag}&gt; from Dropbox, compress these using ZIP and return as part of the Response for download
   *
   * @param tags           the tags used to select the DropboxTag-Entities from Solr
   * @param dropboxTagList the list of {@link DropboxTag}s containing the information of the files to be downloaded and compressed
   * @return a {@link ResponseEntity} containing the zip-archive
   */
  public ResponseEntity<?> downloadTaggedFilesToZipFileFromDropbox(String tags, List<DropboxTag> dropboxTagList) {
    DbxRequestConfig config = DbxRequestConfig.newBuilder("CKTagger").build();
    DbxClientV2 client = new DbxClientV2(config, accessToken);

    List<Metadata> metadataList = collectDropboxMetadataForDropboxTags(dropboxTagList, client);
    Optional<Integer> overallSize = metadataList.stream()
        .map(data -> new JSONObject((data.toString())))
        .filter(json -> json.has("size"))
        .map(json -> json.getInt("size"))
        .reduce((size1, size2) -> size1 + size2);
    int downloadFileSize = overallSize.isPresent() ? overallSize.get() : 0;

    log.debug("OverallSize [{}]", overallSize.isPresent() ? overallSize.get() : "unknown size");

    if (downloadFileSize > maxDownloadFilesize) {
      throw new DropboxDownloadException("Size of selected files exceeds the maximum allowed size of [" + maxDownloadFilesize + "]");
    }

    try (
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(bos)) {
      for (DropboxTag dropboxTag : dropboxTagList) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
          addDropboxFileToZip(client, zipArchiveOutputStream, dropboxTag, byteArrayOutputStream);
        } catch (IOException | DbxException e) {
          throw new DropboxDownloadException("Error occurred during download of [" + dropboxTag.path + "]", e);
        }
      }
      zipArchiveOutputStream.close();
      byte[] zipFile = bos.toByteArray();
      if (null != zipFile && zipFile.length > 0) {
        ByteArrayResource resource = new ByteArrayResource(zipFile);
        return ResponseEntity.ok()
            .contentLength(zipFile.length)
            .contentType(MediaType.asMediaType(MimeType.valueOf("application/zip")))
            .body(resource);
      } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No zipfile has been created. Please contact your provider.");
      }
    } catch (IOException e) {
      throw new DropboxDownloadException("Error occurred during creation of zipfile for tags [" + tags + "]");
    }
  }

  /**
   * Retrieve the {@link Metadata} objects for the {@link DropboxTag} objects
   *
   * @param dropboxTagList a {@link List} of {@link DropboxTag}s
   * @param client         the {@link DbxClientV2} to use for the retrieval
   * @return a {@link List} of {@link Metadata}
   */
  private List<Metadata> collectDropboxMetadataForDropboxTags(List<DropboxTag> dropboxTagList, DbxClientV2 client) {
    return dropboxTagList.stream()
        .map(tag -> tag.path)
        .map(path -> {
          Metadata result;
          try {
            result = client.files().getMetadata(path);
          } catch (DbxException e) {
            throw new DropboxDownloadException("Unable to retrieve metadata for path [" + path + "]");
          }
          return result;
        })
        .collect(Collectors.toList());
  }


  /**
   * Add downloaded file to the zip archive
   *
   * @param client                 the {@link DbxClientV2}
   * @param zipArchiveOutputStream a {@link ZipArchiveOutputStream}
   * @param dropboxTag             a {@link DropboxTag}
   * @param byteArrayOutputStream  a {@link ByteArrayOutputStream}
   * @throws DbxException if DropBox causes an error, a {@link DbxException} is thrown
   * @throws IOException  if the Streams cause an error, an {@link IOException} is thrown
   */
  private void addDropboxFileToZip(DbxClientV2 client, ZipArchiveOutputStream zipArchiveOutputStream, DropboxTag dropboxTag, ByteArrayOutputStream byteArrayOutputStream) throws DbxException, IOException {
    DbxDownloader downloader = client.files().download(dropboxTag.path);
    downloader.download(byteArrayOutputStream);

    ZipArchiveEntry archiveEntry = new ZipArchiveEntry(dropboxTag.name);
    archiveEntry.setSize(byteArrayOutputStream.size());
    zipArchiveOutputStream.putArchiveEntry(archiveEntry);
    zipArchiveOutputStream.write(byteArrayOutputStream.toByteArray());
    zipArchiveOutputStream.closeArchiveEntry();
    log.debug("Name [{}], Compressed size [{}], size [{}]", dropboxTag.name, archiveEntry.getCompressedSize(), archiveEntry.getSize());
  }


}
