package org.sakaiproject.guide.logic;

/**
 * Created by george on 9/2/2017.
 */
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.guide.model.TestDifficulty;

import static org.junit.Assert.*;

/**
 * Created by george on 18/1/2017.
 */
public class StatisticalCompetenceAssessmentImplTest {
    private ArrayList<Double> scores;
    private ArrayList<Double> weights;

    @Before
    public void initialize() {
        scores = new ArrayList<>();
        scores.add(0.7);
        scores.add(0.8);
        scores.add(0.7);
        scores.add(1.0);
        weights = new ArrayList<>();
        weights.add(0.5);
        weights.add(0.2);
        weights.add(0.2);
        weights.add(0.1);
    }

    @Test
    public void testUndefinedDifficltyfuzzyGrade() {
        StatisticalCompetenceAssessment s = new StatisticalCompetenceAssessmentImpl();
        double fmark = s.fuzzyGrade(weights,scores, TestDifficulty.UNDEFINED);
        assertEquals(0.71746, fmark, 0.000001);
    }

    @Test
    public void testVeryDifficltfuzzyGrade() {
        StatisticalCompetenceAssessment s = new StatisticalCompetenceAssessmentImpl();
        double fmark = s.fuzzyGrade(weights,scores, TestDifficulty.VERY_DIFFICULT);
        assertEquals(0.7264132, fmark, 0.00000001);
    }

    @Test
    public void testDifficltfuzzyGrade() {
        StatisticalCompetenceAssessment s = new StatisticalCompetenceAssessmentImpl();
        double fmark = s.fuzzyGrade(weights,scores, TestDifficulty.DIFFICULT);
        assertEquals(0.7325224, fmark, 0.00000005);
    }

    @Test
    public void testNormalfuzzyGrade() {
        StatisticalCompetenceAssessment s = new StatisticalCompetenceAssessmentImpl();
        double fmark = s.fuzzyGrade(weights,scores, TestDifficulty.NORMAL);
        assertEquals(0.7388633, fmark, 0.00000005);
    }

    @Test
    public void testEasyfuzzyGrade() {
        StatisticalCompetenceAssessment s = new StatisticalCompetenceAssessmentImpl();
        double fmark = s.fuzzyGrade(weights,scores, TestDifficulty.EASY);
        assertEquals(0.7602667, fmark, 0.00000005);
    }
}

