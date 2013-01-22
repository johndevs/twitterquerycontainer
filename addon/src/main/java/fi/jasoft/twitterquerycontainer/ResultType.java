package fi.jasoft.twitterquerycontainer;

/**
 * Specifies what type of search results you would prefer to receive. The
 * current default is "mixed."
 * 
 * @author John Ahlroos / www.jasoft.fi
 * 
 */
public enum ResultType {

    /**
     * Include both popular and real time results in the response.
     */
    MIXED,

    /**
     * Return only the most recent results in the response
     */
    RECENT,

    /**
     * Return only the most popular results in the response.
     */
    POPULAR
}
