package com.example.hector.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{
    ListView list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        BottomNavigationView navigation = findViewById(R.id.navigation4);
        navigation.getMenu().findItem(R.id.navigation_settings).setChecked(true);//makes navigation icon green
        navigation.setOnNavigationItemSelectedListener(this);


        list = (ListView)findViewById(R.id.settings_ListView);

         list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                switch (position){
                                                    //
                                                    case 0:
                                                        //sort preferences clicked
                                                        Intent intent0 = new Intent(SettingsActivity.this, SettingsSortingPreferenceActivity.class);
                                                        startActivity(intent0);
                                                        break;

                                                    case 1:
                                                        //about us clicked
                                                        Intent intent3 = new Intent(SettingsActivity.this, SettingsAboutUsActivity.class);
                                                        startActivity(intent3);
                                                        break;


                                                }


                                            }
                                        });

    }























    //Navigation bar listener
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {       //navigation bar listener

        switch(menuItem.getItemId()){

            case R.id.navigation_expenses:
                Intent intent1 = new Intent(SettingsActivity.this, ExpensesActivity.class);
                startActivity(intent1);
                finish();
                overridePendingTransition(0,0);
                break;

            case R.id.navigation_shared_expenses:
                Intent intent2 = new Intent(SettingsActivity.this, SharedExpensesActivity.class);
                startActivity(intent2);
                finish();
                overridePendingTransition(0,0);
                break;

            case R.id.navigation_summaries:
                Intent intent3 = new Intent(SettingsActivity.this, SummariesActivity.class);
                startActivity(intent3);
                finish();
                overridePendingTransition(0,0);
                break;

            case R.id.navigation_settings:
                //Do nothing
                break;
        }

        return false;
    }


}
