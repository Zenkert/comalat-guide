package org.sakaiproject.guide.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The Model for the COMALAT User
 *
 * @author George Kakarontzas (gkakaron@teilar.gr)
 */

public class ComalatUser implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long comalatUserId; // primary key - auto-generated
    private String sakaiUserId; // the userId equal to sakai userId
    private Long currentMaxLesson; // the current max Lesson for the User
    // 1-10
    private String specialization;
    private String language;
    private String difficulty;
    private String gender;
    private Integer age;
    private String educationLevel;
    private String instructionLanguage;
    private String currentOccupation;
    private String targetOccupation;
    private String targetPlaceOfResidence;
    private Set<ComalatGroup> groups = new HashSet<>(0);
    private Set<ComalatAchievement> achievements = new HashSet<>(0);
    private String recommendedActivities;

    public ComalatUser() {
    }

    public ComalatUser(Long comalatUserId) {
        this.comalatUserId = comalatUserId;
    }

    public Set<ComalatGroup> getGroups() {
        return groups;
    }

    public void setGroups(Set<ComalatGroup> groups) {
        this.groups = groups;
    }

    public Set<ComalatAchievement> getAchievements() {
        return achievements;
    }

    public void setAchievements(Set<ComalatAchievement> achievements) {
        this.achievements = achievements;
    }

    public String getSakaiUserId() {
        return sakaiUserId;
    }

    public void setSakaiUserId(String sakaiUserId) {
        this.sakaiUserId = sakaiUserId;
    }

    public Long getComalatUserId() {
        return comalatUserId;
    }

    public void setComalatUserId(Long comalatUserId) {
        this.comalatUserId = comalatUserId;
    }

    public Long getCurrentMaxLesson() {
        return currentMaxLesson;
    }

    public void setCurrentMaxLesson(Long currentMaxLesson) {
        this.currentMaxLesson = currentMaxLesson;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public String getInstructionLanguage() {
        return instructionLanguage;
    }

    public void setInstructionLanguage(String instructionLanguage) {
        this.instructionLanguage = instructionLanguage;
    }

    public String getCurrentOccupation() {
        return currentOccupation;
    }

    public void setCurrentOccupation(String currentOccupation) {
        this.currentOccupation = currentOccupation;
    }

    public String getTargetOccupation() {
        return targetOccupation;
    }

    public void setTargetOccupation(String targetOccupation) {
        this.targetOccupation = targetOccupation;
    }

    public String getTargetPlaceOfResidence() {
        return targetPlaceOfResidence;
    }

    public void setTargetPlaceOfResidence(String targetPlaceOfResidence) {
        this.targetPlaceOfResidence = targetPlaceOfResidence;
    }

    public String getRecommendedActivities() {
        return recommendedActivities;
    }

    public void setRecommendedActivities(String recommendedActivities) {
        this.recommendedActivities = recommendedActivities;
    }
}
