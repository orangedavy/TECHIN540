package com.example.test_quiz.Model;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class Question implements Serializable{
    private String question;
    private ArrayList<MetaQuestion> metaquestions;

    //constructer for setting values


    public Question(String question) {
        this.question = question;
        this.metaquestions = new ArrayList<MetaQuestion>();

    }


    public String getQuestion() {
        return question;
    }

    public int getMeta(){
        return this.metaquestions.size();
    }

    public MetaQuestion getOpt(int index) {
        return this.metaquestions.get(index);
    }

    public void addQueue(MetaQuestion question) {
        this.metaquestions.add(question);
    }

//    public String getAnswer() {
//        return answer;
//    }

//    public void setAnswer(String answer) {
//        this.answer = answer;
//    }
}