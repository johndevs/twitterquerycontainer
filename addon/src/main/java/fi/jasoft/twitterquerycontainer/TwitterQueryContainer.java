package fi.jasoft.twitterquerycontainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

/**
 * A container that uses the Twitter GET API to retrieve its results
 * 
 * @author John Ahlroos / www.jasoft.fi
 * 
 */
@SuppressWarnings("serial")
public class TwitterQueryContainer extends IndexedContainer implements
	SocialContainer {

    protected String TWITTER_API_URL = "http://search.twitter.com/search.json?";

    protected Logger logger = Logger.getLogger(TwitterQueryContainer.class
	    .getName());

    private boolean includeEntities = false;

    private String query = "";

    private ResultType resultType = ResultType.MIXED;

    private int resultsPerPage = 50;

    private int maxResults = 100;

    private short currentPage = 1;

    private boolean querying = false;

    private boolean queryIsQueued = false;

    /**
     * Default constructor
     * 
     * @param queryString
     *            The query string to search for
     */
    public TwitterQueryContainer() {

	// Add properties
	for (String property : ALL_PROPERTIES) {
	    if (property == METADATA_PROPERTY) {
		// Metadata properties are added separately
		continue;
	    }

	    if (property == CREATED_AT_PROPERTY) {
		addContainerProperty(property, Date.class, null);
	    } else {
		addContainerProperty(property, String.class, null);
	    }
	}

	// Add metadata properties
	for (String metaProp : METADATA_PROPERTIES) {
	    addContainerProperty(metaProp, String.class, null);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.example.twitterquerycontainer.SocialContainer#refresh()
     */
    public void refresh() {
	if (!querying) {
	    currentPage = 1;
	    removeAllItems();
	    doQuery();
	} else {
	    queryIsQueued = true;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.example.twitterquerycontainer.SocialContainer#setQuery(java.lang.
     * String)
     */
    public void setQuery(String queryString) {
	if (queryString != null && !this.query.equals(queryString)) {
	    this.query = queryString;
	}
    }

    /**
     * Does the query and re-populates the container
     */
    protected synchronized void doQuery() {
	querying = true;

	StringBuilder urlBuilder = new StringBuilder(TWITTER_API_URL);

	try {
	    urlBuilder.append("q=" + URLEncoder.encode(query, "UTF-8"));
	} catch (UnsupportedEncodingException uee) {
	    logger.log(Level.SEVERE, "Could not encode query string");
	    return;
	}

	if (maxResults - size() >= resultsPerPage) {
	    urlBuilder.append("&rpp=" + resultsPerPage);
	} else {
	    urlBuilder.append("&rpp=" + (maxResults - size()));
	}

	urlBuilder.append("&include_entities=" + isEntitiesIncluded());
	urlBuilder.append("&result_type="
		+ getResultType().name().toLowerCase());
	urlBuilder.append("&page=" + currentPage);

	Reader resultReader;
	try {
	    resultReader = doRequest(urlBuilder.toString());
	    if (resultReader == null) {
		querying = false;
		return;
	    }
	} catch (IOException e) {
	    // Thrown when page no longer can be found
	    querying = false;
	    return;
	}

	int resultsProcessed = processJSONResponse(resultReader);

	if (resultsProcessed > 0 && size() < maxResults) {
	    /*
	     * Query next page if the maximum has not yet been reached
	     */
	    currentPage++;
	    doQuery();
	} else if (queryIsQueued) {
	    queryIsQueued = false;
	    querying = false;
	    refresh();

	} else {
	    querying = false;
	}
    }

    /**
     * Processes the json response recieved from the server
     * 
     * @param jsonString
     *            The JSON to process
     */
    protected int processJSONResponse(Reader reader) {
	JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
	JsonArray results = json.get("results").getAsJsonArray();
	for (int r = 0; r < results.size(); r++) {
	    JsonObject result = results.get(r).getAsJsonObject();
	    Item item = getItem(addItem());
	    if (item != null) {
		for (String property : ALL_PROPERTIES) {
		    if (result.has(property)) {
			JsonElement value = result.get(property);

			if (value instanceof JsonNull) {
			    continue;
			}

			if (property == METADATA_PROPERTY) {
			    if (isEntitiesIncluded()) {
				for (String metaProperty : METADATA_PROPERTIES) {
				    JsonElement metaValue = ((JsonObject) value)
					    .get(metaProperty);
				    if (((JsonObject) value).has(metaProperty)) {
					item.getItemProperty(metaProperty)
						.setValue(
							metaValue.getAsString());
				    }
				}
			    }

			} else if (property == GEO_PROPERTY) {
			    // FIXME being lazy and assuming "type" is always
			    // Point
			    JsonArray array = ((JsonObject) value).get(
				    "coordinates").getAsJsonArray();
			    if (array.size() == 2) {
				item.getItemProperty(property).setValue(
					array.get(0).getAsString() + ";"
						+ array.get(1).getAsString());
			    }

			} else if (property == CREATED_AT_PROPERTY) {
			    String dateStr = value.getAsString();
			    try {
				Date date = CREATED_AT_PROPERTY_DATE_FORMAT
					.parse(dateStr);
				item.getItemProperty(property).setValue(date);
			    } catch (ParseException e) {
				e.printStackTrace();
			    }

			} else if (value instanceof JsonObject) {
			    // Unknown entity

			} else {
			    item.getItemProperty(property).setValue(
				    value.getAsString());
			}

		    }
		}
	    }
	}
	return results.size();
    }

    /**
     * Makes the HTTP Request
     * 
     * @param uri
     *            The uri to make the request to
     * @return Returns a reader for the results
     * @throws IOException
     */
    protected Reader doRequest(String uri) throws IOException {
	URL url = new URL(uri);
	URLConnection connection = url.openConnection();
	logger.log(Level.INFO, "Making a HTTP request to " + url);
	return new BufferedReader(new InputStreamReader(
		connection.getInputStream()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.example.twitterquerycontainer.SocialContainer#setEntitiesIncluded
     * (boolean)
     */
    public void setEntitiesIncluded(boolean included) {
	if (includeEntities != included) {
	    includeEntities = included;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.example.twitterquerycontainer.SocialContainer#isEntitiesIncluded()
     */
    public boolean isEntitiesIncluded() {
	return includeEntities;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.example.twitterquerycontainer.SocialContainer#setResultType(com.example
     * .twitterquerycontainer.ResultType)
     */
    public void setResultType(ResultType resultType) {
	if (resultType != null && resultType != this.resultType) {
	    this.resultType = resultType;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.example.twitterquerycontainer.SocialContainer#getResultType()
     */
    public ResultType getResultType() {
	return this.resultType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.example.twitterquerycontainer.SocialContainer#setMaxResults(int)
     */
    public void setMaxResults(int results) {
	if (maxResults != results && results > 0) {
	    this.maxResults = results;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.example.twitterquerycontainer.SocialContainer#getMaxResults()
     */
    public int getMaxResults() {
	return maxResults;
    }
}
