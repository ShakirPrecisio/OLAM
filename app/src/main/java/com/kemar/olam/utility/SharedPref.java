package com.kemar.olam.utility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Base64;

import com.kemar.olam.login.activity.LoginActivity;


public class SharedPref {

    public static SharedPreferences mSharedPref;
    public static final String MyPREFERENCES = "olam app";
    private static EncrypterDecrypter mEncryptionDecryption;

    public SharedPref() {

    }

    public static void init(Context context) {
        if (mSharedPref == null)
            mSharedPref = context.getSharedPreferences(MyPREFERENCES, Activity.MODE_PRIVATE);
          mEncryptionDecryption = new EncrypterDecrypter(context);
    }

    public static String read(String key, String defValue) {
        String finalValue  = "";
        String  value = mSharedPref.getString(key, defValue);
        try {
            finalValue = mEncryptionDecryption.decrypt(Constants.secreteKey, Constants.initialVector, value);
        }catch (Exception e){
            e.printStackTrace();
        }
        return finalValue;
    }

    public static int readInt(String key) {
        String finalValue  = "";
        String  value = mSharedPref.getString(key, "");
        try {
            finalValue = mEncryptionDecryption.decrypt(Constants.secreteKey, Constants.initialVector, value);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(finalValue.isEmpty()){
            finalValue="0";
        }
        int finalVal  =  Integer.valueOf(finalValue);
        return finalVal;
    }

    public static void write(String key, String value) {
        String finalValue  = "";
        try {
            finalValue = mEncryptionDecryption.encrypt(Constants.secreteKey, Constants.initialVector, value);
        }catch (Exception e){
            e.printStackTrace();
        }
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putString(key, finalValue);
        prefsEditor.commit();
    }

   /* public static void saveInt( String key,Integer value) {
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putInt(key, value);
        prefsEditor.commit();
    }*/



    public static int getUserId(String key) {
        String finalValue  = "";
        String  value = mSharedPref.getString(key, "");
        try {
            finalValue = mEncryptionDecryption.decrypt(Constants.secreteKey, Constants.initialVector, value);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(finalValue.isEmpty()){
            finalValue="0";
        }
        int finalVal  = Integer.parseInt(finalValue);
        return finalVal;
    }


    public static boolean read(String key, boolean defValue) {
        return mSharedPref.getBoolean(key, defValue);
    }

    public static String read(String key) {
        String finalValue  = "";
        String  value = mSharedPref.getString(key, "");
        try {
            finalValue = mEncryptionDecryption.decrypt(Constants.secreteKey, Constants.initialVector, value);
        }catch (Exception e){
            e.printStackTrace();
            }
        return finalValue;

    }

   /* public static Integer readNotificationCount(String key) {
        return mSharedPref.getInt(key, 0);
    }*/

    public static void write(String key, boolean value) {
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putBoolean(key, value);
        prefsEditor.commit();
    }


  /*  public static Integer read(String key, int defValue) {
        return mSharedPref.getInt(key, defValue);
    }*/

    /*public static void write(String key, Integer value) {
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putInt(key, value).commit();

    }*/





    public static void  logoutWithLoginRedirection(Context mContext){
        mSharedPref.edit().clear().commit();
        Intent i = new Intent(mContext, LoginActivity.class);
        // Closing all the Activities
        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Staring Login Activity
        mContext.startActivity(i);
    }

    public static void clearAllData() {
        mSharedPref.edit().clear().commit();
    }

}
