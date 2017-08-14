package org.sakaiproject.guide.tool.pages;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.guide.logic.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The transition page which indicates loading of results after test submission
 *
 * @author Baris Watzke (baris.watzke@student.uni-siegen.de)
 * @author Pascal Nowak (pascal.nowak@student.uni-siegen.de)
 */
public class SkippedActivitiesPage extends WebPage {

    private static final long serialVersionUID = 1L;
    @SpringBean(name = "org.sakaiproject.guide.logic.SakaiProxy")
    protected static SakaiProxy sakaiProxy;
    @SpringBean(name = "org.sakaiproject.guide.logic.ComalatUtilities")
    protected ComalatUtilities comalatUtilities;
    @SpringBean(name = "org.sakaiproject.guide.logic.RecommendationLogic")
    protected RecommendationLogic recommendationLogic;
    @SpringBean(name = "org.sakaiproject.guide.logic.GradingLogic")
    private GradingLogic gradingLogic;
    @SpringBean(name = "org.sakaiproject.guide.logic.ProjectLogic")
    private ProjectLogic projectLogic;
    @SpringBean(name = "org.sakaiproject.guide.logic.ComalatNotificationService")
    private ComalatNotificationService comalatNotificationService;

    private List<String> skippedActivities;

    private String userID;
    private String siteLanguage;

    public SkippedActivitiesPage() {
        initUser();
        addSkippedActivitiesBox();
    }

    /**
     * init recent grade information from current user
     */
    private void initUser() {
        siteLanguage = comalatUtilities.getShortLanguageOfCurrentSite();
        userID = sakaiProxy.getCurrentUserId();

        skippedActivities = comalatUtilities.getSkippedActivities(userID);
    }

    /**
     * add all resources for the Evaluation Box
     */
    private void addSkippedActivitiesBox() {
        add(new Label("title", new ResourceModel("skippedActivitiesPage.skippedActivities.title")));

        List<String[]> rows = new ArrayList<>();

        for (String s : skippedActivities) {
            String[] row = {s};
            rows.add(row);
        }
        ListView<String[]> listView = new ListView<String[]>("skippedRows", rows) {
            @Override
            protected void populateItem(ListItem<String[]> item) {
                String[] row = item.getModelObject();
                item.add(new Label("col1", row[0]));
            }
        };

        add(listView);

        add(new Label("skippedTitle", new ResourceModel("skippedActivitiesPage.skippedActivities.title")));
    }
}