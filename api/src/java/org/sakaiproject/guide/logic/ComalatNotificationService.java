package org.sakaiproject.guide.logic;

import java.util.ArrayList;

/**
 * interface ComalatNotificationService - the notification service of the comalat project
 *
 * @author Pascal Nowak (pascal.nowak@student.uni-siegen.de)
 */
public interface ComalatNotificationService {

    /**
     * set the site for every user
     *
     * @param userId - String
     * @param siteId   - String
     */
    void setSite(String userId, String siteId);

    /**
     * set the session id for the current user
     *
     * @param userId    - String
     * @param sessionId - String
     */
    void setSessionId(String userId, String sessionId);

    /**
     * delete the observer in the eventTrackingService
     */
    void removeObserver();

    boolean shouldTransition(String sessionId);

    int shouldNavigate(String sessionId);

    void addNavigationCode(String userId, int code);

    int getRedirectionCode();

    void setRedirectionCode(int redirectionCode);

    /**
     * skip the activities and proceed to next step
     *
     * @param userId - String
     * @return
     */
    ArrayList<String> skipActivities(String userId);
}
