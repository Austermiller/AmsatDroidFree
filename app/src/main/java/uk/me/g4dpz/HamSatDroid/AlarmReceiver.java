package uk.me.g4dpz.HamSatDroid;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author kb9str
 *
 */

public class AlarmReceiver extends BroadcastReceiver {

    // create a HamSatDroid object so this class can have access to the NotificationArrayList
    uk.me.g4dpz.HamSatDroid.HamSatDroid hamSatDroid = new HamSatDroid();
    // create an ArrayList to store the satellite pass alerts that are scheduled.
    ArrayList<String> notifyArrayList = hamSatDroid.getNotificationArrayList();

    @Override
    public void onReceive(Context context, Intent intent) {
        // The name of the satellite used for this pass alarm
        String sname = intent.getStringExtra("SATNAME");
        // AOS date and time used for this pass alarm.
        String aosDT = intent.getStringExtra("AOSDATETIME");
        // The Request Code used for this satellite pass alarm.
        int reqCode = intent.getIntExtra("RCODE", 0);
        // find this notification intent's requestCode from the arrayList
        if (notifyArrayList != null){
            // if the user closed the app before the onReceive event then we need to retrieve the SharedPref Alarm arrayList values
            if (notifyArrayList.size() == 0){
                // Read the sharedPref value for populating the alarm array
                final SharedPreferences readsharedPrefs1 = PreferenceManager.getDefaultSharedPreferences(context);
                // Create a Gson object which is used to serialize Java objects to JSON (and deserialize them back to Java).
                Gson gson = new Gson();
                // get the "ALARM" sharedpref value
                String json = readsharedPrefs1.getString("ALARM", "[100000001|IO-117|90|0|180]");
                Type type1 = new TypeToken<List<String>>() {}.getType();
                // set the notification array list data
                hamSatDroid.setNotificationArrayList(gson.fromJson(json, type1));
                // Retrieve the notification array list
                notifyArrayList = hamSatDroid.getNotificationArrayList();
            }
            // iterate through the notifyArrayList and remove any alarms with a matching request code.
            for(int i = 0; i < notifyArrayList.size(); i++) {
                String listItem;
                // listItem holds the alert delimited value.  Example... "[100000001234|IO-117|90|1234|180]"
                listItem = notifyArrayList.get(i);
                // split the string
                String[] listItemSplit;
                listItemSplit=listItem.split("\\|");
                int arrayListReqCode = 0;
                // get the requestCode out of listItemSplit[3]
                arrayListReqCode = tryParseInt(listItemSplit[3]);
                // If the request codes match, then remove this alert from the notifyArrayList.
                if (arrayListReqCode == reqCode){
                    // remove this alarm entry from the array list
                    notifyArrayList.remove(i);
                    // save the updated alarm arrayList (with this alarm removed) to the application's sharedPrefs
                    updateAlarmArrayList(context);
                    // break out of the for loop since a matching requestCode was found and it's array element removed
                    break;
                }
            }
        }
        // create a new HamSatDroid object
        HamSatDroid hsd = new HamSatDroid();
        // fire off the notification now
        hsd.createNotification(context, sname, aosDT);
    }

    public static Integer tryParseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 123;
        }
    }

    public void setAlarm(Context context, Date alarmDT, String aosAz, String losAz)
    {
        // this overload is used when the user initially sets the alarm/notification by tapping on the checkbox.
        setAlarm(context, alarmDT, 0, "0", aosAz, losAz, "");
    }

    public void setAlarm(Context context, long DateMilliSeconds, String reqCode, String satName, String aosAz, String losAz)
    {
        // this overload is used when the user reboots their phone and the BootBroadcastReceiver's @Override "onReceive" is called.
        setAlarm(context, null, DateMilliSeconds, reqCode, aosAz, losAz, satName);
    }
    
    public void setAlarm(Context context, Date alarmDT, long DateMilliSeconds, String reqCode, String aosAz, String losAz, String satName){
        // used to define the unique pending intent requestCode
        int requestCodeInt = 0;
        String requestCodeString = "0";
        // storing the alert to the notifyArrayList is not needed if the setAlarm is called because the device was rebooted.
        // storeToArrayList keeps track if this alarm be store in the notifyArrayList OR is it already in the notifyArrayList
        boolean storeToArrayList = false;
        Calendar cal = Calendar.getInstance();
        // Determine which overload called this setAlarm function.
        if (alarmDT != null){
            storeToArrayList = true;
            cal = toCalendar(alarmDT);
            // define a new unique pending intent requestCode
            requestCodeInt = (int)(System.currentTimeMillis()/1000);
            requestCodeString = Integer.toString(requestCodeInt);
        } else {
            // reuse the pending intent requestCode from the notificationArrayList
            requestCodeInt = Integer.valueOf(reqCode);
            requestCodeString = reqCode;
            // The alert is already in the notifyArrayList.
            storeToArrayList = false;
            // get the sat name used for this alarm.
            HamSatDroid.selectedSatName = satName;
            // set cal object to the date/time of this alarm's AOS
            cal.setTimeInMillis(DateMilliSeconds);
            // convert cal date/time to Date variable
            alarmDT = cal.getTime();
        }
        String PassAOSinMillSec = "";
        long DTinMillis = cal.getTimeInMillis();
        // The date/time of this alarm's AOS
        PassAOSinMillSec = Long.toString(DTinMillis);
        // subtract 16 minutes from the cal time.
        // Alerts will show approximately 15 minutes prior to the satellite's AOS time.
        cal.add(Calendar.MINUTE, -16);

        // Display AOS date and time in Local date/time OR in UTC date/time.
        TimeZone timeZoneUTC = TimeZone.getTimeZone("UTC");
        TimeZone timeZoneDefault = TimeZone.getDefault();
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
        SimpleDateFormat TIME_FORMAT;
        boolean UseUTC = AppConfig.isUtcDateTime();
        // display the local or UTC date/time depending on the user config setting.
        if (UseUTC == false){
            TIME_FORMAT = new SimpleDateFormat("HH:mm 'Local'", Locale.ENGLISH);
            TIME_FORMAT.setTimeZone(timeZoneDefault);
            DATE_FORMAT.setTimeZone(timeZoneDefault);
            //TZone = " loc";
        } else {
            TIME_FORMAT = new SimpleDateFormat("HH:mm 'UTC'", Locale.ENGLISH);
            TIME_FORMAT.setTimeZone(timeZoneUTC);
            DATE_FORMAT.setTimeZone(timeZoneUTC);
            //TZone = " UTC";
        }

        // The dateTimeString is used to display the date and time of the AOS within the alert notification window that pops up
        String dateTimeString = DATE_FORMAT.format(alarmDT.getTime()) + " " + TIME_FORMAT.format(alarmDT.getTime());

        // Create a new intent for the alert that will hold the Satellites alert message details in the putExtra's.
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("MY_NOTIFICATION");
        intent.putExtra("SATNAME", HamSatDroid.selectedSatName);
        intent.putExtra("AOSDATETIME",dateTimeString);
        intent.putExtra("RCODE", requestCodeInt);

        // The PendingIntent below is what the onReceive will use to create the alert notification.
        PendingIntent sender = PendingIntent.getBroadcast(context.getApplicationContext(), requestCodeInt, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Get the AlarmManager service
        AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        // set the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(), sender);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
        }

        // build alarm array string entry for this alarm
        String alarmSPstring = "";

        //  notifyArrayList entry will be in this format...   AOS time in Miiliseconds | satname | AOS Azimuth | RequestCode | LOS Azimuth
        alarmSPstring = PassAOSinMillSec + "|" + HamSatDroid.selectedSatName + "|" + aosAz + "|" + requestCodeString + "|" + losAz;

        // store this alarm to the notifyArrayList if doesn't already exist in the array.
        if (storeToArrayList == true){

            // add this alert to the notifiyArrayList
            notifyArrayList.add(alarmSPstring);

            // final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            // Create a Gson object which is used to serialize Java objects to JSON (and deserialize them back to Java).
            Gson gson = new Gson();
            String json = gson.toJson(notifyArrayList);
            // store the "ALARM" sharedpref value
            editor.putString("ALARM", json);  //json
            editor.commit();
        }
    }


    public void updateAlarmArrayList(Context context){
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        // Create a Gson object which is used to serialize Java objects to JSON (and deserialize them back to Java).
        Gson gson = new Gson();
        String json = gson.toJson(notifyArrayList);
        // store the "ALARM" sharedpref value
        editor.putString("ALARM", json);
        editor.commit();
    }

    public void cancelPendingAlarm(Context context, int requestCode){
        // Get the AlarmManager service
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        // create an intent object
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("MY_NOTIFICATION");
        // create a Pending Intent using the same requestCode as the original Alarm's PendingIntent so that we can cancel the correct pending alarm.
        PendingIntent sender = PendingIntent.getBroadcast(context.getApplicationContext(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        // cancel the alarm
        alarmManager.cancel(sender);
    }

    public static Calendar toCalendar(Date date){
        // converts a Date object to a Calendar object.
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }
}

