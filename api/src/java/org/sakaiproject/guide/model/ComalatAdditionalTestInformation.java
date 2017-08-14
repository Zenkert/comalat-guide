package org.sakaiproject.guide.model;

import java.io.Serializable;
import java.util.Map;

/**
 * ComalatAdditionalTestInformation - display the grades of a record in an activity or assessment in the transition page
 *
 * @author Pascal Nowak (pascal.nowak@student.uni-siegen.de)
 */
public class ComalatAdditionalTestInformation implements Serializable {

    private static final long serialVersionUID = 1L;
    private int id;
    private int gradableObjectId;
    private String userId;
    private double pointsEarned;
    private Map<String, TaoExercise> taoExerciseInformation;

    public ComalatAdditionalTestInformation() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGradableObjectId() {
        return gradableObjectId;
    }

    public void setGradableObjectId(int gradableObjectId) {
        this.gradableObjectId = gradableObjectId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(double pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public Map<String, TaoExercise> getTaoExerciseInformation() {
        return taoExerciseInformation;
    }

    public void setTaoExerciseInformation(Map<String, TaoExercise> taoExerciseInformation) {
        this.taoExerciseInformation = taoExerciseInformation;
    }
}
