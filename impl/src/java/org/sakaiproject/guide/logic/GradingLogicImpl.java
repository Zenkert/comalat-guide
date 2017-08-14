package org.sakaiproject.guide.logic;

import lombok.Getter;
import lombok.Setter;
import org.sakaiproject.authoring.model.ComalatActivity;
import org.sakaiproject.authoring.model.ComalatAssessment;
import org.sakaiproject.authoring.model.ComalatGradeThreshold;
import org.sakaiproject.authoring.model.ComalatMetadata;
import org.sakaiproject.guide.model.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GradingLogic - to calculate the points of an assessment
 *
 * @author Pascal Nowak (pascal.nowak@student.uni-siegen.de)
 */
public class GradingLogicImpl implements GradingLogic {

    private HashMap<String, List<ComalatAdditionalTestInformation>> additionalTestInformationListMap;
    private HashMap<String, ComalatAdditionalTestInformation> additionalTestInformationMap;
    private HashMap<String, Object> testInformationMap;
    private HashMap<String, ComalatGrade> comalatGradeMap;
    private HashMap<String, ArrayList<ComalatGrade>> subItemsOfAssessment;

    @Getter
    @Setter
    private ProjectLogic projectLogic;

    public GradingLogicImpl() {
    }

    /**
     * init method for the bean
     */
    public void init() {
        additionalTestInformationListMap = new HashMap<>();
        additionalTestInformationMap = new HashMap<>();
        testInformationMap = new HashMap<>();
        comalatGradeMap = new HashMap<>();
        subItemsOfAssessment = new HashMap<>();

    }

    @Override
    public ComalatAdditionalTestInformation getAdditionalTestInformation(String studentId) {
        return additionalTestInformationMap.get(studentId);
    }

    @Override
    public void setAdditionalTestInformationList(String studentId, List<ComalatAdditionalTestInformation> additionalTestInformationList) {
        additionalTestInformationListMap.put(studentId, additionalTestInformationList);
    }

    @Override
    public Object getTestInformation(String studentId) {
        return testInformationMap.get(studentId);
    }

    @Override
    public boolean setTestInformation(String studentId, Object testInformation, String userId, String languageShort) {
        this.testInformationMap.put(studentId, testInformation);
        return buildComalatGradeObject(userId, languageShort);
    }

    /**
     * build the comalat grade based on the information of records and the testInformation
     *
     * @param userId        - String
     * @param languageShort - String
     */
    private boolean buildComalatGradeObject(String userId, String languageShort) {
        //check the list of additionalTestInformation if its an assessment of the lesson builder or if its a comalatactivity
        List<ComalatAdditionalTestInformation> additionalTestInformationList = additionalTestInformationListMap.get(userId);
        Object testInformation = testInformationMap.get(userId);
        ComalatAdditionalTestInformation additionalTestInformation = null;
        ComalatGrade comalatGrade = null;
        boolean assessment = false;
        for (ComalatAdditionalTestInformation rinfo : additionalTestInformationList) {
            /*
            SAMIGO TEST
             */
            if (testInformation instanceof ComalatAssessmentInformation) {
                if (((ComalatAssessmentInformation) testInformation).getAssessmentId() == rinfo.getGradableObjectId()) {
                    additionalTestInformation = rinfo;
                    //get the identifier of the comalat assessment from the ComalatActivity table
                    ComalatActivity comalatActivity = (ComalatActivity) projectLogic.getComalatActivityOrAssessment((((ComalatAssessmentInformation) testInformation).getAssessmentName()));
                    comalatGrade = new ComalatGrade(comalatActivity.getComalatIdentifier(), ((ComalatAssessmentInformation) testInformation).getPubAssessmentId(),
                            ((ComalatAssessmentInformation) testInformation).getAssessmentName(), additionalTestInformation.getUserId(),
                            additionalTestInformation.getPointsEarned(), ((ComalatAssessmentInformation) testInformation).getMaximumScore(),
                            getPercentage(additionalTestInformation.getPointsEarned(), comalatActivity),
                            Timestamp.valueOf(LocalDateTime.now()), comalatActivity.isDecisionPoint(), comalatActivity.isNonAssessed(),
                            comalatActivity.getWeighting(), false, false, "", false);
                    comalatGrade = getMetadataNameOfComalatGrade(comalatGrade, comalatActivity.getMetadataTag(), comalatActivity.getCompetence(), languageShort);
                    break;
                }
            }
            /*
            COMALAT ACTIVITY - TAO TEST
             */
            else if (testInformation instanceof ComalatActivity) {
                comalatGrade = new ComalatGrade(((ComalatActivity) testInformation).getComalatIdentifier(),
                        ((ComalatActivity) testInformation).getComalatActivityId().toString(),
                        ((ComalatActivity) testInformation).getActivityName(), userId,
                        rinfo.getPointsEarned(), ((ComalatActivity) testInformation).getScore(),
                        getPercentage(rinfo.getPointsEarned(), testInformation),
                        Timestamp.valueOf(LocalDateTime.now()), ((ComalatActivity) testInformation).isDecisionPoint(),
                        ((ComalatActivity) testInformation).isNonAssessed(),
                        ((ComalatActivity) testInformation).getWeighting(), false, false, "", false);
                comalatGrade = getMetadataNameOfComalatGrade(comalatGrade, ((ComalatActivity) testInformation).getMetadataTag(),
                        ((ComalatActivity) testInformation).getCompetence(), languageShort);
                comalatGrade.setTaoExercise((HashMap<String, TaoExercise>) rinfo.getTaoExerciseInformation());
            }
            /*
            COMALAT ASSESSMENT LIKE FINAL TEST
             */
            else if (testInformation instanceof ComalatAssessment) {
                assessment = true;
                comalatGrade = new ComalatGrade(((ComalatAssessment) testInformation).getComalatIdentifier(),
                        ((ComalatAssessment) testInformation).getComalatAssessmentId().toString(),
                        ((ComalatAssessment) testInformation).getAssessmentName(), userId,
                        rinfo.getPointsEarned(), ((ComalatAssessment) testInformation).getScore(),
                        getPercentage(rinfo.getPointsEarned(), testInformation),
                        Timestamp.valueOf(LocalDateTime.now()), false,
                        false, null, true, false, "", false);
                testInformation = getItemsOfAssessment(rinfo, (ComalatAssessment) testInformation, languageShort);
                //grade Items of the assessment and save them in the database
                ArrayList<ComalatGrade> gradesToCalculate = saveItemsOfAssessment(rinfo.getTaoExerciseInformation(), userId, languageShort, (ComalatAssessment) testInformation);
                calculateAverageOrFuzzyGradingOfAssessment(gradesToCalculate, userId, comalatGrade, languageShort);
            }
        }
        if (comalatGrade.isDecisionPoint()) {
            comalatGrade = calculatePercentageOfActivityDecisionPoint(userId, comalatGrade, languageShort);
        }
        comalatGrade = getCountOfActivitiesOfThisSection(comalatGrade);
        comalatGradeMap.put(userId, comalatGrade);
        return assessment;
    }

    @Override
    public ComalatGrade buildComalatGradeToSkip(Object activityOrAssessment, String userId, String languageShort) {
        ComalatGrade gradeToSkip = null;
        // normal tests
        if (activityOrAssessment instanceof ComalatActivity) {
            ComalatActivity cA = (ComalatActivity) activityOrAssessment;
            gradeToSkip = new ComalatGrade(cA.getComalatIdentifier(), cA.getComalatActivityId().toString(), cA.getActivityName(),
                    userId, cA.getScore(), cA.getScore(), 100, Timestamp.valueOf(LocalDateTime.now()), cA.isDecisionPoint(), cA.isNonAssessed(),
                    cA.getWeighting(), false, false, "", true);
            gradeToSkip = getMetadataNameOfComalatGrade(gradeToSkip, cA.getMetadataTag(),
                    cA.getCompetence(), languageShort);
        }
        // final tests
        else {
            ComalatAssessment cA = (ComalatAssessment) activityOrAssessment;
            gradeToSkip = new ComalatGrade(cA.getComalatIdentifier(), cA.getComalatAssessmentId().toString(),
                    cA.getAssessmentName(), userId, cA.getScore(), cA.getScore(), 100, Timestamp.valueOf(LocalDateTime.now()),
                    false, false, null, true, false, "", true);
            List<ComalatActivity> subItems = projectLogic.getComalatActivitiesByAssessmentIdentifier(cA.getComalatIdentifier());
            //grade Items of the assessment and save them in the database
            ArrayList<ComalatGrade> gradesToCalculate = saveItemsOfAssessmentToSkip(userId, subItems, languageShort);
            calculateAverageOrFuzzyGradingOfAssessment(gradesToCalculate, userId, gradeToSkip, languageShort);
        }
        if (gradeToSkip.isDecisionPoint()) {
            gradeToSkip = calculatePercentageOfActivityDecisionPoint(userId, gradeToSkip, languageShort);
        }
        gradeToSkip = getCountOfActivitiesOfThisSection(gradeToSkip);
        comalatGradeMap.put(userId, gradeToSkip);
        return gradeToSkip;
    }

    /**
     * get the items that belong to an assessment and save them in that object
     *
     * @param rinfo           - ComalatAdditionalTestInformation
     * @param testInformation - ComalatAssessment
     * @param languageShort   - String
     * @return the comalat assessment
     */
    private ComalatAssessment getItemsOfAssessment(ComalatAdditionalTestInformation rinfo, ComalatAssessment testInformation, String languageShort) {
        Set<ComalatActivity> activities = new HashSet<>();
        activities.addAll(projectLogic.getComalatActivitiesByAssessmentIdentifier(testInformation.getComalatIdentifier()));
        //activities.addAll(tempActivities2);
        testInformation.setComalatActivities(activities);
        return testInformation;
    }

    /**
     * save the items of the assessment in a grade object
     */
    private ArrayList<ComalatGrade> saveItemsOfAssessment(Map<String, TaoExercise> taoExercises, String userId, String languageShort, ComalatAssessment testInformation) {
        //get the items
        HashSet<ComalatActivity> items = (HashSet<ComalatActivity>) testInformation.getComalatActivities();
        ArrayList<ComalatGrade> gradesToSave = new ArrayList<>();
        for (ComalatActivity cA : items) {
            ComalatGrade itemGrade = new ComalatGrade(cA.getComalatIdentifier(),
                    cA.getComalatActivityId().toString(),
                    cA.getActivityName(), userId,
                    taoExercises.get(cA.getComalatIdentifier()).getScore(), cA.getScore(),
                    getPercentage(taoExercises.get(cA.getComalatIdentifier()).getScore(), cA),
                    Timestamp.valueOf(LocalDateTime.now()), cA.isDecisionPoint(),
                    cA.isNonAssessed(), cA.getWeighting(), false, true, "", false);
            itemGrade = getMetadataNameOfComalatGrade(itemGrade, cA.getMetadataTag(),
                    cA.getCompetence(), languageShort);
            itemGrade.setTaoExercise((HashMap<String, TaoExercise>) taoExercises);
            gradesToSave.add(itemGrade);
        }
        subItemsOfAssessment.put(userId, gradesToSave);
        //saveGradesOfAssessmentInDatabase(gradesToSave);
        return gradesToSave;
    }

    /**
     * save the items of the assessment that is skipped in a grade object
     *
     * @param userId        - String
     * @param activities    -list of activities (subitems)
     * @param languageShort String
     * @return list of grades
     */
    private ArrayList<ComalatGrade> saveItemsOfAssessmentToSkip(String userId, List<ComalatActivity> activities, String languageShort) {
        //build up the grades
        ArrayList<ComalatGrade> gradesToSave = new ArrayList<>();
        for (ComalatActivity cA : activities) {
            ComalatGrade itemGrade = new ComalatGrade(cA.getComalatIdentifier(),
                    cA.getComalatActivityId().toString(), cA.getActivityName(), userId, cA.getScore(), cA.getScore(),
                    100, Timestamp.valueOf(LocalDateTime.now()), cA.isDecisionPoint(), cA.isNonAssessed(),
                    cA.getWeighting(), false, true, "", true);
            itemGrade = getMetadataNameOfComalatGrade(itemGrade, cA.getMetadataTag(),
                    cA.getCompetence(), languageShort);
            gradesToSave.add(itemGrade);
        }
        subItemsOfAssessment.put(userId, gradesToSave);
        //saveGradesOfAssessmentInDatabase(gradesToSave);
        return gradesToSave;
    }

    @Override
    public void saveGradesOfAssessmentInDatabase(String userId) {
        ArrayList<ComalatGrade> listOfItems = subItemsOfAssessment.get(userId);
        //get the assessmentID of the last inserted assessment of the user
        List<ComalatGrade> gradesOfUser = projectLogic.getComalatGrading(userId);
        //last grade is the assessment
        ComalatGrade assessmentGrade;
        try {
            assessmentGrade = gradesOfUser.get(gradesOfUser.size() - 1);
        } catch (IndexOutOfBoundsException e) {
            assessmentGrade = null;
        }
        for (ComalatGrade gradeToSave : listOfItems) {
            if (assessmentGrade != null) {
                gradeToSave.setParentAssessmentId(assessmentGrade.getComalatGradingId().toString());
            }
            projectLogic.saveComalatGrade(gradeToSave);
        }
    }

    @Override
    public void setActivityInformation(ComalatActivity comalatActivity, String userId, double pointsEarned, String languageShort) {
        double percentage;
        if (comalatActivity.isNonAssessed()) {
            percentage = 100;
        } else {
            percentage = (pointsEarned / comalatActivity.getScore()) * 100;
        }
        ComalatGrade comalatGrade = new ComalatGrade(comalatActivity.getComalatIdentifier(),
                comalatActivity.getComalatActivityId().toString(), comalatActivity.getActivityName(), userId, pointsEarned,
                comalatActivity.getScore(), percentage, Timestamp.valueOf(LocalDateTime.now()),
                comalatActivity.isDecisionPoint(), comalatActivity.isNonAssessed(), comalatActivity.getWeighting(), false, false, "", false);
        comalatGrade = getMetadataNameOfComalatGrade(comalatGrade, comalatActivity.getMetadataTag(), comalatActivity.getCompetence(), languageShort);
        comalatGradeMap.put(userId, comalatGrade);
    }

    @Override
    public ComalatGrade getComalatGrade(String studentId) {
        return comalatGradeMap.get(studentId);
    }

    @Override
    public void setComalatGrade(String studentId, ComalatGrade grade) {
        comalatGradeMap.put(studentId, grade);
    }

    @Override
    public double[] getTestResult(String userId) {
        ComalatGrade comalatGrade = comalatGradeMap.get(userId);
        return new double[]{comalatGrade.getPointsEarned(), comalatGrade.getMaximumPoints(), comalatGrade.getPercentage()};
    }

    /**
     * calculate percentage. if the activity is non assessed, the percentage is automatically 100%
     *
     * @param pointsEarned - double
     * @param object       - Object
     * @return the percentage
     */
    private double getPercentage(double pointsEarned, Object object) {
        if (object instanceof ComalatActivity) {
            if (((ComalatActivity) object).isNonAssessed()) {
                return 100;
            } else {
                return Math.round((pointsEarned / ((ComalatActivity) object).getScore()) * 100);
            }
        } else
            return Math.round((pointsEarned / ((ComalatAssessment) object).getScore()) * 100);
    }


    /**
     * calculate the percentage if the comalatgrade is an activity decision point
     * take all tests that belong to that activity and then calculate the percentage of all grades
     * @param userId - String
     * @param comalatGrade - ComalatGrade
     * @param languageShort - String
     * @return the comalat Grade
     */
    private ComalatGrade calculatePercentageOfActivityDecisionPoint(String userId, ComalatGrade comalatGrade, String languageShort) {
        // help lists to calculate the percentages
        //weights of the test (e.g. 2.0)
        ArrayList<Double> weightList = new ArrayList<>();
        ArrayList<Double> percentList = new ArrayList<>();
        //normalized weights (e.g. 0.5)
        ArrayList<Double> weightsNormalized = new ArrayList<>();
        List<ComalatGrade> gradeList = projectLogic.getComalatGrading(userId);// get the comalatId to get all grades that belong together
        String[] tempString = comalatGrade.getComalatId().split("-");
        HashSet<ComalatGrade> listToCalculate = new HashSet<>();


        StringBuilder compareString = new StringBuilder();
        for (int i = 0; i < (tempString.length - 1); i++) {
            compareString.append(tempString[i]).append("-");
        }

        // get the first weights and percentage of the decisionpoint
        double weightingSum = getWeighting(comalatGrade.getWeighting());
        weightList.add(getWeighting(comalatGrade.getWeighting()));
        percentList.add(comalatGrade.getPercentage());

        // fill the tempList of objects that belong together and will be rated
        ArrayList<ComalatGrade> tempList = new ArrayList<>();
        for (ComalatGrade cG : gradeList) {
            if ((!cG.isDecisionPoint()) && (!cG.isNonAssessed()) && (cG.getComalatId().contains(compareString.toString()))) {
                tempList.add(cG);
            }
        }

        // if the current gradeObject ist the last of its type, add it to the list of objects that will be rated
        for (ComalatGrade cG : tempList) {
            ComalatGrade maxIdGrade = null;
            for (ComalatGrade cG2 : tempList) {
                if (cG.getComalatId().equals(cG2.getComalatId())) {
                    if (cG2.getComalatGradingId() >= cG.getComalatGradingId()) {
                        maxIdGrade = cG2;
                    }
                }
            }
            listToCalculate.add(maxIdGrade);
        }

        // add the weights and percentages of the grades that will be rated
        for (ComalatGrade cG : listToCalculate) {
            double tempSum = getWeighting(cG.getWeighting());
            weightingSum += tempSum;
            weightList.add(tempSum);
            percentList.add(cG.getPercentage());
        }

        // add the normalized weights of all the grade objects
        for (Double aWeightList : weightList) {
            weightsNormalized.add(aWeightList / weightingSum);
        }

        return averageOrFuzzyGrading(weightsNormalized, percentList, comalatGrade, languageShort);
    }

    /**
     * calculate the percentage if the comalatgrade is an assessment (final test)
     * take all tests that belong to that assessment and then calculate the percentage of all grades
     *
     * @param gradesToCalculate - list of grades
     * @param userId - String
     * @param comalatGrade - ComalatGrade
     * @param languageShort - String
     * @return
     */
    private ComalatGrade calculateAverageOrFuzzyGradingOfAssessment(ArrayList<ComalatGrade> gradesToCalculate, String userId, ComalatGrade comalatGrade, String languageShort) {
        // help lists to calculate the percentages
        //weights of the test (e.g. 2.0)
        ArrayList<Double> weightList = new ArrayList<>();
        ArrayList<Double> percentList = new ArrayList<>();
        //normalized weights (e.g. 0.5)
        ArrayList<Double> weightsNormalized = new ArrayList<>();

        // get all the grades and weights of the gradesToCalculate
        double weightingSum = 0;
        for (ComalatGrade cG : gradesToCalculate) {
            double tempWeighting = getWeighting(cG.getWeighting());
            weightingSum += tempWeighting;
            weightList.add(tempWeighting);
            percentList.add(cG.getPercentage());
        }

        // add the normalized weights of all the grade objects
        for (Double weight : weightList) {
            weightsNormalized.add(weight / weightingSum);
        }
        return averageOrFuzzyGrading(weightsNormalized, percentList, comalatGrade, languageShort);
    }

    /**
     * average or fuzzy grading of the object
     *
     * @param weightsNormalized - list of normalized weights (e.g. 0.5)
     * @param percentList - list of percentages
     * @param comalatGrade -ComalatGrade
     * @param languageShort - String
     * @return the comalat grade
     */
    private ComalatGrade averageOrFuzzyGrading(ArrayList<Double> weightsNormalized, ArrayList<Double> percentList, ComalatGrade comalatGrade, String languageShort) {
        //get number of finished assessments of this kind. if there are less then NO_PR_GRADES constant -> average. above NO_PR_GRADES fuzzy
        List<ComalatGrade> gradingsOfThisTest = projectLogic.getComalatGradingOfTest(comalatGrade.getComalatId());
        //average percentage in grading is set always regardless the number of existing gradings for a test
        double avg = averageGrading(weightsNormalized, percentList);
        comalatGrade.setAveragePercentage(avg);
        //if the gradings of the test are more than NO_PR_GRADES and the average grade is between
        // the threshold plus-minus 1.0 then we also set the fuzzy percentage
        double base = 60.0;  //default threshold
        List<ComalatGradeThreshold> thresholds = projectLogic.getComalatGradeThreshold();
        for (ComalatGradeThreshold cgt : thresholds) {
            String shortLanguage = languageShort;
            //String shortLanguage = comalatUtilities.getShortLanguageOfCurrentSite();
            if (cgt.getLanguage().equals(shortLanguage)) {
                base = cgt.getPercentageActivity();
                break;
            }
        }
        if (gradingsOfThisTest.size() > StatisticalCompetenceAssessment.NO_PR_GRADES &&
                (avg>=base-10.0 && avg<=base+10.0)) {
            TestDifficulty difficulty = projectLogic.getDifficultyOfTest(comalatGrade.getComalatId());
            comalatGrade.setFuzzyPercentage(fuzzyGrading(weightsNormalized, percentList, difficulty));
        }
        return comalatGrade;
    }

    /**
     * get the numbers of the weighting-String for normalization
     *
     * @param weighting - String
     * @return the numbers 1 to 4
     */
    private double getWeighting(String weighting) {
        switch (weighting) {
            case "Very Important":
                return 4.0;
            case "Important":
                return 3.0;
            case "Less Important":
                return 1.0;
            default:
                return 2.0;
        }
    }

    /**
     * TODO Hey George, this is your entry point
     *
     * @param weightsNormalized - ArrayList<Double>
     * @param percentList       - ArrayList<Double>
     * @param difficulty
     * @return the percentage
     */
    private double fuzzyGrading(ArrayList<Double> weightsNormalized, ArrayList<Double> percentList, TestDifficulty difficulty) {
        StatisticalCompetenceAssessment s = new StatisticalCompetenceAssessmentImpl();
        return s.fuzzyGrade(weightsNormalized, percentList, difficulty);
    }

    /**
     * set average grading of the decision point if the number of finished tests is below 100
     *
     * @param weightsNormalized - ArrayList<Double>
     * @param percentList       - ArrayList<Double>
     * @return the percentage
     */
    private double averageGrading(ArrayList<Double> weightsNormalized, ArrayList<Double> percentList) {
        double result = 0.0;
        for (int i = 0; i < weightsNormalized.size(); i++) {
            result += weightsNormalized.get(i) * percentList.get(i);
        }
        result = Math.round(result * 100);
        return result / 100;
    }

    /**
     * get metadata of the comalat grade
     *
     * @param sectionTag    - String
     * @param competence    - String
     * @param languageShort - String
     */
    private ComalatGrade getMetadataNameOfComalatGrade(ComalatGrade grade, String sectionTag, String competence, String languageShort) {
        List<ComalatMetadata> metadataNames = projectLogic.getComalatMetadata(sectionTag);
        ComalatGrade result = grade;
        for (ComalatMetadata comalatMetadata : metadataNames) {
            if (comalatMetadata.getLanguage().equalsIgnoreCase((languageShort))) {
                result.setSectionName(comalatMetadata.getMetadataName());
                break;
            }
        }
        metadataNames = projectLogic.getComalatMetadata(competence);
        for (ComalatMetadata comalatMetadata : metadataNames) {
            if (comalatMetadata.getLanguage().equalsIgnoreCase((languageShort))) {
                result.setCompetence(comalatMetadata.getMetadataName());
                break;
            }
        }
        return result;
    }

    @Override
    public ComalatGrade getCountOfActivitiesOfThisSection(ComalatGrade comalatGrade) {
        if (!comalatGrade.isFinalTest()) {
            String identifier = comalatGrade.getComalatId();
            String[] identifiers = identifier.split("-");
            StringBuilder sbIdentifier = new StringBuilder();
            for (int i = 0; i < (identifiers.length - 1); i++) {
                sbIdentifier.append(identifiers[i]).append("-");
            }
            List<ComalatActivity> comalatActivities = projectLogic.getComalatActivities(identifiers[0]).
                    stream().filter(a -> a.getLesson().equals(identifiers[1]) && a.getMetadataTag().equals(identifiers[5]) &&
                    a.getCompetence().equals(identifiers[3]) && a.getPath().equals(identifiers[2])).collect(Collectors.toList());
            int count = 0;
            int numberOfTest = 0;
            for (ComalatActivity cA : comalatActivities) {
                count += 1;
                if (cA.getComalatIdentifier().equals(comalatGrade.getComalatId())) {
                    numberOfTest = count;
                }
            }
            comalatGrade.setNumberOfTest(numberOfTest);
            comalatGrade.setNumberOfAllActivities(count);

        } else {
            comalatGrade.setNumberOfTest(1);
            comalatGrade.setNumberOfAllActivities(1);
        }
        return comalatGrade;
    }

    @Override
    public ArrayList<ComalatGrade> getSubItemsOfAssessment(String userId) {
        return subItemsOfAssessment.get(userId);
    }
}
