package org.sakaiproject.guide.logic;

import org.sakaiproject.authoring.model.ComalatActivity;
import org.sakaiproject.guide.model.ComalatAdditionalTestInformation;
import org.sakaiproject.guide.model.ComalatGrade;

import java.util.ArrayList;
import java.util.List;

/**
 * interface GradingLogic - to calculate the points of an assessment
 *
 * @author Pascal Nowak (pascal.nowak@student.uni-siegen.de)
 */
public interface GradingLogic {

    /**
     * get the additionalGradeInformation
     *
     * @param studentId - String
     * @return additionalGradeInformation
     */
    ComalatAdditionalTestInformation getAdditionalTestInformation(String studentId);

    /**
     * set additional grade information
     *
     * @param studentId                     - String
     * @param additionalTestInformationList - list of objects
     */
    void setAdditionalTestInformationList(String studentId, List<ComalatAdditionalTestInformation> additionalTestInformationList);

    /**
     * get the test information
     *
     * @param studentId - String
     * @return the object testinformation
     */
    Object getTestInformation(String studentId);

    /**
     * build a comalat grade when user skips the activity
     * grade to 100% and boolean skipped = true * @param activityOrAssessment
     *
     * @param activityOrAssessment - Object
     * @param userId               - String
     * @param languageShort        - String
     * @return the grade to skip
     */
    ComalatGrade buildComalatGradeToSkip(Object activityOrAssessment, String userId, String languageShort);

    /**
     * set assessment information and build the comalatGrade
     *
     * @param studentId       - String
     * @param testInformation - Object
     * @param userId          - String
     * @param languageShort   - String
     * @return true if assessment
     */
    boolean setTestInformation(String studentId, Object testInformation, String userId, String languageShort);

    /**
     * get the comalat grade
     *
     * @param studentId String
     * @return the grade object
     */
    ComalatGrade getComalatGrade(String studentId);

    /**
     * set the last grade of an user
     *
     * @param studentId - String
     * @param grade     - ComalatGrade
     */
    void setComalatGrade(String studentId, ComalatGrade grade);

    /**
     * set the activity information in the object of the comalat grade
     *
     * @param comalatActivity - ComalatActivity
     * @param userId          - String
     * @param pointsEarned    - double
     * @param languageShort   - String
     */
    void setActivityInformation(ComalatActivity comalatActivity, String userId, double pointsEarned, String languageShort);

    /**
     * save the subitem of an assessment in the database
     *
     * @param userId       - String
     */
    void saveGradesOfAssessmentInDatabase(String userId);

    /**
     * get the result values of the last assessment
     *
     * @param studentId - String
     * @return a double array (Points earned, Maximum Points, Percentage)
     */
    double[] getTestResult(String studentId);

    /**
     * get the number of all activities and the index of the current one
     *
     * @param comalatGrade - ComalatGrade
     * @return the updated comalatGrade
     */
    ComalatGrade getCountOfActivitiesOfThisSection(ComalatGrade comalatGrade);

    /**
     * get the sub items of an assessment
     *
     * @param userId - String
     * @return the list of subitems (ComalatGrade)
     */
    ArrayList<ComalatGrade> getSubItemsOfAssessment(String userId);
}
