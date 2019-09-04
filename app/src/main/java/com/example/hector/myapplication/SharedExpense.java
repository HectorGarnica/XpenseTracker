package com.example.hector.myapplication;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SharedExpense extends Expense {


    private List<User> users= new ArrayList<User>();


    public SharedExpense(int uid, Double amount, Date date, String category, String comment, List<User> users) {
        super(uid, amount, date, category, comment);
        this.users=users;
    }
    public List<User> getUsers() {
        return users;
    }
}
