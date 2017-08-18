package org.sakaiproject.guide.tool.toolkit;

import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.guide.logic.GradingLogic;
import org.sakaiproject.guide.logic.ProjectLogic;
import org.sakaiproject.guide.logic.SakaiProxy;
import org.sakaiproject.guide.model.ComalatGrade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PageTools {

    @SpringBean(name = "org.sakaiproject.guide.logic.SakaiProxy")
    protected static SakaiProxy sakaiProxy;
    @SpringBean(name = "org.sakaiproject.guide.logic.ComalatUtilities")
    protected static org.sakaiproject.guide.logic.ComalatUtilities comalatUtilities;
    @SpringBean(name = "org.sakaiproject.guide.logic.GradingLogic")
    private static GradingLogic gradingLogic;
    @SpringBean(name = "org.sakaiproject.guide.logic.ProjectLogic")
    private static ProjectLogic projectLogic;

    /**
     * Create ArrayList with Evaluation Answers <Correct, False>
     */
    public static List<String[]> buildEvaluationRows(ComalatGrade comalatGrade, String userID) {
        List<String[]> rows = new ArrayList<>();
        String[] initializer = { new ResourceModel("record.evaluation.user_choice").getObject(),
                new ResourceModel("record.evaluation.correct_answer").getObject() };
        rows.add(initializer);
        ComalatGrade tempGrade = comalatGrade;
        // test if comalat grade is a final test
        if (comalatGrade.isFinalTest()) {
            ArrayList<ComalatGrade> subItemsOfTest = gradingLogic.getSubItemsOfAssessment(userID);
            for (ComalatGrade cG : subItemsOfTest) {
                rows.addAll(getNormalTestResultInformation(cG));
            }
        } else {
            rows.addAll(getNormalTestResultInformation(tempGrade));
        }
        // dummy entries for table rows stuffing
        while (rows.size() < 4) {
            String[] emptyArray = {"", ""};
            rows.add(emptyArray);
        }
        return rows;
    }

    /**
     * get the result information of the comalat test
     *
     * @param comalatGrade - ComalatGrade
     * @return the results in a list as arrays
     */
    private static ArrayList<String[]> getNormalTestResultInformation(ComalatGrade comalatGrade) {
        ArrayList<String[]> result = new ArrayList<>();
        try {
            String[] userChoices;
            if (comalatGrade.getUserChoices() == null) {
                userChoices = new String[0];
            } else {
                userChoices = comalatGrade.getUserChoices().split(";");
            }
            String[] correctChoices;
            if (comalatGrade.getCorrectChoices() == null) {
                correctChoices = new String[userChoices.length];
                Arrays.fill(correctChoices, "N/A");
            } else {
                correctChoices = comalatGrade.getCorrectChoices().split(";");
            }
            if (comalatGrade.getParentAssessmentId() != null && !comalatGrade.getParentAssessmentId().isEmpty()) {
                String[] itemName = {comalatGrade.getAssessmentName(), comalatGrade.getAssessmentName()};
                result.add(itemName);
            }
            for (int i = 0; i < correctChoices.length; i++) {
                String userChoice = "";
                if (i >= userChoices.length) {
                    userChoice = "";
                } else {
                    userChoice = userChoices[i];
                }
                String[] row = { userChoice, correctChoices[i] };
                result.add(row);
            }
        } catch (NullPointerException e) {
            return result;
        }
        return result;
    }

    /**
     * @param lessonIdentifier from ComalatGrade
     * @return lesson name in a readable format
     */
    public static String retrieveLessonName(String lessonIdentifier) {
        // Remove leading zero digit in lesson number
        String lessonNumber = Integer.valueOf(lessonIdentifier.substring(1)).toString();
        return new ResourceModel("welcome.lesson").getObject() + " " + lessonNumber;
    }
}
