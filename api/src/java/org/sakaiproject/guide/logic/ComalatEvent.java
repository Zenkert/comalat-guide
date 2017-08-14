package org.sakaiproject.guide.logic;

/**
 * interface ComalatEvent - event of comalat
 *
 * @author Pascal Nowak (pascal.nowak@student.uni-siegen.de)
 */
public interface ComalatEvent {

    String COMALAT_EVENT_ASSESSMENT_ID = "assessmentID";
    String COMALAT_EVENT_USER_ID = "userID";

    /**
     * gets the key of the event
     * @return the key
     */
    String getKey();

    /**
     * sets the key of the event
     * @param key - String
     */
    void setKey(String key);

    /**
     * gets the gradebookId of the event
     * @return gradebookId
     */
    String getGradebookId();

    /**
     * set the gradebookId of the event
     * @param gradebookId - String
     */
    void setGradebookId(String gradebookId);

    /**
     * gets the published assessment id
     *
     * @return pubAssessmentId
     */
    String getPubAssessmentId();

    /**
     * set the published assessment id
     *
     * @param pubAssessmentId - String
     */
    void setPubAssessmentId(String pubAssessmentId);

    /**
     * get the sessionId of the event
     * @return - sessionId
     */
    String getSessionId();

    /**
     * set the sessionId of the event
     * @param sessionId - String
     */
    void setSessionId(String sessionId);

    /**
     * gets the userId  of the event
     * @return the userId
     */
    String getUserId();

    /**
     * sets the userId of the event
     *
     * @param userId - String
     */
    void setUserId(String userId);

    /**
     * get the name of the assessment
     * @return the name
     */
    String getAssessmentName();

    /**
     * set the name of the assessment
     *
     * @param assessmentName -String
     */
    void setAssessmentName(String assessmentName);

    /**
     * get the observer object of the user
     *
     * @return the observer
     */
    Object getObserverObject();

    /**
     * set the observer object of the user
     *
     * @param observerObject - Object
     */
    void setObserverObject(Object observerObject);
}
