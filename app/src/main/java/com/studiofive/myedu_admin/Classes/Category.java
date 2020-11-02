package com.studiofive.myedu_admin.Classes;

public class Category {
    private String id;
    private String name;
    private String no0fSets;
    private String setCounter;

    public Category(String id, String name, String no0fSets, String setCounter) {
        this.id = id;
        this.name = name;
        this.no0fSets = no0fSets;
        this.setCounter = setCounter;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNo0fSets() {
        return no0fSets;
    }

    public void setNo0fSets(String no0fSets) {
        this.no0fSets = no0fSets;
    }

    public String getSetCounter() {
        return setCounter;
    }

    public void setSetCounter(String setCounter) {
        this.setCounter = setCounter;
    }
}
