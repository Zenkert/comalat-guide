package org.sakaiproject.guide.logic;

import lombok.Getter;
import lombok.Setter;
import org.sakaiproject.authoring.model.ComalatActivity;
import org.sakaiproject.authz.api.*;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

import java.util.*;


/**
 * Created by george on 7/8/2016.
 */
public class ComalatUtilitiesImpl implements ComalatUtilities {

    @Getter
    @Setter
    private SakaiProxy sakaiProxy;
    private HashMap<String, List<String>> lessonURLsMap;
    private HashMap<String, String> lessonUrlMap;
    private HashMap<String, String> lessonIdentifierMap;
    private HashMap<String, ArrayList<String>> skippedActivitiesMap;
    private HashMap<String, String> userExecutionId;

    public void init() {
        lessonURLsMap = new HashMap<>();
        lessonUrlMap = new HashMap<>();
        lessonIdentifierMap = new HashMap<>();
        skippedActivitiesMap = new HashMap<>();
        userExecutionId = new HashMap<>();
    }

    @Override
    public void addMemberToGroup(Site site, Group group, String userId, Role role) {
        //we create a security advisor at this point
        SecurityService securityService = sakaiProxy.getSecurityService();
        SecurityAdvisor secAdvice = getAddSecurityAdvisor();
        securityService.pushAdvisor(secAdvice);
        try {
            Member m = group.getMember(sakaiProxy.getCurrentUserId());
            if (m == null) {
                group.addMember(userId, role.getId(), true, false);
            }
            sakaiProxy.getSiteService().save(site);
        } catch (IdUnusedException | PermissionException e) {
            e.printStackTrace();
        } finally {
            securityService.popAdvisor();
        }
    }

    @Override
    public void addMemberToGroup(ArrayList<String> groupIds, String userId) {
        //we create a security advisor at this point
        SecurityService securityService = sakaiProxy.getSecurityService();
        SecurityAdvisor secAdvice = getAddSecurityAdvisor();
        securityService.pushAdvisor(secAdvice);
        try {
            Site site = sakaiProxy.getCurrentSite();
            Role siteRole = site.getUserRole(sakaiProxy.getCurrentUserId());
            for (String groupId : groupIds) {
                Group group = site.getGroup(groupId);
                Role role = group.getRole(siteRole.getId());
                if (role == null) {
                    role = group.addRole("access");
                }
                Member m = group.getMember(sakaiProxy.getCurrentUserId());
                if (m == null) {
                    group.addMember(userId, role.getId(), true, false);
                }
            }
            sakaiProxy.getSiteService().save(site);
        } catch (IdUnusedException | RoleAlreadyDefinedException | PermissionException e) {
            e.printStackTrace();
        } finally {
            securityService.popAdvisor();
        }
    }

    @Override
    public void addMemberToAllGroups(String userId) {
        //we create a security advisor at this point
        SecurityService securityService = sakaiProxy.getSecurityService();
        SecurityAdvisor secAdvice = getAddSecurityAdvisor();
        securityService.pushAdvisor(secAdvice);
        try {
            Collection<Group> existingSiteGroups;
            // add the user to all the groups
            Site site = sakaiProxy.getCurrentSite();
            Role siteRole = site.getUserRole(sakaiProxy.getCurrentUserId());
            existingSiteGroups = site.getGroups();
            //join all base groups
            for (Group g : existingSiteGroups) {
                Role role = g.getRole(siteRole.getId());
                if (g.getTitle().contains("INCOMPLETE") && g.getTitle().contains("-")) {
                    g = addMemberHelper(g, userId, role, true, false);
                } else if (!g.getTitle().contains("COMPLETE") && g.getTitle().contains("-")) {
                    g = addMemberHelper(g, userId, role, true, false);
                }
            }
            sakaiProxy.getSiteService().save(site);
        } catch (IdUnusedException | RoleAlreadyDefinedException | PermissionException e) {
            e.printStackTrace();
        } finally {
            securityService.popAdvisor();
        }
    }

    private Group addMemberHelper(Group g, String userId, Role role, boolean active, boolean provided) throws RoleAlreadyDefinedException {
        if (role == null) {
            role = g.addRole("access");
        }
        Member m = g.getMember(sakaiProxy.getCurrentUserId());
        if (m == null) {
            g.addMember(userId, role.getId(), active, provided);
        }
        return g;
    }

    @Override
    public void removeMemberFromGroup(Site site, Group group, String userId) {
        //we create a security advisor at this point
        SecurityService securityService = sakaiProxy.getSecurityService();
        SecurityAdvisor secAdvice = getAddSecurityAdvisor();
        securityService.pushAdvisor(secAdvice);
        try {
            group.removeMember(userId);
            sakaiProxy.getSiteService().save(site);
        } catch (IdUnusedException | PermissionException e) {
            e.printStackTrace();
        } finally {
            securityService.popAdvisor();
        }
    }

    @Override
    public Group addGroupToSite(Site site, String groupTitle) {
        //we create a security advisor at this point
        SecurityService securityService = sakaiProxy.getSecurityService();
        SecurityAdvisor secAdvice = new SecurityAdvisor() {
            public SecurityAdvice isAllowed(String userId, String function, String reference) {
                if (AuthzGroupService.SECURE_ADD_AUTHZ_GROUP.equals(function)) {
                    return SecurityAdvice.ALLOWED;
                } else if (SiteService.SECURE_UPDATE_SITE.equals(function)) {
                    return SecurityAdvice.ALLOWED;
                } else {
                    return SecurityAdvice.NOT_ALLOWED;
                }
            }
        };
        securityService.pushAdvisor(secAdvice);
        try {
            Collection<Group> existingSiteGroups = site.getGroups();
            //group already exists in Site so return
            for (Group group : existingSiteGroups) {
                if (groupTitle.equals(group.getTitle())) {
                    //a group with the given title already exists
                    //so the group is not added
                    return null;
                }
            }
            //add a new group in the site
            Group newGroup = site.addGroup();
            Set<Role> roles = site.getRoles();
            for (Role role : roles) {
                try {
                    newGroup.addRole(role.getId(), role);
                } catch (RoleAlreadyDefinedException ignored) {
                }
            }
            //set the new group's title to the given group
            newGroup.setTitle(groupTitle);
            // needed to get it to show in the UI
            newGroup.getProperties().addProperty("group_prop_wsetup_created", Boolean.TRUE.toString());
            sakaiProxy.getSiteService().save(site);
            return newGroup;
        } catch (IdUnusedException | PermissionException e) {
            e.printStackTrace();
        } finally {
            securityService.popAdvisor();
        }
        return null;
    }

    @Override
    public void removeGroupFromSite(Site site, String groupTitle) {
        //we create a security advisor at this point
        SecurityService securityService = sakaiProxy.getSecurityService();
        SecurityAdvisor secAdvice = getRemoveSecurityAdvisor();
        securityService.pushAdvisor(secAdvice);
        try {
            Collection<Group> existingSiteGroups = site.getGroups();
            //group already exists in Site so return
            for (Group group : existingSiteGroups) {
                if (groupTitle.equals(group.getTitle())) {
                    site.removeGroup(group);
                    sakaiProxy.getSiteService().save(site);
                    return;
                }
            }
        } catch (IdUnusedException | PermissionException e) {
            e.printStackTrace();
        } finally {
            securityService.popAdvisor();
        }
    }

    @Override
    public boolean isCurrentUserMemberOfGroup(Group group) {
        Member m = group.getMember(sakaiProxy.getCurrentUserId());
        return (m != null);
    }

    @Override
    public String getShortLanguageOfSite(String language) {
        String result = "";
        switch (language) {
            case "english":
                result = "EN";
                break;
            case "german":
                result = "DE";
                break;
            case "spanish":
                result = "ES";
                break;
            case "DAF":
                result = "DAF";
                break;
            case "DAF1617":
                result = "DAF1617";
                break;
            default:
                result = "";
        }
        return result;
    }

    @Override
    public String getShortLanguageOfCurrentSite() {
        try {

            String language = sakaiProxy.getCurrentSite().getProperties().getProperty("language");
            switch (language) {
                case "english":
                    return "EN";
                case "german":
                    return "DE";
                case "spanish":
                    return "ES";
                case "DAF":
                    return "DAF";
                case "DAF1617":
                    return "DAF1617";
                default:
                    throw new IllegalArgumentException("Invalid language: " + language);
            }
        } catch (IdUnusedException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getLessonURL(String studentId) {
        return lessonUrlMap.get(studentId);
    }

    @Override
    public void setLessonURL(String studentId, String url) {
        lessonUrlMap.put(studentId, url);
    }

    @Override
    public void setLessonIdentifier(String studentId, String lessonIdentifier) {
        String[] lessonItems = lessonIdentifier.split(" ");
        int number = Integer.parseInt(lessonItems[1]);
        String lessonId;
        if (number < 10) {
            lessonId = "L0" + number;
        } else {
            lessonId = "L" + number;
        }
        lessonIdentifierMap.put(studentId, lessonId);
    }

    @Override
    public String getLessonIdentifier(String studentId) {
        return lessonIdentifierMap.get(studentId);
    }

    @Override
    public List<String> getLessonURLs(String studentId) {
        return lessonURLsMap.get(studentId);
    }

    @Override
    public void setLessonURLs(String studentId, List<String> lessonURLs) {
        lessonURLsMap.put(studentId, lessonURLs);
    }

    @Override
    public void setSkippedActivities(String studentId, ArrayList<String> skippedActivities) {
        skippedActivitiesMap.put(studentId, skippedActivities);
    }

    @Override
    public ArrayList<String> getSkippedActivities(String studentId) {
        return skippedActivitiesMap.get(studentId);
    }

    @Override
    public void setUserExecutionId(String studentId, String exectionId) {
        userExecutionId.put(studentId, exectionId);
    }

    @Override
    public String getUserExecutionId(String studentId) {
        return userExecutionId.get(studentId);
    }

    @Override
    public void unlockActivitiesInCurrentLesson(String studentId) {
        //we create a security advisor at this point
        SecurityService securityService = sakaiProxy.getSecurityService();
        SecurityAdvisor secAdvice = getAddSecurityAdvisor();
        securityService.pushAdvisor(secAdvice);
        try {
            Collection<Group> existingSiteGroups;
            Site site = sakaiProxy.getCurrentSite();
            Role siteRole = site.getUserRole(sakaiProxy.getCurrentUserId());
            existingSiteGroups = site.getGroups();
            for (Group g : existingSiteGroups) {
                Role role = g.getRole(siteRole.getId());
                String[] groupItems = g.getTitle().split("-");
                String lessonIdentifier = getLessonIdentifier(studentId);
                if (groupItems.length > 1 && lessonIdentifier != null) {
                    if (lessonIdentifier.equals(groupItems[1])) {
                        if (g.getTitle().contains("INCOMPLETE") && g.getTitle().contains("-")) {
                            g = addMemberHelper(g, studentId, role, true, false);
                        } else if (!g.getTitle().contains("COMPLETE") && g.getTitle().contains("-")) {
                            g = addMemberHelper(g, studentId, role, true, false);
                        }
                    }
                }
            }
            sakaiProxy.getSiteService().save(site);
        } catch (IdUnusedException | PermissionException | RoleAlreadyDefinedException e) {
            e.printStackTrace();
        } finally {
            securityService.popAdvisor();
        }
    }

    private SecurityAdvisor getAddSecurityAdvisor() {
        return (userId, function, reference) -> {
            if (AuthzGroupService.SECURE_ADD_AUTHZ_GROUP.equals(function)) {
                return SecurityAdvisor.SecurityAdvice.ALLOWED;
            } else if (AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP.equals(function)) {
                return SecurityAdvisor.SecurityAdvice.ALLOWED;
            } else if (SiteService.SECURE_UPDATE_SITE.equals(function)) {
                return SecurityAdvisor.SecurityAdvice.ALLOWED;
            } else {
                return SecurityAdvisor.SecurityAdvice.NOT_ALLOWED;
            }
        };
    }

    private SecurityAdvisor getRemoveSecurityAdvisor() {
        return (userId, function, reference) -> {
            if (AuthzGroupService.SECURE_REMOVE_AUTHZ_GROUP.equals(function)) {
                return SecurityAdvisor.SecurityAdvice.ALLOWED;
            } else if (SiteService.SECURE_UPDATE_SITE.equals(function)) {
                return SecurityAdvisor.SecurityAdvice.ALLOWED;
            } else {
                return SecurityAdvisor.SecurityAdvice.NOT_ALLOWED;
            }
        };
    }

    @Override
    public boolean isTestIntermediate(Object activity) {
        boolean result = false;
        if (activity instanceof ComalatActivity) {
            String lesson = ((ComalatActivity) activity).getLesson();
            int number = Integer.parseInt(lesson.replace("L", ""));
            if (number > 10) {
                result = true;
            }
        }
        return result;
    }
}
