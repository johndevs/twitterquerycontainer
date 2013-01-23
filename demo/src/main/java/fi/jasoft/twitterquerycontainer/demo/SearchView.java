package fi.jasoft.twitterquerycontainer.demo;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.themes.Reindeer;

import fi.jasoft.twitterquerycontainer.IdenticaQueryContainer;
import fi.jasoft.twitterquerycontainer.ResultType;
import fi.jasoft.twitterquerycontainer.SocialContainer;
import fi.jasoft.twitterquerycontainer.TwitterQueryContainer;

public class SearchView extends CustomComponent {

    private TwitterQueryContainer twitterContainer = new TwitterQueryContainer();

    private IdenticaQueryContainer identicaContainer = new IdenticaQueryContainer();

    private Table table;

    public SearchView() {
        setCaption("Twitter Search");
        setSizeFull();
        
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setSpacing(true);
        setCompositionRoot(vl);

        Label header = new Label("Twitter Search");
        header.setStyleName(Reindeer.LABEL_H1);
        vl.addComponent(header);

        Label description = new Label(
                "This application lets you search using the Twitter Search Rest API. "
                        + "You can search with simple search terms like 'vaadin' or 'reindeer' "
                        + "or with more advanced criterias. Twitter provides search results for "
                        + "the last 6 days or so.");
        description.setStyleName(Reindeer.LABEL_SMALL);
        vl.addComponent(description);

        OptionGroup service = new OptionGroup("Service: ", Arrays.asList(
                "Twitter", "Identi.ca"));
        service.setImmediate(true);
        service.setValue("Twitter");
        service.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                if (event.getProperty().getValue().equals("Twitter")) {
                    setTableContainer(twitterContainer);
                } else if (event.getProperty().getValue().equals("Identi.ca")) {
                    setTableContainer(identicaContainer);
                }
            }
        });

        //vl.addComponent(service);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth("100%");
        hl.setSpacing(true);
        vl.addComponent(hl);

        final TextField search = new TextField();
        search.setWidth("100%");
        search.setInputPrompt("Enter search query ('vaadin' for instance)...");
        search.setImmediate(true);
        search.addTextChangeListener(new FieldEvents.TextChangeListener() {
            public void textChange(TextChangeEvent event) {
                SocialContainer sc = (SocialContainer) table
                        .getContainerDataSource();
                sc.setQuery(event.getText());
                sc.refresh();
            }
        });
        search.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                SocialContainer sc = (SocialContainer) table
                        .getContainerDataSource();
                sc.setQuery(event.getProperty().getValue().toString());
                sc.refresh();
            }
        });
        hl.addComponent(search);

        Button advanced = new Button("Advanced search",
                new Button.ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        AdvancedSearchWindow win = new AdvancedSearchWindow();
                        win.addCloseListener(new Window.CloseListener() {
                            public void windowClose(CloseEvent e) {
                                search.setValue(((AdvancedSearchWindow) e
                                        .getWindow()).getQuery());
                            }
                        });
                        getUI().addWindow(win);

                    }
                });
        hl.addComponent(advanced);
        hl.setExpandRatio(search, 1);

        table = new Table();
        table.setSizeFull();

        addGeneratedColumnsToTable();
        setTableContainer(twitterContainer);

        vl.addComponent(table);
        vl.setExpandRatio(table, 1);

        FormLayout options = new FormLayout();
        options.setCaption("Options");
        options.setMargin(true);
        Panel optionPanel = new Panel(options);
        optionPanel.setWidth("100%");
        vl.addComponent(optionPanel);

        OptionGroup maxResults = new OptionGroup("Max results", Arrays.asList(
                5, 10, 50, 100));
        maxResults.setImmediate(true);
        maxResults.setStyleName("horizontal");
        SocialContainer sc = (SocialContainer) table.getContainerDataSource();
        maxResults.setValue(sc.getMaxResults());
        maxResults.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                SocialContainer sc = (SocialContainer) table
                        .getContainerDataSource();
                sc.setMaxResults(Integer.parseInt(event.getProperty()
                        .getValue().toString()));
                sc.refresh();
            }
        });
        options.addComponent(maxResults);

        OptionGroup type = new OptionGroup("Results:", Arrays.asList("Popular",
                "Recent", "Both"));
        type.setStyleName("horizontal");
        type.setValue("Both");
        type.setImmediate(true);
        type.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                String value = event.getProperty().getValue().toString();
                SocialContainer sc = (SocialContainer) table
                        .getContainerDataSource();
                if (value.equals("Popular")) {
                    sc.setResultType(ResultType.POPULAR);
                } else if (value.equals("Recent")) {
                    sc.setResultType(ResultType.RECENT);
                } else if (value.equals("Both")) {
                    sc.setResultType(ResultType.MIXED);
                }
                sc.refresh();
            }
        });
        options.addComponent(type);

        final CheckBox metadata = new CheckBox("Include metadata");
        metadata.setImmediate(true);
        metadata.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                SocialContainer sc = (SocialContainer) table
                        .getContainerDataSource();
                sc.setEntitiesIncluded(metadata.booleanValue());
                sc.refresh();
            }
        });
        options.addComponent(metadata);
    }

    @SuppressWarnings("serial")
    private void addGeneratedColumnsToTable() {
        // Replace image url column with images
        table.addGeneratedColumn(TwitterQueryContainer.PROFILE_IMAGE_PROPERTY,
                new Table.ColumnGenerator() {
                    public Object generateCell(Table source, Object itemId,
                            Object columnId) {
                        Object url = source.getItem(itemId)
                                .getItemProperty(columnId).getValue();
                        if (url == null) {
                            url = source
                                    .getItem(itemId)
                                    .getItemProperty(
                                            TwitterQueryContainer.PROFILE_IMAGE_PROPERTY_HTTP)
                                    .getValue();
                        }
                        if (url != null) {
                            Embedded image = new Embedded(null,
                                    new com.vaadin.server.ExternalResource(url.toString()));
                            image.setWidth("48px");
                            image.setHeight("48px");
                            return image;
                        }
                        return url;
                    }
                });

        // Replace username and user name columns with one custom column
        table.addGeneratedColumn("usernameAndNameColumn",
                new Table.ColumnGenerator() {
                    public Object generateCell(Table source, Object itemId,
                            Object columnId) {
                        Object name = source
                                .getItem(itemId)
                                .getItemProperty(
                                        TwitterQueryContainer.FROM_USER_NAME_PROPERTY)
                                .getValue();
                        Object nick = source
                                .getItem(itemId)
                                .getItemProperty(
                                        TwitterQueryContainer.FROM_USER_PROPERTY)
                                .getValue();
                        if (nick != null && name != null) {
                            return nick + " (" + name + ")";
                        } else if (nick != null) {
                            return nick;
                        } else if (name != null) {
                            return name;
                        }
                        return null;
                    }
                });

        // Format the timestamp
        table.addGeneratedColumn(TwitterQueryContainer.CREATED_AT_PROPERTY,
                new Table.ColumnGenerator() {
                    public Object generateCell(Table source, Object itemId,
                            Object columnId) {
                        Object value = source.getItem(itemId)
                                .getItemProperty(columnId).getValue();
                        if (value != null) {
                            Date date = (Date) value;
                            long millisecondsAgo = System.currentTimeMillis()
                                    - date.getTime();

                            if (TimeUnit.MILLISECONDS.toHours(millisecondsAgo) == 0) {
                                // xx min, xx sec ago
                                return String
                                        .format("%d min, %d sec ago",
                                                TimeUnit.MILLISECONDS
                                                        .toMinutes(millisecondsAgo),
                                                TimeUnit.MILLISECONDS
                                                        .toSeconds(millisecondsAgo)
                                                        - TimeUnit.MINUTES
                                                                .toSeconds(TimeUnit.MILLISECONDS
                                                                        .toMinutes(millisecondsAgo)));
                            } else if (TimeUnit.MILLISECONDS
                                    .toHours(millisecondsAgo) < 12) {
                                // xx hours, xx min ago
                                return String
                                        .format("%d hours, %d min ago",
                                                TimeUnit.MILLISECONDS
                                                        .toHours(millisecondsAgo),
                                                TimeUnit.MILLISECONDS
                                                        .toMinutes(millisecondsAgo)
                                                        - TimeUnit.HOURS
                                                                .toMinutes(TimeUnit.MILLISECONDS
                                                                        .toHours(millisecondsAgo)));
                            } else {
                                // date
                                SimpleDateFormat format = new SimpleDateFormat(
                                        "dd MMM");
                                return format.format(date);
                            }

                        }
                        return value;
                    }
                });

        // Replace urls with actual links
        table.addGeneratedColumn(TwitterQueryContainer.TEXT_PROPERTY,
                new Table.ColumnGenerator() {
                    public Object generateCell(Table source, Object itemId,
                            Object columnId) {
                        Object value = source.getItem(itemId)
                                .getItemProperty(columnId).getValue();
                        if (value != null) {
                            String text = value.toString();

                            // Replace links
                            String regex = "(\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])";

                            value = text.replaceAll(regex,
                                    "<a target=\"_blank\" href=\"$1\">$1</a>");

                            Label lbl = new Label(value.toString(), ContentMode.HTML);
                       
                            lbl.setSizeFull();
                            CssLayout layout = new CssLayout();
                            layout.setSizeFull();
                            layout.addComponent(lbl);
                            value = layout;
                        }

                        return value;
                    }
                });

    }

    private void setTableContainer(SocialContainer container) {
        table.setContainerDataSource(container);

        table.setVisibleColumns(new Object[] {
                SocialContainer.CREATED_AT_PROPERTY,
                SocialContainer.PROFILE_IMAGE_PROPERTY,
                "usernameAndNameColumn", SocialContainer.TEXT_PROPERTY, });

        table.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
        table.setColumnWidth(SocialContainer.PROFILE_IMAGE_PROPERTY, 50);
        table.setColumnWidth(SocialContainer.TEXT_PROPERTY, 300);
        table.setColumnExpandRatio(SocialContainer.TEXT_PROPERTY, 1);
    }
}
