package com.gal.media;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Sensor acc, mag;
    private SensorManager sensorManager;
    private TextView xAcc, yAcc, zAcc, xMag, yMag, zMag, xOrient, yOrient, zOrient, listSize, durationText, sampleRateText;
    private ToggleButton captureButton;
    private Button reset, settingsButton;
    private Switch armSwitch;

    private float[] acceleration = new float[3];
    private float[] magnet = null;
    private float[] gravity = new float[3];
    private float[] accForOrient = new float[3];

    private LineChart chart;

    private long samplingStartTime;

    final float alpha = 0.8f;

    final List<float[]> capturedData = new LinkedList<float[]>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xAcc = (TextView) findViewById(R.id.xAcc);
        yAcc = (TextView) findViewById(R.id.yAcc);
        zAcc = (TextView) findViewById(R.id.zAcc);

        xMag = (TextView) findViewById(R.id.xMag);
        yMag = (TextView) findViewById(R.id.yMag);
        zMag = (TextView) findViewById(R.id.zMag);

        xOrient = (TextView) findViewById(R.id.xOrient);
        yOrient = (TextView) findViewById(R.id.yOrient);
        zOrient = (TextView) findViewById(R.id.zOrient);

        captureButton = (ToggleButton) findViewById(R.id.capture);
        listSize = (TextView) findViewById(R.id.listSize);
        durationText = (TextView) findViewById(R.id.durationText);
        sampleRateText = (TextView) findViewById(R.id.sampleRateText);
        reset = (Button) findViewById(R.id.reset);
        chart = (LineChart) findViewById(R.id.chart);

        settingsButton = (Button) findViewById(R.id.settings_button);
        armSwitch = (Switch) findViewById(R.id.arm_switch);



        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);



        captureButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    reset.setClickable(false);
                    samplingStartTime = System.currentTimeMillis();

                }else {

                    listSize.setText(String.valueOf(capturedData.size()));
                    reset.setClickable(true);

                    setMisc();

                    setUpChart(chart, capturedData);


                }
            }
        });
        //Arm Switch Listener
        armSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){


            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    int rate =  sp.getInt("sensorRate", 3);
                    sensorManager.registerListener(MainActivity.this, acc, rate);
                    sensorManager.registerListener(MainActivity.this, mag, rate);
                    settingsButton.setClickable(false);

                } else {

                    sensorManager.unregisterListener(MainActivity.this);
                    settingsButton.setClickable(true);
                }


            }
        });

        reset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                capturedData.clear();
                listSize.setText(String.valueOf(capturedData.size()));
                chart.clearValues();
                durationText.setText("");
                sampleRateText.setText("");

            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                //Intent i = new Intent(MainActivity.this, Settings.class);
                startActivity(new Intent(MainActivity.this, Settings.class));

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

        armSwitch.setChecked(false);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            //Apply low-pass filter
            gravity[0] = lowPassFilter(event.values[0], gravity[0]);
            gravity[1] = lowPassFilter(event.values[1], gravity[1]);
            gravity[2] = lowPassFilter(event.values[2], gravity[2]);

            //Apply high-pass filter
            acceleration[0] = highPassFilter(event.values[0], gravity[0]);
            acceleration[1] = highPassFilter(event.values[1], gravity[1]);
            acceleration[2] = highPassFilter(event.values[2], gravity[2]);

            //Sets the values to the text view
            xAcc.setText(String.format("%.4f",acceleration[0]));
            yAcc.setText(String.format("%.4f", acceleration[1]));
            zAcc.setText(String.format("%.4f", acceleration[2]));

            if (captureButton.isChecked()) {

                float[] vector = new float[3];

                for (int i = 0; i < 3; i++) {
                    vector[i] = acceleration[i];
                }
                capturedData.add(vector);


            }

            //Acc for Orientation
           System.arraycopy(event.values, 0, accForOrient, 0, 3);

        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

            xMag.setText(String.format("%.4f", event.values[0]));
            yMag.setText(String.format("%.4f", event.values[1]));
            zMag.setText(String.format("%.4f", event.values[2]));

            magnet = new float[3];
            System.arraycopy(event.values, 0, magnet, 0, 3);

        }

        if (acceleration != null && magnet != null) {

            float rotationMatrix[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(rotationMatrix, null, accForOrient, magnet);

            if (success) {

                float[] orientationMatrix = new float[3];
                SensorManager.getOrientation(rotationMatrix, orientationMatrix);

                /*
                xOrient.setText(String.format("%.4f", orientationMatrix[1]));
                yOrient.setText(String.format("%.4f", orientationMatrix[2]));
                zOrient.setText(String.format("%.4f", orientationMatrix[0]));
                */

                xOrient.setText(String.format("%.4f", Math.toDegrees(orientationMatrix[1])));
                yOrient.setText(String.format("%.4f", Math.toDegrees(orientationMatrix[2])));
                zOrient.setText(String.format("%.4f",Math.toDegrees(orientationMatrix[0])));

            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //Gives wight to the constant forces
    private float lowPassFilter (float current, float gravity) {

        return gravity * alpha + current * (1 - alpha);

    }

    //Gives weight to the movement of the user
    private float highPassFilter (float current, float gravity) {

        return current - gravity;

    }

    private void setMisc() {

        float duration = (float) (System.currentTimeMillis() - samplingStartTime) / 1000;
        durationText.setText(String.format("%.3f", duration));
        sampleRateText.setText(String.format("%.0f", capturedData.size() / duration));

    }

    public static void setUpChart(Chart chart, List<float[]> capturedData) {
        //Creates a list of entries and fill it

        //LineDaya << LineDataSet << Entries
        ArrayList<Entry> xEntries = new ArrayList<Entry>();
        ArrayList<Entry> yEntries = new ArrayList<Entry>();
        ArrayList<Entry> zEntries = new ArrayList<Entry>();
        int index = 0;

        for (float[] array : capturedData) {

            xEntries.add(new Entry(array[0] * 100, index));
            yEntries.add(new Entry(array[1] * 100, index));
            zEntries.add(new Entry(array[2] * 100, index));
            index++;
        }

        //Creates a data set which consist of "xEntries" and a label
        LineDataSet xDataSet = new LineDataSet(xEntries, "X Values");
        LineDataSet yDataSet = new LineDataSet(yEntries, "Y Values");
        LineDataSet zDataSet = new LineDataSet(zEntries, "Z Values");

        //Plot aginst the Y axis
        xDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        yDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        zDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        //Sets up colors for the circles and the Legend
        int[] colors = {Color.BLUE, Color.GREEN, Color.RED};

        xDataSet.setCircleColor(colors[0]);
        yDataSet.setCircleColor(colors[1]);
        zDataSet.setCircleColor(colors[2]);


        //Creates a list of DataSets
        ArrayList<LineDataSet> dataSetsList = new ArrayList<LineDataSet>();
        dataSetsList.add(xDataSet);
        dataSetsList.add(yDataSet);
        dataSetsList.add(zDataSet);

        //Adds labels to to the X axis
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < index; i++) {
            xVals.add(String.valueOf(i));
        }


        LineData data = new LineData(xVals, dataSetsList);

        chart.setData(data);
        String[] labels = {"X", "Y", "Z"};
        chart.getLegend().setCustom(colors, labels);
        chart.invalidate();

    }
}
