package com.example.hector.myapplication;


public class User {


    private double paid;
    private String name;

    public User(){
        this.paid=0;
        this.name = "";
    }
    public User(String name, double paid){
        this.name= name;
        this.paid = paid;
    }


    public void setName(String name){
        this.name= name;
    }

    public void setPaid(double paid){
        this.paid = paid;
    }

    public double getPaid() {
        return paid;
    }

    public String getName() {
        return name;
    }


}
