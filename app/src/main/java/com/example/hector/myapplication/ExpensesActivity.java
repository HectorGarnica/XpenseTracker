package com.example.hector.myapplication;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ExpensesActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener {

    DatabaseHelperExpense mDatabaseHelperExpense;
    private ListView list;//listView of expense_items
    List<Expense> expenses= new ArrayList<Expense>();
    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
    int uid= 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses); //corresponding layout for this activity
        BottomNavigationView navigation = findViewById(R.id.navigation1);
        navigation.setOnNavigationItemSelectedListener(this);

        list =(ListView) findViewById(R.id.expensesListView);
        mDatabaseHelperExpense = new DatabaseHelperExpense(ExpensesActivity.this);

        //replaces actionbar with toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Expenses");

        //try to populate list with expenses: error if date was not in correct format
        try {
            populateExpenses();
            sortMyExpenses();
            populateListView();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        //listViewitem is clicked
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Expense selectedExpense = expenses.get(position);
                Log.i("hectoroni",": "+ expenses.get(position).getExid());
                showDialog(selectedExpense);
            }
        });

    }


    /////////////////////////////////////////////////////////////
    //////Sorts Expenses by Date and Manage Sort Pref///////////
    ///////////////////////////////////////////////////////////

    public void sortMyExpenses(){

        SharedPreferences settingsPreferences = getSharedPreferences("MyPrefs" , Context.MODE_PRIVATE);
        boolean prefToMonthly = settingsPreferences.getBoolean("checked", false);

        // Sort in decending order
        Collections.sort(expenses, new Comparator<Expense>() {
            public int compare(Expense e1, Expense e2) {
                return Long.valueOf(e2.getDate().getTime()).compareTo(e1.getDate().getTime());
            }
        });

        Log.i("hectoroni", "prefToMonthly = " + prefToMonthly);
        if(!prefToMonthly){//prepares for repeated dates to not have a date header

            for(int i = 1; i< expenses.size(); i++){

                if(expenses.get(i).getDate().getMonth()==expenses.get(i-1).getDate().getMonth()&&
                        expenses.get(i).getDate().getDay()==expenses.get(i-1).getDate().getDay() &&
                        expenses.get(i).getDate().getYear()==expenses.get(i-1).getDate().getYear() ){
                    expenses.get(i).setHasHeader(false);
                }
                else
                    expenses.get(i).setHasHeader(true);
            }
        }

        else {  //if preference set to month

            for(int i = 1; i< expenses.size(); i++){

                if(expenses.get(i).getDate().getMonth()==expenses.get(i-1).getDate().getMonth()&&
                        expenses.get(i).getDate().getYear()==expenses.get(i-1).getDate().getYear() ){
                    expenses.get(i).setHasHeader(false);
                }
                else
                    expenses.get(i).setHasHeader(true);
            }
        }



    }


    /////////////////////////////////////////////////////////////
    /////////reads database and fills expenses List/////////////
    ///////////////////////////////////////////////////////////

    private void populateExpenses() throws ParseException {

        Cursor data = mDatabaseHelperExpense.getData();//query of expense_table

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
    /////////////Expense List to ListView adapter///////////////
    ///////////////////////////////////////////////////////////

    private void populateListView() {
        ArrayAdapter<Expense> adapter = new MyListAdapter();
        list.setAdapter(adapter);
    }

    // turns expenses arrayList into displayable Views
    private class MyListAdapter extends ArrayAdapter<Expense> {

        SharedPreferences settingsPreferences = getSharedPreferences("MyPrefs" , Context.MODE_PRIVATE);
        boolean prefToMonthly = settingsPreferences.getBoolean("checked", false);

        public MyListAdapter() {
            super(ExpensesActivity.this, R.layout.expense_item, expenses);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            //make sure we have a view to work with (may have been given null
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.expense_item, parent, false);
            }

            //set values of listViews elements

            Expense currentExpense = expenses.get(position);

            if (currentExpense.isHasHeader()) {//gives green header if first occurrence of it's date
                TextView dateText = (TextView) itemView.findViewById(R.id.expense_item_DateView);
                dateText.setBackgroundResource(R.color.colorPrimary);
                if (!prefToMonthly)
                    dateText.setText(formatter.format(currentExpense.getDate()));
                else
                    dateText.setText(toMonth(currentExpense.getDate().getMonth() + 1) + " of " + (currentExpense.getDate().getYear() + 1900));

            } else {
                TextView dateText = (TextView) itemView.findViewById(R.id.expense_item_DateView);
                dateText.setVisibility(View.GONE);
            }

            TextView smallDateText = (TextView) itemView.findViewById(R.id.expense_item_smallDateTextView);
            smallDateText.setText(formatter.format(currentExpense.getDate()));

            TextView categoryText = (TextView) itemView.findViewById(R.id.expense_item_categoryTextView);
            categoryText.setText(currentExpense.getCategory());

            TextView amountText = (TextView) itemView.findViewById(R.id.expense_item_amount);
            if(currentExpense.getAmount()>1){
                DecimalFormat format = new DecimalFormat("$#");
                format.setMinimumFractionDigits(2);
                amountText.setText(format.format (currentExpense.getAmount()));
            }
            else {
                DecimalFormat format = new DecimalFormat("$#0");
                format.setMinimumFractionDigits(2);
                amountText.setText(format.format (currentExpense.getAmount()));
            }

            TextView commentText = (TextView) itemView.findViewById(R.id.expense_item_commentTextView);
            commentText.setText(currentExpense.getComment());

            return itemView;
        }

        private String toMonth(int i) {
            switch (i) {
                case 1:
                    return "Jan";
                case 2:
                    return "Feb";
                case 3:
                    return "Mar";
                case 4:
                    return "Apr";
                case 5:
                    return "May";
                case 6:
                    return "Jun";
                case 7:
                    return "Jul";
                case 8:
                    return "Aug";
                case 9:
                    return "Sep";
                case 10:
                    return "Oct";
                case 11:
                    return "Nov";
                case 12:
                    return "Dec";
            }
            return "error";
        }
    }



    /////////////////////////////////////////////////////////////
    /////////////Add Expense: icon and dialog///////////////////
    ///////////////////////////////////////////////////////////

    //Adds the '+' icon on the tool bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_expense,menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i("hectoroni", "'+' selected");
        showDialog(null);//no expense passed = add new expense
        Log.i("hectoroni", "dialog showed");
        return super.onOptionsItemSelected(item);
    }

    ////////////////////////////////////////////
    ////////////     Dialog     ///////////////
    //////////////////////////////////////////
    String selectedCategory, selectedAmount, selectedDate,selectedComment;
    DatePickerDialog.OnDateSetListener mDateSetListener;
    boolean isanEdit = false;

    public boolean showDialog(final Expense selectedExpense) {

        isanEdit = false;
        Log.i("hectoroni", "this isanEdit = "+ isanEdit);
        if(selectedExpense!=null)
            isanEdit=true;

        Log.i("hectoroni", "this isanEdit = "+ isanEdit);

        selectedCategory=null;selectedAmount=null;selectedDate=null;selectedComment=null;//resets dialog input

        //icon was clicked: prepares dialog
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(ExpensesActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_add_expense,null);
        Log.i("hectoroni", "builder initiated");
        final EditText mamount = (EditText)mView.findViewById(R.id.AmountInputView);
        mamount.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(10,2)});//restricts user input for amount

        final EditText mcomment = (EditText)mView.findViewById(R.id.commentInputView);

        final TextView mdate = (TextView)mView.findViewById(R.id.DateInputView);


        // Date TextView listener
        mdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);

                int month = cal.get(Calendar.MONTH);

                int day = cal.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog dialog = new DatePickerDialog(

                        ExpensesActivity.this,

                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,

                        mDateSetListener,

                        year,month,day);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                dialog.show();
            }
        });


        //date DatePicker Listener
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override

            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                month = month + 1;

                Log.d("ExpensesActivity", "onDateSet: mm/dd/yyy: " + month + "/" + day + "/" + year);

                String date = month + "/" + day + "/" + year;

                mdate.setText(date);

            }

        };

        Log.i("hectoroni", "before spinner");
        //fills spinner and set layout
        Spinner spinny = mView.findViewById(R.id.CategorySpinnerView);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ExpensesActivity.this, R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinny.setAdapter(adapter);
        spinny.setOnItemSelectedListener(this);

        Button mSave = (Button) mView.findViewById(R.id.add_expense_button);

        final TextView dialogHeader = (TextView) mView.findViewById(R.id.AddButtonTitleView);
        final ImageView deleteButton = (ImageView) mView.findViewById(R.id.Delete_iconView);
        Log.i("hectoroni", "before handling isanEdit");

        //sets default input from selected expense
        if(isanEdit){

            //set title from "Add expense" to "Edit expense"
            Log.i("hectoroni", "entered isanEdit");

            dialogHeader.setText("Edit expense");
            deleteButton.setVisibility(View.VISIBLE);

            Log.i("hectoroni","trying to set default inputs in dialog");
            Log.i("hectoroni","set spinner selection based on Expense edited");
            selectedCategory=selectedExpense.getCategory();
            spinny.setSelection(getCategoryIndex(selectedExpense.getCategory()));

            selectedAmount=String.valueOf(selectedExpense.getAmount());
            mamount.setText(selectedAmount);

            selectedDate=formatter.format(selectedExpense.getDate());
            mdate.setText(selectedDate);

            selectedComment=selectedExpense.getComment();
            mcomment.setText(selectedComment);

        }


        //opens "new expense" dialog
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();


        /////////////////   Diolog   ////////////////////////////
        /////////////Save Button listener///////////////////////
        ////////////////////////////////////////////////////////
        mSave.setOnClickListener(new View.OnClickListener() {
            // when save is clicked
            @Override
            public void onClick(View v) {
                Log.i("hectoroni","Saved clicked");

                if( mamount.getText().toString().trim().length() == 0 || mdate.getText().toString().trim().equals("Select Date") ){// if data missing

                    Log.i("hectoroni","missing info");
                    return;
                }
                else
                    Log.i("hectoroni","entered else");
                if(mamount.getText().toString().trim().equals("."))//if single decimal was given in amount
                    return;
                selectedAmount = mamount.getText().toString();
                selectedDate= mdate.getText().toString();
                selectedComment = mcomment.getText().toString();

                try {

                    Expense newexpense =new Expense(uid,Double.valueOf(selectedAmount), formatter.parse(selectedDate),selectedCategory, selectedComment);
                    if(isanEdit){
                        DeleteData(selectedExpense.getExid());
                    }
                    AddData(newexpense);//adds to database
                    //expenses.add(newexpense);//adds to expenses
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                dialog.dismiss();
                finish();
                startActivity(getIntent());
            }
        });


        /////////////////   Diolog   ////////////////////////////
        /////////////Delete Button listener/////////////////////
        ///////////////////////////////////////////////////////
        deleteButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Log.i("hectoroni", "delete was clicked: ");

                    DeleteData(selectedExpense.getExid());
                    dialog.dismiss();
                    finish();
                    startActivity(getIntent());

            }
        });

        return true;
    }

    /////////////////////////////////////////////////////////////
    /////////////Category SpinnerView Listener/////////////////
    ///////////////////////////////////////////////////////////
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedCategory = parent.getItemAtPosition(position).toString();
        Log.i("hectoroni", "item selected: " + selectedCategory);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        selectedCategory = "rent";
    }

    //used when displaying dialog when editing an expense
    public int getCategoryIndex(String category) {

        switch (category){

            case "Rent":
                return 0;
            case "Entertainment":
                return 1;
            case "Eating Out":
                return 2;
            case "Shopping":
                return 3;
            case "Cash":
                return 4;
            case "Education":
                return 5;
            case "Fitness":
                return 6;
            case "Gas":
                return 7;
            case "Grocery":
                return 8;
            case "Medical":
                return 9;
            case "Mobile":
                return 10;
            case "Tax":
                return 11;
            case "Travel":
                return 12;
            case "Loan":
                return 13;
            case "Other":
                return 14;
        }
        return -1;
    }


    /////////////////////////////////////////////////////////////
    ///////////////////Update Database//////////////////////////
    ///////////////////////////////////////////////////////////

    //add expense to expense table in database
    public void AddData(Expense newEntry) {

        boolean insertData = mDatabaseHelperExpense.insertData(uid, newEntry.getCategory(), newEntry.getAmount(), formatter.format(newEntry.getDate()), newEntry.getComment());

    }

    //delete expense from expense table in database
    public void DeleteData(int exid) {

        mDatabaseHelperExpense.deleteExid(exid);

    }


    /////////////////////////////////////////////////////////////
    /////////////Navigation bar listener////////////////////////
    ///////////////////////////////////////////////////////////
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch(menuItem.getItemId()){

            case R.id.navigation_expenses:
                //Do nothing
                break;

            case R.id.navigation_shared_expenses:
                Intent intent2 = new Intent(ExpensesActivity.this, SharedExpensesActivity.class);
                startActivity(intent2);
                finish();
                overridePendingTransition(0,0);
                break;

            case R.id.navigation_summaries:
                Intent intent3 = new Intent(ExpensesActivity.this, SummariesActivity.class);
                startActivity(intent3);
                finish();
                overridePendingTransition(0,0);
                break;

            case R.id.navigation_settings:
                Intent intent4 = new Intent(ExpensesActivity.this, SettingsActivity.class);
                startActivity(intent4);
                finish();
                overridePendingTransition(0,0);
                break;
        }

        return false;
    }

}





