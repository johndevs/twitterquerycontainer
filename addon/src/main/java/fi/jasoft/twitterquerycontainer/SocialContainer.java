package fi.jasoft.twitterquerycontainer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.vaadin.data.Container;

/**
 * Interface for containers who implement a Twitter like REST api (currently
 * Twitter + Identi.ca)
 * 
 * @author John Ahlroos / www.jasoft.fi
 * 
 */
public interface SocialContainer extends Container.Indexed {

    /*
     * As defined at https://dev.twitter.com/docs/api/1/get/search
     */
    public static final String ID_PROPERTY = "id_str";
    public static final String FROM_USER_NAME_PROPERTY = "from_user_name";
    public static final String FROM_USER_PROPERTY = "from_user";
    public static final String FROM_USER_ID_PROPERTY = "from_user_id_str";
    public static final String TEXT_PROPERTY = "text";
    public static final String CREATED_AT_PROPERTY = "created_at";
    public static final String GEO_PROPERTY = "geo";
    public static final String ISO_LANGUAGE_PROPERTY = "iso_language_code";
    public static final String METADATA_PROPERTY = "metadata";
    public static final String PROFILE_IMAGE_PROPERTY = "profile_image_url_https";
    public static final String PROFILE_IMAGE_PROPERTY_HTTP = "profile_image_url";
    public static final String TO_USER_NAME_PROPERTY = "to_user_name";
    public static final String TO_USER_PROPERTY = "to_user";
    public static final String TO_USER_ID_PROPERTY = "to_user_id_str";
    public static final Collection<String> ALL_PROPERTIES = Collections
	    .unmodifiableCollection(Arrays.asList(ID_PROPERTY,
		    FROM_USER_ID_PROPERTY, FROM_USER_NAME_PROPERTY,
		    FROM_USER_PROPERTY, TEXT_PROPERTY, CREATED_AT_PROPERTY,
		    GEO_PROPERTY, ISO_LANGUAGE_PROPERTY,
		    PROFILE_IMAGE_PROPERTY, TO_USER_ID_PROPERTY,
		    TO_USER_NAME_PROPERTY, TO_USER_PROPERTY, METADATA_PROPERTY));

    public static final String METADATA_RESULT_TYPE_PROPERTY = "result_type";
    public static final String METADATA_RECENT_RETWEETS = "recent_retweets";
    public static final Collection<String> METADATA_PROPERTIES = Collections
	    .unmodifiableCollection(Arrays.asList(
		    METADATA_RESULT_TYPE_PROPERTY, METADATA_RECENT_RETWEETS));

    public static final DateFormat CREATED_AT_PROPERTY_DATE_FORMAT = new SimpleDateFormat(
	    "EEE, d MMM yyyy HH:mm:ss Z");

    /**
     * Makes the query and refreshes the items
     */
    public abstract void refresh();

    /**
     * Sets the query to perform
     * 
     * @param queryString
     */
    public abstract void setQuery(String queryString);

    /**
     * Should the entities be included?
     */
    public abstract void setEntitiesIncluded(boolean included);

    /**
     * Are the entities included
     * 
     * @return
     */
    public abstract boolean isEntitiesIncluded();

    /**
     * Set the result type. See {@link ResultType} for more information about
     * different result types
     * 
     * @param resultType
     *            The result type to use
     */
    public abstract void setResultType(ResultType resultType);

    /**
     * Get the result type. See {@link ResultType} for more information about
     * different result types g
     */
    public abstract ResultType getResultType();

    /**
     * Set the maximim amount of tweets that should be fetched.
     * 
     * @param results
     */
    public abstract void setMaxResults(int results);

    /**
     * Get the maximum limit of tweets that will be fetched
     * 
     * @return
     */
    public abstract int getMaxResults();

}