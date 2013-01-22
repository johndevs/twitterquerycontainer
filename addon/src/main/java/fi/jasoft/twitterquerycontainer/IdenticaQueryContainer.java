package fi.jasoft.twitterquerycontainer;

import java.util.logging.Logger;

public class IdenticaQueryContainer extends TwitterQueryContainer {
    {
	TWITTER_API_URL = "http://identi.ca/api/search.json?";
	logger = Logger.getLogger(IdenticaQueryContainer.class.getName());
    }
}
