# CKTag REST Service

This service is the result for the task provided by Eurowings Digital in the scope of the application process.

## Description
The service allows to store, remove and search for tags for Dropbox-Metadata in an Apache Solr instance.

## Getting started
This service comes with three docker-compose-files:

  `docker-compose.yml`   
  This file contains the necessary configuration to run the CKTag REST service.
  
  `cktag-and-solr.docker-compose.yml`   
  This docker-compose has an additional Apache Solr-container configured to test the service standalone.
  
  `solr-only.docker-compose.yml`   
  In this file only the Apache Solr container is configured. It can be used during development stage.

To run you need to execute

  ```
  docker-compose build
  docker-compose -f <docker-compose-filename> up
  ```
  
### docker-compose content

    
    version: "3.2"
    
    networks:
      cktag-network:
        driver: bridge
    
    services:
      cktag:
        build: ./cktag
        container_name: cktag
        ports:
          - "8080:8080"
        networks:
          - cktag-network
        environment:
          - DROPBOX_MAX_DOWNLOADSIZE=524288000
          - DROPBOX_ACCESS_TOKEN=<DROP-BOX-ACCESS-TOKEN>
          - SOLR_HOST_URL=<Apache-Solr-Host-URL>
          - SOLR_TAG_COLLECTION=<Solr collection or core name>
          
#### Environment variables
In the docker-compose files you can configure the service for your environment.

##### DROPBOX_MAXDOWNLOADSIZE
You can configure the maximum size in bytes of selected files (uncompressed) that CKTag should allow when creating a ZIP-archive.

##### DROPBOX_ACCESS_TOKEN
In order to access any Dropbox-Account, you need to get an access token for a registered app with that account.
CKTag REST service **does not** provide a means to login to a Dropbox Account and register an application.

##### SOLR_HOST_URL
The complete base URL to your Apache Solr installation, e.g. `http://localhost:8983/solr`

##### SOLR_TAG_COLLECTION
The name of the core or collection inside Apache Solr that you would like to use. 
It must already exist.

## REST-API Description    

### Add tags for Dropbox information

  Adds a new Dropboxtag information to the index and returns the newly created entry in JSON.
  In order to add one or more tags to a Dropbox-Metadata using the service, you have to provide 
  the name and path of the element you want to add. You can retrieve this information using the Dropbox-API.

* **URL**

  /api/v1/dropboxtags

* **Method:**

  `POST`
  
*  **URL Params**

  None

* **Data Params**

   **Required:**
   **Content:** `{"name": "myCookbook.doc", "path": "/myCookbook.doc", "tags": "cooking,book"}`  

* **Success Response:**

  * **Code:** 201 <br />
    **Content:** `{"id":"09d21416-ca08-4295-a067-37d2f15d3316","name":"myCookbook.doc","path":"/myCookbook.doc","tags":"cooking,book","_links":{"self":{"href":"<hostUrl>/api/v1/dropboxtags/09d21416-ca08-4295-a067-37d2f15d3316"},"tags":{"href":"<hostUrl>/api/v1/dropboxtags?tags=*&offset=0&limit=10"}}}`
 
* **Error Response:**

  * **500:** 500 INTERNAL SERVER ERROR <br />
    **Content:** `{"code":"INTERNAL_SERVER_ERROR","message":"A SolrServerException occurred"}`


* **Sample Call:**

  `curl -v -X POST <hosturl>/api/v1/dropboxtags -H 'Content-type:application/json' -d '{"name": "myCookbook.doc", "path": "/myCookbook.doc", "tags": "cooking,book"}'`
  
### Show stored Dropboxtags

  Retrieve stored information from the Solr index

* **URL**

  /api/v1/dropboxtags?tags=:tags&offset=:offset&limit=:limit

* **Method:**

  `GET`
  
*  **URL Params**

  **Required:**
  
  None

  **Optional:**
  
  `tags=[alphanumeric]`  
  example: tags=cooking%20AND%20book
  
  `offset=[integer]`  
  example: offset=0
 
  `limit=[integer]`  
  example: limit=14

* **Data Params**

   **Required:**
   
   None
   
   **Optional:**
   
   None  

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `{"_embedded":{"dropboxTagList":[{"id":"09d21416-ca08-4295-a067-37d2f15d3316","name":"myCookbook.doc","path":"/myCookbook.doc","tags":"cooking,book","_links":{"self":{"href":"<hostUrl>/api/v1/dropboxtags/09d21416-ca08-4295-a067-37d2f15d3316"},"tags":{"href":"<hostUrl>/api/v1/dropboxtags?tags=*&offset=0&limit=10"}}}]},"_links":{"self":{"href":"<hostUrl>/api/v1/dropboxtags?tags=book&offset=0&limit=1"}}}`
 
* **Error Response:**

  * **500:** 500 INTERNAL SERVER ERROR <br />
    **Content:** `{"code":"INTERNAL_SERVER_ERROR","message":"A SolrServerException occurred"}`


* **Sample Call:**

  `curl -X GET "<hostUrl>/api/v1/dropboxtags?tags=book&offset=0&limit=1"`
      
### Update stored Dropboxtags

  Update the stored Dropboxtag to add, change or remove name, path or tags

* **URL**

  /api/v1/dropboxtags/{id}   
  example: /api/v1/dropboxtags/09d21416-ca08-4295-a067-37d2f15d3316

* **Method:**

  `PATCH`
  
*  **URL Params**

  **Required:**
  
  None

  **Optional:**
  
  None

* **Data Params**

   **Required:**
   
   **Content:**  `{"id":"09d21416-ca08-4295-a067-37d2f15d3316","name":"myCookbook.doc","path":"/myCookbook.doc","tags":"new age cooking,book,updated"}`
   
   **Optional:**
   
   None  

* **Success Response:**

  * **Code:** 201 <br />
    **Content:** `{"id":"09d21416-ca08-4295-a067-37d2f15d3316","name":"myCookbook.doc","path":"/myCookbook.doc","tags":"new age cooking,book,updated","_links":{"self":{"href":"<hostUrl>/api/v1/dropboxtags/09d21416-ca08-4295-a067-37d2f15d3316"},"dropboxtags":{"href":"<hostUrl>/api/v1/dropboxtags?tags=*&offset=0&limit=10"}}}`
 
* **Error Response:**

  * **500:** 500 INTERNAL SERVER ERROR <br />
    **Content:** `{"code":"INTERNAL_SERVER_ERROR","message":"A SolrServerException occurred"}`


* **Sample Call:**

  `curl -v -X PATCH <hostUrl>/api/v1/dropboxtags/09d21416-ca08-4295-a067-37d2f15d3316 -H 'Content-type:application/json' -d '{"id":"09d21416-ca08-4295-a067-37d2f15d3316","name":"myCookbook.doc","path":"/myCookbook.doc","tags":"new age cooking,book,updated"}'`
    
    
### Delete a stored Dropboxtags-entry

  Delete a stored Dropboxtag-entry by id

* **URL**

  /api/v1/dropboxtags/{id}   
  example: /api/v1/dropboxtags/09d21416-ca08-4295-a067-37d2f15d3316

* **Method:**

  `DELETE`
  
*  **URL Params**

  **Required:**
  
  None

  **Optional:**
  
  None

* **Data Params**

   **Required:**
   
   None
   
   **Optional:**
   
   None  

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `DropboxTag with id [09d21416-ca08-4295-a067-37d2f15d3316] has been deleted.`
 
* **Error Response:**

  * **500:** 500 INTERNAL SERVER ERROR <br />
    **Content:** `{"code":"INTERNAL_SERVER_ERROR","message":"A SolrServerException occurred"}`


* **Sample Call:**

  `curl -v -X DELETE <hostUrl>/api/v1/dropboxtags/09d21416-ca08-4295-a067-37d2f15d3316`
    
   
### Download ZIP-Archive of all Dropbox-entries for a tag-selection

  You can download a zip-archive of all dropbox-files that are tagged with your selection. 
  The selection must not be greater than the configured maximum size of files before compression.

* **URL**

  /api/v1/dropboxtags/zipped?tags=:tags   
  example: /api/v1/dropboxtags/zipped?tags=book

* **Method:**

  `GET`
  
*  **URL Params**

  **Required:**
  
  None

  **Optional:**
  
  `tags=[alphanumeric]`   
  example: tags=book

* **Data Params**

   **Required:**
   
   None
   
   **Optional:**
   
   None  

* **Success Response:**

  * **Code:** 200 <br />
    **Content-Type:** `application/zip`   
    **Content:** `<binary content>`
 
* **Error Response:**

  * **Code:** 500 INTERNAL SERVER ERROR <br />
    **Content:** `{"code":"DropboxDownloadException","message":"Unable to retrieve metadata for path [/myCookbook.doc]"}`


* **Sample Call:**

  `curl -v -X GET "localhost:8080/api/v1/dropboxtags/zipped?tags=download&offset=0&limit=1" --output output.zip`
