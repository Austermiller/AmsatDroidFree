package uk.me.g4dpz.HamSatDroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kb9str
 *
 */

public class BootBroadcastReceiver extends BroadcastReceiver {

    // This class handles when a phone reboot is performed and sets the alarms once again since rebooting wiped them all out.


    // create a HamSatDroid object so this class can have access to the NotificationArrayList
    uk.me.g4dpz.HamSatDroid.HamSatDroid hamSatDroid = new HamSatDroid();
    // create an ArrayList to store the satellite pass alerts that are scheduled.
    ArrayList<String> notifyArrayList = hamSatDroid.getNotificationArrayList();

    @Override
    public void onReceive(Context context, Intent intent) {
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
                // set the notification array list
                hamSatDroid.setNotificationArrayList(gson.fromJson(json, type1));
                // Retrieve the notification array list
                notifyArrayList = hamSatDroid.getNotificationArrayList();
            }
            // iterate through the notifyArrayList and set any alarms with a valid request code (Request code not equal to 0).
            for(int i = 0; i < notifyArrayList.size(); i++) {
                String listItem;
                // listItem holds the alert delimited value.  Example... "[100000001234|IO-117|90|1234|180]"
                listItem = notifyArrayList.get(i);
                String[] listItemSplit;
                // split the string
                listItemSplit=listItem.split("\\|");
                String arrayListReqCode = "0";
                // get the requestCode out of listItemSplit[3]
                arrayListReqCode = listItemSplit[3];

                if (!"0".equals(arrayListReqCode)){
                    // request Code of "0" is the test data
                    long passAosTime;
                    passAosTime = Long.parseLong(listItemSplit[0]);
                    // create an alarmReceiver object.
                    AlarmReceiver alarmReceiver = new AlarmReceiver();
                    // set the alarms once again since rebooting wiped them all out.
                    alarmReceiver.setAlarm(context, passAosTime, arrayListReqCode, listItemSplit[1], listItemSplit[2], listItemSplit[4]);
                }
            }
        }
    }
}
