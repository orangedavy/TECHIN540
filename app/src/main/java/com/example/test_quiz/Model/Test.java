package com.example.test_quiz.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class Test implements Serializable{
    String Name;
    ArrayList<Question> Questions;
    Long time;

    boolean is_question;

    Integer set_color;
    Integer ground_truth_heartrate;

    public Integer rank;
    public Test() {
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public void setHeartrate(Integer heartrate){ this.ground_truth_heartrate = heartrate;}

    public void setColor(Integer color){ this.set_color = color;}

    public Integer getColor(){ return set_color;}

    public Integer getHeartrate(){ return ground_truth_heartrate;}

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public boolean isQuestion() {return is_question;}

    public void setIsQuestion(boolean is_question){this.is_question = is_question;}

    public ArrayList<Question> getQuestions() {
        return Questions;
    }

    public void setQuestions(ArrayList<Question> questions) {
        Questions = questions;
    }
}