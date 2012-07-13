package fi.jasoft.twitterquerycontainer.demo;

import com.vaadin.Application;

public class TwitterquerycontainerApplication extends Application {

    @Override
    public void init() {
	setTheme("DemoTheme");
	setMainWindow(new SearchWindow());
    }
}