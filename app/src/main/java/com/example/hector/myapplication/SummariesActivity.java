package com.example.hector.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class SummariesActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    DatePickerDialog.OnDateSetListener fromDateSetListener;
    DatePickerDialog.OnDateSetListener toDateSetListener;
    DatabaseHelperExpense IndividualDatabaseHelperExpense;
    List<Expense> expenses= new ArrayList<Expense>();
    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
    private TextView fromDateView;
    private TextView toDateView;
    double rent = 0.0;
    double entertainment = 0.0;
    double  eatingOut= 0.0;
    double shopping = 0.0;
    double cash = 0.0;
    double education = 0.0;
    double fitness = 0.0;
    double gas = 0.0;
    double grocery = 0.0;
    double medical = 0.0;
    double mobile = 0.0;
    double tax = 0.0;
    double travel = 0.0;
    double loan = 0.0;
    double other = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summaries);
        BottomNavigationView navigation = findViewById(R.id.navigation3);
        navigation.getMenu().findItem(R.id.navigation_summaries).setChecked(true);//makes navigation icon green
        navigation.setOnNavigationItemSelectedListener(this);

        //replaces actionbar with toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Expense Summary");

        IndividualDatabaseHelperExpense = new DatabaseHelperExpense(SummariesActivity.this);





        ////////////////////////////////////////////
        ////////////     Date 1     ///////////////
        //////////////////////////////////////////
        fromDateView = (TextView)findViewById(R.id.summaries_date1_view);
        fromDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);

                int month = cal.get(Calendar.MONTH);

                int day = cal.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog dialog = new DatePickerDialog(

                        SummariesActivity.this,

                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,

                        fromDateSetListener,

                        year,month,day);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                dialog.show();
            }
        });
        //date DatePicker Listener1
        fromDateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override

            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                month = month + 1;

                String date = month + "/" + day + "/" + year;

                fromDateView.setText(date);

            }

        };

        ////////////////////////////////////////////
        ////////////     Date 2     ///////////////
        //////////////////////////////////////////
        toDateView = (TextView)findViewById(R.id.summaries_date2_view);
        toDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);

                int month = cal.get(Calendar.MONTH);

                int day = cal.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog dialog = new DatePickerDialog(

                        SummariesActivity.this,

                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,

                        toDateSetListener,

                        year,month,day);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                dialog.show();
            }
        });

        //date DatePicker Listener2
        toDateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override

            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                month = month + 1;

                String date = month + "/" + day + "/" + year;

                toDateView.setText(date);

            }

        };




        //populate and sort ExpenseList from database
        try {
            populateExpenses();
            sortMyExpenses();
        } catch (ParseException e) {
            e.printStackTrace();
        }





        ////////////////////////////////////////////
        ////////     Generate Button     //////////
        //////////////////////////////////////////
        Button getSummaryButton = (Button) findViewById(R.id.GetSummary);
        getSummaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Date startDate=null;
                Date endDate=null;
                if( toDateView.getText().toString().trim().equals("Select Date") || fromDateView.getText().toString().trim().equals("Select Date") ){// if data missing

                    Log.i("hectoroni","missing info");
                    return;
                }
                else{
                    //sets start and end dates
                    try {
                        //String startString = formatter.parse(fromDateView.getText().toString());
                        //Date startDate = new Date(String.valueOf(formatter.parse(fromDateView.getText().toString())));
                        startDate = formatter.parse(fromDateView.getText().toString());
                        endDate = (formatter.parse(toDateView.getText().toString()));
                        Log.i("summaries", "Start date is "+ startDate.getMonth());
                        Log.i("summaries", "End date is "+ endDate.getMonth());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                //start date is after end date: not correct
                if(startDate.compareTo(endDate)==1){
                    Log.i("summaries", "startdate compare is : 1" );
                    //do nothing
                }

                //are equal or (start date is before end date) :correct
                if(startDate.compareTo(endDate)<=0) {
                    Log.i("summaries", "startdate compare is : 0");
                    for(int i = 0; i < expenses.size(); i++){
                        //date is in range
                        if(expenses.get(i).getDate().compareTo(startDate) >=0 && expenses.get(i).getDate().compareTo(endDate)<=0){
                            switch (expenses.get(i).getCategory()){

                                case "Rent":
                                    rent+= expenses.get(i).getAmount();
                                    break;
                                case "Entertainment":
                                    entertainment+= expenses.get(i).getAmount();
                                    break;
                                case "Eating Out":
                                    eatingOut+= expenses.get(i).getAmount();
                                    break;
                                case "Shopping":
                                    shopping+= expenses.get(i).getAmount();
                                    break;
                                case "Cash":
                                    cash+= expenses.get(i).getAmount();
                                    break;
                                case "Education":
                                    education+= expenses.get(i).getAmount();
                                    break;
                                case "Fitness":
                                    fitness+= expenses.get(i).getAmount();
                                    break;
                                case "Gas":
                                    gas+= expenses.get(i).getAmount();
                                    break;
                                case "Grocery":
                                    grocery+= expenses.get(i).getAmount();
                                    break;
                                case "Medical":
                                    medical+= expenses.get(i).getAmount();
                                    break;
                                case "Mobile":
                                    mobile+= expenses.get(i).getAmount();
                                    break;
                                case "Tax":
                                    tax+= expenses.get(i).getAmount();
                                    break;
                                case "Travel":
                                    travel+= expenses.get(i).getAmount();
                                    break;
                                case "Loan":
                                    loan+= expenses.get(i).getAmount();
                                    break;
                                case "Other":
                                    other+= expenses.get(i).getAmount();
                                    break;
                            }
                        }


                    }

                    TextView categories = (TextView) findViewById(R.id.summariesCatView);
                    categories.setText("Category\nRent\n Entertainment\n Eating Out\n Shopping\n Cash\n Education\n Fitness\nGas\n Grocery\n Medical\n Mobile\n Tax\n Travel\n Loan\n Other");

                    TextView Totals = (TextView)findViewById(R.id.SummariesTotalView);
                    String concat = "Total\n" + rent+"\n" + entertainment+ "\n" + eatingOut+ "\n" + shopping + "\n" + cash +"\n" + education + "\n" + fitness + "\n" + gas +"\n" + grocery + "\n" + medical + "\n" + mobile +"\n" + tax + "\n" + travel + "\n" + loan + "\n" + other;
                    Totals.setText(concat);




                }


            }
        });



    }


    /////////////////////////////////////////////////////////////
    //////////////Fills Expenses from DB///////////////////////
    ///////////////////////////////////////////////////////////
    private void populateExpenses() throws ParseException {

        Cursor data = IndividualDatabaseHelperExpense.getData();//query of expense_table

        while(data.moveToNext()){

            //then add it to the Expenses
            int exid = data.getInt(0);
            int userid = data.getInt(1);
            String category = data.getString(2);
            Double amount = data.getDouble(3);
            String  date= data.getString(4);
            String comment = data.getString(5);
            Expense newExpense = new Expense(userid, amount,formatter.parse(date),category,comment);
            newExpense.setExid(exid);
            expenses.add(newExpense);

        }
    }


    /////////////////////////////////////////////////////////////
    ///////////////////Sorts Expenses //////////////////////////
    ///////////////////////////////////////////////////////////

    public void sortMyExpenses(){

        // Sort in decending order
        Collections.sort(expenses, new Comparator<Expense>() {
            public int compare(Expense e1, Expense e2) {
                return Long.valueOf(e2.getDate().getTime()).compareTo(e1.getDate().getTime());
            }
        });

    }




    /////////////////////////////////////////////////////////////
    /////////////Navigation bar listener////////////////////////
    ///////////////////////////////////////////////////////////
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {       //listener

        switch(menuItem.getItemId()){

            case R.id.navigation_expenses:
                Intent intent1 = new Intent(this, ExpensesActivity.class);
                startActivity(intent1);
                finish();
                overridePendingTransition(0,0);
                break;

            case R.id.navigation_shared_expenses:
                Intent intent2 = new Intent(this, SharedExpensesActivity.class);
                startActivity(intent2);
                finish();
                overridePendingTransition(0,0);
                break;

            case R.id.navigation_summaries:
                //Do nothing
                break;

            case R.id.navigation_settings:
                Intent intent4 = new Intent(this, SettingsActivity.class);
                startActivity(intent4);
                finish();
                overridePendingTransition(0,0);
                break;
        }

        return false;
    }



}
