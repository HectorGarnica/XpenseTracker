package com.example.hector.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelperExpense extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelperExpense";

    //public static final String DATABASE_NAME = "expenses.db";
    public static final String TABLE_NAME = "expense_table";

    public static final String COL1 ="EXID";
    public static final String COL2 ="uid";
    public static final String COL3 ="category";
    public static final String COL4 ="amount";
    public static final String COL5 ="date";
    public static final String COL6 ="comment";


    public DatabaseHelperExpense(Context context) {
        super(context, TABLE_NAME, null, 1);
        SQLiteDatabase db =this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE "+ TABLE_NAME + " (EXID INTEGER PRIMARY KEY AUTOINCREMENT, uid INTEGER , category TEXT, amount DOUBLE, date TEXT, comment TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        onCreate(db);

    }


    public boolean insertData(int uid,String category, double amount, String date, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COL2, uid);
        Log.i("hectoroni", "insert category = " + category );
        contentValues.put(COL3,category);
        contentValues.put(COL4, amount);
        contentValues.put(COL5, date);
        contentValues.put(COL6, comment);

        long result = db.insert(TABLE_NAME,null ,contentValues);

        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getData(){

        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM " + TABLE_NAME;

        Cursor data = db.rawQuery(query, null);

        return data;

    }


    public void deleteExid(int exid){

        SQLiteDatabase db = this.getWritableDatabase();

        String query = "DELETE FROM " + TABLE_NAME + " WHERE "

                + COL1 + " = '" + exid + "'" ;

        Log.d(TAG, "deleteName: query: " + query);

        db.execSQL(query);
    }




//    public Cursor getItemID(String name){//NOT POSSIBLE FOR EXPENSES
//
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        String query = "SELECT " + COL1 + " FROM " + TABLE_NAME +
//
//                " WHERE " + COL2 + " = '" + name + "'";
//
//        Cursor data = db.rawQuery(query, null);
//
//        return data;
//
//    }
//
//    public void updateCategory(int exid, String newCategory){
//
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        String query = "UPDATE " + TABLE_NAME + " SET " + COL3 +
//
//                " = '" + newCategory + "' WHERE " + COL1 + " = '" + exid + "'" ;
//
//        Log.d(TAG, "updateCategory: query: " + query);
//
//        db.execSQL(query);
//
//    }
}
