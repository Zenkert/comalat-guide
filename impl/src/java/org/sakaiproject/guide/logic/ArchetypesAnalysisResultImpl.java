package org.sakaiproject.guide.logic;

/**
 * Created by george on 5/5/2017.
 */
public class ArchetypesAnalysisResultImpl implements ArchetypesAnalysisResult {
    private String RSSPlotFilename;
    private String ScatterPlotFilename;
    private String TernaryPlotFilename;
    private String ArchetypesString;
    private String ArchetypesSimilarityString;
    private String ArchetypesCriterionString;

    public ArchetypesAnalysisResultImpl() {

    }

    public String getRSSPlotFilename() {
        return RSSPlotFilename;
    }

    public String getScatterPlotFilename() {
        return ScatterPlotFilename;
    }

    public String getTernaryPlotFilename() {
        return TernaryPlotFilename;
    }

    public String getArchetypesString() {
        return ArchetypesString;
    }

    public String getArchetypesSimilarityString() {
        return ArchetypesSimilarityString;
    }

    public String getArchetypesCriterionString() {
        return ArchetypesCriterionString;
    }

    public void setRSSPlotFilename(String RSSPlotFilename) {
        this.RSSPlotFilename = RSSPlotFilename;
    }

    public void setScatterPlotFilename(String scatterPlotFilename) {
        ScatterPlotFilename = scatterPlotFilename;
    }

    public void setTernaryPlotFilename(String ternaryPlotFilename) {
        TernaryPlotFilename = ternaryPlotFilename;
    }

    public void setArchetypesString(String archetypesString) {
        ArchetypesString = archetypesString;
    }

    public void setArchetypesSimilarityString(String archetypesSimilarityString) {
        ArchetypesSimilarityString = archetypesSimilarityString;
    }

    public void setArchetypesCriterionString(String archetypesCriterionString) {
        ArchetypesCriterionString = archetypesCriterionString;
    }
}
