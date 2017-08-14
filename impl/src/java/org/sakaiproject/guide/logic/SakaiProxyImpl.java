package org.sakaiproject.guide.logic;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;

import java.util.*;

/**
 * Implementation of our SakaiProxy API
 *
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 */
public class SakaiProxyImpl implements SakaiProxy, Observer {

    private static final Logger log = Logger.getLogger(SakaiProxyImpl.class);
    @Getter
    @Setter
    private ToolManager toolManager;
    @Getter
    @Setter
    private SessionManager sessionManager;
    @Getter
    @Setter
    private UserDirectoryService userDirectoryService;
    @Getter
    @Setter
    private SecurityService securityService;
    @Getter
    @Setter
    private EventTrackingService eventTrackingService;
    @Getter
    @Setter
    private ServerConfigurationService serverConfigurationService;
    @Getter
    @Setter
    private SiteService siteService;
    @Getter
    @Setter
    private AuthzGroupService authzGroupService;
    private ArrayList<Event> lastEvent = new ArrayList<Event>();

    /**
     * {@inheritDoc}
     */
    public String getCurrentSiteId() {
        return toolManager.getCurrentPlacement().getContext();
    }

    /**
     * {@inheritDoc}
     */
    public String getCurrentUserId() {
        return sessionManager.getCurrentSessionUserId();
    }

    /**
     * {@inheritDoc}
     */
    public String getCurrentUserDisplayName() {
        return userDirectoryService.getCurrentUser().getDisplayName();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSuperUser() {
        return securityService.isSuperUser();
    }

    /**
     * {@inheritDoc}
     */
    public void postEvent(String event, String reference, boolean modify) {
        eventTrackingService.post(eventTrackingService.newEvent(event, reference, modify));
    }

    /**
     * {@inheritDoc}
     */
    public boolean getConfigParam(String param, boolean dflt) {
        return serverConfigurationService.getBoolean(param, dflt);
    }

    /**
     * {@inheritDoc}
     */
    public String getConfigParam(String param, String dflt) {
        return serverConfigurationService.getString(param, dflt);
    }

    /**
     * init - perform any actions required here for when this bean starts up
     */
    public void init() {
        log.info("init");
        eventTrackingService.addObserver(this);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getLessonToolList(String siteId) {
        Map<String, String> output = null;
        try {
            output = new HashMap<String, String>();
            for (ToolConfiguration tc : siteService.getSite(siteId).getTools("sakai.lessonbuildertool")) {
                output.put(tc.getPageId(), tc.getTitle());
            }
        } catch (IdUnusedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return output;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getLessonToolListForUser() throws IdUnusedException {
        Map<String, String> output = null;
        output = new HashMap<String, String>();
        for (Site s : siteService.getUserSites()) {
            for (ToolConfiguration tc : siteService.getSite(s.getId()).getTools("sakai.lessonbuildertool")) {
                output.put(tc.getToolId(), tc.getTitle());
            }
        }
        return output;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getLessonToolListForUserByLanguage(String lang) throws IdUnusedException {
        Map<String, String> output = null;
        output = new HashMap<String, String>();

        for (Site s : siteService.getUserSites()) {
            String str = s.getProperties().getProperty("language");
            if (str != null && str.equals(lang)) {
                for (ToolConfiguration tc : siteService.getSite(s.getId()).getTools("sakai.lessonbuildertool")) {
                    output.put(tc.getId(), tc.getTitle());
//					if(!siteService.getSite(tc.getPageId()).isPublished())siteService.getSite(tc.getPageId()).setPublished(true);	//force publish
                    log.info(tc.getSiteId());
                }
            }
        }
        return output;
    }

    /**
     * {@inheritDoc}
     */
    public String getUserSites() {
        String out = "";
        for (Site s : siteService.getUserSites()) {
            out += "Site: " + s.getTitle() + " ID: " + s.getId() + " Sprache: " + s.getProperties().getProperty("language") + "\n";
            try {
                for (ToolConfiguration tc : siteService.getSite(s.getId()).getTools("sakai.lessonbuildertool")) {
                    out += "Page ID:" + tc.getPageId() + " Site ID:" + tc.getSiteId() + "\n";
                }
            } catch (IdUnusedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return out;
    }

    /**
     * {@inheritDoc}
     */
    public Site getCurrentSite() throws IdUnusedException {
        return siteService.getSite(toolManager.getCurrentPlacement().getContext());
    }

    @Override
    public String getLastEvent() {
        // TODO Auto-generated method stub
        String out = "";
        ArrayList<Event> copy = (ArrayList<Event>) lastEvent.clone();
        for (Event e : copy) {
            if (e != null) {
                out += e.getEvent() + " " + e.getEventTime() + " " + e.getModify() + " " + e.getResource() + "\n";
            }
        }
        lastEvent.removeAll(copy);
        return out;
    }

    @Override
    public void update(Observable o, Object arg) {
        // TODO Auto-generated method stub
        if (arg == null || !(arg instanceof Event))
            return;
        Event event = (Event) arg;
        this.lastEvent.add(event);
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public ToolManager getToolManager() {
        return toolManager;
    }
}
