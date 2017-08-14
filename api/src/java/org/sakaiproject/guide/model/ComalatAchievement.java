package org.sakaiproject.guide.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * ComalatAchievement - model to save reached achievements by comalat users
 *
 * @author Baris Watzke (baris.watzke@student.uni-siegen.de)
 */
public class ComalatAchievement implements Serializable {

    private static final long serialVersionUID = 1L;
    private long comalatAchievementId;
    private String name;
    private String language;
    private String type;
    private int segment;
    private Timestamp createdDate;
    private Timestamp lastModifiedDate;

    private ComalatUser comalatUser;

    public ComalatAchievement(String name, String language, String type, int segment, ComalatUser user,
                              Timestamp createdDate, Timestamp lastModifiedDate) {
        this.name = name;
        this.language = language;
        this.type = type;
        this.segment = segment;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.comalatUser = user;
    }

    /**
     * empty constructor for hibernation
     */
    public ComalatAchievement() {

    }

    public long getComalatAchievementId() {
        return comalatAchievementId;
    }

    public void setComalatAchievementId(long comalatAchievementId) {
        this.comalatAchievementId = comalatAchievementId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSegment() {
        return segment;
    }

    public void setSegment(int segment) {
        this.segment = segment;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public Timestamp getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Timestamp lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public ComalatUser getComalatUser() {
        return comalatUser;
    }

    public void setComalatUser(ComalatUser comalatUser) {
        this.comalatUser = comalatUser;
    }
}
