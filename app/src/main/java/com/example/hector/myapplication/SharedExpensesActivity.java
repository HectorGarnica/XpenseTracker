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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
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
import android.widget.ListAdapter;
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

public class SharedExpensesActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener{

    DatabaseHelperSharedExpense mDatabaseHelper;
    private ListView list;//listView of expense_items
    private ListView userList;//listView of users in  add dialog
    List<SharedExpense> expenses= new ArrayList<SharedExpense>();
    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
    int uid= 0;
    List<User> dialogUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_expenses);
        BottomNavigationView navigation = findViewById(R.id.navigation2);
        navigation.getMenu().findItem(R.id.navigation_shared_expenses).setChecked(true);//makes navigation icon green
        navigation.setOnNavigationItemSelectedListener(this);

        list =(ListView) findViewById(R.id.shared_expensesListView);
        mDatabaseHelper = new DatabaseHelperSharedExpense(SharedExpensesActivity.this);

        //replaces actionbar with toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Shared Expenses");

        //try to populate list with expenses: error if date was not in correct format
        try {
            populateExpenses();
            sortMyExpenses();
            populateExpenseListView();

        } catch (ParseException e) {
        e.printStackTrace();
        }

        //listViewitem is clicked
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SharedExpense selectedExpense = expenses.get(position);
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
        Collections.sort(expenses, new Comparator<SharedExpense>() {
            public int compare(SharedExpense e1, SharedExpense e2) {
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

        Cursor expenseData = mDatabaseHelper.getDataExpenses();//query of expense_table


        while(expenseData.moveToNext()){

            //then add it to the Expenses
            int exid = expenseData.getInt(0);
            int userid = expenseData.getInt(1);
            String category = expenseData.getString(2);
            Double amount = expenseData.getDouble(3);
            String  date= expenseData.getString(4);
            String comment = expenseData.getString(5);

            Cursor userData = mDatabaseHelper.getDataUsers(exid);//table of an expenses users
            List<User> users = new ArrayList<User>();

            while(userData.moveToNext()){
                //friendid0 expenseid1, name2, paid3

                String name = userData.getString(2);
                Log.i("hectoroni","userData.name = "+name);
                Double paid = userData.getDouble(3);
                Log.i("hectoroni","userData.paid = "+paid);
                User newUser = new User(name,paid);
                users.add(newUser);

            }


            SharedExpense newExpense = new SharedExpense(userid, amount,formatter.parse(date),category,comment,users);
            newExpense.setExid(exid);
            expenses.add(newExpense);

        }
    }



    /////////////////////////////////////////////////////////////
    /////////////Expense List to ListView adapter///////////////
    ///////////////////////////////////////////////////////////


    private void populateExpenseListView() {
        ArrayAdapter<SharedExpense> adapter = new SharedExpensesActivity.MyListAdapter();
        list.setAdapter(adapter); //list is  listView
    }

    // turns expenses arrayList into displayable Views
    private class MyListAdapter extends ArrayAdapter<SharedExpense> {

        SharedPreferences settingsPreferences = getSharedPreferences("MyPrefs" , Context.MODE_PRIVATE);
        boolean prefToMonthly = settingsPreferences.getBoolean("checked", false);

        public MyListAdapter() {
            super(SharedExpensesActivity.this, R.layout.shared_expense_item, expenses);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            //make sure we have a view to work with (may have been given null
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.shared_expense_item, parent, false);
            }

            //set values of listViews elements

            SharedExpense currentExpense = expenses.get(position);

            //headerDateView
            if (currentExpense.isHasHeader()) {//gives green header if first occurrence of it's date
                TextView dateText = (TextView) itemView.findViewById(R.id.shared_expense_item_DateView);
                dateText.setBackgroundResource(R.color.colorPrimary);
                if (!prefToMonthly)
                    dateText.setText(formatter.format(currentExpense.getDate()));
                else
                    dateText.setText(toMonth(currentExpense.getDate().getMonth() + 1) + " of " + (currentExpense.getDate().getYear() + 1900));

            } else {
                TextView dateText = (TextView) itemView.findViewById(R.id.shared_expense_item_DateView);
                dateText.setVisibility(View.GONE);
            }

            //smallDateView
            TextView smallDateText = (TextView) itemView.findViewById(R.id.shared_expense_item_smallDateTextView);
            smallDateText.setText(formatter.format(currentExpense.getDate()));

            //categoryView
            TextView categoryText = (TextView) itemView.findViewById(R.id.shared_expense_item_categoryTextView);
            categoryText.setText(currentExpense.getCategory());

            //totalAmountView
            TextView totalAmountText = (TextView) itemView.findViewById(R.id.shared_expense_item_amount);
            if(currentExpense.getAmount()>1){
                DecimalFormat format = new DecimalFormat("$#");
                format.setMinimumFractionDigits(2);
                totalAmountText.setText(format.format (currentExpense.getAmount()));
            }
            else {
                DecimalFormat format = new DecimalFormat("$#0");
                format.setMinimumFractionDigits(2);
                totalAmountText.setText(format.format (currentExpense.getAmount()));
            }

            TextView usersInvolved = (TextView) itemView.findViewById(R.id.UsersInvolvedView);
            TextView usersPaid = (TextView) itemView.findViewById(R.id.UsersPaidView);
            TextView usersDebt= (TextView) itemView.findViewById(R.id.UsersDebtView);


            usersInvolved.setText("");
            usersPaid.setText("");
            usersDebt.setText("");

            //sets users name, amount paid, and debt
            for(int i =0; i < currentExpense.getUsers().size(); i++){
                usersInvolved.append(currentExpense.getUsers().get(i).getName()+ "\n");
                usersPaid.append(currentExpense.getUsers().get(i).getPaid()+"\n");
                double debt = currentExpense.getUsers().get(i).getPaid()-(currentExpense.getAmount()/(currentExpense.getUsers().size()));//actuallyPaid- suggested paid

                //make text green :overpaid
                if(debt>0){
                    Spannable word = new SpannableString("+" + debt);
                    word.setSpan(new ForegroundColorSpan(Color.GREEN),0,word.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    usersDebt.append(word);
                    usersDebt.append("\n");

                }

                //makes text red : underpaid
                else if(debt<0){
                    Spannable word = new SpannableString(String.valueOf(debt));
                    word.setSpan(new ForegroundColorSpan(Color.RED),0,word.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    usersDebt.append(word);
                    usersDebt.append("\n");
                }
                //debt is 0.00
                else{
                    usersDebt.append(debt+"");
                    usersDebt.append("\n");
                }

            }


            //suggested paid
            TextView splitEvenly = itemView.findViewById(R.id.TotalSplitEvenlyView);
            splitEvenly.setText("Total split evenly : $" + currentExpense.getAmount()/(currentExpense.getUsers().size()));

            //commentView
            TextView commentText = (TextView) itemView.findViewById(R.id.shared_expense_item_commentTextView);
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

    ////////////////////////////////////////////////////////
    ////User List to ListView adapter in add Dialog/////////
    ////////////////////////////////////////////////////////

    private void populateUserListView() {
        ArrayAdapter<User> adapter = new SharedExpensesActivity.MyUserListAdapter();
        userList.setAdapter(adapter);
    }

    // turns expenses arrayList into displayable Views
    private class MyUserListAdapter extends ArrayAdapter<User> {

        public MyUserListAdapter() {
            super(SharedExpensesActivity.this, R.layout.user_item, dialogUsers);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            //make sure we have a view to work with (may have been given null
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.user_item, parent, false);
            }

            //set values of listViews elements
            User currentUser = dialogUsers.get(position);

            EditText mname = (EditText) itemView.findViewById(R.id.UserEditText);
            mname.setText(currentUser.getName());

            EditText mUserAmount = (EditText) itemView.findViewById(R.id.shared_User_paid);
            mUserAmount.setText(String.valueOf(currentUser.getPaid()));

            return itemView;
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

    //icon listener to inflates dialog
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //plus icon clicked
        showDialog(null);//no expense passed = add new expense

        return super.onOptionsItemSelected(item);
    }



    ////////////////////////////////////////////
    ////////////     Dialog     ///////////////
    //////////////////////////////////////////
    String selectedCategory, selectedAmount, selectedDate,selectedComment;
    DatePickerDialog.OnDateSetListener mDateSetListener;
    boolean isanEdit = false;
    public boolean showDialog(final SharedExpense selectedExpense) {

        isanEdit = false;
        Log.i("hectoroni", "this isanEdit = "+ isanEdit);
        if(selectedExpense!=null)
            isanEdit=true;


        selectedCategory=null;selectedAmount=null;selectedDate=null;selectedComment=null;dialogUsers=null;//resets dialog input

        //prepares dialog
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(SharedExpensesActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_add_shared_expense,null);
        Log.i("hectoroni", "builder initiated");

        final TextView dialogHeader = (TextView) mView.findViewById(R.id.shared_AddButtonTitleView);

        final EditText mamount = (EditText)mView.findViewById(R.id.shared_AmountInputView);
        mamount.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(10,2)});//restricts user input for amount
        final EditText mcomment = (EditText)mView.findViewById(R.id.shared_commentInputView);
        final TextView mdate = (TextView)mView.findViewById(R.id.shared_DateInputView);


        // Date TextView listener
        Log.i("hectoroni", "before date listener");
        mdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);

                int month = cal.get(Calendar.MONTH);

                int day = cal.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog dialog = new DatePickerDialog(

                        SharedExpensesActivity.this,

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

                String date = month + "/" + day + "/" + year;

                mdate.setText(date);

            }

        };


        //fills spinner
        Log.i("hectoroni", "before spinner");
        Spinner spinny = mView.findViewById(R.id.shared_CategorySpinnerView);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(SharedExpensesActivity.this, R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinny.setAdapter(adapter);
        spinny.setOnItemSelectedListener(this);

        Button mSave = (Button) mView.findViewById(R.id.shared_add_expense_button);

        //delete button
        final ImageView deleteButton = (ImageView) mView.findViewById(R.id.shared_Delete_iconView);
        Log.i("hectoroni", "before handling isanEdit");

        final EditText youAmount = (EditText) mView.findViewById(R.id.YouAmount);
        youAmount.setText("0");

        View uView = getLayoutInflater().inflate(R.layout.user_item,null);
        userList = mView.findViewById(R.id.userList);


        dialogUsers = new ArrayList<User>();        //every user not including you
        //initial entry for users
        dialogUsers.add(new User() );
        populateUserListView();

        final ImageView addUserButton =(ImageView) mView.findViewById(R.id.addUserView);

        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //checks if newusers
                for(int i = 0; i < dialogUsers.size(); i ++){
                    View view = userList.getChildAt(i);

                    EditText nameEditText = view.findViewById(R.id.UserEditText);
                    String name = nameEditText.getText().toString();
                    dialogUsers.get(i).setName(name);

                    EditText paidEditText = view.findViewById(R.id.shared_User_paid);
                    String paid = paidEditText.getText().toString();
                    dialogUsers.get(i).setPaid(Double.valueOf(paid));
                }

                dialogUsers.add(new User());
                //populateUserListView();
                setListViewHeightBasedOnChildren(userList);

            }
        });



        //sets default input from selected expense
        if(isanEdit){

            //set title from "Add expense" to "Edit expense"
            Log.i("hectoroni", "entered isanEdit");
            //header
            dialogHeader.setText("Edit expense");
            deleteButton.setVisibility(View.VISIBLE);

            Log.i("hectoroni","trying to set default inputs in dialog");
            Log.i("hectoroni","set spinner selection based on Expense edited");
            //category
            selectedCategory=selectedExpense.getCategory();
            spinny.setSelection(getCategoryIndex(selectedExpense.getCategory()));

            //amount
            selectedAmount=String.valueOf(selectedExpense.getAmount());
            mamount.setText(selectedAmount);

            //date
            selectedDate=formatter.format(selectedExpense.getDate());
            mdate.setText(selectedDate);

            //youAmount
            youAmount.setText(String.valueOf(selectedExpense.getUsers().get(0).getPaid()));

            //users
            dialogUsers= new ArrayList<User>();
            for(int i =1; i < selectedExpense.getUsers().size();i++){
                dialogUsers.add(selectedExpense.getUsers().get(i));
            }
            populateUserListView();

            //comment
            selectedComment=selectedExpense.getComment();
            mcomment.setText(selectedComment);

            setListViewHeightBasedOnChildren(userList);

        }


        //opens "new expense" dialog
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();


        /////////////////   Diolog   ////////////////////////////
        /////////////Save Button listener///////////////////////////
        ///////////////////////////////////////////////////////////
        mSave.setOnClickListener(new View.OnClickListener() {
            // when save is clicked
            @Override
            public void onClick(View v) {
                Log.i("hectoroni","Saved clicked");

                //if amount is left blanik or date is not set
                if( mamount.getText().toString().trim().length() == 0 || mdate.getText().toString().trim().equals("Select Date") ){// if data missing

                    Log.i("hectoroni","missing info");
                    return;
                }
                else{
                    Log.i("hectoroni","entered else");
                    if(mamount.getText().toString().trim().equals("."))//if single decimal was given in amount
                    return;
                }
                selectedAmount = mamount.getText().toString();
                selectedDate= mdate.getText().toString();
                selectedComment = mcomment.getText().toString();



                //gets values from userListView
                for(int i = 0; i < dialogUsers.size(); i ++){
                    View view = userList.getChildAt(i);

                    EditText nameEditText = view.findViewById(R.id.UserEditText);
                    String name = nameEditText.getText().toString();
                    dialogUsers.get(i).setName(name);

                    EditText paidEditText = view.findViewById(R.id.shared_User_paid);
                    String paid = paidEditText.getText().toString();
                    dialogUsers.get(i).setPaid(Double.valueOf(paid));
                }
                trimUserList(dialogUsers);//omits blank names


                try {
                    //add yourself to dialog users before being placed in the expense
                    dialogUsers.add(0,new User("You",Double.valueOf(youAmount.getText().toString())));
                    SharedExpense newexpense =new SharedExpense(uid,Double.valueOf(selectedAmount), formatter.parse(selectedDate),selectedCategory, selectedComment,dialogUsers);
                    //deletes previous data before updating it
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
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        selectedCategory = "rent";
    }


    //used when displaying dialog when editing an expense
    //if expense.getcategory is rent: spinner position 0
    public int getCategoryIndex(String category) {
        Log.i("hectoroni","getCategoryIndex()) called") ;
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
        return 0;
    }


    /////////////////////////////////////////////////////////////
    ///////////////////Update Database//////////////////////////
    ///////////////////////////////////////////////////////////

    //add expense to expense table in database
    public void AddData(SharedExpense newEntry) {

        Log.i("hectoroni", "new entry User Name1: "+ newEntry.getUsers().get(0).getName());
        Log.i("hectoroni", "new entry User Name2: "+ newEntry.getUsers().get(1).getName());
        boolean insertData = mDatabaseHelper.insertData(uid, newEntry.getCategory(), newEntry.getAmount(), formatter.format(newEntry.getDate()), newEntry.getComment(), newEntry.getUsers());

        if (insertData) {
            Log.i("hectoroni", "success");

        } else {
            Log.i("hectoroni", "somethiings went wrong");

        }
    }

    //delete expense from expense table in database
    public void DeleteData(int exid) {

        Log.i("hectoroni", "deleting data: ");
        mDatabaseHelper.deleteExid(exid);

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
                //Do nothing
                break;

            case R.id.navigation_summaries:
                Intent intent3 = new Intent(this, SummariesActivity.class);
                startActivity(intent3);
                finish();
                overridePendingTransition(0,0);
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


    /////////////////////////////////////////////////////////////
    //////Dynamically set layout height of UsersList////////////
    ///////////////////////////////////////////////////////////
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
    //removes blank users in dialog
    public void trimUserList(List<User> users){
        if(users.size()==1)
            return;
        Log.i("trimUser", "size before trim "+ users.size());
        for(int i = users.size()-1 ; i >= 0; i--){
            if(users.size()==1)
                return;
            Log.i("trimUser", "user.getname = "+ users.get(i).getName().length());
            if(users.get(i).getName().length() == 0){
                users.remove(i);
            }
        }
        Log.i("trimUser", "size after trim "+ users.size());
    }

}
