package com.example.hector.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Collection;
import java.util.List;

public class DatabaseHelperSharedExpense extends SQLiteOpenHelper {

    private static final String TAG = "DbHelperSharedExpense";

    //database name
    public static final String DATABASE_NAME = "sharedExpenses.db";

    //table names
    public static final String EXPENSE_TABLE_NAME = "shared_expense_table";
    public static final String USERS_TABLE_NAME = "users_table" ;

    //common column
    public static final String COL1 ="EXID";

    //expense table
    public static final String COL2 ="uid";
    public static final String COL3 ="category";
    public static final String COL4 ="amount";
    public static final String COL5 ="date";
    public static final String COL6 ="comment";

    //users involved in expense table
    public static final String COL7 = "FRIENDID";
    public static final String COL8 ="name";
    public static final String COL9 ="paid";

    // Table Create Statements
    private static final String CREATE_EXPENSE_TABLE = "CREATE TABLE shared_expense_table(EXID INTEGER PRIMARY KEY AUTOINCREMENT, uid INTEGER , category TEXT, amount DOUBLE, date TEXT, comment TEXT)";

    private static final String CREATE_USER_TABLE = "CREATE TABLE users_table(FRIENDID INTEGER PRIMARY KEY AUTOINCREMENT, EXID INTEGER , name  TEXT , paid DOUBLE)";



    public DatabaseHelperSharedExpense(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db =this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //creating required tables
        db.execSQL(CREATE_EXPENSE_TABLE);
        db.execSQL(CREATE_USER_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        db.execSQL("DROP TABLE IF EXISTS " + EXPENSE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE_NAME);
        //Create new tables
        onCreate(db);

    }


    public boolean insertData(int uid, String category, double amount, String date, String comment, List<User> users) {
        SQLiteDatabase db = this.getWritableDatabase();

        //insert new row to expense table
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, uid);
        contentValues.put(COL3,category);
        contentValues.put(COL4, amount);
        contentValues.put(COL5, date);
        contentValues.put(COL6, comment);

        long result = db.insert(EXPENSE_TABLE_NAME,null ,contentValues);

        //gets expense id of most recently inserted row
        String selectQuery = "SELECT  * FROM " + EXPENSE_TABLE_NAME;
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToLast();
        int currentExpense = cursor.getInt(0);

        //inserts users
        for(int i = 0; i < users.size(); i++){
            Log.i("hectoroni", "column index value is ");
            ContentValues contentValuesUsers = new ContentValues();
            contentValuesUsers.put(COL1,currentExpense );
            contentValuesUsers.put(COL8, users.get(i).getName());
            contentValuesUsers.put(COL9,users.get(i).getPaid());
            long resultFriend = db.insert(USERS_TABLE_NAME,null ,contentValuesUsers);
        }

        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getDataExpenses(){

        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM " + EXPENSE_TABLE_NAME;

        Cursor data = db.rawQuery(query, null);

        return data;

    }
    public Cursor getDataUsers(int exid){

        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM " + USERS_TABLE_NAME + " WHERE " + COL1 + " = '" + exid+ "'";

        Cursor data = db.rawQuery(query, null);

        return data;

    }
    public Cursor getItemID(String name){//NOT POSSIBLE FOR EXPENSES

        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT " + COL1 + " FROM " + EXPENSE_TABLE_NAME +

                " WHERE " + COL2 + " = '" + name + "'";

        Cursor data = db.rawQuery(query, null);

        return data;

    }

    public void updateCategory(int exid, String newCategory){

        SQLiteDatabase db = this.getWritableDatabase();

        String query = "UPDATE " + EXPENSE_TABLE_NAME + " SET " + COL3 +

                " = '" + newCategory + "' WHERE " + COL1 + " = '" + exid + "'" ;

        Log.d(TAG, "updateCategory: query: " + query);

        db.execSQL(query);

    }


    public void deleteExid(int exid){

        SQLiteDatabase db = this.getWritableDatabase();

        //DELETES EXPENSE
        String query = "DELETE FROM " + EXPENSE_TABLE_NAME + " WHERE "

                + COL1 + " = '" + exid + "'" ;

        Log.d(TAG, "deleteName: query: " + query);

        db.execSQL(query);

        //DELETES ASSOCIATED USERS
        query = "DELETE FROM " + USERS_TABLE_NAME + " WHERE "

                + COL1 + " = '" + exid + "'" ;

        Log.d(TAG, "deleteName: query: " + query);

        db.execSQL(query);
    }

}
