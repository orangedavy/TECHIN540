package com.example.test_quiz.Model;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;


public class MetaQuestion implements Serializable{
    private String description;
    private Integer type;
    private ArrayList<String> options;

    public MetaQuestion(String description, ArrayList<String> options, Integer type){
        this.type = type;
        this.description = description;
        this.options = options;
    }

    public String getDescription(){
        return this.description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public int getType(){
        return this.type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public ArrayList<String> getOpts(){
        return this.options;
    }

    public void addOpt(String option){
        this.options.add(option);
    }
}