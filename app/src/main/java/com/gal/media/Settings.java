package com.gal.media;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by Gal on 16/11/2015.
 */
public class Settings extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    Spinner sensorRateSpinner;
    EditText tresholdText;
    Button apply;

    private int selectedSensorRate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        sensorRateSpinner = (Spinner) findViewById(R.id.sensor_refresh_rate_spinner);
        tresholdText = (EditText) findViewById(R.id.treshold);
        apply = (Button) findViewById(R.id.apply);

        setUpSppiner();

        //Sets up the listeners
        sensorRateSpinner.setOnItemSelectedListener(this);
        apply.setOnClickListener(this);

        loadPrefs();

    }

    /**
     * Provides the ability to retrieve data from the SharedPref
     */

    private void loadPrefs(){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
      //  contentServerAddress.setText(sp.getString("CONTENT_SERVER_ADDRESS",getString(R.string.defaultContentServer)));

        sensorRateSpinner.setSelection(sp.getInt("sensorRate", 3));
        tresholdText.setText(String.valueOf(sp.getFloat("treshold", 0)));

    }

    /**
     * Provides the ability to store String values in the
     * shared preferences
     * @param key
     * The key for accessing the data
     * @param value
     * The actual String data
     */

    private void savePrefs (String key, String value) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        //In order to edit the data in the SharedPref we need to call the editor
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(key, value);
        edit.commit();
    }

    private void savePrefs (String key, int value) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        //In order to edit the data in the SharedPref we need to call the editor
        SharedPreferences.Editor edit = sp.edit();
        edit.putInt(key, value);
        edit.commit();
    }

    private void savePrefs (String key, float value) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        //In order to edit the data in the SharedPref we need to call the editor
        SharedPreferences.Editor edit = sp.edit();
        edit.putFloat(key, value);
        edit.commit();
    }


    @Override
    public void onBackPressed() {

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.apply) {

            savePrefs("sensorRate", selectedSensorRate);
            savePrefs("treshold", Float.valueOf(tresholdText.getText().toString()));

            finish();
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        selectedSensorRate = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void setUpSppiner() {

        Integer[] rates = {SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_DELAY_GAME,
                SensorManager.SENSOR_DELAY_UI, SensorManager.SENSOR_DELAY_NORMAL};

        ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, rates);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        sensorRateSpinner.setAdapter(arrayAdapter);

    }
}
