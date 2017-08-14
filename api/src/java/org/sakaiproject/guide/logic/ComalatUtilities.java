package org.sakaiproject.guide.logic;

import org.sakaiproject.authz.api.Role;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by george on 7/8/2016.
 */
public interface ComalatUtilities {

    void init();

    void addMemberToGroup(Site site, Group group, String userId, Role role);

    void addMemberToGroup(ArrayList<String> groupIds, String userId);

    void addMemberToAllGroups(String userId);

    void removeMemberFromGroup(Site site, Group group, String userId);

    Group addGroupToSite(Site site, String groupName);

    void removeGroupFromSite(Site site, String groupTitle);

    boolean isCurrentUserMemberOfGroup(Group group);

    /**
     * get the short version of the language
     *
     * @param language - long version String
     * @return short version of language
     */
    String getShortLanguageOfSite(String language);

    /**
     * @return short language of current site
     */
    String getShortLanguageOfCurrentSite();

    /**
     * get the url of the current lesson to navigate to
     *
     * @param studentId - String
     * @return the url
     */
    String getLessonURL(String studentId);

    /**
     * set the lesson url
     *
     * @param studentId - String
     * @param url       - String
     */
    void setLessonURL(String studentId, String url);

    /**
     * set the lesson identifier
     *
     * @param studentId        - String
     * @param lessonIdentifier - String
     */
    void setLessonIdentifier(String studentId, String lessonIdentifier);

    /**
     * get the lesson identifier
     *
     * @param studentId - String
     * @return the identifier
     */
    String getLessonIdentifier(String studentId);

    /**
     * get the urls of the current lesson list
     *
     * @param studentId - String
     * @return - the urls
     */
    List<String> getLessonURLs(String studentId);

    /**
     * set the lesson url list
     *
     * @param studentId  - String
     * @param lessonURLS - list of lessons
     */
    void setLessonURLs(String studentId, List<String> lessonURLS);

    /**
     * save the skipped activities that belong to a user
     *
     * @param studentId         - String
     * @param skippedActivities - ArrayList<String>
     */
    void setSkippedActivities(String studentId, ArrayList<String> skippedActivities);

    /**
     * get the skipped activities of a user
     *
     * @param studentId - String
     * @return list of skipped activities
     */
    ArrayList<String> getSkippedActivities(String studentId);

    /**
     * get the tao executionid of the student
     *
     * @param studentId - String
     * @return the id
     */
    String getUserExecutionId(String studentId);

    /**
     * set the tao executionid of the student
     *
     * @param studentId   - String
     * @param executionId - String
     */
    void setUserExecutionId(String studentId, String executionId);

    /**
     * unlock all activities
     *
     * @param studentId - String
     */
    void unlockActivitiesInCurrentLesson(String studentId);

    /**
     * check if the test is intermediate lvl
     *
     * @param activity - Object
     * @return true if intermediate
     */
    boolean isTestIntermediate(Object activity);
}
