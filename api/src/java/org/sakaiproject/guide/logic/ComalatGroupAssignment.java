package org.sakaiproject.guide.logic;

import org.sakaiproject.event.api.Event;

import java.util.HashMap;

/**
 * Interface ComalatGroupAssignment - assign a user to a new group if events get fired
 *
 * @author Pascal Nowak (pascal.nowak@student.uni-siegen.de)
 */
public interface ComalatGroupAssignment {

    /**
     * assign the user to a new group
     *
     * @param studentId - String
     * @param activityName - String
     * @param userAndSiteIds - HashMap<String, String>
     * @return true - if assigned
     */
    boolean assignUserToGroup(String studentId, String activityName, HashMap<String, String> userAndSiteIds);

    /**
     * assign a user to a group after he completed a lesson
     *
     * @param event - Event
     * @param userAndSiteIds - HashMap<String, String>
     */
    void assignUserToGroupAfterLesson(Event event, HashMap<String, String> userAndSiteIds);
}
