package org.sakaiproject.guide.logic;

import java.io.Serializable;

/**
 * ComalatEvent - saves information for the ComalatNotificationService
 *
 * @author Pascal Nowak (pascal.nowak@student.uni-siegen.de)
 */
public class ComalatEventImpl implements ComalatEvent, Serializable {

    private static final long serialVersionUID = 1L;

    private String key = "";
    private String gradebookId = "";
    private String pubAssessmentId = "";
    private String sessionId = "";
    private String userId = "";
    private String assessmentName = "";
    private Object observerObject;

    /**
     * constructor of ComalatEvent
     *
     * @param key   - String
     */
    public ComalatEventImpl(String key) {
        this.key = key;
    }

    /**
     * empty constructor for the bean
     */
    public ComalatEventImpl(){

    }

    /**
     * init method for the bean
     */
    /*public void init(){

    }*/

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getGradebookId() {
        return gradebookId;
    }

    @Override
    public void setGradebookId(String gradebookId) {
        this.gradebookId = gradebookId;
    }

    @Override
    public String getPubAssessmentId() {
        return pubAssessmentId;
    }

    @Override
    public void setPubAssessmentId(String pubAssessmentId) {
        this.pubAssessmentId = pubAssessmentId;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getAssessmentName() {
        return assessmentName;
    }

    @Override
    public void setAssessmentName(String assessmentName) {
        this.assessmentName = assessmentName;
    }

    @Override
    public Object getObserverObject() {
        return observerObject;
    }

    @Override
    public void setObserverObject(Object observerObject) {
        this.observerObject = observerObject;
    }
}
