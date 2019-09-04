package com.example.hector.myapplication;

import java.util.Date;

class Expense {

    private int exid;//expense id
    private int uid;
    private Double amount;
    private Date date;
    private String category;
    private String comment;
    boolean hasHeader= true;

public Expense( int uid,Double amount, Date date, String category, String comment){

    this.uid=uid;
    this.amount = amount;
    this.date=date;
    this.category=category;
    this.comment=comment;
}

    public void setHasHeader(boolean hasHeader) { this.hasHeader = hasHeader; }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public void setExid(int exid) { this.exid = exid; }

    public double getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public String getComment() {
        return comment;
    }

    public int getExid() {  return exid; }

    public int getUid() { return uid; }

}
