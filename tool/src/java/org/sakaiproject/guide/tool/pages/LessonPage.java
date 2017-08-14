package org.sakaiproject.guide.tool.pages;

import com.googlecode.wicket.jquery.ui.widget.menu.IMenuItem;
import com.googlecode.wicket.jquery.ui.widget.menu.Menu;
import com.googlecode.wicket.jquery.ui.widget.menu.MenuItem;
import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.guide.logic.ComalatNotificationService;
import org.sakaiproject.guide.logic.GradingLogic;
import org.sakaiproject.site.api.Group;

import java.util.*;

/**
 * The lesson page which provides an iframe for lessons and tests in the same
 * place
 *
 * @author Johannes Zenkert (johannes.zenkert@uni-siegen.de)
 */
public class LessonPage extends BasePage {

    // Redirection Codes for switching pages
    public static final int NO_REDIRECTION = 0;
    public static final int REDIRECT_PROGRESS_PAGE = 1;
    public static final int REDIRECT_PROFILE_PAGE = 2;
    public static final int REDIRECT_ACHIEVEMENT_PAGE = 3;
    public static final int REDIRECT_WELCOME_PAGE = 4;
    public static final int REDIRECT_TRANSITION_PAGE = 5;
    public static final int REDIRECT_SKIPPED_PAGE = 6;

    private final static Logger log = Logger.getLogger(LessonPage.class);
    private final String DATE_FORMAT = "dd-MMM-yyyy";
    private final String TIME_FORMAT = "HH:mm:ss";
    @SpringBean(name = "org.sakaiproject.guide.logic.ComalatNotificationService")
    private ComalatNotificationService comalatNotificationService;
    @SpringBean(name = "org.sakaiproject.guide.logic.GradingLogic")
    private GradingLogic gradingLogic;
    private RedirectPage page;
    private InlineFrame frame;
    private LessonPage instance = null;
    private boolean assessmentTransmitted = false;
    private String transitionUrl = "";
    private String siteId = "";

    private List<IMenuItem> menuList = new ArrayList<>();
    private String sessionUserId;

    //private AjaxLink skipButton;

    private String userId;

    public LessonPage() {

        sessionUserId = sessionManager.getCurrentSessionUserId();
        final String sessionId = usageSessionService.getSessionId();
        disableLink(firstLink);
        userId = sakaiProxy.getCurrentUserId();

        add(new Menu("menu", createMenu()));

        transitionUrl = getRequest().getContextPath();
        siteId = sakaiProxy.getCurrentSiteId();
        getRedirectionURL(REDIRECT_WELCOME_PAGE);
        page = new RedirectPage(transitionUrl);
        frame = new InlineFrame("frame", page);
        add(frame);
        instance = this;
        eventTrackingService.post(eventTrackingService.newEvent("samigo.test", null, true));
        comalatNotificationService.setSite(sessionUserId, sakaiProxy.getCurrentSiteId());
        comalatNotificationService.setSessionId(sessionUserId, sessionId);

        AbstractAjaxTimerBehavior timer = new AbstractAjaxTimerBehavior(Duration.milliseconds(500)) {

            protected void onTimer(AjaxRequestTarget target) {
                if (comalatNotificationService.shouldTransition(sessionId)) {
                    getRedirectionURL(REDIRECT_TRANSITION_PAGE);
                    page = new RedirectPage(transitionUrl);
                    log.info("AJAX BEHAVIOUR FIRED");
                    frame = new InlineFrame("frame", page);
                    instance.get("frame").replaceWith(frame);
                    frame.setResponsePage(frame.getPage());
                }
                /*if (comalatNotificationService.getRedirectionCode() != NO_REDIRECTION) {
                    String url = getRedirectionURL(comalatNotificationService.getRedirectionCode());
                    page = new RedirectPage(url);
                    comalatNotificationService.setRedirectionCode(NO_REDIRECTION);
                    setResponsePage(page);
                }*/
                int redirectionCode = comalatNotificationService.shouldNavigate(sessionId);
                if (redirectionCode != -1 && redirectionCode != NO_REDIRECTION) {
                    String url = getRedirectionURL(redirectionCode);
                    page = new RedirectPage(url);
                    //comalatNotificationService.setRedirectionCode(NO_REDIRECTION);
                    setResponsePage(page);
                }
            }
        };
        add(timer);

        Form<?> skipButton = new Form<Void>("skipButton") {
            @Override
            protected void onSubmit() {
                 /*
                get groups and collect incomplete ones
                get the activities that belong to the groups and finish them with 100% and the boolean "skipped" true
                then get the next steps and add the user to the new groups
                 */
                comalatUtilities.setSkippedActivities(userId, comalatNotificationService.skipActivities(userId));
                getRedirectionURL(REDIRECT_SKIPPED_PAGE);
                page = new RedirectPage(transitionUrl);
                frame = new InlineFrame("frame", page);
                instance.get("frame").replaceWith(frame);
                frame.setResponsePage(frame.getPage());
            }
        };
        add(skipButton);

        Form<?> unlockActivities = new Form<Void>("unlockActivities") {
            @Override
            protected void onSubmit() {
                 /*
                get groups and collect incomplete ones
                get the activities that belong to the groups and finish them with 100% and the boolean "skipped" true
                then get the next steps and add the user to the new groups
                 */
                comalatUtilities.unlockActivitiesInCurrentLesson(userId);
            }
        };
        add(unlockActivities);
    }

    public void onBeforeRender() {
        log.info("Rendering: " + this);
        super.onBeforeRender();
    }

    private List<IMenuItem> createMenu() {
        menuList = new ArrayList<>();

        // create the default page menu Item
        menuList.add(new MenuItem(new ResourceModel("lesson.menu.home")) {
            private final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                Menu m = (Menu) instance.get("menu");
                ArrayList<IMenuItem> list = (ArrayList<IMenuItem>) m.getItemList();
                /*while (list.size() > 2) {
                    list.remove(list.size() - 1);
                }*/
                getRedirectionURL(REDIRECT_WELCOME_PAGE);
                page = new RedirectPage(transitionUrl);
                log.info("Requestetd URL in IFrame:  " + transitionUrl);
                frame = new InlineFrame("frame", page);
                instance.get("frame").replaceWith(frame);
                frame.setResponsePage(frame.getPage()); // reload

                //reset the lesson menu
                // add lesson menu items
                while (menuList.size() > 1) {
                    menuList.remove(menuList.size() - 1);
                }
                addLessonMenu();
            }
        });
        addLessonMenu();
        return menuList;
    }

    /**
     * add the lessonmeu to the menu of the lesson page
     * Pascal Nowak (pascal.nowak@student.uni-siegen.de)
     */
    private void addLessonMenu() {
        try {
            // add lesson menu items
            menuList.add(new MenuItem(new ResourceModel("lesson.menu.lesson"),
                    lessonMenu(sakaiProxy.getLessonToolListForUserByLanguage(
                            sakaiProxy.getCurrentSite().getProperties().getProperty("language")))));
        } catch (IdUnusedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * build lesson menu structure
     * depending on the chosen lesson, the testmenu gets all available tests
     *
     * @param inputList - map
     * @return list of menu items
     * @throws IdUnusedException
     */
    private List<IMenuItem> lessonMenu(Map<String, String> inputList) throws IdUnusedException {
        List<IMenuItem> subList = new ArrayList<>();

        Map<String, String> tools = inputList;
        // convert map to list
        List<Map.Entry<String, String>> list = new LinkedList<>(tools.entrySet());

        // sort list with comparator, to compare the map values
        Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {

                if (o1.getValue().matches(".*\\s\\d+") && o2.getValue().matches(".*\\s\\d+")) {
                    int num1 = Integer.parseInt(o1.getValue().substring(o1.getValue().lastIndexOf(" ") + 1));
                    int num2 = Integer.parseInt(o2.getValue().substring(o2.getValue().lastIndexOf(" ") + 1));
                    return num1 - num2;
                } else {
                    return o1.getValue().compareTo(o2.getValue());
                }
            }
        });

        if (comalatUser.getDifficulty() == null) {
            comalatUser.setDifficulty("Beginner");
            projectLogic.saveComalatUser(comalatUser);
        }

        // convert sorted map back to a map
        Map<String, String> sortedMap = new LinkedHashMap<>();
        int i = 0;
        for (Iterator<Map.Entry<String, String>> it = list.iterator(); it.hasNext(); ) {
            if (comalatUser.getDifficulty().equals("Beginner") && i == 10) {
                break;
            }
            Map.Entry<String, String> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
            i++;
        }

        List<String> lessonURL_List = new ArrayList<>();

        // build up menu
        for (String UUID : sortedMap.keySet()) {
            String lessonName = sortedMap.get(UUID);

            String lessonURL = "/portal/tool-reset/" + UUID;
            lessonURL_List.add(lessonURL);

            //add lesson items to menu list
            subList.add(new MenuItem(tools.get(UUID)) {
                private final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    while (menuList.size() > 2) {
                        menuList.remove(menuList.size() - 1);
                    }
                    //update menu and add/replace final test item
                    Menu m = (Menu) instance.get("menu");
                    ArrayList<IMenuItem> list = (ArrayList<IMenuItem>) m.getItemList();
                    if (list.size() > 3) {
                        list.remove(list.size() - 1);
                    }
                    String lessonTitle = this.getTitle().getObject();
                    //lessonTitle = String.valueOf(lessonTitle.charAt(0)) + String.valueOf(lessonTitle.charAt(lessonTitle.length() - 1));
                    ((MenuItem) menuList.get(1)).setTitle(this.getTitle());
                    ArrayList<Group> groupList = new ArrayList<>();
                    try {
                        Collection<Group> existingSiteGroups = sakaiProxy.getCurrentSite().getGroups();
                        for (Group g : existingSiteGroups) {
                            //String[] groupTitle = g.getTitle().split("-");
                            // if (groupTitle.length > 1) {
                                /*if (groupTitle[1].equals(lessonTitle) && g.getTitle().endsWith("INCOMPLETE") && g.getUsers().contains(sakaiProxy.getCurrentUserId())) {
                                    groupList.add(g);
                                }*/
                            if (g.getTitle().endsWith("INCOMPLETE") && g.getUsers().contains(sakaiProxy.getCurrentUserId())) {
                                groupList.add(g);
                            }
                            // }
                        }
                    } catch (IdUnusedException e) {
                        e.printStackTrace();
                    }

                    String url = "/portal/tool-reset/" + UUID;
                    //menuList.add(new MenuItem(new ResourceModel("guide.menu.recommended"), JQueryIcon.ARROW_1_E,
                    //        recommendedMenu(groupList, url)));

                    comalatUtilities.setLessonURL(userId, url);
                    comalatUtilities.setLessonIdentifier(userId, lessonTitle);
                    page = new RedirectPage(url);
                    log.info("Requestetd URL in IFrame:  " + url);
                    frame = new InlineFrame("frame", page);
                    instance.get("frame").replaceWith(frame);
                    frame.setResponsePage(frame.getPage()); // reload
                }
            });
        }
        comalatUtilities.setLessonURLs(userId, lessonURL_List);
        return subList;
    }

    /**
     * handle clicks on the test menu and load the corresponding recommended test in the iframe
     *
     * @param input - Map of tests
     * @return the list with onclick function
     * Pascal nowak (pascal.nowak@student.uni-siegen.de)
     */
    /*private List<IMenuItem> recommendedMenu(Map<String, String> input) {
        List<IMenuItem> list = new ArrayList<>();

        //get the menu items and set the on click listener
        for (String alias : input.keySet()) {
            //add lesson items to menu list and set onClick
            list.add(new MenuItem(input.get(alias)) {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    String url = "/samigo-app/servlet/Login?id=" + alias;
                    page = new RedirectPage(url);
                    log.info("Requestetd URL in IFrame:  " + url);
                    frame = new InlineFrame("frame", page);
                    instance.get("frame").replaceWith(frame);
                    frame.setResponsePage(frame.getPage()); // reload
                }
            });
        }
        return list;
    }*/

    /**
     * handle clicks on the test menu and load the corresponding recommended action in the iframe
     *
     * @param input - list of groups
     * @param url   - String
     * @return the list with on click function
     */
    private List<IMenuItem> recommendedMenu(ArrayList<Group> input, String url) {
        List<IMenuItem> list = new ArrayList<>();

        //get the menu items and set the on click listener
        for (Group g : input) {
            //add lesson items to menu list and set onClick
            list.add(new MenuItem(g.getTitle()) {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    page = new RedirectPage(url);
                    log.info("Requestetd URL in IFrame:  " + url);
                    frame = new InlineFrame("frame", page);
                    instance.get("frame").replaceWith(frame);
                    frame.setResponsePage(frame.getPage()); // reload
                }
            });
        }
        return list;
    }

    /**
     * depending on the redirection code a new redirection url
     * will be generated and/or returned
     * Pascal Nowak (pascal.nowak@student.uni-siegen.de)
     * Baris Watzke (baris.watzke@student.uni-siegen.de)
     *
     * @param redirectionCode
     */
    private String getRedirectionURL(int redirectionCode) {
        switch (redirectionCode) {
            case REDIRECT_PROGRESS_PAGE:
                return getRequest().getContextPath() + "/progress/";
            case REDIRECT_PROFILE_PAGE:
                return getRequest().getContextPath() + "/profile/";
            case REDIRECT_ACHIEVEMENT_PAGE:
                return getRequest().getContextPath() + "/achievement/";

            // different logic for WelcomePage and TransitionPage
            // these pages are embedded in an iframe
            case REDIRECT_WELCOME_PAGE:
                transitionUrl = transitionUrl.replace("/site/" + siteId, "");
                if (transitionUrl.contains("/transition/")) {
                    transitionUrl = transitionUrl.replace("/transition/", "");
                }
                if (transitionUrl.contains("/skippedActivities/")) {
                    transitionUrl = transitionUrl.replace("/skippedActivities/", "");
                }
                if (!transitionUrl.contains("/welcome/")) {
                    transitionUrl += "/welcome/";
                }
                break;
            case REDIRECT_TRANSITION_PAGE:
                transitionUrl = transitionUrl.replace("/site/" + siteId, "");
                if (transitionUrl.contains("/welcome/")) {
                    transitionUrl = transitionUrl.replace("/welcome/", "");
                }
                if (transitionUrl.contains("/skippedActivities/")) {
                    transitionUrl = transitionUrl.replace("/skippedActivities/", "");
                }
                if (!transitionUrl.contains("/transition/")) {
                    transitionUrl += "/transition/";
                }
                break;
            case REDIRECT_SKIPPED_PAGE:
                transitionUrl = transitionUrl.replace("/site/" + siteId, "");
                if (transitionUrl.contains("/welcome/")) {
                    transitionUrl = transitionUrl.replace("/welcome/", "");
                }
                if (transitionUrl.contains("/transition/")) {
                    transitionUrl = transitionUrl.replace("/transition/", "");
                }
                if (!transitionUrl.contains("/skippedActivities/")) {
                    transitionUrl += "/skippedActivities/";
                }
        }
        return null;
    }
}
