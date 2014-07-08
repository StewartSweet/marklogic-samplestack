package com.marklogic.samplestack.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.Transaction;
import com.marklogic.client.document.DocumentPage;
import com.marklogic.client.document.JSONDocumentManager;
import com.marklogic.client.extensions.ResourceManager;
import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.query.QueryDefinition;
import com.marklogic.client.query.QueryManager.QueryView;
import com.marklogic.samplestack.domain.ClientRole;
import com.marklogic.samplestack.domain.SamplestackType;

public interface MarkLogicOperations {

	/**
	 * Returns a JSON document, as a Jackson JSONNode from the database by URI
	 * @param role The client role to use in accessing the database.
	 * @param documentUri the URI of the document in MarkLogic server.
	 * @return A JsonNode containing the document.
	 */
	public JsonNode getJsonDocument(ClientRole role, String documentUri);
	
	/**
	 * Convenience method to send a String query over a particular range of documents.
	 * The expectation is that there will be an options node on the server that corresponds
	 * to this directory/Class of objects, and which configures the search string
	 * that the client uses here.  This version of search provides a SearchHandle object
	 * which is populated by data from the Search API response object.
	 * @param role The security role under which to run the query.
	 * @param type The directory to look in.  Must begin and end with '/'
	 * @param queryString The Search API query string, as configured by a persisted options file.
	 * @param start The index of the first result returned.
	 * @return A page of results.
	 */
	public DocumentPage searchInClass(ClientRole role, SamplestackType type,
			String queryString, long start);
	
	public DocumentPage search(ClientRole role, QueryDefinition queryDefinition, 
			long start);

	public void deleteDirectory(ClientRole role, SamplestackType type);
	
	public void delete(ClientRole role, String documentUri);

	public JSONDocumentManager newJSONDocumentManager(ClientRole role);
	
	/**
	 * Initializes a resource manager, part of setup for application context
	 * @param role Role to search with
	 * @param name name or resource extension
	 * @param testResourceManager
	 */
	public <T extends ResourceManager> void initResource(ClientRole role, String name,
			T resourceManager);

	/**
	 * The main search for samplestack results.  It is
	 * a pass-through response from the middle tier to the browser,
	 * so that the browser can simply use search results from the MarkLogic JSON
	 * Search API response.
	 * @param role Role to search with
	 * @param structuredQuery structured query, JSON String, from browser
	 * @param start cursor position
	 * @param view Client can specify which view to retrieve.
	 * @return A JSON representation of MarkLogic query results.
	 */
	public ObjectNode qnaSearch(ClientRole role, JsonNode structuredQuery,
			long start, QueryView view);
	
	/**
	 * Wraps a call to the MarkLogic suggest capability
	 * @param role Role to search with
	 * @param suggestPattern
	 * @return An array of Strings matching the suggest pattern.
	 */
	public String[] suggestTags(ClientRole role, String suggestPattern);

	
	/**
	 * Start a transaction
	 * @param role Role to search with
	 * @return A transaction to use in subsequent calls to MarkLogic 
	 */
	public Transaction start(ClientRole role);

	
}