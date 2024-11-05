package uk.me.g4dpz.HamSatDroid;

import static androidx.appcompat.R.style.Base_Theme_AppCompat_Dialog;
import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import uk.me.g4dpz.satellite.SatPassTime;

/**
 * @author kb9str
 *
 */

public class RecyclerViewPassAdapter extends RecyclerView.Adapter<RecyclerViewPassAdapter.ViewHolder> {

    // create a list of SatPassTime's
    private List<SatPassTime> passes;

    // channelPermissionDialogOkay is used to store if the user tapped "settings" or "cancel" in the showPermissionRationaleDialog window.
    private boolean channelPermissionDialogOkay = false;


    // create a HamSatDroid object so this class can have access to the NotificationArrayList
    uk.me.g4dpz.HamSatDroid.HamSatDroid hamSatDroid = new HamSatDroid();


    // create an ArrayList to store the satellite pass alerts that are scheduled.
    ArrayList<String> notifyArrayList = hamSatDroid.getNotificationArrayList();

    public RecyclerViewPassAdapter(List<SatPassTime> satPassTimeList) {
        this.passes = satPassTimeList;
    }


    // Create new views
    @Override
    public RecyclerViewPassAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_row, null);
        // create ViewHolder used to keep track of which list item is selected or checked etc.
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    Integer getAzimuthDifference(Integer a1, Integer a2) {
        // returns the degrees difference between two Azimuth values
        return Math.min((a1-a2)<0?a1-a2+360:a1-a2, (a2-a1)<0?a2-a1+360:a2-a1);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        // ViewHolder used for the RecyclerViewPassAdapter to keep track of which list item is selected or checked etc.
        // This is the position of the item in the RecyclerView list ( staring with position [0] )
        final int pos = position;
        Date passAOS = passes.get(position).getStartTime();
        Integer currentRowAosAzimuth = passes.get(position).getAosAzimuth();
        Integer currentRowLosAzimuth = passes.get(position).getLosAzimuth();
        Calendar cal = AlarmReceiver.toCalendar(passAOS);
        long DTinMillis = cal.getTimeInMillis();

        // now iterate through all of the set alarms in the arrayList
        // and place a checkmark on the passes that have a matching date/time and matching satellite AOS/LOS.
        //
        // The satellite name can't be used to determine a match because different KEP sources will use
        // different satellite names for the same satellite.  Example: The keps from "amsat.org" calls a
        // satellite, "IO-117", and celestrak.com keps calls it, "Greencube".  So to find a match, we use a
        // combination of the date/time and matching satellite AOS/LOS.
        if (notifyArrayList != null){
            for(int i = 0; i < notifyArrayList.size(); i++) {
                String alarmListItem;
                alarmListItem = notifyArrayList.get(i);
                String[] listItemSplit;
                listItemSplit=alarmListItem.split("\\|");
                // get the AOS time (from the notifyArrayList)
                Long fullAOStimeMilli0 = Long.valueOf(listItemSplit[0]);

                // Do the AOS Azimuths match
                Boolean aosMatch = false;
                Integer aosAz = Integer.valueOf(listItemSplit[2]);
                Integer aosDegDifference = getAzimuthDifference(aosAz, currentRowAosAzimuth);
                if (aosDegDifference < 6){
                    aosMatch = true;
                }

                // Do the LOS Azimuths match
                Boolean losMatch = false;
                Integer losAz = Integer.valueOf(listItemSplit[4]);
                Integer losDegDifference = getAzimuthDifference(losAz, currentRowLosAzimuth);
                if (losDegDifference < 6){
                    losMatch = true;
                }

                // Do the AOS times match
                Boolean aosTimeMatch = false;
                aosTimeMatch = doDateTimeMatch(fullAOStimeMilli0,DTinMillis);

                //  In order to apply a check to the checkbox for an alert, the following must be true...
                //  the AOS times must match -AND- the AOS Azimuths must match -AND- the LOS Azimuths must match.
                if (aosTimeMatch == true && aosMatch == true && losMatch == true){
                    // check this checkbox
                    passes.get(position).setSelected(true);
                    // break out of the for loop since a matching AOS was found and it's checkbox was selected
                    break;
                }
            }
        }

        // set the text and checked status for the viewHolder
        viewHolder.satPassDetails.setText(passes.get(position).getDetails());
        viewHolder.chkAlertSelected.setChecked(passes.get(position).isSelected());
        //  set the viewHolder tag and then later retrieve that viewHolder / RecyclerView view based on that tag
        viewHolder.chkAlertSelected.setTag(passes.get(position));

        // set up the click listener for the checkboxes in the satellite pass RecyclerView
        viewHolder.chkAlertSelected.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                SatPassTime spt = (SatPassTime) cb.getTag();
                spt.setSelected(cb.isChecked());
                // update the SatPassTime "checked" status for this pass item to what it is set to in the viewHolder
                passes.get(pos).setSelected(cb.isChecked());
                // is the checkbox that the user clicked on checked or UnChecked?
                if (cb.isChecked()){
                    // The user "checked" a notification checkbox.
                    // Look first to see if the user gave permissions for alarm/notifications.
                    // Show user the permission rationale and then ask the user for permission.
                    Boolean permStatus = checkPermission(v.getContext());
                    if (permStatus.equals(true)){
                        // user gave permission.
                    } else {
                        // user didn't given permission.
                        passes.get(pos).setSelected(false);
                        // update the DataSet so the UI will be updated for the user
                        RecyclerViewPassAdapter.this.notifyDataSetChanged();
                        return;
                    }
                    // get the "checked" pass item instance from the passes list
                    SatPassTime pass = passes.get(pos);
                    // get the AOS date/time for this "checked" pass
                    Date passDT = pass.getStartTime();
                    // create alarmReceiver object
                    AlarmReceiver alarmReceiver = new AlarmReceiver();
                    // get the AOZ and LOS azimuths as strings
                    String aosAz = String.valueOf(pass.getAosAzimuth());
                    String losAz = String.valueOf(pass.getLosAzimuth());
                    // set alarm for notification
                    alarmReceiver.setAlarm(cb.getContext(), passDT, aosAz, losAz);
                    // display a toast pop-up message for the user
                    Toast.makeText(v.getContext(), "Alert Set 15 mins\nprior to AOS", Toast.LENGTH_SHORT).show();
                } else {
                    // The user unchecked a notification checkbox

                    // get the pass item for the given position in the passes list
                    SatPassTime pass = passes.get(pos);
                    // get the pass AOS date/time
                    Date passDT = pass.getStartTime();
                    // convert the passDT DATE object to a Calendar object.
                    Calendar calRemove = AlarmReceiver.toCalendar(passDT);
                    long DTmillRemove = calRemove.getTimeInMillis();
                    // requestCode placeholder for the pending alarm's intent.
                    Integer reqCode = 0;
                    // now iterate through all of the set alarms in the arrayList to find the alert in the array list
                    if ( notifyArrayList != null){
                        for(int i = 0; i < notifyArrayList.size(); i++) {
                            String alarmListItem;
                            // get the current list item in the array
                            alarmListItem = notifyArrayList.get(i);
                            String[] listItemSplit;
                            // split the list item so we can use the data
                            listItemSplit=alarmListItem.split("\\|");
                            // get the AOS time (from the notifyArrayList)
                            Long fullAOStimeMilli0 = Long.valueOf(listItemSplit[0]);
                            // Do the AOS times match
                            Boolean aosTimeMatch = false;
                            aosTimeMatch = doDateTimeMatch(fullAOStimeMilli0,DTmillRemove);
                            // Do the AOS Azimuths match
                            Boolean aosMatch = false;
                            Integer aosAz = Integer.valueOf(listItemSplit[2]);
                            Integer aosDegDifference = getAzimuthDifference(aosAz, currentRowAosAzimuth);
                            // AOS Aizmuth matching doesn't have to be "exact", but needs to be a very close match.
                            // An exact match comparison would not work since the AOS Azimuths can very slightly
                            // each time the user clicks the "Calculate Pass" button.
                            if (aosDegDifference < 6){
                                aosMatch = true;
                            }
                            // Do the LOS Azimuths match
                            Boolean losMatch = false;
                            Integer losAz = Integer.valueOf(listItemSplit[4]);
                            Integer losDegDifference = getAzimuthDifference(losAz, currentRowLosAzimuth);
                            // LOS Aizmuth matching doesn't have to be "exact", but needs to be a very close match.
                            // An exact match comparison would not work since the LOS Azimuths can very slightly
                            // each time the user clicks the "Calculate Pass" button.
                            if (losDegDifference < 6){
                                losMatch = true;
                            }
                            //  In order to be able to remove an alert from the notifyArrayList, the following must be true...
                            //  the AOS times must match -AND- the AOS Azimuths must match -AND- the LOS Azimuths must match.
                            if (aosTimeMatch == true && aosMatch == true && losMatch == true){
                                reqCode = Integer.valueOf(listItemSplit[3]);
                                // remove this alarm entry from the array list
                                notifyArrayList.remove(i);
                                // save the updated alarm arrayList (with this alarm removed) to the sharedPrefs
                                AlarmReceiver alarmReceiver = new AlarmReceiver();
                                alarmReceiver.updateAlarmArrayList(cb.getContext());
                                // cancel pending intent alarm using the requestCode from the array list
                                alarmReceiver.cancelPendingAlarm(cb.getContext(), reqCode);
                                // break out of the for loop since a matching AOS was found and removed
                                break;
                            }
                        }
                    }
                    // give the user a toast message, confirming the notification for this sat pass has been cancelled.
                    Toast.makeText(v.getContext(), "Alert\ncancelled", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // set up the click listener for the pass items in the satellite pass RecyclerView
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected pass and start SkyView.
                HamSatDroid.setSelectedPass((SatPassTime) passes.get(pos));
                final Intent launchSkyViewIntent = new Intent().setClass(v.getContext(), SkyView.class);
                v.getContext().startActivity(launchSkyViewIntent);
            }
        });
    }

    private boolean doDateTimeMatch(Long milliseconds1, Long milliseconds2){
        // Do the two time values match each other? (within the tolerance of 240000 milliseconds... aka 4 minutes)
        long resultMilliseconds = milliseconds1 - milliseconds2;
        long absDiffMilli = Math.abs(resultMilliseconds);
        // time difference is greater than 4 minutes apart Returns false.
        // time difference is less than (or equal to) 4 minutes apart Returns true.
        return absDiffMilli <= 240000;
    }

    private void showPermissionRationaleDialog(Context context, String titleText, String messageText){
        // Shows the user a dialog popup message and asks if they want to go to the permissions settings screen.
        Activity activity = (Activity) context;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, Base_Theme_AppCompat_Dialog);
        builder.setMessage(messageText);
        builder.setTitle(titleText);
        // DialogInterface.OnClickListener dialogClickListener = null;
        builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // user clicked the settings button
                channelPermissionDialogOkay = true;
                checkPermission(context);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //do what you want when cancel is clicked
                channelPermissionDialogOkay = false;
            }
        });
        builder.create();
        builder.show();
    }

    private boolean checkPermission(Context context) {

        try {
            // ======================================
            // check if our app can set exact alarms
            // ======================================
            // Android 12L (API 31 aka "S") requires the “Alarms & reminders” special app access if apps that
            // target Android 12L (API 31 aka "S") want to set exact alarms.
            // Look in Settings/Apps/AmsatDroid/Alarms & Reminders

            // Get the AlarmManager service
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if(alarmManager.canScheduleExactAlarms()){
                    // This device can schedule exact alarms
                    // Do nothing here.
                } else {
                    // This device can't schedule exact alarms
                    // Inform the user and then take them to the alarms & reminders settings menu window
                    if (channelPermissionDialogOkay == false){
                        showPermissionRationaleDialog(context,"AmsatDroid", "To use this feature, you need to enable Alarms & Reminders on the next screen.");
                        return false;
                    }
                    // go to exact alarm settings
                    //PackageInfo pki = new PackageInfo();
                    Intent iit = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    Bundle myB = new Bundle();
                    startActivity(context, iit, myB);
                    return false;
                }
            }

            // =========================================================
            // check if the user turned off notifications for this app.
            // =========================================================
            Intent notificationSettingsIntent = new Intent();
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            boolean areNotificationsEnabled = notificationManagerCompat.areNotificationsEnabled();
            Bundle myB = new Bundle();
            // check if Notifications are enabled on this phone.
            if (areNotificationsEnabled == false){
                // Inform the user that the Notifications aren't enabled.
                if (channelPermissionDialogOkay == false){
                    showPermissionRationaleDialog(context,"AmsatDroid", "To use this feature, you need to enable Notifications on the next screen.");
                    return false;
                }
                // Take user to the app Notifications settings screen.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    //  as of Oreo Android 8.0 (API level 26) you can open the notification settings via this intent
                    notificationSettingsIntent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra(Settings.EXTRA_APP_PACKAGE,   "uk.me.g4dpz.HamSatDroid");
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    notificationSettingsIntent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    notificationSettingsIntent.putExtra("app_package", "uk.me.g4dpz.HamSatDroid");
                    notificationSettingsIntent.putExtra("app_uid", context.getApplicationInfo().uid);
                } else {
                    // Works for Android 4 API 19 - KitKat until Lollipop (code above)
                    notificationSettingsIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    notificationSettingsIntent.addCategory(Intent.CATEGORY_DEFAULT);
                    notificationSettingsIntent.setData(Uri.parse("package:" + "uk.me.g4dpz.HamSatDroid"));
                }
                // display the Notifications settings screen.
                startActivity(context, notificationSettingsIntent, myB);
                return false;
            } else {
                //  Notifications are Enabled, so do nothing here.
            }

            // =====================================================================================
            // check if our "Satellite Reminders" Channel (aka Notification Category) is turned off.
            // =====================================================================================
            NotificationManager nManager = null;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                nManager = context.getSystemService(NotificationManager.class);
                NotificationChannel channel = nManager.getNotificationChannel("satReminder");
                // Channel should return IMPORTANCE_NONE if the channel is turned ON.
                boolean chanImpt = channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
                // if channel/category is turned off then inform the user
                if (chanImpt == false){
                    if (channelPermissionDialogOkay == false){
                        showPermissionRationaleDialog(context,"AmsatDroid", "To use this feature, you need to enable the Satellite Reminders Notification Category on the next screen.");
                        return false;
                    }
                    //  as of Oreo Android 8.0 (API level 26) you can open the specific CHANNEL's settings via this intent
                    notificationSettingsIntent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra(Settings.EXTRA_APP_PACKAGE,   "uk.me.g4dpz.HamSatDroid")
                            .putExtra(Settings.EXTRA_CHANNEL_ID, "satReminder");
                    startActivity(context, notificationSettingsIntent, myB);
                    return false;
                }
            }
            return true; // result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            // Trouble checking Permissions
            return false;
        }
    }

    // Return the size arraylist
    @Override
    public int getItemCount() {
        return passes.size();
    }

    //  The viewHolder class is used to manage the RecyclerView.  It can
    //  store tag data and then later retrieve that viewHolder / RecyclerView view based on that tag
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView satPassDetails;
        public CheckBox chkAlertSelected;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            satPassDetails = (TextView) itemLayoutView.findViewById(R.id.satPassDetails);
            chkAlertSelected = (CheckBox) itemLayoutView.findViewById(R.id.chkAlertSelected);
        }
    }

}


