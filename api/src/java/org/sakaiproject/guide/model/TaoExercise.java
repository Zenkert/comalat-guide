package org.sakaiproject.guide.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * class TaoExercise - to save the exercise information to a tao test
 *
 * @author Pascal Nowak (pascal.nowak@student.uni-siegen.de)
 */
public class TaoExercise implements Serializable {

    private static final long serialVersionUID = 1L;

    private String exerciseName;
    private Double score;
    private ArrayList<String> userChoice;
    private ArrayList<String> correctChoice;


    public TaoExercise(String exerciseName) {
        this.exerciseName = exerciseName;
        this.score = 0.0;
        this.userChoice = new ArrayList<>();
        this.correctChoice = new ArrayList<>();
    }

    /**
     * get the name
     *
     * @return the name
     */
    public String getExerciseName() {
        return exerciseName;
    }

    /**
     * get the score
     *
     * @return the score
     */
    public Double getScore() {
        return score;
    }

    /**
     * set the score
     *
     * @param score - Double
     */
    public void setScore(Double score) {
        this.score = score;
    }

    /**
     * get the user choices
     *
     * @return - list of choices
     */
    public ArrayList<String> getUserChoice() {
        return userChoice;
    }

    /**
     * add a user choice to the list
     *
     * @param choice - String
     */
    public void addUserChoice(String choice) {
        this.userChoice.add(choice);
    }

    /**
     * get the correct answers
     *
     * @return - list of answers
     */
    public ArrayList<String> getCorrectChoice() {
        return correctChoice;
    }

    /**
     * add a correct answer to the list
     *
     * @param choice - String
     */
    public void addCorrectChoice(String choice) {
        this.correctChoice.add(choice);
    }
}
