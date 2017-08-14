package org.sakaiproject.guide.tool.pages;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.guide.logic.GradingLogic;
import org.sakaiproject.guide.logic.ProjectLogic;
import org.sakaiproject.guide.logic.SakaiProxy;
import org.sakaiproject.guide.model.ComalatGrade;
import org.sakaiproject.guide.tool.toolkit.PageTools;

import java.util.List;

/**
 * The transition page which indicates loading of results after test submission
 *
 * @author Baris Watzke (baris.watzke@student.uni-siegen.de)
 * @author Pascal Nowak (pascal.nowak@student.uni-siegen.de)
 */
public class RecordPage extends WebPage {

    private static final long serialVersionUID = 1L;
    @SpringBean(name = "org.sakaiproject.guide.logic.SakaiProxy")
    protected static SakaiProxy sakaiProxy;
    @SpringBean(name = "org.sakaiproject.guide.logic.ComalatUtilities")
    protected org.sakaiproject.guide.logic.ComalatUtilities comalatUtilities;
    @SpringBean(name = "org.sakaiproject.guide.logic.GradingLogic")
    private GradingLogic gradingLogic;
    @SpringBean(name = "org.sakaiproject.guide.logic.ProjectLogic")
    private ProjectLogic projectLogic;
    private String userID;

    private List<ComalatGrade> gradeList;
    private ComalatGrade comalatGrade;

    public RecordPage(ComalatGrade comalatGrade) {
        this.comalatGrade = comalatGrade;
        initUser();
        addBackButton();
        addResultBox();
        addEvaluationBox();
    }

    /**
     * load recent grades considering the current site language
     */
    private void initUser() {
        String siteLanguage = comalatUtilities.getShortLanguageOfCurrentSite();
        userID = sakaiProxy.getCurrentUserId();
    }

    private void addBackButton() {
        AjaxLink backButton = new AjaxLink("backButton") {
            @Override
            public void onClick(AjaxRequestTarget target) {
            }
        };
        backButton.setBody(Model.of(new ResourceModel("record.action.back")));
        backButton.add(new AttributeAppender("onclick",
                new Model("window.history.back();"), ";"));
        add(backButton);
    }

    /**
     * add all Labels for the Test Result Box
     */
    private void addResultBox() {
        add(new Label("resultTitle", new ResourceModel("transition.result.title")));

        add(new Label("name", comalatGrade.getAssessmentName()));
        add(new Label("subsection",
                new ResourceModel("transition.progress.subsection").getObject() +
                        ": " + comalatGrade.getSectionName() +
                        " (" + comalatGrade.getCompetence() + ")"));

        if (comalatGrade.isDecisionPoint()) {
            if (comalatGrade.isSuccessful()) {
                add(new Label("title", new ResourceModel("transition.title.decisionPoint")));
                add(new Label("score", ""));
            } else {
                add(new Label("title", "Hallo" +
                        new ResourceModel("transition.title.decisionPointFailed")));
                add(new Label("score", ""));
            }

        } else if (comalatGrade.isNonAssessed()) {
            add(new Label("title", new ResourceModel("transition.title.successful")));
            add(new Label("score", getResultMessage()));
        } else {
            if (comalatGrade.isSuccessful()) {
                add(new Label("title", new ResourceModel("transition.title.successful")));
                add(new Label("score", getResultMessage()));
            } else {
                add(new Label("title", new ResourceModel("transition.title.failed")));
                add(new Label("score", getResultMessage()));
            }
        }
    }

    /**
     * add all resources for the Evaluation Box
     */
    private void addEvaluationBox() {
        List<String[]> rows = PageTools.buildEvaluationRows(comalatGrade, userID);
        ListView<String[]> listView = new ListView<String[]>("evaluationRows", rows) {
            @Override
            protected void populateItem(ListItem<String[]> item) {
                String[] row = item.getModelObject();
                item.add(new Label("col1", row[0]));
                item.add(new Label("col2", row[1]));
            }
        };
        add(listView);
        add(new Label("evaluationTitle", new ResourceModel("transition.evaluation.title")));
    }

    /**
     * @return a message containing the test result
     */
    private String getResultMessage() {
        return comalatGrade.getPointsEarned() + new ResourceModel("transition.progress.preposition").getObject() + " " +
                comalatGrade.getMaximumPoints() + " " + new ResourceModel("transition.result.points").getObject() + " (" +
                comalatGrade.getPercentage() + "%)";
    }
}