package com.example.hector.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;



// https://material.io/tools/icons/   where icons were found

public class MainActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //switch activity to expense activity
        Intent intent = new Intent(MainActivity.this, ExpensesActivity.class);
        startActivity(intent);
        finish();

    }



}
