package com.example.gio.autostop;


public class Constants {
    public static final int SUCCESS_RESULT = 0;

    public static final int FAILURE_RESULT = 1;

    public static final String PACKAGE_NAME =
            "com.example.gio.autostop";

    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";

    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";

    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
    public static final String chosenMode = "chosenMode";

    public static final int INDEX_KM = 0;
    public static final int HOUR_MULTIPLIER = 3600;
    public static final double UNIT_MULTIPLIERS[] = { 0.001, 0.000621371192 };
    public static final int INDEX_MILES = 1;
    public static final double Speed_Margin=30;
}
