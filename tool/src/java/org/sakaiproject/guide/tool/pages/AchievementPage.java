package org.sakaiproject.guide.tool.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.guide.logic.GradingLogic;
import org.sakaiproject.guide.logic.ProjectLogic;
import org.sakaiproject.guide.logic.RecommendationLogic;
import org.sakaiproject.guide.logic.SakaiProxy;
import org.sakaiproject.guide.model.ComalatAchievement;
import org.sakaiproject.guide.model.ComalatUser;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The achievement page is used for visualization of earned and in-progress
 * achievements in the learning progress
 *
 * @author Johannes Zenkert (johannes.zenkert@uni-siegen.de)
 * @author Baris Watzke (baris.watzke@student.uni-siegen.de)
 */

public class AchievementPage extends BasePage {

    private static final long serialVersionUID = 1L;
    private static final int BRONZE_SEGMENT = 1;
    private static final int SILVER_SEGMENT = 2;
    private static final int GOLD_SEGMENT = 3;
    private static final int PLATINUM_SEGMENT = 4;
    private static final int DIAMOND_SEGMENT = 5;
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
    private List<ComalatAchievement> achievements;

    private ComalatUser comalatUser;
    private String userID;
    private String siteLanguage;

    public AchievementPage() {
        disableLink(fourthLink);
        initUser();
        loadLessonAchievements();
        loadCompetenceAchievements();
        loadCourseAchievements();
        loadLevelAchievements();
    }

    private void initUser() {
        siteLanguage = comalatUtilities.getShortLanguageOfCurrentSite();
        userID = sakaiProxy.getCurrentUserId();
        comalatUser = projectLogic.getComalatUserBySakaiUserId(userID, null);

        achievements = comalatUser.getAchievements().
                stream().filter(l -> l.getLanguage().equals(siteLanguage)).collect(Collectors.toList());
    }

    private void loadLessonAchievements() {
        List<ComalatAchievement> lessonAchievements =
                achievements.stream().filter(l -> l.getType().equals("lesson")).collect(Collectors.toList());

        // Lesson Titles
        add(new Label(("lessons_beginner"), new ResourceModel("achievement.lessons.beginner")));
        add(new Label(("lessons_intermediate"), new ResourceModel("achievement.lessons.intermediate")));
        for (int i = 1; i <= 20; i++) {
            add(new Label(("lesson" + i),
                    new ResourceModel("achievement.lesson").getObject() + " " + i));
        }

        boolean[] checklist = new boolean[20];
        for (ComalatAchievement lessonAchievement : lessonAchievements) {

            int segment = lessonAchievement.getSegment();

            int number = Integer.valueOf(lessonAchievement.getName().substring(1)) - 1;
            checklist[number] = segment > 0;

            switch (segment) {
                case BRONZE_SEGMENT:
                    add(new Image(lessonAchievement.getName(), new PackageResourceReference(AchievementPage.class,
                            "../achievements/blue_copper.png")));
                    add(new Label(lessonAchievement.getName() + "_tooltip",
                            new ResourceModel("achievement.lesson.tooltip.bronze")));
                    break;
                case SILVER_SEGMENT:
                    add(new Image(lessonAchievement.getName(), new PackageResourceReference(AchievementPage.class,
                            "../achievements/blue_silver.png")));
                    add(new Label(lessonAchievement.getName() + "_tooltip",
                            new ResourceModel("achievement.lesson.tooltip.silver")));
                    break;
                case GOLD_SEGMENT:
                    add(new Image(lessonAchievement.getName(), new PackageResourceReference(AchievementPage.class,
                            "../achievements/blue_gold.png")));
                    add(new Label(lessonAchievement.getName() + "_tooltip",
                            new ResourceModel("achievement.lesson.tooltip.gold")));
                    break;
                case PLATINUM_SEGMENT:
                    add(new Image(lessonAchievement.getName(), new PackageResourceReference(AchievementPage.class,
                            "../achievements/blue_platin.png")));
                    add(new Label(lessonAchievement.getName() + "_tooltip",
                            new ResourceModel("achievement.lesson.tooltip.platinum")));
                    break;
                case DIAMOND_SEGMENT:
                    add(new Image(lessonAchievement.getName(), new PackageResourceReference(AchievementPage.class,
                            "../achievements/blue_diamond.png")));
                    add(new Label(lessonAchievement.getName() + "_tooltip",
                            new ResourceModel("achievement.lesson.tooltip.diamond")));
                    break;
            }
        }
        for (int i = 0; i < checklist.length; i++) {
            if (!checklist[i]) {
                String lessonName = i < 9 ? "L0" + (i+1) : "L" + (i+1);
                add(new Image(lessonName, new PackageResourceReference(AchievementPage.class,
                        "../achievements/NA_gray.png")));
                add(new Label(lessonName + "_tooltip",
                        new ResourceModel("achievement.tooltip.none")));
            }
        }
    }

    private void loadCompetenceAchievements() {
        List<ComalatAchievement> competenceAchievements = achievements.
                stream().filter(l -> l.getType().equals("competence")).collect(Collectors.toList());

        addCompetencePlaceholderImages();
        // Competence Titles
        add(new Label(("competences_beginner"), new ResourceModel("achievement.competences.beginner")));
        add(new Label(("competences_intermediate"), new ResourceModel("achievement.competences.intermediate")));
        add(new Label( "grammar", new ResourceModel("achievement.grammar")));
        add(new Label( "vocabulary", new ResourceModel("achievement.vocabulary")));
        add(new Label( "reading_writing", new ResourceModel("achievement.reading")));
        add(new Label( "listening_speaking", new ResourceModel("achievement.listening")));
        add(new Label( "specialization", new ResourceModel("achievement.specialization")));

        if (competenceAchievements.isEmpty()) {
            return;
        }
        for (ComalatAchievement achievement : competenceAchievements) {
            int segment = achievement.getSegment();

            switch (segment) {
                case BRONZE_SEGMENT:
                    addOrReplace(new Image(achievement.getName(), new PackageResourceReference(AchievementPage.class,
                        "../achievements/yellow_copper.png")));
                    addOrReplace(new Label(achievement.getName() + "_tooltip",
                            new ResourceModel("achievement.competence.tooltip.bronze")));
                    break;
                case SILVER_SEGMENT:
                    addOrReplace(new Image(achievement.getName(), new PackageResourceReference(AchievementPage.class,
                        "../achievements/yellow_silver.png")));
                    addOrReplace(new Label(achievement.getName() + "_tooltip",
                            new ResourceModel("achievement.competence.tooltip.silver")));
                    break;
                case GOLD_SEGMENT:
                    addOrReplace(new Image(achievement.getName(), new PackageResourceReference(AchievementPage.class,
                        "../achievements/yellow_gold.png")));
                    addOrReplace(new Label(achievement.getName() + "_tooltip",
                            new ResourceModel("achievement.competence.tooltip.gold")));
                    break;
                case PLATINUM_SEGMENT:
                    addOrReplace(new Image(achievement.getName(), new PackageResourceReference(AchievementPage.class,
                        "../achievements/yellow_platin.png")));
                    addOrReplace(new Label(achievement.getName() + "_tooltip",
                            new ResourceModel("achievement.competence.tooltip.platinum")));
                    break;
                case DIAMOND_SEGMENT:
                    addOrReplace(new Image(achievement.getName(), new PackageResourceReference(AchievementPage.class,
                        "../achievements/yellow_diamond.png")));
                    addOrReplace(new Label(achievement.getName() + "_tooltip",
                            new ResourceModel("achievement.competence.tooltip.diamond")));
                    break;
            }
        }
    }

    private void loadCourseAchievements() {
        List<ComalatAchievement> courseAchievements = achievements.
                stream().filter(l -> l.getType().equals("course")).collect(Collectors.toList());

        addCoursePlaceholderImages();
        // Course Titles
        add(new Label(("courses"), new ResourceModel("achievement.courses")));
        for (int i = 1; i <= 4; i++) {
            add(new Label(("course" + i),
                    new ResourceModel("achievement.course").getObject() + " " + i));
        }
        if (courseAchievements.isEmpty()) {
            return;
        }
        for (ComalatAchievement achievement : courseAchievements) {
            addOrReplace(new Image(achievement.getName(), new PackageResourceReference(AchievementPage.class,
                    "../achievements/green_diamond.png")));
            addOrReplace(new Label(achievement.getName() + "_tooltip",
                    new ResourceModel("achievement.course.tooltip")));
        }

    }

    private void loadLevelAchievements() {
        List<ComalatAchievement> levelAchievements = achievements.
                stream().filter(l -> l.getType().equals("level")).collect(Collectors.toList());

        addLevelPlaceholderImages();
        // Level Titles
        add(new Label(("levels"), new ResourceModel("achievement.levels")));
        add(new Label(("level_beginner"), new ResourceModel("achievement.levels.beginner")));
        add(new Label(("level_intermediate"), new ResourceModel("achievement.levels.intermediate")));

        if (levelAchievements.isEmpty()) {
            return;
        }
        for (ComalatAchievement achievement : levelAchievements) {
            addOrReplace(new Image(achievement.getName(), new PackageResourceReference(AchievementPage.class,
                    "../achievements/red_diamond.png")));
            addOrReplace(new Label(achievement.getName() + "_tooltip",
                    new ResourceModel("achievement.level.tooltip")));
        }

    }

    private void addCompetencePlaceholderImages() {
        // Beginner Level
        addOrReplace(new Image("G_beginner", new PackageResourceReference(AchievementPage.class,
                "../achievements/NA_gray.png")));
        addOrReplace(new Label("G_beginner_tooltip", new ResourceModel("achievement.tooltip.none")));
        addOrReplace(new Image("V_beginner", new PackageResourceReference(AchievementPage.class,
                "../achievements/NA_gray.png")));
        addOrReplace(new Label("V_beginner_tooltip", new ResourceModel("achievement.tooltip.none")));
        addOrReplace(new Image("R_beginner", new PackageResourceReference(AchievementPage.class,
                "../achievements/NA_gray.png")));
        addOrReplace(new Label("R_beginner_tooltip", new ResourceModel("achievement.tooltip.none")));
        addOrReplace(new Image("L_beginner", new PackageResourceReference(AchievementPage.class,
                "../achievements/NA_gray.png")));
        addOrReplace(new Label("L_beginner_tooltip", new ResourceModel("achievement.tooltip.none")));

        // Intermediate Level
        addOrReplace(new Image("G_intermediate", new PackageResourceReference(AchievementPage.class,
                "../achievements/NA_gray.png")));
        addOrReplace(new Label("G_intermediate_tooltip", new ResourceModel("achievement.tooltip.none")));
        addOrReplace(new Image("V_intermediate", new PackageResourceReference(AchievementPage.class,
                "../achievements/NA_gray.png")));
        addOrReplace(new Label("V_intermediate_tooltip", new ResourceModel("achievement.tooltip.none")));
        addOrReplace(new Image("R_intermediate", new PackageResourceReference(AchievementPage.class,
                "../achievements/NA_gray.png")));
        addOrReplace(new Label("R_intermediate_tooltip", new ResourceModel("achievement.tooltip.none")));
        addOrReplace(new Image("L_intermediate", new PackageResourceReference(AchievementPage.class,
                "../achievements/NA_gray.png")));
        addOrReplace(new Label("L_intermediate_tooltip", new ResourceModel("achievement.tooltip.none")));
        addOrReplace(new Image("S_intermediate", new PackageResourceReference(AchievementPage.class,
                "../achievements/NA_gray.png")));
        addOrReplace(new Label("S_intermediate_tooltip", new ResourceModel("achievement.tooltip.none")));
    }

    private void addCoursePlaceholderImages() {
        // Course 1
        addOrReplace(new Image("C01", new PackageResourceReference(AchievementPage.class,
                "../achievements/NA_gray.png")));
        addOrReplace(new Label("C01_tooltip", new ResourceModel("achievement.tooltip.none")));

        // Course 2
        addOrReplace(new Image("C02", new PackageResourceReference(AchievementPage.class,
                "../achievements/NA_gray.png")));
        addOrReplace(new Label("C02_tooltip", new ResourceModel("achievement.tooltip.none")));

        // Course 3
        addOrReplace(new Image("C03", new PackageResourceReference(AchievementPage.class,
                "../achievements/NA_gray.png")));
        addOrReplace(new Label("C03_tooltip", new ResourceModel("achievement.tooltip.none")));

        // Course 4
        addOrReplace(new Image("C04", new PackageResourceReference(AchievementPage.class,
                "../achievements/NA_gray.png")));
        addOrReplace(new Label("C04_tooltip", new ResourceModel("achievement.tooltip.none")));
    }

    private void addLevelPlaceholderImages() {
        // Beginner
        addOrReplace(new Image("level_beginner_image", new PackageResourceReference(AchievementPage.class,
                "../achievements/NA_gray.png")));
        addOrReplace(new Label("level_beginner_tooltip", new ResourceModel("achievement.tooltip.none")));

        // Intermediate
        addOrReplace(new Image("level_intermediate_image", new PackageResourceReference(AchievementPage.class,
                "../achievements/NA_gray.png")));
        addOrReplace(new Label("level_intermediate_tooltip", new ResourceModel("achievement.tooltip.none")));
    }
}
