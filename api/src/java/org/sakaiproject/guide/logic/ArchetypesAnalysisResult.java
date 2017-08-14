package org.sakaiproject.guide.logic;

/**
 * Created by george on 5/6/2017.
 */
public interface ArchetypesAnalysisResult {
    String getRSSPlotFilename();

    String getScatterPlotFilename();

    String getTernaryPlotFilename();

    String getArchetypesString();

    String getArchetypesSimilarityString();

    String getArchetypesCriterionString();

    void setRSSPlotFilename(String RSSPlotFilename);

    void setScatterPlotFilename(String scatterPlotFilename);

    void setTernaryPlotFilename(String ternaryPlotFilename);

    void setArchetypesString(String archetypesString);

    void setArchetypesSimilarityString(String archetypesSimilarityString);

    void setArchetypesCriterionString(String archetypesCriterionString);
}
