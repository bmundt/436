package com.example.a436;

import android.app.Application;

/**
 * Created by brmun on 2/2/2017.
 */

public class MyApp extends Application {
    private int[] leftHandResults;
    private int[] rightHandResults;

    // we only need one of these because we will be doing all left and then all right
    private int testNumber;

    public MyApp() {
        super();
        leftHandResults = new int[5];
        rightHandResults = new int[5];
        testNumber = 0;
    }

    public void newLeftHandTest(int result) {
        // for the transition from right to left
        if (testNumber >= rightHandResults.length - 1) {
            testNumber = 0;
        } else {
            leftHandResults[testNumber] = result;
            testNumber++;
        }
    }

    public void newRightHandTest(int result) {
        // for the transition from left to right
        if (testNumber >= rightHandResults.length - 1) {
            testNumber = 0;
        } else {
            rightHandResults[testNumber] = result;
            testNumber++;
        }
    }

    public double getAvgRight() {
        return (double) (sum(rightHandResults) / rightHandResults.length);
    }

    public double getAvgLeft() {
        return (double) (sum(leftHandResults) / leftHandResults.length);
    }

    private double sum(int[] arr) {
        double sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        return sum;
    }

    public int getRealTestNum(){
        return testNumber + 1;
    }

}
