package org.sakaiproject.guide.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.guide.logic.ArchetypesAnalysisResult;
import org.sakaiproject.guide.logic.ComalatUtilities;
import org.sakaiproject.util.ResourceLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

/**
 * Created by george on 5/6/2017.
 */
public class ArchetypalAnalysisPage extends BasePage {
    private final static Logger log = Logger.getLogger(ArchetypalAnalysisPage.class);
    private transient ResourceLoader rl = new ResourceLoader( "org.sakaiproject.guide.tool.MyApplication" );

    public ArchetypalAnalysisPage() {
        super();
        //find the site's short language
        String shortLanguage = comalatUtilities.getShortLanguageOfCurrentSite();
        ArchetypesAnalysisResult result = stat.archetypesClassification(shortLanguage);

        String result1 = (result == null) ? "<p><i>"+rl.getString("archetypalAnalysisPage.label.info_not_avail")+"</i></p>":
                result.getArchetypesString();
        String result2 = (result == null) ? "<p><i>"+rl.getString("archetypalAnalysisPage.label.similarity_not_avail")+"</i></p>" :
                result.getArchetypesSimilarityString();
        Label l = new Label("archetypes", result1);
        l.setEscapeModelStrings(false);
        Label l1 = new Label("archetypes_similarity", result2);
        l1.setEscapeModelStrings(false);
        add(l);
        add(l1);

        //plots
        try {
            //rss elbow plot
            if (result != null && result.getRSSPlotFilename() != null) {
                Path path = Paths.get(result.getRSSPlotFilename());
                System.out.println("RSS Plot path: " + result.getRSSPlotFilename());
                byte[] data = Files.readAllBytes(path);
                Image rssPlot = new Image("rssPlot",
                        new ByteArrayResource("multipart/form-data", data));
                add(rssPlot);
                //delete RSSPlot file
                Files.delete(path);
            } else {
                add(new Image("rssPlot", "../achievements/picture_not_yet_available.png"));
            }
            if (result != null && result.getScatterPlotFilename() != null) {
                Path path1 = Paths.get(result.getScatterPlotFilename());
                System.out.println("Scatter Plot path: " + result.getScatterPlotFilename());
                byte[] data1 = Files.readAllBytes(path1);
                Image scatterPlot = new Image("scatterPlot",
                        new ByteArrayResource("multipart/form-data", data1));
                add(scatterPlot);
                //delete scatterPlot file
                Files.delete(path1);
            } else {
                add(new Image("scatterPlot", "../achievements/picture_not_yet_available.png"));
            }

            if (result != null && result.getTernaryPlotFilename() != null) {
                Path path2 = Paths.get(result.getTernaryPlotFilename());
                System.out.println("Ternary Plot path: " + result.getTernaryPlotFilename());
                byte[] data2 = Files.readAllBytes(path2);
                Image ternaryPlot = new Image("ternaryPlot",
                        new ByteArrayResource("multipart/form-data", data2));
                add(ternaryPlot);
                //delete ternaryPlot file
                Files.delete(path2);
            } else {
                add(new Image("ternaryPlot", "../achievements/picture_not_yet_available.png"));
            }

        } catch (IOException ioe) {

            System.out.println("Exception: " + ioe.getLocalizedMessage());
        }
    }

    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        if (rl==null) {
            rl = new ResourceLoader( "org.sakaiproject.guide.tool.MyApplication" );
        }
    }

}

