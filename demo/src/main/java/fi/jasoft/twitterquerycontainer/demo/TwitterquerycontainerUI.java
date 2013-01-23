package fi.jasoft.twitterquerycontainer.demo;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

@Theme("DemoTheme")
public class TwitterquerycontainerUI extends UI {

	@Override
	protected void init(VaadinRequest request) {
		setContent(new SearchView());
	}
}