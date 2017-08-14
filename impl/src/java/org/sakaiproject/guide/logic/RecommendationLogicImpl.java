package org.sakaiproject.guide.logic;

import lombok.Getter;
import lombok.Setter;
import org.sakaiproject.authoring.model.ComalatActivity;
import org.sakaiproject.authoring.model.ComalatFeedback;
import org.sakaiproject.util.ResourceLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * RecommendationLogic - based on the next groups the user gets the recommendation to a new activity
 *
 * @author Pascal Nowak (pascal.nowak@student.uni-siegen.de)
 */
public class RecommendationLogicImpl implements RecommendationLogic {

    @Getter
    @Setter
    private ProjectLogic projectLogic;
    private HashMap<String, ArrayList<ComalatActivity>> recommendedAction;
    public RecommendationLogicImpl() {

    }

    /**
     * init method for the bean
     */
    public void init() {
        recommendedAction = new HashMap<>();
    }

    @Override
    public void recommendBasedOnGroups(String userId, String[] recommendedGroups, String languageShort, String languageProperty) {
        ArrayList<ComalatActivity> comalatActivities = new ArrayList<>();
        StringBuilder recommendedActivitiesForComalatUser = new StringBuilder();
        if (recommendedGroups != null) {
            for (String groupName : recommendedGroups) {
                groupName = groupName.trim();
                if (!groupName.contains("INCOMPLETE") && !groupName.contains("COMPLETE")) {
                    ComalatActivity activityToAdd = projectLogic.getComalatActivityByIdentifier(groupName);
                    if (activityToAdd != null) {
                        comalatActivities.add(activityToAdd);
                        recommendedActivitiesForComalatUser.append(activityToAdd.getActivityName()).append("\n");
                    }
                }
            }
        }
        recommendedAction.put(userId, comalatActivities);
        projectLogic.getComalatUserBySakaiUserId(userId, languageProperty).
                setRecommendedActivities(recommendedActivitiesForComalatUser.toString());
    }

    @Override
    public ArrayList<ComalatActivity> getRecommendedActions(String userId, String languageShort) {
        return recommendedAction.get(userId);
    }


    @Override
    public String getComalatFeedbackMessage(int gradePercentage, boolean decisionPoint, String subsection,
                                            String instructionLanguage) {
        List<ComalatFeedback> feedbackList = projectLogic.getComalatFeedback(gradePercentage, decisionPoint);
        if (feedbackList.size() == 0) return "";

        int randomIndex = (int)((Math.random() * feedbackList.size()));
        String feedbackMessage = "";

        switch (instructionLanguage) {
            case "German":
                feedbackMessage = feedbackList.get(randomIndex).getFeedbackDE();
                break;
            case "English":
                feedbackMessage = feedbackList.get(randomIndex).getFeedbackEN();
                break;
            case "Spanish":
                feedbackMessage = feedbackList.get(randomIndex).getFeedbackES();
                break;
            case "Arabic":
                feedbackMessage = feedbackList.get(randomIndex).getFeedbackAR();
                break;
            case "Kurdish":
                feedbackMessage = feedbackList.get(randomIndex).getFeedbackKU();
                break;
        }
        return feedbackMessage.replaceAll("%SUBSECTION%", subsection);
    }
}

