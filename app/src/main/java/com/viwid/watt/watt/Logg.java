package com.viwid.watt.watt;

import android.util.Log;

/**
 * Created by YOGI on 07-08-2018.
 */
/*
Class for Showing Logs
*/
public class Logg {
    public static void debugMessage(String s)
    {
        Log.d("YOGI",""+s);
    }
    public static void debugMessage(String s,Throwable t)
    {
        Log.d("YOGI",s,t);
    }
}