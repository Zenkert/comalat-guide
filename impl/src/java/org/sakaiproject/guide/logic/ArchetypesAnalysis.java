package org.sakaiproject.guide.logic;

/**
 * Created by george on 5/5/2017.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.github.rcaller.rstuff.RCaller;
import com.github.rcaller.rstuff.RCode;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.sakaiproject.guide.model.UserGradingAverageData;
import org.sakaiproject.util.ResourceLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author george
 */
public class ArchetypesAnalysis {
    /*
    This is where the images will be exported by the program
     */
    private final String exportImagesPath;
    /*
    This is a UUID that will be used as prefix for the 3 images generated
    and the csv file each time archetypes analysis is performed. This guarantees
    that there will be unique filenames for all users using the application.
    */
    private final String generatedImagesPrefix;
    /*
    This is the R code to be sent to R to perfrom the analysis
    It requires that the names libraries in the beginning are installed in R
    and that a dataset is provided.
    */
    private final String benchmarkStudentsFunctionDefinition;
    /*
    This is the prefered analysis type
     */
    private final ArchetypesAnalysisCategories analysisCategory;
    /*
    This is the CSV file of input data that
    will be passed to R. This file will contain the data
    for archetypes analysis.
    */
    private String csvFilename;
    /*
    This is the student id for which we perform the archetypal analysis
     */
    private String studentID;
    private ProjectLogic projectLogic;

    private String language;

    private transient ResourceLoader rl =
            new ResourceLoader( "ComalatImplRB" );


    public ArchetypesAnalysis(String exportImagesPath,
                              ArchetypesAnalysisCategories analysisCategory,
                              String studentID,
                              ProjectLogic projectLogic,
                              String language) {

        this.studentID = studentID;

        this.analysisCategory = analysisCategory;

        generatedImagesPrefix =
                UUID.randomUUID().toString();

        this.exportImagesPath = exportImagesPath;

        csvFilename = exportImagesPath + generatedImagesPrefix + "-data.csv";

        this.projectLogic = projectLogic;

        this.language = language;


        benchmarkStudentsFunctionDefinition
                = "\"BenchmarkStudents\" <- function(\n" +
                "                                NoArch=3,\n" +
                "                                Threshold=0.80){\n" +
                "library(\"archetypes\")\n" +
                "library(\"vcd\")\n" +
                "library(ggplot2)\n" +
                "library(GGally)\n" +
                "library(ade4)\n" +
                "library(\"gridExtra\")\n" +
                "dataset=read.csv(\"" + csvFilename + "\", header=TRUE, sep=\",\")\n" +
                "### Run Archetypal Analysis from k=1 to k=5 archetypes ####\n" +
                "  AARange <- 5\n" +
                "  dat <- dataset\n" +
                "  NoCols <- ncol(dat)\n" +
                "  mat <- as.matrix(dat[,2:NoCols])\n" +
                "  rownames(mat) <- NULL\n" +
                "  \n" +
                "  set.seed(4324)\n" +
                "  \n" +
                "  as <- suppressWarnings(stepArchetypes(mat, k = 1:AARange, nrep=50))\n" +
                "  \n" +
                "  BestAA <- bestModel(as)\n" +
                "  RSSReduction <- c(100*(BestAA[[1]]$rss/BestAA[[1]]$rss),\n" +
                "                            100*(BestAA[[2]]$rss/BestAA[[1]]$rss),\n" +
                "                            100*(BestAA[[3]]$rss/BestAA[[1]]$rss),\n" +
                "                            100*(BestAA[[4]]$rss/BestAA[[1]]$rss),\n" +
                "                            100*(BestAA[[5]]$rss/BestAA[[1]]$rss))\n" +
                "  \n" +
                "  PercRSS <- vector(\"numeric\", AARange)\n" +
                "  \n" +
                "  for (i in 1:AARange){\n" +
                "    PercRSS[i] <- ((RSSReduction[i]-RSSReduction[i+1])/RSSReduction[i])\n" +
                "  }\n" +
                "  \n" +
                "  archetypes.vector <- as.integer(c(1:AARange))\n" +
                "  \n" +
                "  data.RSS <- data.frame(archetypes.vector, RSSReduction)\n" +
                "  \n" +
                "  colnames(data.RSS) <- c(\"Archetypes\",\"RSSReduction\")\n" +
                "  \n" +
                "  \n" +
                "### RSS plot ####\n" +
                " \n" +
                "  \n" +
                "  RSSplot <- ggplot(data=data.RSS, aes(x=Archetypes, y=RSSReduction)) +\n" +
                "    \n" +
                "    theme_bw()+\n" +
                "    \n" +
                "    geom_line()+\n" +
                "    geom_point(colour = \"black\",size=20)+\n" +
                "    \n" +
                "    theme(legend.position=\"none\")+\n" +
                "    theme(axis.title.x = element_text(face=\"bold\", color=\"black\", size=46),\n" +
                "          axis.title.y = element_text(face=\"bold\", color=\"black\", size=46),\n" +
                "          plot.title = element_text(face=\"bold\", color = \"black\", size=46),\n" +
                "          axis.text.x = element_text(size = 46, hjust = 0.5, vjust = 0.5, face = 'bold'),\n" +
                "          axis.text.y = element_text(size = 46, hjust = 0.5, vjust = 0.5, face = 'bold')) +\n" +
                "    labs(x=\"Archetypes\", \n" +
                "         y = \"Percentage reduction of RSS\",\n" +
                "         title = \"Elbow Plot\")\n" +
                "  \n" +
                "  #BestAAVector <- which(PercRSS<=0.20)\n" +
                "  \n" +
                "  \n" +
                "  \n" +
                "### Perform Archetypal Analysis for the Best Archetypal Solution ####\n" +
                "  BestAASolution <- NoArch\n" +
                "  \n" +
                "  a3 <- bestModel(as[[BestAASolution]])\n" +
                "  \n" +
                "  which <- apply(coef(a3, \"alphas\"), 2, which.max)\n" +
                "  \n" +
                "  players <- function(which) {\n" +
                "    players <- list()\n" +
                "    players$which <- which\n" +
                "    players$mat <- mat[which, ]\n" +
                "    players$coef <- coef(a3, \"alphas\")[which, ]\n" +
                "    players$dat <- dat[which, ]\n" +
                "    \n" +
                "    players\n" +
                "  }\n" +
                "  \n" +
                "  atypes <- players(which)\n" +
                "  \n" +
                "  AACoefficients <- data.frame(round(atypes$coef,3))\n" +
                "  \n" +
                "  NamesAA <- vector(\"numeric\",BestAASolution)\n" +
                "  \n" +
                "  for (i in 1:BestAASolution){\n" +
                "    NamesAA[i] <- paste(\"Archetype\", i, sep = \" \")\n" +
                "  }\n" +
                "  \n" +
                "  Archetypes <- data.frame(cbind(atypes$dat,AACoefficients))\n" +
                "\n" +
                "colnames(Archetypes) <- c(colnames(atypes$dat),NamesAA)\n" +
                "  \n" +
                "  FinalModels  <- NULL\n" +
                "  FinalCoeff <- NULL\n" +
                "  FinalNoRow <- NULL \n" +
                "  \n" +
                "  for (i in 1:BestAASolution){\n" +
                "    which <- which(coef(a3, \"alphas\")[, i] >=Threshold)\n" +
                "    atypes1 <- players(which)\n" +
                "    TempCoeff <- atypes1$coef\n" +
                "    if(class(TempCoeff)==\"numeric\"){\n" +
                "      TempCoeff <- t(as.matrix(TempCoeff))\n" +
                "    }\n" +
                "    \n" +
                "    OrderingIndices <- order(-TempCoeff[,i])\n" +
                "    TempCoeff <- TempCoeff[OrderingIndices,]\n" +
                "    TempModels <- atypes1$dat\n" +
                "    TempModels <-  TempModels[OrderingIndices,]\n" +
                "    FinalModels<- rbind(FinalModels,TempModels)\n" +
                "    FinalCoeff<- round(rbind(FinalCoeff,TempCoeff),3)\n" +
                "    if(class(atypes1$coef)==\"numeric\"){\n" +
                "      atypes1$coef <- t(as.matrix(atypes1$coef))\n" +
                "    }\n" +
                "    TempNoRow <- nrow(atypes1$coef)\n" +
                "    FinalNoRow <- c(FinalNoRow,TempNoRow)\n" +
                "  }\n" +
                "  \n" +
                "  NearArchetypes <- data.frame(cbind(FinalModels,FinalCoeff))\n" +
                "  colnames(NearArchetypes) <- c(colnames(atypes$dat),NamesAA)\n" +
                "\n" +
                "NearArchetypesIndices <- as.numeric(rownames(NearArchetypes))\n" +
                "\n" +
                "  \n" +
                "  RestModels <- dat[-NearArchetypesIndices,]\n" +
                "\n" +
                "\n" +
                "  AllCoeff <- round(coef(a3, 'alphas'),3)\n" +
                "\n" +
                "\n" +
                "\n" +
                "  RestCoeff <- AllCoeff[-NearArchetypesIndices,]\n" +
                "  \n" +
                "  if(class(RestCoeff)==\"numeric\"){\n" +
                "    RestCoeff <- t(as.matrix(RestCoeff))\n" +
                "  }\n" +
                "\n" +
                "  AwayArchetypes <- data.frame(cbind(RestModels,RestCoeff))\n" +
                "\n" +
                "  colnames(AwayArchetypes) <- c(colnames(atypes$dat),NamesAA)\n" +
                "\n" +
                "\n" +
                "  AllStudents <- rbind(NearArchetypes,AwayArchetypes)\n" +
                "  \n" +
                "\n" +
                "  ArchetypalStudents <- data.frame(round(parameters(a3),3))\n" +
                "  ArchetypalStudents <- data.frame(NamesAA, ArchetypalStudents)\n" +
                "  colnames(ArchetypalStudents)[1] <- \"Archetypes\"\n" +
                "   \n" +
                "\n" +
                "\n" +
                "####### Graphical Evaluation of Students' Performances ####\n" +
                "\n" +
                "  if (ncol(mat)==2){\n" +
                "  \n" +
                "  hull <- dat[chull(dat[,2],dat[,3]),]\n" +
                "  \n" +
                "  ScatterPlot <- ggplot(data=dat,aes(x=dat[,2], y=dat[,3])) +\n" +
                "    theme_bw()+\n" +
                "    \n" +
                "    geom_polygon(data = hull, aes(x=hull[,2], y=hull[,3]), colour = \"gray50\", fill=\"grey\", alpha=0.8)+\n" +
                "    geom_point(data=dat,aes(x=dat[,2], y=dat[,3]), colour = \"black\",size=10)+\n" +
                "    scale_x_continuous(limits=c(0,1))+\n" +
                "    scale_y_continuous(limits=c(0,1))+\n" +
                "    theme(legend.position=\"none\")+\n" +
                "    theme(axis.title.x = element_text(face=\"bold\", color=\"black\", size=36),\n" +
                "          axis.title.y = element_text(face=\"bold\", color=\"black\", size=36),\n" +
                "          plot.title = element_text(face=\"bold\", color = \"black\", size=36),\n" +
                "          axis.text.x = element_text(size = 36, hjust = 0.5, vjust = 0.5, face = 'bold'),\n" +
                "          axis.text.y = element_text(size = 36, hjust = 0.5, vjust = 0.5, face = 'bold')) +\n" +
                "    labs(x=colnames(dat)[2],\n" +
                "         y=colnames(dat)[3])\n" +
                "  }  else {\n" +
                "    ScatterPlot <- ggparcoord(data=dat, columns=c(2:NoCols),scale=\"globalminmax\",\n" +
                "                              mapping = aes(size = 0.5,alphaLines=0.1))+\n" +
                "      \n" +
                "      theme_bw()+\n" +
                "      \n" +
                "      #   scale_colour_manual(values = c(\"grey85\"),\n" +
                "      #                       name=\"\",\n" +
                "      #                       labels=c(\"Students\"))+\n" +
                "      #   \n" +
                "      theme(legend.position=\"none\", legend.text = element_text(size=36),\n" +
                "            axis.title.x = element_blank() ,\n" +
                "            axis.title.y = element_text(face=\"bold\", color=\"black\", size=36),\n" +
                "            plot.title = element_text(face=\"bold\", color = \"black\", size=36),\n" +
                "            axis.text = element_text(size=26,face=\"bold\", color = \"black\")) +\n" +
                "      labs(      y = \"Grade\")\n" +
                "  }\n" +
                "  \n" +
                "####### Graphical Representation of Archetypes ####\n" +
                "  if(BestAASolution==3){\n" +
                "\n" +
                "    ############ Ternary Plot in ggplot2\n" +
                "    AlphaMatrix <- coef(a3, \"alphas\")\n" +
                "    \n" +
                "    dataset <- AlphaMatrix\n" +
                "    ArchetypeVec  <- vector(\"numeric\", nrow(dataset))\n" +
                "    \n" +
                "    for(i in 1:nrow(dataset)){\n" +
                "      if (dataset[i,1]>=Threshold){\n" +
                "        ArchetypeVec[i] <- c(\"Archetype 1\")\n" +
                "      } \n" +
                "      else if (dataset[i,2]>=Threshold){\n" +
                "        ArchetypeVec[i] <- c(\"Archetype 2\")\n" +
                "      }\n" +
                "      \n" +
                "      else if (dataset[i,3]>=Threshold){\n" +
                "        ArchetypeVec[i] <- c(\"Archetype 3\")\n" +
                "      }\n" +
                "      \n" +
                "      else {\n" +
                "        ArchetypeVec[i] <- c(\"Rest\")\n" +
                "      }\n" +
                "    }\n" +
                "    datasetTernPlot <- data.frame(dataset, ArchetypeVec)\n" +
                "    colnames(datasetTernPlot) <- c(\"a1\", \"a2\", \"a3\", \"Archetype\")\n" +
                "    \n" +
                "   library(ggtern) \n" +
                "  TernaryPlot  <- ggtern(data = datasetTernPlot,\n" +
                "           aes(x = a1, y = a2, z =a3))+\n" +
                "      theme_bw()+\n" +
                "    geom_mask() +\n" +
                "\n" +
                "      geom_point(aes(shape=Archetype, fill=Archetype),size=24) +\n" +
                "      scale_shape_manual(values=c(21,22,24,8))+\n" +
                "      theme(\n" +
                "        #axis.text.y = element_blank(),\n" +
                "        legend.position=\"bottom\",\n" +
                "        legend.title=element_blank(),\n" +
                "        legend.text = element_text(size=46),\n" +
                "        tern.axis.text = element_text(face=\"bold\", color=\"black\", size=46),\n" +
                "        tern.axis.line = element_line(size=2),\n" +
                "        tern.axis.title = element_text(face=\"bold\", color=\"black\", size=46))+\n" +
                "\n" +
                "\n" +
                "    labs(x=expression(a[1]),\n" +
                "           y = expression(a[2]),\n" +
                "           z = expression(a[3]))\n" +
                "  detach(\"package:ggtern\", unload=FALSE)\n" +
                "  } else {\n" +
                "    TernaryPlot  <- simplexplot(a3,radius=30, points_cex=3,show_circle=T)\n" +
                "\n" +
                "  }\n" +
                "  \n" +
                "  PlotList  = list(RSSplot=RSSplot,\n" +
                "                   ScatterPlot=ScatterPlot,\n" +
                "                   TernaryPlot=TernaryPlot)\n" +
                "  \n" +
                "  invisible(mapply(ggsave, width=18,height=10,dpi=300,\n" +
                "                   file=paste0(\"" + this.generatedImagesPrefix + "-\", names(PlotList), \".png\"), plot=PlotList))\n" +
                "  \n" +
                "  \n" +
                "\n" +
                "### Outputs #####\n" +
                "### Outputs #####\n" +
                "  \n" +
                "  data.RSS <- as.matrix(data.RSS)\n" +
                "  ArchetypalStudents.Archetypes <-as.vector(ArchetypalStudents[,1])\n" +
                "  ArchetypalStudents.Grades <- as.matrix(ArchetypalStudents[,-1])\n" +
                "  \n" +
                "  row.names(ArchetypalStudents.Grades) <- NULL\n" +
                "\n" +
                "  NearArchetypes.Archetypes <-as.vector(NearArchetypes[,1])\n" +
                "  NearArchetypes.Grades <- as.matrix(NearArchetypes[,-1])\n" +
                "  \n" +
                "  row.names(NearArchetypes.Grades) <- NULL\n" +
                "  \n" +
                "  \n" +
                "\n" +
                "  AllStudents.Archetypes <- as.vector(AllStudents[,1])\n" +
                "  AllStudents.Grades <- as.matrix(AllStudents[,-1])\n" +
                "  \n" +
                "  row.names(AllStudents.Grades) <- NULL\n" +
                "  \n" +
                "  \n" +
                "\n" +
                "  list(\n" +
                "    ### Ouputs from Examination of Archetypal Solutions \n" +
                "    RSSplot=RSSplot,\n" +
                "    data.RSS=data.RSS,\n" +
                "    BestAASolution=BestAASolution,\n" +
                "    ### Outputs from data.frame Archetypal Students \n" +
                "    ### The data.frame was separated into a character vector named ArchetypalStudents.Archetypes\n" +
                "    ### and a matrix named ArchetypalStudents.Grades\n" +
                "    \n" +
                "    ArchetypalStudents.Archetypes=ArchetypalStudents.Archetypes,\n" +
                "    ArchetypalStudents.Grades=ArchetypalStudents.Grades,\n" +
                "    \n" +
                "    ### Outputs from data.frame NearArchetypes \n" +
                "    ### The data.frame was separated into a character vector named NearArchetypes.Archetypes\n" +
                "    ### and a matrix named NearArchetypes.Grades\n" +
                "    NearArchetypes.Archetypes=NearArchetypes.Archetypes,\n" +
                "    NearArchetypes.Grades=NearArchetypes.Grades,\n" +
                "    \n" +
                "    ### Outputs from data.frame NearArchetypes \n" +
                "    ### The data.frame was separated into a character vector named AllStudents.Archetypes\n" +
                "    ### and a matrix named AllStudents.Grades\n" +
                "    \n" +
                "    AllStudents.Archetypes=AllStudents.Archetypes,\n" +
                "    AllStudents.Grades=AllStudents.Grades\n" +
                ")\n" +
                "}\n";
    }

    private String getRSSPlotFilename() {
        return this.exportImagesPath +
                this.generatedImagesPrefix +
                "-RSSplot.png";
    }


    private String getScatterFilename() {
        return this.exportImagesPath +
                this.generatedImagesPrefix +
                "-ScatterPlot.png";
    }

    private String getTernaryPlotFilename() {
        return this.exportImagesPath +
                this.generatedImagesPrefix +
                "-TernaryPlot.png";
    }

    /**
     * This method performs the query in the database and produces the required CSV file
     * in accordance to the criterion set for analysis (e.g. Beginner-Intermediate or G-V-R-L.
     *
     * @return If the return value is false this means that fewer than 3 complete records were inserted in the CSV
     * Otherwise (more than 3 complete records) it returns true.
     */
    private boolean prepareCSVFile() {
        int noOfColumns = noOfColumns = analysisCategory.getNoOfDataColumns() + 1;
        String[] headers = new String[noOfColumns];
        //depending on the analysis type we request the data from the database
        headers[0] = "ID";
        int countCompleteRecords = 0;
        if (analysisCategory.equals(ArchetypesAnalysisCategories.G_R_V_L)) {
            headers[1] = rl.getString("archetypalAnalysisPage.image.grammar");
            headers[2] = rl.getString("archetypalAnalysisPage.image.reading");
            headers[3] = rl.getString("archetypalAnalysisPage.image.vocabulary");
            headers[4] = rl.getString("archetypalAnalysisPage.image.listening");
        } else if (analysisCategory.equals(ArchetypesAnalysisCategories.BEGINNER_INTERMEDIATE)) {
            headers[1] = rl.getString("archetypalAnalysisPage.image.beginner");
            headers[2] = rl.getString("archetypalAnalysisPage.image.intermediate");
        }

        String NEW_LINE_SEPARATOR = System.lineSeparator();
        FileWriter fileWriter = null;
        CSVPrinter csvFilePrinter = null;
        CSVFormat csvFileFormat =
                CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
        try {
            fileWriter = new FileWriter(csvFilename);
            csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
            csvFilePrinter.printRecord(headers);

            if (analysisCategory.equals(ArchetypesAnalysisCategories.G_R_V_L)) {
                //get the student ids from the database
                Set<String> userIds = projectLogic.getGradedStudentsIDs();

                for (String userId : userIds) {
                    //get the average of the student in 4 competences

                    UserGradingAverageData g = projectLogic.getAssessedAvgByUserIdAndCompetence(userId, language, "G");
                    UserGradingAverageData r = projectLogic.getAssessedAvgByUserIdAndCompetence(userId, language, "R");
                    UserGradingAverageData v = projectLogic.getAssessedAvgByUserIdAndCompetence(userId, language, "V");
                    UserGradingAverageData l = projectLogic.getAssessedAvgByUserIdAndCompetence(userId, language, "L");
                    //include in the csv only records of students who have marks in all four
                    //competences (G,V,R and L)
                    if (g.getAverage() != -1 && r.getAverage() != -1 && v.getAverage() != -1 && l.getAverage() != -1) {
                        //create a record with the student id and the grades and write it in the csv file
                        List<String> dataRecord = new ArrayList<>();
                        dataRecord.add(userId);
                        dataRecord.add("" + g.getAverage());
                        dataRecord.add("" + r.getAverage());
                        dataRecord.add("" + v.getAverage());
                        dataRecord.add("" + l.getAverage());
                        csvFilePrinter.printRecord(dataRecord);
                        countCompleteRecords++;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
                csvFilePrinter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter/csvPrinter !!!");
                e.printStackTrace();
            }
            if (countCompleteRecords<3) return false;
            return true;
        }


        /*
        Object[][] data = {
                {"S1", "S2", "S3", "S4", "S5"},
                {0.033575266638986, 0.0339397386818459, 0.00516168303940143, 0.00516168303940143, 0.0279795445770369},
                {0.0378111850667339, 0.0395174487402537, 0.00854048239893049, 0.00854048239893049, 0.0387475448488693},
                {0.0626864642376051, 0.0626922333709362, 0.0126766686593968, 0.0126766686593968, 0.0574630556736231},
                {0.0285883777610432, 0.0297107219240993, 0.0061190078361395, 0.0061190078361395, 0.0274203808016215}};

        String NEW_LINE_SEPARATOR = "\n";
        FileWriter fileWriter = null;
        CSVPrinter csvFilePrinter = null;
        CSVFormat csvFileFormat =
                CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
        try {
            fileWriter = new FileWriter(csvFilename);
            csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
            csvFilePrinter.printRecord(headers);
            for (int j = 0; j < 5; j++) {
                List<String> dataRecord = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    dataRecord.add((data[i][j]).toString());
                }
                csvFilePrinter.printRecord(dataRecord);
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
                csvFilePrinter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter/csvPrinter !!!");
                e.printStackTrace();
            }
        }
        */
    }

    public ArchetypesAnalysisResult benchmarkStudents() {
        RCaller rcaller = RCaller.create();
        boolean canArchetypalAnalysisBePerformed = this.prepareCSVFile();
        if (canArchetypalAnalysisBePerformed) {
            //form the result of this call
            ArchetypesAnalysisResult result = new ArchetypesAnalysisResultImpl();
            try {
                RCode code = RCode.create();
                code.clear();
                /**
                 * Required libraries archetypes vcd ggplot2 GGally ade4 gridExtra
                 */
                code.R_require("archetypes");
                code.R_require("vcd");
                code.R_require("ggplot2");
                code.R_require("ggplot2");
                code.R_require("ade4");
                code.R_require("gridExtra");
                code.addRCode(benchmarkStudentsFunctionDefinition);
                code.addRCode("setwd(\"" + this.exportImagesPath + "\")");
                code.addRCode("Results<-BenchmarkStudents(3,0.80)");
                rcaller.setRCode(code);
                rcaller.runAndReturnResult("Results");

                result.setRSSPlotFilename(this.getRSSPlotFilename());
                result.setScatterPlotFilename(this.getScatterFilename());
                result.setTernaryPlotFilename(this.getTernaryPlotFilename());

                int requestedColumns = this.analysisCategory.getNoOfDataColumns();
                String[] categoriesLabels = analysisCategory.getColLabels();
                //get the results from rcaller
                //get archetypal students labels
                String[] archetypalStudentsLabels =
                        rcaller.getParser().getAsStringArray("ArchetypalStudents_Archetypes");

                //form the result string for the archetypes and their values for the various categories
                StringBuffer sb = new StringBuffer();
                sb.append("<p><b>"+rl.getString("archetypalAnalysisPage.message.there_are") +" " +
                        archetypalStudentsLabels.length + " "+
                        rl.getString("archetypalAnalysisPage.message.archetypes")+".</b>");
                //get the archetypes as a double[][]
                double[][] archetypes =
                        rcaller.getParser().getAsDoubleMatrix("ArchetypalStudents_Grades", requestedColumns,
                                archetypalStudentsLabels.length);
                sb.append("<ul>");
                for (int j = 0; j < archetypalStudentsLabels.length; j++) {
                    sb.append("<li> " + archetypalStudentsLabels[j] + ":</br>");

                    for (int i = 0; i < archetypes.length; i++) {
                        sb.append("&nbsp;&nbsp;&nbsp;" + categoriesLabels[i] + " "+
                                rl.getString("archetypalAnalysisPage.message.grade_is")+
                                " " + archetypes[i][j]);
                    }
                    sb.append("</li>");
                }
                sb.append("</ul>");
                result.setArchetypesString(sb.toString());
                sb.setLength(0);

                //get all archetypes students' ids
                String[] allArchetypesIDs =
                        rcaller.getParser().getAsStringArray("AllStudents_Archetypes");
                //find the position of the student for which we perform this analysis
                int studentPosition = -1;
                for (int i = 0; i < allArchetypesIDs.length; i++) {
                    if (allArchetypesIDs[i].equals(studentID)) {
                        studentPosition = i;
                        break;
                    }
                }
                //if the student was found
                if (studentPosition != -1) {
                    double[][] allArchetypes =
                            rcaller.getParser().getAsDoubleMatrix("AllStudents_Grades", requestedColumns + 3,
                                    allArchetypesIDs.length);
                    sb.append("<p><b>"+rl.getString("archetypalAnalysisPage.message.performance")+"</b></p>");
                    //sb.append("<p><b>Your performance was the following:</b></p>");
                    sb.append("<ul>");
                    for (int i = 0; i < requestedColumns; i++) {
                        sb.append("<li>" + categoriesLabels[i] + ": " + allArchetypes[i][studentPosition] + "</li>");
                    }
                    sb.append("</ul><p><b>"+rl.getString("archetypalAnalysisPage.message.similarity")+":</b></p>");
                    //sb.append("</ul><p><b>Your similarity to the archetypes was as follows:</b></p>");
                    for (int i = 0; i < 3; i++) {
                        sb.append("<li>" + archetypalStudentsLabels[i] + ": "
                                + allArchetypes[requestedColumns + i][studentPosition] + "</li>");
                    }
                    sb.append("</ul>");
                    result.setArchetypesSimilarityString(sb.toString());
                    sb.setLength(0);
                }
                else {
                    sb.append("<p>"+rl.getString("archetypalAnalysisPage.message.please_complete")+"</p>");
                    //sb.append("<p>Please complete at least one activity from each category (G, V, R, L) to see how you compare with the archetypal students</p>");
                    result.setArchetypesSimilarityString(sb.toString());
                    sb.setLength(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //delete csv file
            File f = new File(csvFilename);
            f.delete();
            return result;
        }
        File f = new File(csvFilename);
        f.delete();
        return null;
    }
}

