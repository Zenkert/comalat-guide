package org.sakaiproject.guide.logic;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.guide.model.ComalatGrade;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import java.util.*;

/**
 * class ComalatNotificationService - the notification service of the comalat project
 *
 * @author Pascal Nowak (pascal.nowak@student.uni-siegen.de)
 */
public class ComalatNotificationServiceImpl implements ComalatNotificationService, Observer {

    private final static Logger log = Logger.getLogger(ComalatNotificationServiceImpl.class);
    @Getter
    @Setter
    private GradingLogic gradingLogic;
    @Getter
    @Setter
    private ProjectLogic projectLogic;
    @Getter
    @Setter
    private EventTrackingService eventTrackingService;
    @Getter
    @Setter
    private SakaiProxy sakaiProxy;
    @Getter
    private ArrayList<ComalatEvent> events;
    //TODO save site id instead of sites objects
    private HashMap<String, String> userAndSiteIds;
    @Getter
    @Setter
    private ComalatGroupAssignment comalatGroupAssignment;
    @Getter
    @Setter
    private ComalatUtilities comalatUtilities;
    private HashMap<String, String> sessionIds;
    private Set<String> transitions;
    private HashMap<String, Integer> redirectionCodes;
    @Getter
    @Setter
    private int redirectionCode;

    private HashMap<String, String> userAssessmentName;

    /**
     * bean startup handling
     */
    public void init() {
        //TODO remove observer if session is closed
        eventTrackingService.addLocalObserver(this);
        userAssessmentName = new HashMap<>();
        userAndSiteIds = new HashMap<>();
        events = new ArrayList<>();
        transitions = new HashSet<>();
        redirectionCodes = new HashMap<>();
        sessionIds = new HashMap<>();
    }

    /**
     * bean destruction handling
     */
    public void destroy() {
        eventTrackingService.deleteObserver(this);
    }

    @Override
    public void setSite(String userId, String siteId) {
        if (userAndSiteIds.containsKey(userId)) {
            userAndSiteIds.replace(userId, siteId);
        } else {
            userAndSiteIds.put(userId, siteId);
        }
    }

    @Override
    public void setSessionId(String userId, String sessionId) {
        if (sessionIds.containsKey(userId)) {
            sessionIds.replace(userId, sessionId);
        } else {
            sessionIds.put(userId, sessionId);
        }
    }

    @Override
    public void removeObserver() {
        eventTrackingService.deleteObserver(this);
    }

    @Override
    public boolean shouldTransition(String sessionId) {
        return transitions.remove(sessionId);
    }

    public int shouldNavigate(String sessionId) {
        try {
            return redirectionCodes.remove(sessionId);
        } catch (NullPointerException e) {
            return -1;
        }
    }

    public void addNavigationCode(String userId, int code) {
        redirectionCodes.put(sessionIds.get(userId), code);
    }

    @Override
    public void update(Observable observable, Object o) {
        if (!(o instanceof Event)) {
            return;
        }
        // get the event
        Event event = (Event) o;
        String eventType = event.getEvent();
        ComalatEvent assessmentEvent = null;
        // check if the event is a specific eventType
        if (eventType.equals("gradebook.comalatEvent.test")) {
            assessmentEvent = buildComalatEvent(event, eventType);
            comalatGroupAssignment.assignUserToGroup(gradingLogic.getComalatGrade(event.getUserId()).getUserId(), gradingLogic.getComalatGrade(event.getUserId()).getAssessmentName(), userAndSiteIds);
            //notifyLessonPage(assessmentEvent);
            transitions.add(event.getSessionId());
        } else if (eventType.equals("gradebook.comalatEvent.taoTest")) {
            String eventResource = event.getResource();
            String[] assessmentIdInformation = eventResource.split("\\|");
            String[] assessmentName = assessmentIdInformation[4].split("=");
            String[] userName = assessmentIdInformation[2].split("=");
            userAssessmentName.put(userName[1], assessmentName[1]);
            //assessmentEvent = buildComalatEvent(event, eventType);
            //comalatGroupAssignment.assignUserToGroup(gradingLogic.getComalatGrade(assessmentEvent.getUserId()).getUserId(), gradingLogic.getComalatGrade(assessmentEvent.getUserId()).getAssessmentName(), userAndSiteIds);
            //events.add(assessmentEvent);
        } else if (eventType.equals("gradebook.comalatEvent.lesson")) {
            comalatGroupAssignment.assignUserToGroupAfterLesson(event, userAndSiteIds);
        } else if (eventType.equals("Tao ExecutionId")) {
            String[] notificationResource = event.getResource().split("_:_");
            comalatUtilities.setUserExecutionId(notificationResource[1], notificationResource[0]);
            assessmentEvent = buildComalatEventForTao(notificationResource[1], "Tao ExecutionId");
            comalatGroupAssignment.assignUserToGroup(gradingLogic.getComalatGrade(assessmentEvent.getUserId()).getUserId(), gradingLogic.getComalatGrade(assessmentEvent.getUserId()).getAssessmentName(), userAndSiteIds);
            events.add(assessmentEvent);
        } else if (eventType.equals("Tao Bye Bye")) {
            //split the information of the tao event
            String[] information = event.getResource().split("\\.");
            String sessionId = information[0];
            SessionManager sessionManager = ComponentManager.get(SessionManager.class);
            /*
            get the lesson page and iterate through the list to get the right event to be notified
             */
            List<Session> sessionList = sessionManager.getSessions();
            for (Session s : sessionList) {
                if (s.getId().equals(sessionId)) {
                    for (ComalatEvent comalatEvent : events) {
                        if (comalatEvent.getUserId().equals(s.getUserId())) {
                            //notifyLessonPage(comalatEvent);
                            transitions.add(sessionIds.get(s.getUserId()));
                            events.remove(comalatEvent);
                            break;
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * build the comalat event for tao and insert the grade into the database
     *
     * @param userId    - String
     * @param eventType - String
     */
    private ComalatEvent buildComalatEventForTao(String userId, String eventType) {
        ComalatEvent assessmentEvent = new ComalatEventImpl(eventType);
        //get all the data to create the object and save it to the database
        String assessmentUserId = userId;
        String assessmentName = userAssessmentName.get(userId);

        assessmentEvent.setUserId(assessmentUserId);
        assessmentEvent.setGradebookId("");
        assessmentEvent.setPubAssessmentId("");
        assessmentEvent.setSessionId("");
        assessmentEvent.setAssessmentName(assessmentName);

        return saveComalatGradeIntoDatabase(assessmentEvent, true);
    }

    /**
     * build the comalat event and insert the grade into the database
     *
     * @param event     - Event
     * @param eventType - String
     */
    private ComalatEvent buildComalatEvent(Event event, String eventType) {
        ComalatEvent assessmentEvent = new ComalatEventImpl(eventType);
        boolean taoTest = false;
        if (eventType.equals("gradebook.comalatEvent.taoTest")) {
            taoTest = true;
        }
        //get all the data to create the object and save it to the database
        String assessmentUserId = "";
        String gradebookId = "";
        String pubAssessmentId = "";
        String assessmentName = "";
        String eventResource = event.getResource();
        // if tao test: the attributes need to be created in another way if its no tao test
        if (!taoTest) {
            assessmentUserId = event.getUserId();
        }
        if (!eventResource.isEmpty()) {
            String[] assessmentIdInformation = eventResource.split("\\|");
            try {
                String[] temp = assessmentIdInformation[0].split("=");
                gradebookId = temp[1];
                temp = assessmentIdInformation[1].split("=");
                pubAssessmentId = temp[1];
                if (taoTest) {
                    temp = assessmentIdInformation[2].split("=");
                    assessmentUserId = temp[1];
                    temp = assessmentIdInformation[4].split("=");
                    assessmentName = temp[1];
                }
            } catch (IndexOutOfBoundsException e) {
                //gradebookId = "Id not found!";
                //pubAssessmentId = "Id not found!";
            }
        }
        assessmentEvent.setUserId(assessmentUserId);
        assessmentEvent.setGradebookId(gradebookId);
        assessmentEvent.setPubAssessmentId(pubAssessmentId);
        assessmentEvent.setSessionId(event.getSessionId());
        assessmentEvent.setAssessmentName(assessmentName);

        return saveComalatGradeIntoDatabase(assessmentEvent, taoTest);
    }

    /**
     * build the comalat grade object and save it to the database
     * if its a tao test the information of the test will be found in another database
     *
     * @param comalatEvent - ComalatEvent
     * @param taoTest      - boolean (true if its a tao test)
     * @return the event that was used to save the grade
     */
    private ComalatEvent saveComalatGradeIntoDatabase(ComalatEvent comalatEvent, boolean taoTest) {
        /*
        get all information about the grades out of the database and save it into a temporary attribute to
        create the comalat object and calculate the grade
         */
        try {
            Site site = sakaiProxy.getSiteService().getSite(userAndSiteIds.get(comalatEvent.getUserId()));

            String languageShort = comalatUtilities.getShortLanguageOfSite(site.getProperties().getProperty("language"));
            boolean intermediate = comalatUtilities.isTestIntermediate(projectLogic.getComalatActivityOrAssessment(comalatEvent.getAssessmentName()));
            gradingLogic.setAdditionalTestInformationList(comalatEvent.getUserId(), projectLogic.getComalatAdditionalTestInformation(comalatEvent.getUserId(),
                    comalatEvent.getAssessmentName(), taoTest, intermediate, comalatUtilities.getUserExecutionId(comalatEvent.getUserId())));
            boolean assessment = false;
            if (taoTest) {
                assessment = gradingLogic.setTestInformation(comalatEvent.getUserId(), projectLogic.getComalatActivityOrAssessment(comalatEvent.getAssessmentName()),
                        comalatEvent.getUserId(), languageShort);
            } else {
                assessment = gradingLogic.setTestInformation(comalatEvent.getUserId(), projectLogic.getAssessmentInformation(comalatEvent.getPubAssessmentId()),
                        comalatEvent.getUserId(), languageShort);
            }
            projectLogic.saveComalatGrade(gradingLogic.getComalatGrade(comalatEvent.getUserId()));
            if (assessment) {
                //set the parentAssessmentID for all sub items and save them in the database
                gradingLogic.saveGradesOfAssessmentInDatabase(comalatEvent.getUserId());
            }
        } catch (IdUnusedException e) {
            e.printStackTrace();
        }
        return comalatEvent;

    }

    @Override
    public ArrayList<String> skipActivities(String userId) {
        ArrayList<String> skippedActivitites = new ArrayList<>();
        //get the last lesson the user was into and skip all incomplete activities to their next step
        String lessonURL = comalatUtilities.getLessonURL(userId);
        int lessonIndex = (comalatUtilities.getLessonURLs(userId).indexOf(lessonURL)) + 1;
        String lessonIdentifier = "";
        if (lessonIndex < 10) {
            lessonIdentifier = "0" + lessonIndex;
        } else {
            lessonIdentifier = Integer.toString(lessonIndex);
        }
        Collection<Group> groups = new ArrayList<>();
        try {
            groups = sakaiProxy.getCurrentSite().getGroups();
        } catch (IdUnusedException e) {
            e.printStackTrace();
        }
        ArrayList<Group> groupsToSkip = new ArrayList<>();
        //get all INCOMPLETE groups the user is assigned in
        if (!groups.isEmpty()) {
            for (Group g : groups) {
                if (g.getTitle().contains("INCOMPLETE") && g.getTitle().contains(lessonIdentifier)) {
                    Member m = g.getMember(userId);
                    if (m != null) {
                        groupsToSkip.add(g);
                    }
                }
            }
        }
        /*
        take the activities and add the user to the next steps
        this cant be done in 1 step with getting the groups, because then the whole learning path would be skipped
         */
        for (Group g : groupsToSkip) {
            String result = computeGradeOfActivityToSkip(g, userId);
            if (!result.isEmpty()) {
                skippedActivitites.add(result);
            }
        }
        return skippedActivitites;
    }

    /**
     * check the next step of the activity that belongs to the given group
     * insert user in the next step
     *
     * @param group  - Group
     * @param userId - String
     * @return the name of the activity skipped
     */
    private String computeGradeOfActivityToSkip(Group group, String userId) {
        String activityIdentifier = group.getTitle().replace("-INCOMPLETE", "");
        Object result = projectLogic.getComalatActivityOrAssessmentByIdentifier(activityIdentifier);
        String languageShort = null;
        if (result != null) {
            try {
                languageShort = comalatUtilities.getShortLanguageOfSite(sakaiProxy.getCurrentSite().getProperties().getProperty("language"));
            } catch (IdUnusedException e) {
                e.printStackTrace();
            }
            //insert a grade of 100% and a boolean skipped to true in the grade table
            ComalatGrade gradeToSave = gradingLogic.buildComalatGradeToSkip(result, userId, languageShort);
            String name = projectLogic.saveComalatGrade(gradingLogic.buildComalatGradeToSkip(result, userId, languageShort));
            if (gradeToSave.isFinalTest()) {
                gradingLogic.saveGradesOfAssessmentInDatabase(userId);
            }
            //add user to next step groups
            if (comalatGroupAssignment.assignUserToGroup(userId,
                    gradingLogic.getComalatGrade(userId).getAssessmentName(), userAndSiteIds)) {
                return name;
            }
        }
        return "";
    }
}
