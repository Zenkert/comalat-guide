package org.sakaiproject.guide.model;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * ComalatGrading - model to save the required data of an taken assessment in the comalat grading tables in the database
 *
 * @author Pascal Nowak (pascal.nowak@student.uni-siegen.de)
 */
public class ComalatGrade implements Serializable {

    private static final long serialVersionUID = 1L;
    private final static Logger log = Logger.getLogger(ComalatGrade.class);
    private Long comalatGradingId;
    private String comalatId;
    private String pubAssessmentId;
    private String assessmentName;
    private String userId;
    private double pointsEarned;
    private double maximumPoints;
    private double percentage;
    private Timestamp date;
    private boolean decisionPoint;
    private boolean nonAssessed;
    private String weighting;
    private double fuzzyPercentage;
    private double averagePercentage;
    private boolean fuzzyPercentageSet;
    private boolean successful;
    private String sectionName;
    private String competence;
    private int numberOfTest;
    private int numberOfAllActivities;
    private boolean finalTest;
    private boolean subItemOfFinalTest;
    private String parentAssessmentId;
    private String correctChoices;
    private String userChoices;
    private HashMap<String, TaoExercise> taoExercise;
    private boolean skipped;

    public ComalatGrade(String comalatId, String pubAssessmentId, String assessmentName, String userId, double pointsEarned,
                        double maximumPoints, double percentage, Timestamp date, boolean decisionPoint, boolean nonAssessed,
                        String weighting, boolean finalTest, boolean subItemOfFinalTest, String parentAssessmentId, boolean skipped) {
        this.comalatId = comalatId;
        this.pubAssessmentId = pubAssessmentId;
        this.assessmentName = assessmentName;
        this.userId = userId;
        this.pointsEarned = pointsEarned;
        this.maximumPoints = maximumPoints;
        this.percentage = percentage;
        this.date = date;
        this.decisionPoint = decisionPoint;
        this.nonAssessed = nonAssessed;
        this.weighting = weighting;
        this.finalTest = finalTest;
        this.subItemOfFinalTest = subItemOfFinalTest;
        this.parentAssessmentId = parentAssessmentId;
        this.taoExercise = new HashMap<>();
        this.skipped = skipped;
    }

    /**
     * empty constructor for hibernation
     */
    public ComalatGrade() {

    }

    /*
    Utility method that breaks down the comalat identifier and returns a string depending on the
    parameter part.
    For example for the comalat identifier
    EN-L01-N-G-P-GREET-1
    if part is "LANGUAGE" it will return "EN"
    if part is "LESSON" it will return "L01"
    if part is "BRANCH" it will return "N"
    if part is "COMPETENCE" it will return "G"
    if part is "TYPE" it will return "P"
    if part is "METADATA" it will return "GREET"
    if part is "NUMBER" it will return "1"
      */
    public String getPartOfComalatID(String part) {
        // get the comalatId to get all grades that belong together
        String[] idParts = comalatId.split("-");
        if      (part.equals("LANGUAGE"))   return idParts[0];
        else if (part.equals("LESSON"))     return idParts[1];
        else if (part.equals("BRANCH"))     return idParts[2];
        else if (part.equals("COMPETENCE")) return idParts[3];
        else if (part.equals("TYPE"))       return idParts[4];
        else if (part.equals("METADATA"))   return idParts[5];
        else if (part.equals("NUMBER"))     return idParts[6];
        else return "UNKNOWN";
    }


    /**
     * get the comalat grading id (increments for every item added to the database)
     *
     * @return the id
     */
    public Long getComalatGradingId() {
        return comalatGradingId;
    }

    /**
     * set the comalat grading id (increments for every item added to the database)
     *
     * @param comalatGradingId - Long
     */
    public void setComalatGradingId(Long comalatGradingId) {
        this.comalatGradingId = comalatGradingId;
    }

    /**
     * get the comalat identifier taken from the activity page in authoring
     *
     * @return the id
     */
    public String getComalatId() {
        return comalatId;
    }

    /**
     * set the comalat identifier taken from the activity page in authoring
     *
     * @param comalatId - String
     */
    public void setComalatId(String comalatId) {
        this.comalatId = comalatId;
    }

    /**
     * get the published assessment id given from samigo tool or the id of the activity in authoring
     *
     * @return the id
     */
    public String getPubAssessmentId() {
        return pubAssessmentId;
    }

    /**
     * set the published assessment id given from samigo tool or the id of the activity in authoring
     *
     * @param pubAssessmentId - String
     */
    public void setPubAssessmentId(String pubAssessmentId) {
        this.pubAssessmentId = pubAssessmentId;
    }

    /**
     * get the assessment / activity name
     *
     * @return the name
     */
    public String getAssessmentName() {
        return assessmentName;
    }

    /**
     * set the assessment / activity name
     *
     * @param assessmentName - String
     */
    public void setAssessmentName(String assessmentName) {
        this.assessmentName = assessmentName;
    }

    /**
     * get the user id (sakai ID)
     *
     * @return the id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * set the user id (sakai ID)
     *
     * @param userId - String
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * get the points earned
     *
     * @return the points
     */
    public double getPointsEarned() {
        return pointsEarned;
    }

    /**
     * set the points earned
     *
     * @param pointsEarned - double
     */
    public void setPointsEarned(double pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    /**
     * get the maximum points
     *
     * @return the points
     */
    public double getMaximumPoints() {
        return maximumPoints;
    }

    /**
     * set the maximum points
     *
     * @param maximumPoints - double
     */
    public void setMaximumPoints(double maximumPoints) {
        this.maximumPoints = maximumPoints;
    }

    /**
     * get the percentage of the result 0-100
     *
     * @return the percentage
     */
    public double getPercentage() {
        return percentage;
    }

    /**
     * set the percentage of the result 0-100
     *
     * @param percentage - double
     */
    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    /**
     * get the date
     *
     * @return the date
     */
    public Timestamp getDate() {
        return date;
    }

    /**
     * set the date
     *
     * @param date - Timestamp
     */
    public void setDate(Timestamp date) {
        this.date = date;
    }

    /**
     * get decisionPoint status
     *
     * @return true if decision point
     */
    public boolean isDecisionPoint() {
        return decisionPoint;
    }

    /**
     * set decision point status
     *
     * @param decisionPoint - boolean
     */
    public void setDecisionPoint(boolean decisionPoint) {
        this.decisionPoint = decisionPoint;
    }

    /**
     * get nonAssessed status - test should not be graded
     *
     * @return true if non assessed
     */
    public boolean isNonAssessed() {
        return nonAssessed;
    }

    /**
     * set non assessed status - test should not be graded
     *
     * @param nonAssessed - boolean
     */
    public void setNonAssessed(boolean nonAssessed) {
        this.nonAssessed = nonAssessed;
    }

    /**
     * get the weighting for grading
     *
     * @return the weights
     */
    public String getWeighting() {
        return weighting;
    }

    /**
     * set the weighting for grading
     *
     * @param weighting - String
     */
    public void setWeighting(String weighting) {
        this.weighting = weighting;
    }

    /**
     * get the percentage of the fuzzy grading
     *
     * @return the percentage
     */
    public double getFuzzyPercentage() {
        return fuzzyPercentage;
    }

    /**
     * set the percentage of the fuzzy grading
     *
     * @param fuzzyPercentage - double
     */
    public void setFuzzyPercentage(double fuzzyPercentage) {
        fuzzyPercentageSet = true;
        this.fuzzyPercentage = fuzzyPercentage;
    }

    public double getAveragePercentage() {
        return averagePercentage;
    }

    public void setAveragePercentage(double averagePercentage) {
        this.averagePercentage = averagePercentage;
    }

    public boolean isFuzzyPercentageSet() {
        return fuzzyPercentageSet;
    }

    public void setFuzzyPercentageSet(boolean fuzzyPercentageSet) {
        this.fuzzyPercentageSet = fuzzyPercentageSet;
    }

    public double retrieveAppropriatePercentage() {
        if (this.isFuzzyPercentageSet()) {
            return getFuzzyPercentage();
        }
        return getAveragePercentage();
    }


    /**
     * get successful state of the grade object
     *
     * @return true if successful
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * set successful state of the test
     *
     * @param successful - boolean
     */
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    /**
     * get section name of the grade
     *
     * @return the tag
     */
    public String getSectionName() {
        return sectionName;
    }

    /**
     * set section name of the grade
     *
     * @param sectionName - String
     */
    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    /**
     * get the competence of the grade
     *
     * @return the competence
     */
    public String getCompetence() {
        return competence;
    }

    /**
     * set the competence of the grade
     *
     * @param competence - String
     */
    public void setCompetence(String competence) {
        this.competence = competence;
    }

    /**
     * get the number of the current test
     *
     * @return the number
     */
    public int getNumberOfTest() {
        return numberOfTest;
    }

    /**
     * set the number of the current test
     *
     * @param numberOfTest - int
     */
    public void setNumberOfTest(int numberOfTest) {
        this.numberOfTest = numberOfTest;
    }

    /**
     * get final test state
     *
     * @return true if final test
     */
    public boolean isFinalTest() {
        return finalTest;
    }

    /**
     * set the final test state
     *
     * @param finalTest - boolean
     */
    public void setFinalTest(boolean finalTest) {
        this.finalTest = finalTest;
    }

    /**
     * get the number of all activities that belong to the decision point
     *
     * @return the number
     */
    public int getNumberOfAllActivities() {
        return numberOfAllActivities;
    }

    /**
     * set the number of all activities
     *
     * @param numberOfAllActivities - int
     */
    public void setNumberOfAllActivities(int numberOfAllActivities) {
        this.numberOfAllActivities = numberOfAllActivities;
    }

    /**
     * true, if the grade is a sub item of a final test
     * false if not.
     */
    public boolean isSubItemOfFinalTest() {
        return subItemOfFinalTest;
    }

    /**
     * set the sub-item-state
     *
     * @param subItemOfFinalTest - true if it is a subitem of a final test
     */
    public void setSubItemOfFinalTest(boolean subItemOfFinalTest) {
        this.subItemOfFinalTest = subItemOfFinalTest;
    }

    /**
     * get the parent assessment id
     *
     * @return the id
     */
    public String getParentAssessmentId() {
        return parentAssessmentId;
    }

    /**
     * set the parent assessment Id
     *
     * @param parentAssessmentId - String
     */
    public void setParentAssessmentId(String parentAssessmentId) {
        this.parentAssessmentId = parentAssessmentId;
    }

    /**
     * get correct choices from a test
     *
     * @return
     */
    public String getCorrectChoices() {
        return correctChoices;
    }

    /**
     * set correct choices to a test
     *
     * @param correctChoices
     */
    public void setCorrectChoices(String correctChoices) {
        this.correctChoices = correctChoices;
    }

    /**
     * get user choices from a test
     *
     * @return
     */
    public String getUserChoices() {
        return userChoices;
    }

    /**
     * set user choices to a test
     *
     * @param userChoices
     */
    public void setUserChoices(String userChoices) {
        this.userChoices = userChoices;
    }

    /**
     * get the results of the exercise in tao
     *
     * @return - taoExercise
     */
    public HashMap<String, TaoExercise> getTaoExercise() {
        return taoExercise;
    }

    /**
     * set the results of the tao exercise
     *
     * @param taoExercise - HashMap<String, TaoExercise>
     */
    public void setTaoExercise(HashMap<String, TaoExercise> taoExercise) {
        this.taoExercise = taoExercise;
        log.info("TAOTAOTAO COMALAT GRADE" + taoExercise.size());
        Iterator it = taoExercise.entrySet().iterator();
        StringBuilder sbUser = new StringBuilder();
        StringBuilder sbCorrect = new StringBuilder();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            ArrayList<String> helpList = ((TaoExercise) pair.getValue()).getUserChoice();
            for (String s : helpList) {
                sbUser.append(s).append(";");
            }
            helpList = ((TaoExercise) pair.getValue()).getCorrectChoice();
            for (String s : helpList) {
                sbCorrect.append(s).append(";");
            }
        }
        String user = sbUser.toString();
        String correct = sbCorrect.toString();
        if (user.length() > 1) {
            this.userChoices = user.substring(0, user.length() - 1);
        }
        if (correct.length() > 1) {
            this.correctChoices = correct.substring(0, correct.length() - 1);
        }
    }

    public boolean isSkipped() {
        return skipped;
    }

    public void setSkipped(boolean skipped) {
        this.skipped = skipped;
    }
}
