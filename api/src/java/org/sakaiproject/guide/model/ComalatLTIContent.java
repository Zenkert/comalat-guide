package org.sakaiproject.guide.model;

import java.io.Serializable;

/**
 * ComalatLTIContent - model to get the link to the tao database for the rest call
 *
 * @author Pascal Nowak (pascal.nowak@student.uni-siegen.de)
 */
public class ComalatLTIContent implements Serializable {

    private static final long serialVersionUID = 1L;
    private int id;
    private String placementSecret;
    private String title;

    public ComalatLTIContent(int id, String placementSecret, String title) {
        this.id = id;
        this.placementSecret = placementSecret;
        this.title = title;
    }

    /**
     * empty constructor for hibernation
     */
    public ComalatLTIContent() {

    }

    /**
     * get the id of the content
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * set id of the content
     *
     * @param id - int
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * get the placementSecret
     *
     * @return the placementSecret
     */
    public String getPlacementSecret() {
        return placementSecret;
    }

    /**
     * set the placementsecret
     *
     * @param placementSecret - String
     */
    public void setPlacementSecret(String placementSecret) {
        this.placementSecret = placementSecret;
    }

    /**
     * get the title
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * set the title
     *
     * @param title - String
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
