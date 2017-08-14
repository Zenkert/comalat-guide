package org.sakaiproject.guide.tool.pages;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.authoring.model.ComalatActivity;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.guide.logic.*;
import org.sakaiproject.guide.model.ComalatAchievement;
import org.sakaiproject.guide.model.ComalatGrade;
import org.sakaiproject.guide.model.ComalatUser;
import org.sakaiproject.guide.tool.toolkit.PageTools;
import org.wicketstuff.progressbar.ProgressBar;
import org.wicketstuff.progressbar.Progression;
import org.wicketstuff.progressbar.ProgressionModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The transition page which indicates loading of results after test submission
 *
 * @author Baris Watzke (baris.watzke@student.uni-siegen.de)
 * @author Pascal Nowak (pascal.nowak@student.uni-siegen.de)
 */
public class TransitionPage extends WebPage implements IHeaderContributor {

    private static final long serialVersionUID = 1L;
    private static final int BRONZE_SEGMENT = 1;
    private static final int SILVER_SEGMENT = 2;
    private static final int GOLD_SEGMENT = 3;
    private static final int PLATINUM_SEGMENT = 4;
    private static final int DIAMOND_SEGMENT = 5;
    private static final String SILVER_PERCENTAGE = "20%";
    private static final String GOLD_PERCENTAGE = "40%";
    private static final String PLATINUM_PERCENTAGE = "60%";
    private static final String DIAMOND_PERCENTAGE = "80%";
    @SpringBean(name = "org.sakaiproject.guide.logic.SakaiProxy")
    protected static SakaiProxy sakaiProxy;
    @SpringBean(name = "org.sakaiproject.guide.logic.ComalatUtilities")
    protected org.sakaiproject.guide.logic.ComalatUtilities comalatUtilities;
    @SpringBean(name = "org.sakaiproject.guide.logic.RecommendationLogic")
    protected RecommendationLogic recommendationLogic;
    @SpringBean(name = "org.sakaiproject.guide.logic.GradingLogic")
    private GradingLogic gradingLogic;
    @SpringBean(name = "org.sakaiproject.guide.logic.ProjectLogic")
    private ProjectLogic projectLogic;
    @SpringBean(name = "org.sakaiproject.guide.logic.ComalatNotificationService")
    private ComalatNotificationService comalatNotificationService;
    private List<ComalatGrade> gradeList;

    private String userID;
    private String siteLanguage;

    private int subsectionProgress;
    private int competenceProgress;
    private int lessonProgress;

    private ComalatGrade grade;
    private String lesson;
    private String competence;

    List<ComalatAchievement> achievementList;

    private boolean intermediate = false;

    public TransitionPage() {
        try {
            initUser();
        } catch (Exception e) {
            e.printStackTrace();
        }
        addResultBox();
        addEvaluationBox();
        addProgressBox();
        addAchievementBox();
        addRecommendationBox();
    }

    /**
     * init recent grade information from current user
     */
    private void initUser() throws Exception {
        siteLanguage = comalatUtilities.getShortLanguageOfCurrentSite();
        userID = sakaiProxy.getCurrentUserId();

        gradeList = projectLogic.getComalatGrading(userID).stream().
                filter(p -> p.getComalatId().split("-")[0].equals(siteLanguage)).collect(Collectors.toList());
        Collections.reverse(gradeList);

        grade = gradingLogic.getComalatGrade(userID);
        lesson = grade.getComalatId().split("-")[1];
        competence = grade.getComalatId().split("-")[3];

        subsectionProgress = projectLogic.getSubsectionProgress(userID, grade);
        competenceProgress = projectLogic.getCompetenceProgress(userID, grade);
        lessonProgress = projectLogic.getLessonProgress(userID, siteLanguage, lesson);
    }

    /**
     * add all resources for the Test Result Box
     */
    private void addResultBox() {
        //gradingLogic.setComalatGrade(projectLogic.getComalatGrading(sakaiProxy.getCurrentUserId()));

        add(new Label("resultTitle", new ResourceModel("transition.result.title")));

        add(new Label("name", grade.getAssessmentName()));
        add(new Label("subsection",
                new ResourceModel("transition.progress.subsection").getObject() +
                        ": " + grade.getSectionName() + " (" + grade.getCompetence() + ")"));

        if (grade.isDecisionPoint()) {
            if (grade.isSuccessful()) {
                add(new Label("title", new ResourceModel("transition.title.decisionPoint")));
                add(new Label("score", draftTestResultMessage()));
            } else {
                add(new Label("title", "Hallo" +
                        new ResourceModel("transition.title.decisionPointFailed")));
                add(new Label("score", draftTestResultMessage()));
            }

        } else if (grade.isNonAssessed()) {
            add(new Label("title", new ResourceModel("transition.title.successful")));
            add(new Label("score", draftTestResultMessage()));
        } else {
            if (grade.isSuccessful()) {
                add(new Label("title", new ResourceModel("transition.title.successful")));
                add(new Label("score", draftTestResultMessage()));
            } else {
                add(new Label("title", new ResourceModel("transition.title.failed")));
                add(new Label("score", draftTestResultMessage()));
            }
        }
    }

    /**
     * add all resources for the Evaluation Box
     */
    private void addEvaluationBox() {
        List<String[]> rows = PageTools.buildEvaluationRows(grade, userID);
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
     * add all resources for the Progress Box
     */
    private void addProgressBox() {
        add(new ProgressBar("subsectionBar", new ProgressionModel() {
            protected Progression getProgression() {
                return new Progression(subsectionProgress);
            }
        }) {
            protected void onFinished(AjaxRequestTarget target) {
                setVisible(false);
            }
        });

        add(new ProgressBar("lessonBar", new ProgressionModel() {
            protected Progression getProgression() {
                return new Progression(lessonProgress);
            }
        }) {
            protected void onFinished(AjaxRequestTarget target) {
                setVisible(false);
            }
        });

        add(new Label("progressTitle", new ResourceModel("transition.progress.title")));
        add(new Label("subsectionProgress",
                new ResourceModel("transition.progress.subsection").getObject() + ": " +
                        grade.getSectionName() + " (" + grade.getCompetence() + ")"));

        String progressTag =
                gradeList.isEmpty() ? "" : PageTools.retrieveLessonName(gradeList.get(0).getComalatId().split("-")[1]);

        add(new Label("lessonProgress", progressTag));
    }

    /**
     * add all resources for the Achievement Box
     */
    private void addAchievementBox() {
        achievementList = new ArrayList<>();
        try {
            achievementList = projectLogic.updateAchievements(userID, siteLanguage, grade, lesson, lessonProgress,
                    competence, competenceProgress);
        } catch (Exception e) {
            e.printStackTrace();
        }

        add(new Label("achievementTitle", new ResourceModel("welcome.achievement.title")));

        // dummy entries for table rows stuffing
        while (achievementList.size() < 2) {
            achievementList.add(null);
        }
        ListView<ComalatAchievement> listView = new ListView<ComalatAchievement>("achievementRows", achievementList) {
            @Override
            protected void populateItem(ListItem<ComalatAchievement> item) {
                ComalatAchievement achievement = item.getModelObject();
                item.getModel().getObject();
                String rowMessage = draftAchievementRessources(achievement, item);
                item.add(new Label("achievementMessage", rowMessage));
            }
        };
        add(listView);

        AjaxLink achievementButton = new AjaxLink("achievementButton") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                comalatNotificationService.addNavigationCode(userID, LessonPage.REDIRECT_ACHIEVEMENT_PAGE);
            }
        };
        achievementButton.setBody(Model.of(new ResourceModel("welcome.action.achievement")));
        add(achievementButton);
    }


    /**
     * add all resources for the Recommendation Box
     */
    private void addRecommendationBox() {
        Label recommendationBoxTitle = new Label("recommendationTitle",
                new ResourceModel("transition.recommendation.title"));

        String recomTag =
                gradeList.isEmpty() ? "" : PageTools.retrieveLessonName(gradeList.get(0).getComalatId().split("-")[1]);
        StringBuilder sB = new StringBuilder();
        ArrayList<ComalatActivity> recommendedActivities;
        try {
            recommendedActivities = recommendationLogic.getRecommendedActions(userID,
                    comalatUtilities.getShortLanguageOfSite(sakaiProxy.getCurrentSite().
                            getProperties().getProperty("language")));
        } catch (IdUnusedException e) {
            recommendedActivities = new ArrayList<>();
        }
        String[] lessonNumber = recomTag.split(" ");
        ComalatUser cU = projectLogic.getComalatUserBySakaiUserId(userID, null);

        String subsection = grade.getSectionName() + " (" + grade.getCompetence() + ")";

        // Standard feedback message referring to a test
        String feedBackMessage = recommendationLogic.getComalatFeedbackMessage((int) grade.getPercentage(),
                grade.isDecisionPoint(), subsection, cU.getInstructionLanguage());

        feedBackMessage = embedLearningLanguageInFeedback(feedBackMessage);

        // At the end of Beginner Level a special feedback occurs
        if (lessonNumber[1].equals("10") && lessonProgress >= 80 && cU.getDifficulty().equals("Beginner")) {
            intermediate = true;
            // New line
            feedBackMessage += System.lineSeparator() + System.lineSeparator();
            feedBackMessage += "You mastered the beginner level. Visit your profile to change to intermediate level";
        }

        // New line between feedback and recommendation
        feedBackMessage += System.lineSeparator() + System.lineSeparator() +
                new ResourceModel("up.next").getObject() + " ";

        // Append recommended activities to feedback message
        if (recommendedActivities != null && !recommendedActivities.isEmpty()) {
            feedBackMessage += System.lineSeparator();
            for (ComalatActivity activity : recommendedActivities) {
                sB.append(activity.getActivityName()).append(System.lineSeparator());
            }
            feedBackMessage += sB.toString();
        } else {
            feedBackMessage += recomTag;
        }
        MultiLineLabel feedBack = new MultiLineLabel("feedback", feedBackMessage);

        AjaxLink continueButton = new AjaxLink("continueButton") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                if (intermediate) {
                    comalatNotificationService.addNavigationCode(userID, LessonPage.REDIRECT_PROFILE_PAGE);
                } else {
                    RedirectPage page = new RedirectPage(comalatUtilities.getLessonURL(userID));
                    setResponsePage(page);
                }
            }
        };
        if (intermediate) {
            continueButton.setBody(Model.of(new ResourceModel("welcome.action.profile")));
        } else {
            continueButton.setBody(Model.of(new ResourceModel("transition.link.backToLesson")));
        }

        add(recommendationBoxTitle);
        add(feedBack);
        add(continueButton);
    }

    private String embedLearningLanguageInFeedback(String feedBackMessage) {
        String siteLanguageTranslated = "";
        switch (siteLanguage) {
            case "DE":
                siteLanguageTranslated = new ResourceModel("profile.instructionLanguage.german").getObject();
                break;
            case "EN":
                siteLanguageTranslated = new ResourceModel("profile.instructionLanguage.english").getObject();
                break;
            case "ES":
                siteLanguageTranslated = new ResourceModel("profile.instructionLanguage.spanish").getObject();
                break;
            case "DAF":
                siteLanguageTranslated = new ResourceModel("profile.instructionLanguage.german").getObject();
                break;
            case "DAF1617":
                siteLanguageTranslated = new ResourceModel("profile.instructionLanguage.german").getObject();
                break;
        }
        return feedBackMessage.replaceAll("%TARGET_LANGUAGE%", siteLanguageTranslated);
    }

    private String retrieveCompetenceName(String comptetenceIdentifier) {
        // Example: competenceIdentifier == "G_beginner"

        String competence = "";
        String difficulty = "";

        try {
            competence = comptetenceIdentifier.substring(0, 1);
            difficulty = comptetenceIdentifier.substring(2);
        } catch (IndexOutOfBoundsException iOB) {
            iOB.printStackTrace();
        }

        switch (competence) {
            case "G":
                competence = new ResourceModel("progress_page.grammar").getObject();
                break;
            case "V":
                competence = new ResourceModel("progress_page.vocabulary").getObject();
                break;
            case "R":
                competence = new ResourceModel("progress_page.reading").getObject();
                break;
            case "L":
                competence = new ResourceModel("progress_page.listening").getObject();
                break;
            case "S":
                competence = "Specialization";
                break;
        }
        switch (difficulty) {
            case "beginner":
                difficulty = new ResourceModel("profile.listDifficulties.beginner").getObject();
                break;
            case "intermediate":
                difficulty = new ResourceModel("profile.listDifficulties.intermediate").getObject();
                break;
        }
        return competence + " " + difficulty;
    }

    /**
     * @return a message containing the test result
     */
    private String draftTestResultMessage() {
        // array contains {points earned, maximum points, percentage}
        double[] resultValues = gradingLogic.getTestResult(userID);
        return resultValues[0] + new ResourceModel("transition.progress.preposition").getObject() + " " +
                resultValues[1] + " " + new ResourceModel("transition.result.points").getObject() + " (" +
                resultValues[2] + "%)";
    }

    /**
     * @return messages containing
     *         first: current achievement information
     *         second: information about next achievement
     *
     * in addition: add Achievement Icons to Wicket
     */
    private String draftAchievementRessources(ComalatAchievement achievement, ListItem<ComalatAchievement> item) {
        if (achievement == null) {
            item.add(new Image("achievementIcon", new PackageResourceReference(AchievementPage.class,
                    "../achievements/NA_placeholder.png")));
            return "";
        }
        String achievementName = achievement.getName();
        String achievementType = achievement.getType();

        String achievementSegment = "";
        String achievementPercent = "";

        String achievementNameFormatted = "";

        String achievementColor = "";

        switch (achievementType) {
            case "lesson":
                // Example: New Achievement! Lesson 4: Gold (40%)
                achievementNameFormatted = PageTools.retrieveLessonName(achievementName);
                achievementColor = "blue";
                break;
            case "competence":
                // Example: New Achievement! Grammar beginner: Gold (40%)
                achievementNameFormatted = retrieveCompetenceName(achievementName);
                achievementColor = "yellow";
                break;
            case "course":
                achievementColor = "green";
                break;
            case "level":
                achievementColor = "red";
                break;
        }

        switch (achievement.getSegment()) {
            case BRONZE_SEGMENT:
                achievementSegment = new ResourceModel("achievement.bronze").getObject();
                // no percentage for bronze
                item.add(new Image("achievementIcon", new PackageResourceReference(AchievementPage.class,
                        "../achievements/" + achievementColor + "_copper.png")));
                return new ResourceModel("transition.achievement.new").getObject() + " " +
                        achievementNameFormatted + ": " + achievementSegment;
            case SILVER_SEGMENT:
                achievementSegment = new ResourceModel("achievement.silver").getObject();
                achievementPercent = SILVER_PERCENTAGE;
                item.add(new Image("achievementIcon", new PackageResourceReference(AchievementPage.class,
                        "../achievements/" + achievementColor + "_silver.png")));
                break;
            case GOLD_SEGMENT:
                achievementSegment = new ResourceModel("achievement.gold").getObject();
                achievementPercent = GOLD_PERCENTAGE;
                item.add(new Image("achievementIcon", new PackageResourceReference(AchievementPage.class,
                        "../achievements/" + achievementColor + "_gold.png")));
                break;
            case PLATINUM_SEGMENT:
                achievementSegment = new ResourceModel("achievement.platinum").getObject();
                achievementPercent = PLATINUM_PERCENTAGE;
                item.add(new Image("achievementIcon", new PackageResourceReference(AchievementPage.class,
                        "../achievements/" + achievementColor + "_platin.png")));
                break;
            case DIAMOND_SEGMENT:
                achievementSegment = new ResourceModel("achievement.diamond").getObject();
                achievementPercent = DIAMOND_PERCENTAGE;
                item.add(new Image("achievementIcon", new PackageResourceReference(AchievementPage.class,
                        "../achievements/" + achievementColor + "_diamond.png")));
                break;
        }
        return new ResourceModel("transition.achievement.new").getObject() + " " +
                achievementNameFormatted + ": " + achievementSegment + " (" + achievementPercent + ")";
    }

    /**
     * This block adds the required wrapper markup to style it like a Sakai
     * tool. Add to this any additional CSS or JS references that you need.
     *
     */
    public void renderHead(IHeaderResponse response) {
        // Hide AchievementBox if there are no entries
        try {
            if (achievementList.get(0) == null) {
                response.render(OnDomReadyHeaderItem.forScript("hideAchievementTable()"));
            }
        } catch (IndexOutOfBoundsException iob) {
            iob.printStackTrace();
        } catch (NullPointerException np) {
            np.printStackTrace();
        }
    }
}