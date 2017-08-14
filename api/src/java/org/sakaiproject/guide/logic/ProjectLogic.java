package org.sakaiproject.guide.logic;

import org.sakaiproject.authoring.model.*;
import org.sakaiproject.guide.model.*;

import java.util.List;
import java.util.Set;

/**
 * An example logic interface
 *
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 */
public interface ProjectLogic {

    ComalatUser getComalatUserById(Long comalatUserId);

    boolean saveComalatUser(ComalatUser comalatUser);

    void saveComalatUserList(List<ComalatUser> comalatUsers);

    void deleteComalatUser(ComalatUser comalatUser);

    void deleteAll(List<ComalatUser> comalatUsers);

    Set<String> getGradedStudentsIDs();

    UserGradingAverageData getAssessedAvgByUserIdAndCompetence(String userId, String language, String competence);

    int getCountOfNonAssessedActivitiesByUserIdAndCompetence(String userId, String language, String competence);

    /**
     * get comalat user by sakai id
     *
     * @param sakaiUserId      - String
     * @param languageProperty - String can be null to get property automatically
     * @return the comalat user
     */
    ComalatUser getComalatUserBySakaiUserId(String sakaiUserId, String languageProperty);

    ComalatUser createComalatUserIfNotExistsForCurrentLanguage(String sessionUserId);

    /**
     * get the record information of a comalat user
     * Pascal Nowak (pascal.nowak@student.uni-siegen.de)
     *
     * @param comalatUserId  - String
     * @param assessmentName - String
     * @param tao            - boolean
     * @param intermediate - boolean if user is on an intermediate level
     * @param executionId - String executionID of TAO
     * @return a list with additional test information
     */
    List<ComalatAdditionalTestInformation> getComalatAdditionalTestInformation(String comalatUserId, String assessmentName, boolean tao, boolean intermediate, String executionId);

    /**
     * get the assessment Information of the comalat user
     * Pascal Nowak (pascal.nowak@student.uni-siegen.de)
     *
     * @param pubAssessmentId - String
     * @return ComalatAssessmentInformation
     */
    ComalatAssessmentInformation getAssessmentInformation(String pubAssessmentId);

    /**
     * get the comalat grading of the database
     *
     * @param userId - String
     * @return a list of grading objects of the given user
     */
    List<ComalatGrade> getComalatGrading(String userId);

    /**
     * inserts grades into the comalat grading tables
     *
     * @param comalatGrade - Object
     * @return the name of the grade object
     */
    String saveComalatGrade(ComalatGrade comalatGrade);

    /**
     * update the comalat grade in the database
     *
     * @param comalatGrade - Object
     */
    void updateComalatGrade(ComalatGrade comalatGrade);

    /**
     * get the activity / assessment of the comalat authoring page
     * Pascal Nowak (pascal.nowak@student.uni-siegen.de)
     *
     * @param activityName - String
     * @return Object
     */
    Object getComalatActivityOrAssessment(String activityName);

    /**
     * get the activity / assessment of the comalat authoring page
     * Pascal Nowak (pascal.nowak@student.uni-siegen.de)
     *
     * @param identifier - String
     * @return Object
     */
    Object getComalatActivityOrAssessmentByIdentifier(String identifier);

    /**
     * get the activities that are subitems of an assessment
     * Pascal Nowak (pascal.nowak@student.uni-siegen.de)
     *
     * @param assessmentIdentifier- String
     * @return the list of activities
     */
    List<ComalatActivity> getComalatActivitiesByAssessmentIdentifier(String assessmentIdentifier);

    /**
     * get the comalat activity by identifier
     *
     * @param identifier - String
     * @return the activity
     */
    ComalatActivity getComalatActivityByIdentifier(String identifier);

    /**
     * get all comalat activities
     *
     * @param languageShort - String
     * @return the list of activities
     */
    List<ComalatActivity> getComalatActivities(String languageShort);

    /**
     * get all comalat assessments
     *
     * @param languageShort - String
     * @return the list of assessements
     */
    List<ComalatAssessment> getComalatAssessments(String languageShort);

    /**
     * get all comalat groups
     *
     * @return list of groups
     */
    List<ComalatGroup> getComalatGroups();

    /**
     * save the comalat group
     *
     * @param comalatGroup - ComalatGroup
     */
    void saveComalatGroup(ComalatGroup comalatGroup);

    /**
     * get the threshold for grading, fuzzy and normal
     *
     * @return the list of thresholds
     */
    List<ComalatGradeThreshold> getComalatGradeThreshold();

    /**
     * get metadataname of the tag
     *
     * @param tag - String
     * @return list of metadata objects
     */
    List<ComalatMetadata> getComalatMetadata(String tag);

    List<ComalatMetadata> getComalatMetadataForLanguage(String language);

    /**
     *
     * @param gradePercentage
     * @param decisionPoint
     * @return
     */
    List<ComalatFeedback> getComalatFeedback(int gradePercentage, boolean decisionPoint);

    /**
     * get the comalat gradings of a specific test
     *
     * @param comalatId - String
     * @return a list of grading objects of the given comalatId
     */
    List<ComalatGrade> getComalatGradingOfTest(String comalatId);

    /**
     * get the difficulty of a specific test according to results so far
     *
     * @param comalatId - String
     * @return the difficulty of the test with the given comalatId
     */
    TestDifficulty getDifficultyOfTest(String comalatId);

    /**
     * get the subitems of a parent assessment
     *
     * @param assessmentId - the assessmentId
     * @return the list of comalat grades
     */
    List<ComalatGrade> getSubitemsOfParentAssessment(String assessmentId);

    /**
     * get current user progress within a subsection
     *
     * @param userId - String
     * @param grade  - ComalatGrade
     * @return subsection progress
     */
    int getSubsectionProgress(String userId, ComalatGrade grade);


    /**
     * get current user progress within a competence
     *
     * @param userId - String
     * @param grade  - ComalatGrade
     * @return subsection progress
     */
    int getCompetenceProgress(String userId, ComalatGrade grade);

    /**
     * get current user progress within a lesson
     *
     * @param userId       - String
     * @param siteLanguage - String
     * @param lesson       - String
     * @return lesson progress
     */
    int getLessonProgress(String userId, String siteLanguage, String lesson);

    /**
     * assign/update achievements to user
     * @return achievements - ComalatAchievement
     */
    List<ComalatAchievement> updateAchievements(String userId, String language, ComalatGrade grade, String lesson,
                                                int lessonProgress, String competence, int competenceProgress);

    /**
     * lookup lesson progress after test
     * @return achievements - ComalatAchievement
     */
    ComalatAchievement updateLessonAchievement(ComalatUser comalatUser, String language, String lesson, int lessonProgress);

    /**
     * lookup competence progress after test
     * @return achievements - ComalatAchievement
     */
    ComalatAchievement updateCompetenceAchievement(ComalatUser comalatUser, String language, Boolean beginner,
                                                   String competence, int competenceProgress);

    /**
     * lookup course progressions after test
     * @return achievements - ComalatAchievement
     */
    ComalatAchievement updateCourseAchievement(ComalatUser comalatUser, String language, ComalatGrade grade);

    /**
     * lookup course progressions after test
     * @return achievements - ComalatAchievement
     */
    ComalatAchievement updateUserLevelAchievement(ComalatUser comalatUser, String language, ComalatGrade grade);

    /**
     * get achievement segment value for given progress
     * @param progress - int
     * @return segment
     */
    int retrieveAchievementSegment(int progress);

    List<ComalatUser> getComalatUsersForAllLanguages(String sakaiUserId);

    UserGradingAverageData getAssessedAvgByUserIdAndMetadata(String sakaiUserId, String language, String topic);
}
