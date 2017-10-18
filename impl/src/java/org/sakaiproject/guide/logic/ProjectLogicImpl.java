package org.sakaiproject.guide.logic;

import org.apache.log4j.Logger;
import org.sakaiproject.authoring.model.*;
import org.sakaiproject.basiclti.util.LegacyShaUtil;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.guide.dao.ProjectDao;
import org.sakaiproject.guide.model.*;
import sun.java2d.SurfaceDataProxy;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ProjectLogic}
 *
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 */
public class ProjectLogicImpl implements ProjectLogic {

    private static final Logger log = Logger.getLogger(ProjectLogicImpl.class);
    private ProjectDao dao;

    @lombok.Getter
    @lombok.Setter
    private SakaiProxy sakaiProxy;


    public ProjectLogicImpl() {

    }

    public void setDao(ProjectDao dao) {
        this.dao = dao;
    }

    @Override
    public ComalatUser getComalatUserById(Long comalatUserId) {
        if (comalatUserId == null) {
            throw new IllegalArgumentException("comalatUserId cannot be null when getting comalatUser");
        }
        Search search = new Search(new Restriction("comalatUserId", comalatUserId));
        ComalatUser comalatUser = dao.findOneBySearch(ComalatUser.class, search);
        return comalatUser;
    }

    //returns all unique student ids in the comalat grade table
    @Override
    public Set<String> getGradedStudentsIDs() {
        List<ComalatGrade> grades = dao.findAll(ComalatGrade.class);
        HashSet<String> set = new HashSet<>();
        for (ComalatGrade grade : grades) {
            set.add(grade.getUserId());
        }
        return set;
    }

    @Override
    public UserGradingAverageData getAssessedAvgByUserIdAndCompetence(String userId, String language, String competence) {


        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null when getting comalat grades by user id and competence");
        }
        if (language == null) {
            throw new IllegalArgumentException("language cannot be null when getting comalat grades by user id and competence");
        }

        if (competence == null) {
            throw new IllegalArgumentException("competence cannot be null when getting comalat grades by user id and competence");
        }

        UserGradingAverageData result = new UserGradingAverageData();
        result.setUserId(userId);
        result.setLanguage(language);
        result.setCompetence(competence);

        //get all assessed activities for this user
        Restriction[] r = new Restriction[2];
        r[0] = new Restriction("nonAssessed", false);
        r[1] = new Restriction("userId", userId);


        Search search =
                new Search(r);
        List<ComalatGrade> grades = dao.findBySearch(ComalatGrade.class, search);

        //filter ther result and keep only the records which concern the requested competence
        //and the requested language
        List<ComalatGrade> filteredGrades = new ArrayList<>();
        for (ComalatGrade c : grades) {
            String lang = c.getPartOfComalatID("LANGUAGE");
            String comp = c.getPartOfComalatID("COMPETENCE");
            if (lang.equals(language) && comp.equals(competence))
                filteredGrades.add(c);
        }

        //sort the resulting list
        //using the comalatId. This will make filtering that follows faster
        Collections.sort(filteredGrades, new Comparator<ComalatGrade>() {
            @Override
            public int compare(ComalatGrade lhs, ComalatGrade rhs) {
                return lhs.getComalatId().compareTo(rhs.getComalatId());
            }
        });

        //only choose the unique last activities for each activity id
        int i = 0;
        double sum = 0.0;
        int count = 0;
        while (i < filteredGrades.size()) {
            ComalatGrade uniqueActivityGrade = filteredGrades.get(i);
            boolean finished = false;
            do {
                i++;
                if (i < filteredGrades.size()) {
                    ComalatGrade next = filteredGrades.get(i);
                    if ((next.getComalatId()).equals(uniqueActivityGrade.getComalatId())) {
                        if (uniqueActivityGrade.getDate().before(next.getDate())) {
                            uniqueActivityGrade = next;
                        }
                    } else {
                        //next activity doesn't have the same id
                        finished = true;
                    }
                } else {
                    //no more activities of this competency type for this user
                    finished = true;
                }
            }
            while (!finished);
            //sum += uniqueActivityGrade.retrieveAppropriatePercentage();
            sum += uniqueActivityGrade.getPercentage();
            count++;
        }
        if (count == 0) {
            result.setCount(0);
            result.setAverage(-1.0);
        }
        else {
            double original = (sum / count) / 100.0;
            BigDecimal bigDecimal = new BigDecimal(original);
            BigDecimal roundedWithScale = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
            double scaled = roundedWithScale.doubleValue();
            System.out.println("Original score = "+original+", Scaled score = "+scaled);
            result.setCount(count);
            result.setAverage(scaled);
        }
        return result;
    }

    @Override
    public int getCountOfNonAssessedActivitiesByUserIdAndCompetence(String userId, String language, String competence) {


        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null when getting comalat grades by user id and competence");
        }
        if (language == null) {
            throw new IllegalArgumentException("language cannot be null when getting comalat grades by user id and competence");
        }

        if (competence == null) {
            throw new IllegalArgumentException("competence cannot be null when getting comalat grades by user id and competence");
        }

        //get all assessed activities for this user
        Restriction[] r = new Restriction[2];
        r[0] = new Restriction("nonAssessed", true);
        r[1] = new Restriction("userId", userId);


        Search search =
                new Search(r);
        List<ComalatGrade> grades = dao.findBySearch(ComalatGrade.class, search);

        //filter ther result and keep only the records which concern the requested competence
        //and the requested language
        List<ComalatGrade> filteredGrades = new ArrayList<>();
        for (ComalatGrade c : grades) {
            String lang = c.getPartOfComalatID("LANGUAGE");
            String comp = c.getPartOfComalatID("COMPETENCE");
            if (lang.equals(language) && comp.equals(competence))
                filteredGrades.add(c);
        }

        //sort the resulting list
        //using the comalatId. This will make filtering that follows faster
        Collections.sort(filteredGrades, new Comparator<ComalatGrade>() {
            @Override
            public int compare(ComalatGrade lhs, ComalatGrade rhs) {
                return lhs.getComalatId().compareTo(rhs.getComalatId());
            }
        });

        //only choose the unique last activities for each activity id
        int i = 0;
        double sum = 0.0;
        int count = 0;
        while (i < filteredGrades.size()) {
            ComalatGrade uniqueActivityGrade = filteredGrades.get(i);
            boolean finished = false;
            do {
                i++;
                if (i < filteredGrades.size()) {
                    ComalatGrade next = filteredGrades.get(i);
                    if ((next.getComalatId()).equals(uniqueActivityGrade.getComalatId())) {
                        if (uniqueActivityGrade.getDate().before(next.getDate())) {
                            uniqueActivityGrade = next;
                        }
                    } else {
                        //next activity doesn't have the same id
                        finished = true;
                    }
                } else {
                    //no more activities of this competency type for this user
                    finished = true;
                }
            }
            while (!finished);
            count++;
        }

        return count;
    }

    @Override
    public ComalatUser getComalatUserBySakaiUserId(String sakaiUserId, String languageProperty) {
        if (sakaiUserId == null) {
            throw new IllegalArgumentException("comalatUserId cannot be null when getting comalatUser");
        }

        Search search =
                new Search(new Restriction("sakaiUserId", sakaiUserId));
        List<ComalatUser> comalatUsers = dao.findBySearch(ComalatUser.class, search);
        String language = "";
        if (languageProperty == null) {
            try {
                language = sakaiProxy.getCurrentSite().getProperties().getProperty("language");
            } catch (IdUnusedException e) {
                e.printStackTrace();
            }
        } else {
            language = languageProperty;
        }
        for (ComalatUser cu : comalatUsers) {
            if (cu.getLanguage().equals(language)) {
                return cu;
            }
        }
        return null;
    }

    @Override
    public boolean saveComalatUser(ComalatUser comalatUser) {
        dao.save(comalatUser);
        log.debug(" ComalatUser  " + comalatUser.getComalatUserId() +
                " successfuly saved");
        return true;
    }

    @Override
    public void saveComalatUserList(List<ComalatUser> comalatUsers) {
        for (ComalatUser comalatUser : comalatUsers) {
            saveComalatUser(comalatUser);
        }
    }

    @Override
    public void deleteComalatUser(ComalatUser comalatUser) {
        dao.delete(comalatUser);
    }

    @Override
    public void deleteAll(List<ComalatUser> comalatUsers) {
        for (ComalatUser comalatUser : comalatUsers) {
            deleteComalatUser(comalatUser);
        }
    }

    @Override
    public ComalatUser createComalatUserIfNotExistsForCurrentLanguage(String sessionUserId) {
        //get the comalatUser for the current site language if he exists
        ComalatUser comalatUser =
                this.getComalatUserBySakaiUserId(sessionUserId, null);
        //if comalatUser does not exist for the current language
        //we create a new comalat user
        if (comalatUser == null) {
            //we check to see if a comalat user with the same sakai id exists for other languages
            List<ComalatUser> comalatUsers =
                    this.getComalatUsersForAllLanguages(sessionUserId);

            comalatUser = new ComalatUser();
            comalatUser.setSakaiUserId(sessionUserId);
            comalatUser.setCurrentMaxLesson(1L);
            if (!comalatUsers.isEmpty()) {
                //get the first comalatUser from the already existing accounts
                ComalatUser otherAccount = comalatUsers.get(0);
                comalatUser.setAge(otherAccount.getAge());
                comalatUser.setCurrentOccupation(otherAccount.getCurrentOccupation());
                comalatUser.setEducationLevel(otherAccount.getEducationLevel());
                comalatUser.setGender(otherAccount.getGender());
                comalatUser.setInstructionLanguage(otherAccount.getInstructionLanguage());
            }
            saveComalatUser(comalatUser);
        }
        return comalatUser;
    }

    @Override
    public List<ComalatAdditionalTestInformation> getComalatAdditionalTestInformation(String comalatUserId,
                                                                                      String assessmentName,
                                                                                      boolean tao,
                                                                                      boolean intermediate, String executionId) {
        log.info("TAOTAOTAO " + assessmentName);
        List<ComalatAdditionalTestInformation> resultList;
        Map<String, TaoExercise> taoExercises = null;
        if (!tao) {
            Search search = new Search(new Restriction("userId", comalatUserId));
            resultList = dao.findBySearch(ComalatAdditionalTestInformation.class, search);
        } else {
            Search search = new Search(new Restriction("title", assessmentName));
            ComalatLTIContent comalatLTIContent = dao.findOneBySearch(ComalatLTIContent.class, search);
            String placeStr = "content:" + comalatLTIContent.getId();
            String suffix = ":::" + comalatUserId + ":::" + placeStr;
            String base_string = comalatLTIContent.getPlacementSecret() + suffix;
            String signature = LegacyShaUtil.sha256Hash(base_string);
            TaoResultsImpl taoResults = new TaoResultsImpl();
            double earnedPoints = 0.0;
            earnedPoints = taoResults.getTaoTotalScore(executionId, intermediate);
            ComalatAdditionalTestInformation cATI = new ComalatAdditionalTestInformation();
            cATI.setUserId(comalatUserId);
            cATI.setPointsEarned(earnedPoints);
            resultList = new ArrayList<>();
            taoExercises = taoResults.getTaoIndividualScores(executionId, intermediate);
            if (taoExercises != null) {
                log.info("TAOTAOTAO taoExercises" + taoExercises.size());
                cATI.setTaoExerciseInformation(taoExercises);
            } else {
                taoExercises = new HashMap<>();
                cATI.setTaoExerciseInformation(taoExercises);
                log.info("TAOTAOTAO taoExercises is null");
            }
            resultList.add(cATI);
        }
        return resultList;
    }

    @Override
    public ComalatAssessmentInformation getAssessmentInformation(String pubAssessmentId) {
        ComalatAssessmentInformation result;
        Search search = new Search(new Restriction("pubAssessmentId", pubAssessmentId));
        result = dao.findOneBySearch(ComalatAssessmentInformation.class, search);
        return result;
    }

    @Override
    public List<ComalatGrade> getComalatGrading(String userId) {
        List<ComalatGrade> resultList;
        Search search = new Search(new Restriction("userId", userId));
        resultList = dao.findBySearch(ComalatGrade.class, search);
        return resultList;
    }

    @Override
    public List<ComalatActivity> getComalatActivitiesByAssessmentIdentifier(String assessmentIdentifier) {
        List<ComalatActivity> resultList;
        Search search = new Search(new Restriction("assessmentIdentifier", assessmentIdentifier));
        resultList = dao.findBySearch(ComalatActivity.class, search);
        return resultList;
    }

    @Override
    public String saveComalatGrade(ComalatGrade comalatGrade) {
        dao.save(comalatGrade);
        return comalatGrade.getAssessmentName();
    }

    @Override
    public void updateComalatGrade(ComalatGrade comalatGrade) {
        dao.update(comalatGrade);
    }

    @Override
    public Object getComalatActivityOrAssessment(String activityName) {
        Object result;
        Search search = new Search(new Restriction("activityName", activityName));
        result = dao.findOneBySearch(ComalatActivity.class, search);
        if (result == null) {
            search = new Search(new Restriction("assessmentName", activityName));
            result = dao.findOneBySearch(ComalatAssessment.class, search);
        }
        return result;
    }

    @Override
    public Object getComalatActivityOrAssessmentByIdentifier(String identifier) {
        Object result;
        Search search = new Search(new Restriction("comalatIdentifier", identifier));
        result = dao.findOneBySearch(ComalatActivity.class, search);
        if (result == null) {
            search = new Search(new Restriction("comalatIdentifier", identifier));
            result = dao.findOneBySearch(ComalatAssessment.class, search);
        }
        return result;
    }

    @Override
    public ComalatActivity getComalatActivityByIdentifier(String identifier) {
        ComalatActivity result;
        Search search = new Search(new Restriction("comalatIdentifier", identifier));
        result = dao.findOneBySearch(ComalatActivity.class, search);
        return result;
    }

    @Override
    public List<ComalatActivity> getComalatActivities(String languageShort) {
        List<ComalatActivity> result;
        Search search = new Search(new Restriction("language", languageShort));
        result = dao.findBySearch(ComalatActivity.class, search);
        return result;
    }

    @Override
    public List<ComalatAssessment> getComalatAssessments(String languageShort) {
        List<ComalatAssessment> result;
        Search search = new Search(new Restriction("language", languageShort));
        result = dao.findBySearch(ComalatAssessment.class, search);
        return result;
    }

    @Override
    public List<ComalatGroup> getComalatGroups() {
        List<ComalatGroup> result;
        result = dao.findAll(ComalatGroup.class);
        return result;
    }

    @Override
    public void saveComalatGroup(ComalatGroup comalatGroup) {
        dao.save(comalatGroup);
    }

    @Override
    public List<ComalatGradeThreshold> getComalatGradeThreshold() {
        List<ComalatGradeThreshold> result;
        result = dao.findAll(ComalatGradeThreshold.class);
        return result;
    }

    @Override
    public List<ComalatMetadata> getComalatMetadata(String tag) {
        List<ComalatMetadata> result;
        Search search = new Search(new Restriction("metadataTag", tag));
        result = dao.findBySearch(ComalatMetadata.class, search);
        return result;
    }

    @Override
    public List<ComalatMetadata> getComalatMetadataForLanguage(String language) {
        List<ComalatMetadata> result;
        Search search = new Search(new Restriction("language", language));
        result = dao.findBySearch(ComalatMetadata.class, search);
        return result;
    }

    @Override
    public List<ComalatFeedback> getComalatFeedback(int gradePercentage, boolean decisionPoint) {
        List<ComalatFeedback> allFeedback = new ArrayList<>();
        try {
            allFeedback = dao.findAll(ComalatFeedback.class);
        } catch (NullPointerException | IllegalFormatException | NoSuchElementException e) {
            e.printStackTrace();
        }
        return allFeedback.stream().filter(f -> f.getLowerPercent() <= gradePercentage &&
                f.getUpperPercent() >= gradePercentage && f.isDecisionPoint() == decisionPoint).
                collect(Collectors.toList());
    }

    @Override
    public List<ComalatGrade> getComalatGradingOfTest(String comalatId) {
        List<ComalatGrade> result;
        Search search = new Search(new Restriction("comalatId", comalatId));
        result = dao.findBySearch(ComalatGrade.class, search);
        return result;
    }

    @Override
    public List<ComalatGrade> getSubitemsOfParentAssessment(String assessmentId) {
        List<ComalatGrade> result;
        Search search = new Search(new Restriction("parentAssessmentId", assessmentId));
        result = dao.findBySearch(ComalatGrade.class, search);
        return result;
    }

    @Override
    public TestDifficulty getDifficultyOfTest(String comalatId) {
        List<ComalatGrade> result = this.getComalatGradingOfTest(comalatId);
        int numOfFailures = 0;
        for (ComalatGrade grade : result) {
            if (grade.getAveragePercentage() < 5.0) numOfFailures++;
        }
        double percentageOfFailures = ((double) numOfFailures) / result.size();
        if (percentageOfFailures > 0.8)
            return TestDifficulty.VERY_DIFFICULT;
        else if (percentageOfFailures <= 0.8 && percentageOfFailures > 0.6)
            return TestDifficulty.DIFFICULT;
        else if (percentageOfFailures <= 0.6 && percentageOfFailures > 0.4)
            return TestDifficulty.NORMAL;
        else if (percentageOfFailures <= 0.4 && percentageOfFailures > 0.2)
            return TestDifficulty.EASY;
        else
            return TestDifficulty.VERY_EASY;
    }

    @Override
    public int getSubsectionProgress(String userId, ComalatGrade grade) {

        String siteLanguage = grade.getComalatId().split("-")[0];
        String lesson = grade.getComalatId().split("-")[1];
        String path = grade.getComalatId().split("-")[2];

        if (path.equals("EX") && grade.isSuccessful()) {
            return 100;
        } else if (grade.isFinalTest()) {
            //TODO calculate progress based on items previous final test
            return grade.isSuccessful() ? 100 : 0;
        }

        String competence = grade.getComalatId().split("-")[3];
        String metadata = grade.getComalatId().split("-")[5];

        List<ComalatGrade> grades = getComalatGrading(userId).stream().
                filter(g -> g.getComalatId().split("-")[0].equals(siteLanguage) &&
                        g.getComalatId().split("-")[1].equals(lesson) &&
                        g.getComalatId().split("-")[2].equals(path) &&
                        g.getComalatId().split("-")[3].equals(competence) &&
                        g.getComalatId().split("-")[5].equals(metadata) &&
                        g.isSuccessful()
                ).collect(Collectors.toList());

        List<ComalatActivity> allActivities = getComalatActivities(siteLanguage).stream().
                filter(a -> a.getLesson().equals(lesson) &&
                        a.getCompetence().equals(competence) &&
                        a.getMetadataTag().equals(metadata) &&
                        a.getPath().equals("N")
                ).collect(Collectors.toList());

        // Discard duplications
        List<String> uniqueIdentifiers =
                grades.stream().map(ComalatGrade::getComalatId).distinct().collect(Collectors.toList());

        // Only account user's accomplished activity IDs which are still present in ComalatAuthoring
        List<String> uniqueActivities = uniqueIdentifiers.stream()
                .flatMap(one -> allActivities.stream().filter(two -> one.equals(two.getComalatIdentifier()))
                        .map(two -> one)).collect(Collectors.toList());

        double numberCompletedActivities = uniqueIdentifiers.size();
        double numberAllActivities = allActivities.size();

        if (numberCompletedActivities == 0 || numberAllActivities == 0) {
            return 0;
        } else return (int) (100 * (numberCompletedActivities / numberAllActivities));
    }

    @Override
    public int getCompetenceProgress(String userId, ComalatGrade grade) {

        String siteLanguage = grade.getComalatId().split("-")[0];
        String lesson = grade.getComalatId().split("-")[1];
        String path = grade.getComalatId().split("-")[2];

        int lessonNumber = Integer.valueOf(lesson.substring(1));
        boolean beginnerLevel = lessonNumber <= 10;

        String competence = grade.getComalatId().split("-")[3];
        String metadata = grade.getComalatId().split("-")[5];

        List<ComalatGrade> grades = getComalatGrading(userId);

        if (beginnerLevel) {
            // Filter all grades with lesson 1 to 10
            grades = grades.stream().filter(g -> Integer.
                    valueOf(g.getComalatId().split("-")[1].substring(1)) <= 10).collect(Collectors.toList());
        } else {
            // Filter all grades with lesson 11 to 20
            grades = grades.stream().filter(g -> Integer.
                    valueOf(g.getComalatId().split("-")[1].substring(1)) >= 10).collect(Collectors.toList());
        }

        grades = grades.stream().filter(g -> g.getComalatId().split("-")[0].equals(siteLanguage) &&
                g.getComalatId().split("-")[2].equals(path) &&
                g.getComalatId().split("-")[3].equals(competence) &&
                g.isSuccessful())
                .collect(Collectors.toList());

        List<ComalatActivity> allActivities = getComalatActivities(siteLanguage).stream().
                filter(a -> a.getCompetence().equals(competence) && a.getPath().equals("N"))
                .collect(Collectors.toList());

        // Discard duplications
        List<String> uniqueIdentifiers =
                grades.stream().map(ComalatGrade::getComalatId).distinct().collect(Collectors.toList());

        // Only account user's accomplished activity IDs which are still present in ComalatAuthoring
        List<String> uniqueActivities = uniqueIdentifiers.stream()
                .flatMap(one -> allActivities.stream().filter(two -> one.equals(two.getComalatIdentifier()))
                        .map(two -> one)).collect(Collectors.toList());

        double numberCompletedActivities = uniqueActivities.size();
        double numberAllActivities = allActivities.size();

        if (numberCompletedActivities == 0 || numberAllActivities == 0) {
            return 0;
        } else return (int) (100 * (numberCompletedActivities / numberAllActivities));
    }

    @Override
    public int getLessonProgress(String userId, String siteLanguage, String lesson) {

        List<ComalatGrade> grades = getComalatGrading(userId).stream().
                filter(g -> g.getComalatId().split("-")[0].equals(siteLanguage) &&
                        g.getComalatId().split("-")[1].equals(lesson) &&
                        g.isSuccessful()
                ).collect(Collectors.toList());

        List<ComalatActivity> allActivities = getComalatActivities(siteLanguage).stream().
                filter(a -> a.getLesson().equals(lesson) && (a.getPath().equals("N"))).collect(Collectors.toList());

        List<ComalatAssessment> allAssessments = getComalatAssessments(siteLanguage).stream().
                filter(a -> a.getLesson().equals(lesson)).collect(Collectors.toList());

        // Discard duplications
        List<String> uniqueIdentifiers =
                grades.stream().map(ComalatGrade::getComalatId).distinct().collect(Collectors.toList());

        // Only account user's accomplished activity IDs which are still present in ComalatAuthoring
        List<String> uniqueActivities = uniqueIdentifiers.stream()
                .flatMap(one -> allActivities.stream().filter(two -> one.equals(two.getComalatIdentifier()))
                        .map(two -> one)).collect(Collectors.toList());

        // Only account user's accomplished assessment IDs which are still present in ComalatAuthoring
        List<String> uniqueAssessments = uniqueIdentifiers.stream()
                .flatMap(one -> allAssessments.stream().filter(two -> one.equals(two.getComalatIdentifier()))
                        .map(two -> one)).collect(Collectors.toList());

        double numberCompletedActivities = uniqueActivities.size() + uniqueAssessments.size();
        double numberAllActivities = allActivities.size() + allAssessments.size();

        if (numberCompletedActivities == 0 || numberAllActivities == 0) {
            return 0;
        } else return (int) (100 * (numberCompletedActivities / numberAllActivities));
    }

    @Override
    public ComalatAchievement updateLessonAchievement(ComalatUser comalatUser, String language, String lessonIdentifier,
                                                      int lessonProgress) {

        List<ComalatAchievement> achievements = new ArrayList<>(comalatUser.getAchievements());
        List<ComalatAchievement> lessonAchievements =
                achievements.stream().filter(l -> l.getType().equals("lesson") && l.getLanguage().equals(language) &&
                        l.getName().equals(lessonIdentifier)).collect(Collectors.toList());

        int segment = retrieveAchievementSegment(lessonProgress);

        if (lessonAchievements.size() == 0 && segment > 0) {
            ComalatAchievement lessonAchievement = new ComalatAchievement(lessonIdentifier, language, "lesson",
                    segment, comalatUser, Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()));
            try {
                dao.create(lessonAchievement);
                return lessonAchievement;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (lessonAchievements.size() == 1) {
            ComalatAchievement lessonAchievement = lessonAchievements.get(0);

            if (segment > lessonAchievement.getSegment()) {
                lessonAchievement.setSegment(segment);
                lessonAchievement.setLastModifiedDate(Timestamp.valueOf(LocalDateTime.now()));

                try {
                    dao.update(lessonAchievement);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return lessonAchievement;
            }
        }
        return null;
    }

    @Override
    public ComalatAchievement updateCompetenceAchievement(ComalatUser comalatUser, String language, Boolean beginner,
                                                          String competence, int competenceProgress) {

        String achievementName = beginner ? competence + "_beginner" : competence + "_intermediate";

        List<ComalatAchievement> achievements = new ArrayList<>(comalatUser.getAchievements());
        List<ComalatAchievement> competenceAchievements =
                achievements.stream().filter(c -> c.getType().equals("competence") && c.getLanguage().equals(language) &&
                        c.getName().equals(achievementName)).collect(Collectors.toList());

        int segment = retrieveAchievementSegment(competenceProgress);

        if (competenceAchievements.size() == 0 && segment > 0) {
            ComalatAchievement competenceAchievement = new ComalatAchievement(achievementName, language, "competence",
                    segment, comalatUser, Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()));

            try {
                dao.create(competenceAchievement);
                return competenceAchievement;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (competenceAchievements.size() == 1) {
            ComalatAchievement competenceAchievement = competenceAchievements.get(0);

            if (segment > competenceAchievement.getSegment()) {
                competenceAchievement.setSegment(segment);
                competenceAchievement.setLastModifiedDate(Timestamp.valueOf(LocalDateTime.now()));

                try {
                    dao.update(competenceAchievement);
                    return competenceAchievement;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public ComalatAchievement updateCourseAchievement(ComalatUser comalatUser, String language, ComalatGrade grade) {
        List<ComalatAchievement> achievements = new ArrayList<>(comalatUser.getAchievements());

        String lesson05 = "L05";
        String lesson10 = "L10";
        String lesson15 = "L15";
        String lesson20 = "L20";

        String course1 = "C01";
        String course2 = "C02";
        String course3 = "C03";
        String course4 = "C04";

        List<ComalatAchievement> courseAchievements = achievements.stream().filter(c -> c.getType().equals("course") &&
                c.getLanguage().equals(language)).collect(Collectors.toList());

        // COURSE 1
        List<ComalatAchievement> c01Achievement = courseAchievements.stream().filter(c -> c.getName().equals(course1))
                .collect(Collectors.toList());

        StringJoiner joiner = new StringJoiner("-");
        joiner.add(language).add(lesson05).add("C").add("1");
        String identifier01 = joiner.toString();

        if (c01Achievement.isEmpty() && grade.getComalatId().equals(identifier01) && grade.isSuccessful()) {
            return new ComalatAchievement(course1, language, "course", 5, comalatUser,
                    Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()));
        }

        // COURSE 2
        List<ComalatAchievement> c02Achievement = courseAchievements.stream().filter(c -> c.getName().equals(course2))
                .collect(Collectors.toList());

        joiner = new StringJoiner("-");
        joiner.add(language).add(lesson10).add("C").add("1");
        String identifier02 = joiner.toString();

        if (c02Achievement.isEmpty() && grade.getComalatId().equals(identifier02) && grade.isSuccessful()) {
            return new ComalatAchievement(course2, language, "course", 5, comalatUser,
                    Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()));
        }

        // COURSE 3
        List<ComalatAchievement> c03Achievement = courseAchievements.stream().filter(c -> c.getName().equals(course3))
                .collect(Collectors.toList());

        joiner = new StringJoiner("-");
        joiner.add(language).add(lesson15).add("C").add("1");
        String identifier03 = joiner.toString();

        if (c03Achievement.isEmpty() && grade.getComalatId().equals(identifier03) && grade.isSuccessful()) {
            return new ComalatAchievement(course3, language, "course", 5, comalatUser,
                    Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()));
        }

        // COURSE 4
        List<ComalatAchievement> c04Achievement = courseAchievements.stream().filter(c -> c.getName().equals(course4))
                .collect(Collectors.toList());

        joiner = new StringJoiner("-");
        joiner.add(language).add(lesson20).add("C").add("1");
        String identifier04 = joiner.toString();

        if (c04Achievement.isEmpty() && grade.getComalatId().equals(identifier04) && grade.isSuccessful()) {
            return new ComalatAchievement(course4, language, "course", 5, comalatUser,
                    Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()));
        }
        return null;
    }

    @Override
    public ComalatAchievement updateUserLevelAchievement(ComalatUser comalatUser, String language, ComalatGrade grade) {
        List<ComalatAchievement> achievements = new ArrayList<>(comalatUser.getAchievements());

        String lesson10 = "L10";
        String lesson20 = "L20";

        String beginner = "level_beginner";
        String intermediate = "level_intermediate";

        List<ComalatAchievement> levelAchievements = achievements.stream().filter(c -> c.getType().equals("level") &&
                c.getLanguage().equals(language)).collect(Collectors.toList());

        // BEGINNER
        List<ComalatAchievement> beginnerAchievement = levelAchievements.stream()
                .filter(c -> c.getName().equals(beginner)).collect(Collectors.toList());

        StringJoiner joiner = new StringJoiner("-");
        joiner.add(language).add(lesson10).add("C").add("1");
        String identifier10 = joiner.toString();

        if (beginnerAchievement.isEmpty() && grade.getComalatId().equals(identifier10) && grade.isSuccessful()) {
            return new ComalatAchievement(beginner, language, "course", 5, comalatUser,
                    Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()));
        }

        // INTERMEDIATE
        List<ComalatAchievement> intermediateAchievement = levelAchievements.stream()
                .filter(c -> c.getName().equals(intermediate)).collect(Collectors.toList());

        joiner = new StringJoiner("-");
        joiner.add(language).add(lesson20).add("C").add("1");
        String identifier20 = joiner.toString();

        if (intermediateAchievement.isEmpty() && grade.getComalatId().equals(identifier20) && grade.isSuccessful()) {
            return new ComalatAchievement(intermediate, language, "course", 5, comalatUser,
                    Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()));
        }
        return null;
    }

    @Override
    public List<ComalatAchievement> updateAchievements(String userId, String language, ComalatGrade grade,
                                                       String lessonIdentifier, int lessonProgress, String competence,
                                                       int competenceProgress)
            throws NumberFormatException, NullPointerException {

        ComalatUser comalatUser = getComalatUserBySakaiUserId(userId, null);
        List<ComalatAchievement> newAchievements = new ArrayList<>();

        try {
            // Lesson Achievement
            ComalatAchievement lessonAchievement =
                    updateLessonAchievement(comalatUser, language, lessonIdentifier, lessonProgress);

            // Competence Achievement
            int lessonNumber = Integer.valueOf(lessonIdentifier.substring(1));
            Boolean beginnerLevel = lessonNumber < 10;

            ComalatAchievement competenceAchievement =
                    updateCompetenceAchievement(comalatUser, language, beginnerLevel, competence, competenceProgress);

            // Course Achievement
            ComalatAchievement courseAchievement = updateCourseAchievement(comalatUser, language, grade);

            // User Level Achievement
            ComalatAchievement userLevelAchievement = updateUserLevelAchievement(comalatUser, language, grade);

            if (lessonAchievement != null) newAchievements.add(lessonAchievement);
            if (competenceAchievement != null) newAchievements.add(competenceAchievement);
            if (courseAchievement != null) newAchievements.add(courseAchievement);
            if (userLevelAchievement != null) newAchievements.add(userLevelAchievement);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return newAchievements;
    }

    @Override
    public int retrieveAchievementSegment(int progress) {
        if (progress > 0 && progress < 20) {
            return 1; // BRONZE
        } else if (progress >= 20 && progress < 40) {
            return 2; // SILVER
        } else if (progress >= 40 && progress < 60) {
            return 3; // GOLD
        } else if (progress >= 60 && progress < 80) {
            return 4; // PLATINUM
        } else if (progress >= 80) {
            return 5; // DIAMOND
        }
        return 0;
    }

    @Override
    public List<ComalatUser> getComalatUsersForAllLanguages(String sakaiUserId) {
        if (sakaiUserId == null) {
            throw new IllegalArgumentException("sakaiUserId cannot be null when getting comalatUsers for all languages");
        }

        Search search =
                new Search(new Restriction("sakaiUserId", sakaiUserId));
        List<ComalatUser> comalatUsers = dao.findBySearch(ComalatUser.class, search);

        return comalatUsers;
    }

    @Override
    public UserGradingAverageData getAssessedAvgByUserIdAndMetadata(String userId, String language, String metadata) {

        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null when getting comalat grades by user id and competence");
        }
        if (language == null) {
            throw new IllegalArgumentException("language cannot be null when getting comalat grades by user id and competence");
        }

        if (metadata == null) {
            throw new IllegalArgumentException("metadata cannot be null when getting comalat grades by user id and metadata");
        }

        UserGradingAverageData result = new UserGradingAverageData();
        result.setUserId(userId);
        result.setLanguage(language);
        result.setMetadata(metadata);

        //get all assessed activities for this user
        Restriction[] r = new Restriction[2];
        r[0] = new Restriction("nonAssessed", false);
        r[1] = new Restriction("userId", userId);


        Search search =
                new Search(r);
        List<ComalatGrade> grades = dao.findBySearch(ComalatGrade.class, search);

        //filter ther result and keep only the records which concern the requested metadata
        //and the requested language
        List<ComalatGrade> filteredGrades = new ArrayList<>();
        for (ComalatGrade c : grades) {
            String lang = c.getPartOfComalatID("LANGUAGE");
            String meta= c.getPartOfComalatID("METADATA");
            if (lang.equals(language) && meta.equals(metadata))
                filteredGrades.add(c);
        }

        //sort the resulting list
        //using the comalatId. This will make filtering that follows faster
        Collections.sort(filteredGrades, new Comparator<ComalatGrade>() {
            @Override
            public int compare(ComalatGrade lhs, ComalatGrade rhs) {
                return lhs.getComalatId().compareTo(rhs.getComalatId());
            }
        });

        //only choose the unique last activities for each activity id
        int i = 0;
        double sum = 0.0;
        int count = 0;
        while (i < filteredGrades.size()) {
            ComalatGrade uniqueActivityGrade = filteredGrades.get(i);
            boolean finished = false;
            do {
                i++;
                if (i < filteredGrades.size()) {
                    ComalatGrade next = filteredGrades.get(i);
                    if ((next.getComalatId()).equals(uniqueActivityGrade.getComalatId())) {
                        if (uniqueActivityGrade.getDate().before(next.getDate())) {
                            uniqueActivityGrade = next;
                        }
                    } else {
                        //next activity doesn't have the same id
                        finished = true;
                    }
                } else {
                    //no more activities of this competency type for this user
                    finished = true;
                }
            }
            while (!finished);
            //sum += uniqueActivityGrade.retrieveAppropriatePercentage();
            sum += uniqueActivityGrade.getPercentage();
            count++;
        }
        if (count == 0) {
            result.setCount(0);
            result.setAverage(-1.0);
        }
        else {
            double original = (sum / count) / 100.0;
            BigDecimal bigDecimal = new BigDecimal(original);
            BigDecimal roundedWithScale = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
            double scaled = roundedWithScale.doubleValue();
            //System.out.println("Original score = "+original+", Scaled score = "+scaled);
            result.setCount(count);
            result.setAverage(scaled);
        }
        return result;
    }


}
