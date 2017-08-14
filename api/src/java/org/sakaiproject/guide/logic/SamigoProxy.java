package org.sakaiproject.guide.logic;

import java.util.Map;

/**
 * An interface to abstract the Samigo related API calls in a central method
 * that can be injected into our app.
 *
 * @author Sascha Klein (sascha.klein@student.uni-siegen.de)
 * @author Johannes Zenkert (johannes.zenkert@uni-siegen.de)
 */

public interface SamigoProxy {

    /**
     * get all published assessments of the current site and lesson
     * Pascal Nowak (pascal.nowak@student.uni-siegen.de)
     * @param lessonTitle - String
     * @return map of assessments
     */
    Map<String, String> getPublishedAssessmentsOfCurrentSite(String lessonTitle);

    String getRandomFinalLessonTest(String lesson, String site);

    String getUserIdividualTest(String user, String site);

    String getRandomAdditionalLessonTest(String lesson, String site);

}
