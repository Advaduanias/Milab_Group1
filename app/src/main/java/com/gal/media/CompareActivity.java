package com.gal.media;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import java.util.ArrayList;
import static com.gal.media.CustomList.sessionHolder;

/**
 * Created by Gal on 17/11/2015.
 */
public class CompareActivity extends AppCompatActivity {

    private LineChart chartA, chartB, chartDotProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compare_activity);

        chartA = (LineChart) findViewById(R.id.chart_a);
        chartB = (LineChart) findViewById(R.id.chart_b);
        chartDotProduct = (LineChart) findViewById(R.id.chart_dot_product);

        if (sessionHolder.peekFirst() != null){

            MainActivity.setUpChart(chartA, sessionHolder.getFirst(), "#1 Capture");

            if (sessionHolder.peekLast() != null) {

                MainActivity.setUpChart(chartB, sessionHolder.getLast(), "#2 Capture");
                new CalcDotProduct().execute();

            }

        }

    }

    public class CalcDotProduct extends AsyncTask<Void, Void, ArrayList<Float>> {

        @Override
        protected  ArrayList<Float> doInBackground(Void... params) {

            return sessionHolder.getDotProduct();
        }

        @Override
        protected void onPostExecute(ArrayList<Float> list) {

            MainActivity.setUpChartDotProduct(chartDotProduct, list, "Dot Product");
        }
    }


}
