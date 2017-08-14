package org.sakaiproject.guide.tool.pages;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.authoring.logic.SakaiProxy;
import org.sakaiproject.guide.logic.ComalatNotificationService;
import org.sakaiproject.guide.logic.GradingLogic;
import org.sakaiproject.guide.model.ComalatGrade;
import org.sakaiproject.guide.model.ComalatUser;
import org.wicketstuff.progressbar.ProgressBar;
import org.wicketstuff.progressbar.Progression;
import org.wicketstuff.progressbar.ProgressionModel;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Long.parseLong;

/**
 * The welcome page to show the user what he achieved before
 *
 * @author Pascal Nowak (pascal.nowak@student.uni-siegen.de)
 * @author Baris Watzke (baris.watzke@student.uni-siegen.de)
 */
public class WelcomePage extends WebPage {

    @SpringBean(name = "org.sakaiproject.authoring.logic.SakaiProxy")
    protected static SakaiProxy sakaiProxy;
    @SpringBean(name = "org.sakaiproject.guide.logic.ProjectLogic")
    protected org.sakaiproject.guide.logic.ProjectLogic projectLogic;
    @SpringBean(name = "org.sakaiproject.guide.logic.GradingLogic")
    protected GradingLogic gradingLogic;
    @SpringBean(name = "org.sakaiproject.guide.logic.ComalatUtilities")
    protected org.sakaiproject.guide.logic.ComalatUtilities comalatUtilities;
    @SpringBean(name = "org.sakaiproject.guide.logic.ComalatNotificationService")
    private ComalatNotificationService comalatNotificationService;
    private List<String> lessonURL_List;
    private List<ComalatGrade> gradeList;

    private String userID;
    private String siteLanguage;
    private int lessonIndex;

    // Lesson data from last ComalatGrade entry
    private String lessonName;
    private int lessonProgress;

    private boolean intermediate = false;

    public WelcomePage() {
        initUser();
        addRecentActivityBox();
        addProgressBox();
        addRecommendationBox();
    }

    /**
     * load appropriate data for the user and adjust the shown content
     */
    private void initUser() {
        siteLanguage = comalatUtilities.getShortLanguageOfCurrentSite();
        String userName = sakaiProxy.getCurrentUserDisplayName();
        userID = sakaiProxy.getCurrentUserId();

        Label welcomeMessage = new Label("title",
                new ResourceModel("welcome.title").getObject() + ", " + userName + "!");
        add(welcomeMessage);

        lessonURL_List = comalatUtilities.getLessonURLs(userID);
        gradeList = projectLogic.getComalatGrading(userID).stream().
                filter(p -> p.getComalatId().split("-")[0].equals(siteLanguage)).collect(Collectors.toList());
        Collections.reverse(gradeList);

        String lessonIdentifier = "";

        if (gradeList.isEmpty()) {
            lessonName = "";
        } else {
            lessonIdentifier = gradeList.get(0).getComalatId().split("-")[1];
            lessonName = retrieveLessonName(lessonIdentifier);
        }

        lessonProgress = projectLogic.getLessonProgress(userID, siteLanguage, lessonIdentifier);

        // get last activity of the user
        if (!gradeList.isEmpty()) {
            lessonIndex = -1;
            try {
                lessonIndex += Integer.parseInt(lessonIdentifier.substring(1, lessonIdentifier.length()));
            } catch (NumberFormatException e) {
                lessonIndex = 0;
            }
        }
    }

    /**
     * fill table with recent Activity data according to current Site Language
     */
    private void addRecentActivityBox() {
        Label recentActivityTitle =
                new Label("recentActivitiesTitle", new ResourceModel("welcome.recent.title"));

        List<String[]> rows = new ArrayList<>();
        if (!gradeList.isEmpty()) {
            int counter = 0;

            for (ComalatGrade grade : gradeList) {
                if (!grade.isSubItemOfFinalTest()) {
                    String date = grade.getDate().toString();
                    TemporalAccessor temporal = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S").parse(date);
                    String gradeTime = DateTimeFormatter.ofPattern("dd.MM - HH:mm").format(temporal);

                    String[] row = {
                            // Example: L01: Activity 1 - Introduction
                            grade.getComalatId().split("-")[1] + ": " + grade.getAssessmentName(),
                            grade.getPercentage() + "%",
                            gradeTime,
                            grade.getComalatGradingId()+""
                    };
                    rows.add(row);
                    counter++;
                }
                if (counter == 5) break;
            }
        }

        // dummy entries for table rows stuffing
        while (rows.size() < 5) {
            String[] emptyArray = {"","","","", 0 + ""};
            rows.add(emptyArray);
        }
        ListView<String[]> listView = new ListView<String[]>("activityRows", rows) {
            @Override
            protected void populateItem(ListItem<String[]> item) {
                String[] rowInformation = item.getModelObject();

                item.getModel().getObject();
                item.add(new Label("col1", rowInformation[0]));
                item.add(new Label("col2", rowInformation[1]));

                long gradingID = rowInformation[3].isEmpty() ? 0 : parseLong(rowInformation[3]);
                ComalatGrade comalatGrade;
                if (gradingID != 0) {
                    comalatGrade = projectLogic.getComalatGrading(userID).stream().
                            filter(p -> p.getComalatGradingId().equals(gradingID)).collect(Collectors.toList()).get(0);
                } else {
                    comalatGrade = null;
                }

                AjaxLink viewButton = new AjaxLink("col3") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        if (gradingID != 0) {
                            setResponsePage(new RecordPage(comalatGrade));
                        }
                    }
                };
                item.add(viewButton);
                if (rowInformation[0].isEmpty()) {
                    viewButton.setBody(Model.of(""));
                } else viewButton.setBody(Model.of(rowInformation[2]));
            }
        };
        add(recentActivityTitle);
        add(listView);
    }

    /**
     * add all Labels for the Progress Box
     */
    private void addProgressBox() {
        List<ComalatGrade> grades = projectLogic.getComalatGrading(userID);

        if (!grades.isEmpty()) {
            ComalatGrade grade = grades.get(grades.size() - 1);

            gradingLogic.setComalatGrade(userID, grade);
            gradingLogic.getCountOfActivitiesOfThisSection(grade);

            add(new ProgressBar("bar", new ProgressionModel() {
                protected Progression getProgression() {
                    return new Progression(lessonProgress);
                }
            }) {
                protected void onFinished(AjaxRequestTarget target) {
                    setVisible(false);
                }
            });
        }

        else {
            add(new ProgressBar("bar", new ProgressionModel() {
                protected Progression getProgression() {
                    return new Progression(0);
                }
            }) {
                protected void onFinished(AjaxRequestTarget target) {
                    setVisible(false);
                }
            });
        }

        add(new Label("progressTitle", new ResourceModel("welcome.progress.title")));
        add(new Label("progressLesson", lessonName));

        AjaxLink progressButton = new AjaxLink("progressButton") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                comalatNotificationService.addNavigationCode(userID, LessonPage.REDIRECT_PROGRESS_PAGE);

            }
        };
        progressButton.setBody(Model.of(new ResourceModel("welcome.action.progress")));
        add(progressButton);
    }

    /**
     * add all Labels for the Recommendation Box
     */
    private void addRecommendationBox() {
        Label recommendationTitle =
                new Label("recommendationTitle", new ResourceModel("welcome.recommendation.title"));
        Label recommendation;
        ComalatUser comalatUser = projectLogic.getComalatUserBySakaiUserId(userID, null);
        String recommendedActivities = comalatUser.getRecommendedActivities();

        String lessonNumber = "";
        try {
            lessonNumber = lessonName.equals("") ? "" : lessonName.split(" ")[1];
        } catch (IndexOutOfBoundsException indexException) {
            indexException.printStackTrace();
        }

        if (lessonNumber.equals("10") && lessonProgress >= 80 && comalatUser.getDifficulty().equals("Beginner")) {
            intermediate = true;
            recommendation = new Label("recommendation",
                    "You mastered the beginner level. Visit your profile to change to intermediate level");
        } else if (recommendedActivities != null) {
            recommendation = new Label("recommendation", recommendedActivities);
        } else {
            recommendation = new Label("recommendation", lessonName);
        }

        int userProfileScore = checkUserProfile(comalatUser);

        AjaxLink recommendationButton = new AjaxLink("recommendationButton") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                Page page = new RedirectPage(lessonURL_List.get(lessonIndex));
                comalatUtilities.setLessonURL(userID, lessonURL_List.get(lessonIndex));
                if (intermediate) {
                    comalatNotificationService.addNavigationCode(userID, LessonPage.REDIRECT_PROFILE_PAGE);
                } else if (userProfileScore > 5) {
                    setResponsePage(page);
                } else {
                    comalatNotificationService.addNavigationCode(userID, LessonPage.REDIRECT_PROFILE_PAGE);
                }
            }
        };

        if (userProfileScore <= 5) {
            recommendation = new Label("recommendation",
                    new ResourceModel("welcome.recommendation.profile"));
            recommendationButton.setBody(Model.of(new ResourceModel("welcome.action.profile")));
        } else if (intermediate) {
            recommendationButton.setBody(Model.of(new ResourceModel("welcome.action.profile")));
        } else {
            recommendationButton.setBody(Model.of(new ResourceModel("welcome.action.continue")));
        }

        add(recommendationTitle);
        add(recommendation);
        add(recommendationButton);
    }

    /**
     * @param lessonIdentifier from ComalatGrade
     * @return lesson name in a readable format
     */
    private String retrieveLessonName(String lessonIdentifier) {
        // Remove leading zero digit in lesson number
        String lessonNumber = Integer.valueOf(lessonIdentifier.substring(1)).toString();
        return new ResourceModel("welcome.lesson").getObject() + " " + lessonNumber;
    }

    /**
     * @param user from ComalatUser
     * @return an approximate score to opt for suitable user instructions
     */
    private int checkUserProfile(ComalatUser user) {
        int score = 0; // max score = 10
        if (user.getAge() != null) {
            score++;
        }
        if (user.getLanguage() != null) {
            score++;
        } else return 0;

        if (user.getDifficulty() != null) {
            score++;
        } else return 0;

        if (user.getGender() != null) {
            score++;
        }
        if (user.getAge() != null) {
            score++;
        }
        if (user.getEducationLevel() != null) {
            score++;
        }
        if (user.getInstructionLanguage() != null) {
            score++;
        }
        if (user.getCurrentOccupation() != null) {
            score++;
        }
        if (user.getTargetOccupation() != null) {
            score++;
        }
        if (user.getTargetPlaceOfResidence() != null) {
            score++;
        }
        return score;
    }
}