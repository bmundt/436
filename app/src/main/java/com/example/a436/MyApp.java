package com.example.a436;

import android.app.Application;
import android.util.Log;

/**
 * Created by brmun on 2/2/2017.
 */

public class MyApp extends Application {
    private int[] leftHandResults;
    private int[] rightHandResults;


    public static String TAPS = "TAPS";
    public static String TAPS_L_AVG = "TAPS_LEFT_AVG";
    public static String TAPS_R_AVG = "TAPS_RIGHT_AVG";
    public static String SPIRAL_L = "SPIRAL_LEFT";
    public static String SPIRAL_R = "SPIRAL_RIGHT";
    public static String LEVEL_L = "LEVEL_LEFT";
    public static String LEVEL_R = "LEVEL_RIGHT";
    public static String REACTION_L = "REACTION_LEFT";
    public static String REACTION_R = "REACTION_RIGHT";
    public static String CURL_L = "CURL_LEFT";
    public static String CURL_R = "CURL_RIGHT";
    public static String SWAY = "SWAY";

    public static String PREF_NAME = "PATIENT";
    public static String PID_STR = "patientID";

    // we only need one of these because we will be doing all left and then all right
    private int testNumber;

    private boolean testMode = false;

    public MyApp() {
        super();
        leftHandResults = new int[3];
        rightHandResults = new int[3];
        testNumber = 0;
    }

    public int getNumTrials() {
        return leftHandResults.length;
    }

    public int trialResults(String hand, int num) {
        if (hand == "left") {
            return leftHandResults[num];
        } else {
            return rightHandResults[num];
        }
    }

    public void newLeftHandTest(int result) {
        // for the transition from right to left
        if (testNumber >= rightHandResults.length - 1) {
            leftHandResults[testNumber] = result;
            testNumber = 0;
        } else {
            leftHandResults[testNumber] = result;
            testNumber++;
        }
    }

    public void newRightHandTest(int result) {
        // for the transition from left to right
        if (testNumber >= rightHandResults.length - 1) {
            rightHandResults[testNumber] = result;
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

    public void setTestMode(boolean state) {
        testMode = state;
    }



    public boolean getTestMode() { return testMode; }



}
