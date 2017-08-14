package org.sakaiproject.guide.logic;

import org.sakaiproject.authoring.model.ComalatActivity;
import org.sakaiproject.authoring.model.ComalatFeedback;

import java.util.ArrayList;
import java.util.List;

/**
 * interface GradingLogic - to calculate the points of an assessment
 *
 * @author Pascal Nowak (pascal.nowak@student.uni-siegen.de)
 */
public interface RecommendationLogic {

    /**
     * get a recommendation based on the recommended group titles
     *
     * @param userId            - String
     * @param recommendedGroups - String[]
     * @param languageShort - String
     * @param languageProperty - String can be null to get the property automatically
     */
    void recommendBasedOnGroups(String userId, String[] recommendedGroups, String languageShort, String languageProperty);

    /**
     * get the recommended actions
     *
     * @param studentId - String
     * @param languageShort - String
     * @return list of comalat activities
     */
    ArrayList<ComalatActivity> getRecommendedActions(String studentId, String languageShort);


    /**
     *
     * @param gradePercentage
     * @param decisionPoint
     * @param subsection
     * @param instructionLanguage
     * @return
     */
    String getComalatFeedbackMessage(int gradePercentage, boolean decisionPoint, String subsection,
                                     String instructionLanguage);
}