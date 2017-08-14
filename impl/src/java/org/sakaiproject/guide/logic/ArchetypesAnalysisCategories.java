package org.sakaiproject.guide.logic;

import org.sakaiproject.util.ResourceLoader;

/**
 * Created by george on 5/5/2017.
 */
public enum ArchetypesAnalysisCategories {

    BEGINNER_INTERMEDIATE ("Beginner-Intermediate", 2),
    G_R_V_L ("GRVL", 4);

    private final int noOfDataColumns;
    private final String[] colLabels;
    private final String analysisCategory;
    private transient ResourceLoader rl;

    ArchetypesAnalysisCategories(String analysisCategory, int noOfDataColumns) {
        this.noOfDataColumns = noOfDataColumns;
        this.analysisCategory = analysisCategory;
        rl = new ResourceLoader( "ComalatImplRB" );
        if (analysisCategory.equals("Beginner-Intermediate")) {
            colLabels = new String[2];
            colLabels[0] = rl.getString("archetypalAnalysisPage.image.beginner");
            colLabels[1] = rl.getString("archetypalAnalysisPage.image.intermediate");
        }
        else if (analysisCategory.equals("GRVL")) {
            colLabels = new String[4];

            colLabels[0] = rl.getString("archetypalAnalysisPage.image.grammar");
            colLabels[1] = rl.getString("archetypalAnalysisPage.image.reading");
            colLabels[2] = rl.getString("archetypalAnalysisPage.image.vocabulary");
            colLabels[3] = rl.getString("archetypalAnalysisPage.image.listening");
        }
        else {
            colLabels=new String[1];
            colLabels[0] = rl.getString("archetypalAnalysisPage.message.categories");
        }
    }

    public int getNoOfDataColumns() {
        return noOfDataColumns;
    }

    public String getAnalysisCategory() {
        return analysisCategory;
    }

    public String[] getColLabels() {
        return colLabels;
    }
}

