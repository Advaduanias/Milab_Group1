package com.gal.media;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Gal on 17/11/2015.
 */
public class CustomList<E> extends LinkedList<E> {

    public static final CustomList<List<float[]>> sessionHolder = new CustomList<List<float[]>>();
    public static final ArrayList<Float> dotProductList = new ArrayList<Float>();

    private CustomList() {

    }

    @Override
    public boolean add(E e) {

        if (this.size() == 2) {
            this.removeFirst();
        }

       return super.add(e);

    }

    public ArrayList<Float> getDotProduct() {

        dotProductList.clear();

        Iterator<float[]> itrA, itrB;

        itrA = sessionHolder.getFirst().iterator();
        itrB = sessionHolder.getLast().iterator();

        while (itrA.hasNext() && itrB.hasNext()) {

            dotProductList.add(getAngle(itrA.next(), itrB.next()));

        }

        return dotProductList;
    }

    public float getAngle (float[] v, float [] u) {

        float normV = (float) Math.sqrt(calcDotProduct(v, v));
        float normU = (float) Math.sqrt(calcDotProduct(u, u));
        float dotProductVU = calcDotProduct(v, u);

        return (dotProductVU / (normV * normU));
    }

    public float calcDotProduct (float[] v, float[] u) {

        float sum = 0;

        if (v.length != u.length) {
            return 1;
        } else {

            for (int i = 0; i < 2; i++) {
                sum = sum + v[i] * u[i];
            }
        }

        return sum;
    }

}
