package org.sakaiproject.guide.logic;

import org.sakaiproject.guide.model.TestDifficulty;

import java.util.ArrayList;

/**
 * Created by george on 11/8/2016.
 */
public interface StatisticalCompetenceAssessment {
    //minimum no of grades required for fuzzy grading application
    int NO_PR_GRADES = 100;

    double fuzzyGrade(ArrayList<Double> weightsNormalized, ArrayList<Double> percentList, TestDifficulty difficulty);

    ArchetypesAnalysisResult archetypesClassification(String shortLanguage);

}


