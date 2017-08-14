package org.sakaiproject.guide.model;

import java.io.Serializable;

/**
 * ComalatAssessmentInformation - display the grades of an assessment in the transition page
 *
 * @author Pascal Nowak (pascal.nowak@student.uni-siegen.de)
 */
public class ComalatAssessmentInformation implements Serializable {

    private static final long serialVersionUID = 1L;

    private String assessmentName;
    private int assessmentId;
    private String pubAssessmentId;
    private double maximumScore;

    public ComalatAssessmentInformation() {

    }

    public String getAssessmentName() {
        return assessmentName;
    }

    public void setAssessmentName(String assessmentName) {
        this.assessmentName = assessmentName;
    }

    public int getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(int assessmentId) {
        this.assessmentId = assessmentId;
    }

    public String getPubAssessmentId() {
        return pubAssessmentId;
    }

    public void setPubAssessmentId(String pubAssessmentId) {
        this.pubAssessmentId = pubAssessmentId;
    }

    public double getMaximumScore() {
        return maximumScore;
    }

    public void setMaximumScore(double maximumScore) {
        this.maximumScore = maximumScore;
    }
}
