package com.example.hector.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class SettingsSortingPreferenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_sorting_preference);

        final CheckBox c = (CheckBox) findViewById(R.id.prefCheckBoxView);
        //looks into file "MyPrefs"
        SharedPreferences settings = getSharedPreferences("MyPrefs" , Context.MODE_PRIVATE);
        //can edit values in "MyPrefs"
        final SharedPreferences.Editor editor  = settings.edit();

        //checks if the key "checked" is equal to true
        if ( settings.contains("checked") && settings.getBoolean("checked", false)==true)
            c.setChecked(true);//displays a checked box
        else
            c.setChecked(false);//displays an unchecked box

        //when checkbox is pressed/changed; update the "checked" boolean
        c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(c.isChecked()){
                    editor.putBoolean("checked", true);
                    editor.apply();
                }
                else{
                    editor.putBoolean("checked",false);
                    editor.apply();
                }
            }
        });



    }
}
