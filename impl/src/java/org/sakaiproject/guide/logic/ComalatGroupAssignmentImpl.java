package org.sakaiproject.guide.logic;

import lombok.Getter;
import lombok.Setter;
import org.sakaiproject.authoring.model.ComalatActivity;
import org.sakaiproject.authoring.model.ComalatAssessment;
import org.sakaiproject.authoring.model.ComalatGradeThreshold;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * class ComalatGroupAssignment - assign a user to a new group if events get fired
 *
 * @author Pascal Nowak (pascal.nowak@student.uni-siegen.de)
 */
public class ComalatGroupAssignmentImpl implements ComalatGroupAssignment {

    @Getter
    @Setter
    private ProjectLogic projectLogic;

    @Getter
    @Setter
    private GradingLogic gradingLogic;

    @Getter
    @Setter
    private ComalatUtilities comalatUtilities;

    @Getter
    @Setter
    private RecommendationLogic recommendationLogic;

    @Getter
    @Setter
    private SakaiProxy sakaiProxy;

    /**
     * bean startup handling
     */
    public void init() {
    }

    @Override
    public boolean assignUserToGroup(String studentId, String activityName, HashMap<String, String> userAndSiteIds) {
        //get the information about next groups
        try {
            Site site = sakaiProxy.getSiteService().getSite(userAndSiteIds.get(studentId));
            String languageProperty = site.getProperties().getProperty("language");
            String[] recommendedGroups = getGroupAssignment(studentId, activityName,
                    comalatUtilities.getShortLanguageOfSite(languageProperty));
            recommendationLogic.recommendBasedOnGroups(studentId, recommendedGroups, comalatUtilities.getShortLanguageOfSite(languageProperty), languageProperty);
            //assign to a new group
            Collection<Group> existingSiteGroups = site.getGroups();
            for (String recommendedGroup : recommendedGroups) {
                recommendedGroup = recommendedGroup.trim();
                for (Group group : existingSiteGroups) {
                    if (recommendedGroup.equals(group.getTitle())) {
                        Role siteRole = site.getUserRole(studentId);
                        Role role = group.getRole(siteRole.getId());
                        if (role == null) {
                            try {
                                role = group.addRole("access");
                            } catch (RoleAlreadyDefinedException e) {
                                e.printStackTrace();
                            }
                        }
                        String[] groupSegment = recommendedGroup.split("-");
                        // if the group is named complete, delete the user from the corresponding incomplete group
                        if (groupSegment[groupSegment.length - 1].equals("COMPLETE")) {
                            String groupName = recommendedGroup.replace("COMPLETE", "INCOMPLETE");
                            for (Group groupToRemove : existingSiteGroups) {
                                if (groupToRemove.getTitle().equals(groupName)) {
                                    comalatUtilities.removeMemberFromGroup(site, groupToRemove, studentId);
                                    break;
                                }
                            }
                            //addUserToGroup(site, group, studentId, role, comalatGroup, comalatUser);
                            comalatUtilities.addMemberToGroup(site, group, studentId, role);
                        }
                        /*
                        if the group is named incomplete, check if the user is assigned to the complete group
                        if not, add the user to the incomplete group
                         */
                        else if (groupSegment[groupSegment.length - 1].equals("INCOMPLETE")) {
                            String groupName = recommendedGroup.replace("INCOMPLETE", "COMPLETE");
                            if (!isGroupCompleted(studentId, existingSiteGroups, groupName)) {
                                //addUserToGroup(site, group, studentId, role, comalatGroup, comalatUser);
                                comalatUtilities.addMemberToGroup(site, group, studentId, role);
                            }
                        }
                        /*
                        if the group has no COMPLETE or INCOMPLETE in its name, add the user to the group
                         */
                        else {
                            comalatUtilities.addMemberToGroup(site, group, studentId, role);
                            //addUserToGroup(site, group, studentId, role, comalatGroup, comalatUser);
                        }
                    }
                }
            }
            return true;
        } catch (NullPointerException | IdUnusedException e) {
            return false;
        }
    }

    /**
     * @param studentId    - String
     * @param activityName - String
     * @param language     - String
     * @return the recommended groups
     */
    private String[] getGroupAssignment(String studentId, String activityName, String language) {

        double gradingFuzzyPercentage = 50;
        double gradingPercentageNormalTest = 50;
        List<ComalatGradeThreshold> gradeThresholds = projectLogic.getComalatGradeThreshold();
        if (gradeThresholds != null) {
            for (ComalatGradeThreshold cGT : gradeThresholds) {
                if (cGT.getLanguage().equalsIgnoreCase(language)) {
                    gradingFuzzyPercentage = cGT.getPercentageFuzzyGrading();
                    gradingPercentageNormalTest = cGT.getPercentageActivity();
                    break;
                }
            }
        }
        Object gradeObject = projectLogic.getComalatActivityOrAssessment(activityName);
        boolean activity = false;
        ComalatActivity comalatActivity = null;
        ComalatAssessment comalatAssessment = null;
        String[] recommendedGroups;
        if (gradeObject instanceof ComalatActivity) {
            comalatActivity = (ComalatActivity) gradeObject;
            activity = true;
        } else {
            comalatAssessment = (ComalatAssessment) gradeObject;
            activity = false;
        }
        // if the comalat activity is a decision point, the fuzzy percentage is necessary to get the next recommended actions
        if (gradingLogic.getComalatGrade(studentId).isDecisionPoint() || !activity) {
            if (gradingLogic.getComalatGrade(studentId).retrieveAppropriatePercentage() >= gradingFuzzyPercentage) {
                if (!activity) {
                    recommendedGroups = comalatAssessment.getNextStep().split(",");
                } else {
                    recommendedGroups = comalatActivity.getNextStep().split(",");
                }
                gradingLogic.getComalatGrade(studentId).setSuccessful(true);
            } else {
                if (!activity) {
                    recommendedGroups = comalatAssessment.getAssignedGroupFail().split(",");
                } else {
                    recommendedGroups = comalatActivity.getAssignedGroupFail().split(",");
                }
                gradingLogic.getComalatGrade(studentId).setSuccessful(false);
            }
        }
        // if its no decision point, the normal percentage is necessary to get the next recommended actions
        else {
            String[] metaData = gradingLogic.getComalatGrade(studentId).getComalatId().split("-");
            try {
                recommendedGroups = comalatActivity.getNextStep().split(",");
            } catch (NullPointerException e) {
                recommendedGroups = null;
            }
            if (gradingLogic.getComalatGrade(studentId).getPercentage() >= gradingPercentageNormalTest) {
                gradingLogic.getComalatGrade(studentId).setSuccessful(true);
            } else {
                gradingLogic.getComalatGrade(studentId).setSuccessful(false);
                // if the activity is an extra activity assign in group fail
                if (metaData[2].equalsIgnoreCase("EX")) {
                    try {
                        recommendedGroups = comalatActivity.getAssignedGroupFail().split(",");
                    } catch (NullPointerException e) {
                        recommendedGroups = null;
                    }
                }
            }
        }

        projectLogic.updateComalatGrade(gradingLogic.getComalatGrade(studentId));
        return recommendedGroups;
    }

    /**
     * check if the completed group is assigned to a user
     *
     * @param studentId    - String
     * @param existingSiteGroups - collection of groups
     * @param groupName - name of the group to check
     * @return true if assigned, false otherwise
     */
    private boolean isGroupCompleted(String studentId, Collection<Group> existingSiteGroups, String groupName) {
        for (Group g : existingSiteGroups) {
            if (g.getTitle().equals(groupName)) {
                Member member = g.getMember(studentId);
                if (member == null) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * assign a user to a group after he completed a lesson
     *
     * @param event - Event
     */
    public void assignUserToGroupAfterLesson(Event event, HashMap<String, String> userAndSiteIds) {
        String eventResource;
        String studentId = "";
        String activityName = "";
        double pointsEarned = 0.0;
        eventResource = event.getResource();
        if (!eventResource.isEmpty()) {
            String[] eventInformation = eventResource.split("\\|");
            try {
                String[] temp = eventInformation[3].split("=");
                studentId = temp[1];
                temp = eventInformation[2].split("=");
                activityName = temp[1];
                temp = eventInformation[4].split("=");
                pointsEarned = Double.parseDouble(temp[1]);
            } catch (IndexOutOfBoundsException e) {
                studentId = "Id not found!";
                activityName = "Id not found!";
            } catch (NumberFormatException e) {
                pointsEarned = 0.0;
            }

            try {
                Site site = sakaiProxy.getSiteService().getSite(userAndSiteIds.get(studentId));
                String languageShort = comalatUtilities.getShortLanguageOfSite(site.getProperties().getProperty("language"));
                gradingLogic.setActivityInformation((ComalatActivity) projectLogic.getComalatActivityOrAssessment(activityName), studentId, pointsEarned, languageShort);
                gradingLogic.getComalatGrade(studentId).setSuccessful(true);
                projectLogic.saveComalatGrade(gradingLogic.getComalatGrade(studentId));
            } catch (IdUnusedException e) {
                e.printStackTrace();
            }
        }
        assignUserToGroup(studentId, activityName, userAndSiteIds);
    }
}
