package uk.me.g4dpz.HamSatDroid;

import static android.app.PendingIntent.getActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import uk.me.g4dpz.HamSatDroid.utils.IaruLocator;
import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.InvalidTleException;
import uk.me.g4dpz.satellite.PassPredictor;
import uk.me.g4dpz.satellite.SatNotFoundException;
import uk.me.g4dpz.satellite.SatPassTime;
import uk.me.g4dpz.satellite.Satellite;
import uk.me.g4dpz.satellite.SatelliteFactory;
import uk.me.g4dpz.satellite.TLE;


public class HamSatDroid extends ASDActivity implements OnGestureListener {

    private static final String PERIOD = ".";
    private static final String COMMA = ",";
    private static final String WEATHER_CELESTRAK = "WEATHER_CELESTRAK";
    private static final String CUBESAT_CELESTRAK = "CUBESAT_CELESTRAK";
    private static final String AMATEUR_CELESTRAK = "AMATEUR_CELESTRAK";
    private static final String RESOURCES_CELESTRAK = "RESOURCES_CELESTRAK";
    private static final String NEW_CELESTRAK = "NEW_CELESTRAK";
    private static final String AMATEUR_AMSAT = "AMATEUR_AMSAT";
    private static final String COLON_NL = ":\n";
    private static final String FOR_THE_NEXT = ", for the next ";
    private static final String PASS_PREDICTIONS_FOR_SATELLITE = "Pass predictions for satellite ";
    private static final String SLASH = "/";
    private static final String FOR_HOME_COORDINATES_LAT_LON_GRIDSQUARE = ", for home coordinates (lat/lon/gridsquare) ";
    private static final String UI_THEME = "uiTheme";
    private static final String PASS_HEADER = "passHeader";
    private static final String SELECTED_PASS_TIME = "selectedPassTime";
    private static final String SELECTED_SAT_INDEX = "selectedSatIndex";
    private static final String CANCEL = "Cancel";
    private static final String OK = "OK";
    private static final String FILE = "File ";
    private static final String ZERO_STRING = "0";
    private static final String HOME_LON = "homeLon";
    private static final String HOME_LAT = "homeLat";
    private static final String HOME_LOCATOR = "homeLocator";
    private static final String KEPS_UPDATED = "Keps updated!";
    private Context context;
    private static final String SAT_SORT_METHOD = "SatSortingMethod";
    private static final String UTC_TIME = "utcTime";
    // Filenames and URLs
    private final String elemfile = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/Download/nasabare.txt";
    private static final String BIN_PASS_FILENAME = "prefs.bin";
    private static final String BIN_ELEM_FILENAME = "elems.bin";
    private static final String ELEM_URL_AMATEUR_AMSAT = "https://www.amsat.org/amsat/ftp/keps/current/nasabare.txt";
    private static final String ELEM_URL_AMATEUR_CELESTRAK = "https://celestrak.com/NORAD/elements/amateur.txt";
    private static final String ELEM_URL_WEATHER_CELESTRAK = "https://celestrak.org/NORAD/elements/gp.php?GROUP=weather&FORMAT=tle";
    // was using "https://celestrak.com/NORAD/elements/noaa.txt" which is no longer a valid URL
    private static final String ELEM_URL_CUBESAT_CELESTRAK = "https://celestrak.com/NORAD/elements/cubesat.txt";
    private static final String ELEM_URL_RESOURCES_CELESTRAK = "https://celestrak.com/NORAD/elements/resource.txt";
    private static final String ELEM_URL_NEW_CELESTRAK = "https://celestrak.com/NORAD/elements/tle-new.txt";
    // Various
    private static List<TLE> allSatElems;
    private int defaultSatIndex;
    private static List<SatPassTime> passes = new ArrayList<SatPassTime>();
    private String passHeader;
    private ArrayAdapter<SatPassTime> passAdapter;
    // Used by location methods
    private Boolean trackingLocation = false;
    private final LocationListener locationListener = new UserLocationListener();
    //public static AppConfig appConfig = new AppConfig(false);
    private AlertDialog locationProgressDialog;
    private AsyncTask<Integer, Integer, Boolean> updateKepsTask;
    private AlertDialog kepsProgressDialog;
    private View startTimeDialogLayout;
    private Calendar startTimeCalendar;
    private TextView timeDisplay;
    private Button setStartTime;
    private GestureDetector gestureScanner;
    private static final int SWIPE_MINDISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private AlertDialog timePickerDialog;
    // For SkyView
    private static SatPassTime selectedPass;
    private static GroundStationPosition groundStation;
    private static PassPredictor passPredictor;
    private static Satellite selectedSatellite;
    private static String kepsSource;
    static String selectedSatName = "";
    static String currentLocator = "";
    private static final int NOTIFICATION_ID = 0;
    private static final String CHANNEL_ID = "satReminder";
    private static final CharSequence CHANNEL_NAME = "Satellite Reminders";
    private static final String CHANNEL_DESCRIPTION = "Satellite AOS Reminders";

    // create an ArrayList to store the satellite pass alerts that are scheduled.
    private static ArrayList<String> notificationArrayList = new ArrayList<String>();
    public ArrayList<String> getNotificationArrayList() {
        return notificationArrayList;
    }
    public void setNotificationArrayList(ArrayList<String> notificationArrayList) {
        HamSatDroid.notificationArrayList = notificationArrayList;
    }

    // RecyclerView is used to display a scrolling list of satellite passes
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gestureScanner = new GestureDetector(this);

        // Load layout from XML
        setContentView(R.layout.pass_screen);

        // Save context for later
        context = this;

        // Set default preferences
        PreferenceManager.setDefaultValues(this, R.xml.preference_root, false);

        // setup channel for notifications
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESCRIPTION);
            // Create a notification manager to use for notifying the user of an upcoming satellite pass.
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            // Register the channel with the system. You can't change the importance or other notification behaviors after this.
            notificationManager.createNotificationChannel(channel);
        }

        // Initialise passes header text view
        if (passHeader != null) {
            ((TextView) findViewById(R.id.latlon_view)).setText(passHeader);
        } else {
            ((TextView) findViewById(R.id.latlon_view)).setText("");
        }

        // Get saved element data from binary file
        restoreElemFromFile();
        if (allSatElems == null) {
            // Element data not found, load satellite data from file SD card
            loadElemFromFile();
            if (allSatElems == null) {
                // File could not be loaded from SD card, use default
                loadElemFromInternalFile();
                new AlertDialog.Builder(context)
                        .setMessage(FILE + elemfile + " could not be found. Used default data (may be out of date).")
                        .setPositiveButton(OK, null).show();
            }
            // Save element data to binary file
            saveElemToFile();
        }

        // Initialise Satellite List
        bindSatList();

        // Create a listener for the Satellite selection spinner
        Spinner spinner = (Spinner) findViewById(R.id.SatelliteSelectorSpinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Object item = adapterView.getItemAtPosition(position);
                if (item != null) {
                    selectedSatName = item.toString();
                } else {
                    selectedSatName = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // TODO Auto-generated method stub
                selectedSatName = "";
            }
        });

        // Create passes adapter and bind it to ListView
        bindPassView();

        // Set passes calc button callback
        ((Button) findViewById(R.id.CalculatePassButton)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final String selectedTime = (String) ((Spinner) findViewById(R.id.TimeSelectorSpinner)).getSelectedItem();
                if (selectedTime.equals("Next Pass Only")) {
                    new CalcPassTask().execute(0);
                } else if (selectedTime.equals("6 hours")) {
                    new CalcPassTask().execute(6);
                } else if (selectedTime.equals("12 hours")) {
                    new CalcPassTask().execute(12);
                } else if (selectedTime.equals("24 hours")) {
                    new CalcPassTask().execute(24);
                }
            }
        });

        timeDisplay = (TextView) findViewById(R.id.StartTime);

        // add a click listener to the set start time button
        setStartTime = (Button) findViewById(R.id.SetStartTimeButton);
        setStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                startTimeDialogLayout = inflater.inflate(R.layout.start_time_dialog,
                        (ViewGroup) findViewById(R.id.START_TIME_DIALOG));
                if (startTimeCalendar != null) {
                    final DatePicker datePicker = (DatePicker) startTimeDialogLayout.findViewById(R.id.DatePicker01);
                    final TimePicker timePicker = (TimePicker) startTimeDialogLayout.findViewById(R.id.TimePicker01);
                    timePicker.setCurrentHour(startTimeCalendar.getTime().getHours());
                    timePicker.setCurrentMinute(startTimeCalendar.getTime().getMinutes());
                    datePicker.updateDate(startTimeCalendar.get(Calendar.YEAR), startTimeCalendar.get(Calendar.MONTH),
                            startTimeCalendar.get(Calendar.DAY_OF_MONTH));
                }
                // create an AlertDialog to show the date/time pickers using the inflated start_time_dialog layout.
                timePickerDialog = new AlertDialog.Builder(context).create();
                // The "SET", "Use Current", and "Cancel" buttons are now included in the start_time_dialog layout
                // These buttons use an onClick="dateTimedialogButtonClickHandler"
                timePickerDialog.setView(startTimeDialogLayout);
                timePickerDialog.show();
            }
        });
        // display the current time
        updateStartTimeDisplay();
    }

    public void dateTimedialogButtonClickHandler(View v) {
        // handles onClick for the "SET", "Use Current", and "Cancel" buttons that
        // are shown within the start_time_dialog layout.
        int id = v.getId();
        if(id == R.id.ButtonTimeCancel){
            timePickerDialog.dismiss();
        } else if (id == R.id.ButtonTimeUseCurrent){
            // startTimeCalendar = c;
            startTimeCalendar = null;
            updateStartTimeDisplay();
            timePickerDialog.dismiss();
        } else if (id == R.id.ButtonTimeSet){
            final DatePicker datePicker = (DatePicker) startTimeDialogLayout.findViewById(R.id.DatePicker01);
            final TimePicker timePicker = (TimePicker) startTimeDialogLayout.findViewById(R.id.TimePicker01);
            if (startTimeCalendar == null) {
                startTimeCalendar = Calendar.getInstance();
            }
            startTimeCalendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                    timePicker.getCurrentHour(), timePicker.getCurrentMinute());
            updateStartTimeDisplay();
            timePickerDialog.dismiss();
        }
    }

    @Override
    public void onStart() { super.onStart(); }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void updateStartTimeDisplay() {
        TimeZone timeZoneUTC = TimeZone.getTimeZone("UTC");
        TimeZone timeZoneDefault = TimeZone.getDefault();
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
        SimpleDateFormat TIME_FORMAT;
        boolean UseUTC = AppConfig.isUtcDateTime();
        // display the local or UTC date/time depending on the user config setting.
        if (!UseUTC){
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
        // update the Start date/time in UTC or Local depending on the user's preference setting.
        if (startTimeCalendar != null) {
            String dateTimeString = DATE_FORMAT.format(startTimeCalendar.getTime()) + " " + TIME_FORMAT.format(startTimeCalendar.getTime());
            timeDisplay.setText(dateTimeString);
        } else {
            timeDisplay.setText("Starting: now");
        }
    }

    private void bindSatList() {
        // Initialise Satellite List
        final Spinner s = (Spinner) findViewById(R.id.SatelliteSelectorSpinner);
        final ArrayAdapter<TLE> adapter = new ArrayAdapter<TLE>(context, android.R.layout.simple_spinner_item, allSatElems);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        s.setAdapter(adapter);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        //final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);

        View vv = findViewById(android.R.id.content).getRootView();

        Display display = getWindowManager().getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);

        int width = size.x;
        int height = size.y;
    }


    @Override
    protected void onResume() {
        super.onResume();

        //  This setObserver() was added so that the alarm/notification setup would have the correct information
        //  about the alarm settings.  This way it can keep a reference to display the
        //  alarm Checked in the pass list (recycler view) and also allow the user to remove/delete the pending alarm(s).
        setObserver();

        final SharedPreferences prefs = getPreferences(0);

        // Restore spinner selections
        final int satIndex = prefs.getInt(SELECTED_SAT_INDEX, defaultSatIndex);
        final int timeIndex = prefs.getInt(SELECTED_PASS_TIME, 0);
        ((Spinner) findViewById(R.id.SatelliteSelectorSpinner)).setSelection(satIndex);
        ((Spinner) findViewById(R.id.TimeSelectorSpinner)).setSelection(timeIndex);

        // Restore passes header
        final String restoredText = prefs.getString(PASS_HEADER, null);
        if (restoredText != null) {
            ((TextView) findViewById(R.id.latlon_view)).setText(restoredText);
        }

        // Restore Satellite Sorting Method.
        // The list of satellites displayed to the user can be sorted by
        // Catalog Description, ascending sat name, or descending sat name.
        final String restoredSorting = prefs.getString(SAT_SORT_METHOD, "NAMEASC");
        AppConfig.setSatSortingMethod(restoredSorting);
        // sort the satellite list
        switch (restoredSorting) {
            case "NAMEASC":
                sortAllSatElemsByNameAscending();
                break;
            case "NAMEDESC":
                sortAllSatElemsByNameDescending();
                break;
            case "CATDESC":
                sortAllSatElemsByCatalogDesc();
                break;
            default:
                sortAllSatElemsByCatalogAsc();
        }

        // Restore the user's Time Display preference (utc or local).
        final Boolean restoredUtcPref = prefs.getBoolean(UTC_TIME, false);
        AppConfig.setUtcDateTime(restoredUtcPref);

        // restore the alarm/notification arrayList by reading the sharedPref value
        final SharedPreferences readsharedPrefs1 = PreferenceManager.getDefaultSharedPreferences(context);
        // Create a Gson object which is used to serialize Java objects to JSON (and deserialize them back to Java).
        Gson gson = new Gson();
        // get the "ALARM" sharedpref value
        String json = readsharedPrefs1.getString("ALARM", "[100000001|IO-117|90|0|180]");
        Type type1 = new TypeToken<List<String>>() {
        }.getType();

        // populate the notificationArrayList from the sharedPref values
        notificationArrayList = gson.fromJson(json, type1);

        // Retrieve passes
        restorePassesFromFile();

        final SharedPreferences.Editor SPeditor = getPreferences(0).edit();
        // restore the user's UI Theme preference (dark or light)
        final String restoredUIthemePref = prefs.getString(UI_THEME, "DARK");
        // restore the user's UI Theme preference (dark or light)
        if (restoredUIthemePref.equals("DARK")){
            // Activate Dark theme
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            // set preference string to DARK
            SPeditor.putString(UI_THEME, "DARK");
        } else if (restoredUIthemePref.equals("LIGHT")) {
            // Activate Light theme
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            // set preference string to LIGHT
            SPeditor.putString(UI_THEME, "LIGHT");
        } else {
            // Activate Dark theme
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            // set preference string to DARK
            SPeditor.putString(UI_THEME, "DARK");
        }
        SPeditor.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();

        final SharedPreferences.Editor editor = getPreferences(0).edit();

        // Save spinner selections
        editor.putInt(SELECTED_SAT_INDEX, ((Spinner) findViewById(R.id.SatelliteSelectorSpinner)).getSelectedItemPosition());
        editor.putInt(SELECTED_PASS_TIME, ((Spinner) findViewById(R.id.TimeSelectorSpinner)).getSelectedItemPosition());

        try {
            setObserver();
            setSatellite();
        } catch (IllegalArgumentException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (InvalidTleException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SatNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // Save passes
        if (passes != null) {
            FileOutputStream fos;
            try {
                fos = openFileOutput(BIN_PASS_FILENAME, MODE_PRIVATE);
                final ObjectOutputStream os = new ObjectOutputStream(fos);
                os.writeObject(passes);
                os.close();
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        // Save passes header
        if (passHeader != null) {
            editor.putString(PASS_HEADER, passHeader);
        }

        // Save user's sorting method preference.
        final String satSorting = AppConfig.getSatSortingMethod();
        if (satSorting != null) {
            editor.putString(SAT_SORT_METHOD, satSorting);
        } else {
            editor.putString(SAT_SORT_METHOD, "NAMEASC");
        }

        // Save user's Time Display preference (utc or local).
        final Boolean UTC = AppConfig.isUtcDateTime();
        if (UTC != null) {
            editor.putBoolean(UTC_TIME, UTC);
        } else {
            editor.putBoolean(UTC_TIME, false);
        }

        // save user's UI Theme preference (DARK or LIGHT)
        int themeMode = AppCompatDelegate.getDefaultNightMode();
        if(themeMode == 2){
            // mode is DARK
            editor.putString(UI_THEME, "DARK");
        } else {
            // mode is LIGHT
            editor.putString(UI_THEME, "LIGHT");
        }

        // Create a Gson object which is used to serialize Java objects to JSON (and deserialize them back to Java).
        Gson gson = new Gson();
        String json = gson.toJson(notificationArrayList);
        // Save alarm/notification arrayList to sharedPreferences
        editor.putString("ALARM", json);
        editor.commit();

        if (trackingLocation) {
            cancelLocationRequest();
        }
    }

    @SuppressWarnings("unchecked")
    void restorePassesFromFile() {
        FileInputStream fis;
        try {
            fis = openFileInput(BIN_PASS_FILENAME);
            final ObjectInputStream is = new ObjectInputStream(fis);
            final List<SatPassTime> passTmp = (ArrayList<SatPassTime>) is.readObject();
            if (passTmp != null) {
                HamSatDroid.setPasses(passTmp);
                bindPassView();
            }
            is.close();
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void bindPassView() {
        // find the RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        // Create a divider line decoration that will visually separate list items
        // First, check if its already been created.  If so, then don't add it again.
        // This 0 will be the number of itemDecorator we have in recycler view. First time Calculating a pass it'll be 0.
        if (0 == mRecyclerView.getItemDecorationCount()) {
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, linearLayoutManager.getOrientation());
            dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.divider));
            mRecyclerView.addItemDecoration(dividerItemDecoration);
        } else {
            // "divider" ItemDecoration already exists, so don't add it.
        }
        // Read the sharedPref value for populating the alarm array
        final SharedPreferences readsharedPrefs1 = PreferenceManager.getDefaultSharedPreferences(context);
        // Create a Gson object which is used to serialize Java objects to JSON (and deserialize them back to Java).
        Gson gson = new Gson();
        // get the "ALARM" sharedpref value
        String json = readsharedPrefs1.getString("ALARM", "[100000001|IO-117|90|0|180]");
        Type type1 = new TypeToken<List<String>>() {
        }.getType();
        // populate the notificationArrayList from the sharedPref value
        notificationArrayList = gson.fromJson(json, type1);
        // create the RecyclerView Object using the satellite pass list, "passes"
        mAdapter = new RecyclerViewPassAdapter(passes);
        // set the adapter object to the Recyclerview
        mRecyclerView.setAdapter(mAdapter);
    }


    private void recalcPass(final int hoursAhead) {
        // How long to go back/forward in time to find a passes (in hours)
        final int calcRange = 24;
        // Get home lat and lon from saved preferences
        setObserver();
        Calendar myCal;
        if (startTimeCalendar != null) {
            myCal = startTimeCalendar;
        } else {
            // Get current GMT date and time
            myCal = Calendar.getInstance();
        }
        TLE myelem = null;
        // Calculate next satellite passes
        try {
            myelem = setSatellite();
            HamSatDroid.setPassPredictor(new PassPredictor(myelem, HamSatDroid.getGroundStation()));
            HamSatDroid.setPasses(getPassPredictor().getPasses(myCal.getTime(), hoursAhead, true));
        } catch (final InvalidTleException e) {
            passHeader = "ERROR: Bad Keplerian Elements";
        } catch (final SatNotFoundException e) {
            passHeader = "ERROR: Unknown Satellite";
        }
        // setPass(mysat.calcPass(dayGHAAref, dayGHAAref + (hoursAhead / 24.0),
        // (calcRange / 24.0), homeLat, homeLong));
        final NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setMaximumFractionDigits(4);
        // make sure we have the currently set Maidenhead location
        // Might be used in the future.
        final double homeLat = HamSatDroid.getGroundStation().getLatitude();
        final double homeLong = HamSatDroid.getGroundStation().getLongitude();
        final IaruLocator locator = new IaruLocator(homeLat, homeLong);
        HamSatDroid.currentLocator = locator.toMaidenhead();
        //
        TimeZone timeZoneUTC = TimeZone.getTimeZone("UTC");
        TimeZone timeZoneDefault = TimeZone.getDefault();
        DateFormat df = DateFormat.getDateTimeInstance();
        df.setTimeZone(timeZoneUTC);
        // Format the date that will be used in the pass Header.
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        SimpleDateFormat TIME_FORMAT; // = new SimpleDateFormat("HH:mm a", Locale.ENGLISH);
        // display the local or UTC date/time depending on the user config setting.
        boolean UseUTC = AppConfig.isUtcDateTime();
        if (AppConfig.isUtcDateTime() == false) {
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
        // setup the passHeader
        if (passes.get(0).getMaxEl() == 0) {
            passHeader = "No passes found for satellite " + myelem.getName() + FOR_HOME_COORDINATES_LAT_LON_GRIDSQUARE
                    + formatter.format(homeLat) + SLASH + formatter.format(homeLong) + SLASH + locator.toMaidenhead()
                    + " for the next " + calcRange + " hours.\n";
        } else {
            if (hoursAhead == 0) {
                passHeader = PASS_PREDICTIONS_FOR_SATELLITE + myelem.getName() + FOR_HOME_COORDINATES_LAT_LON_GRIDSQUARE
                        + formatter.format(homeLat) + SLASH + formatter.format(homeLong) + SLASH + locator.toMaidenhead()
                        + FOR_THE_NEXT + "passes only" + ", starting "
                        + DATE_FORMAT.format(myCal.getTime()) + " " + TIME_FORMAT.format(myCal.getTime()) + COLON_NL;
            } else {
                passHeader = PASS_PREDICTIONS_FOR_SATELLITE + myelem.getName() + FOR_HOME_COORDINATES_LAT_LON_GRIDSQUARE
                        + formatter.format(homeLat) + SLASH + formatter.format(homeLong) + SLASH + locator.toMaidenhead()
                        + FOR_THE_NEXT + hoursAhead + " hours, starting "
                        + DATE_FORMAT.format(myCal.getTime()) + " " + TIME_FORMAT.format(myCal.getTime()) + COLON_NL;
            }
        }
    }

    /**
     * @return
     * @throws IllegalArgumentException
     * @throws SatNotFoundException
     * @throws InvalidTleException
     */
    private TLE setSatellite() throws IllegalArgumentException, InvalidTleException, SatNotFoundException {
        final TLE myelem = (TLE) ((Spinner) findViewById(R.id.SatelliteSelectorSpinner)).getSelectedItem();
        HamSatDroid.setSelectedSatellite(SatelliteFactory.createSatellite(myelem));
        HamSatDroid.setPassPredictor(new PassPredictor(myelem, HamSatDroid.getGroundStation()));
        return myelem;
    }

    /**
     * @throws NumberFormatException
     */
    @Override
    public void setObserver() throws NumberFormatException {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String shomeLat = sharedPref.getString(HOME_LAT, ZERO_STRING);
        shomeLat = shomeLat.replace(COMMA, PERIOD);
        String shomeLon = sharedPref.getString(HOME_LON, ZERO_STRING);
        shomeLon = shomeLon.replace(COMMA, PERIOD);
        HamSatDroid.setGroundStation(new GroundStationPosition(Double.valueOf(shomeLat), Double.valueOf(shomeLon), 0));
        // populate the user's Maidenhead position from the ground-station's home Lat and Long.
        // The HamSatDroid.currentLocator may be used in the future.
        final double homeLat = HamSatDroid.getGroundStation().getLatitude();
        final double homeLong = HamSatDroid.getGroundStation().getLongitude();
        final IaruLocator locator = new IaruLocator(homeLat, homeLong);
        HamSatDroid.currentLocator = locator.toMaidenhead();
    }

    private void setLocationPreference(final String provider) {
        if (!trackingLocation) {
            final LocationManager locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!locMgr.isProviderEnabled(provider)) {
                // create an Intent for the permissions settings screen.
                final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                // Shows the user a dialog popup message and asks if they want to go to the permissions settings screen.
                showLocationRationaleDialog(intent, "AmsatDroid", "Please enable " + provider + " location in systems settings screen.");
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // create an Intent for the permissions settings screen.
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
                        // Shows the user a dialog popup message and asks if they want to go to the permissions settings screen.
                        showLocationRationaleDialog(intent, "AmsatDroid", "To use this feature, you need to go into permissions on the next screen and allow Location access for this app.");
                        return;
                    }
                }
                locMgr.requestLocationUpdates(provider, 0, 0, locationListener);
                trackingLocation = true;
                locationProgressDialog = new ProgressDialog(context);
                locationProgressDialog.setMessage("Retrieving location...");
                locationProgressDialog.setCancelable(false);
                locationProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, CANCEL, new LocationDialogListener());
                locationProgressDialog.show();
            }
        }
    }

    private void showLocationRationaleDialog(Intent intent, String titleText, String messageText){
        // Shows the user a dialog popup message and asks if they want to go to the permissions settings screen.
        Activity activity = (Activity) context;
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity, androidx.appcompat.R.style.Base_Theme_AppCompat_Dialog);
        builder.setMessage(messageText);
        builder.setTitle(titleText);
        // handle the "settings" and "cancel" buttons in the Location rationale Dialog window.
        builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Take user to the permissions screen
                // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //do what you want when cancel is clicked
            }
        });
        builder.create();
        builder.show();
    }

    private ActivityResultLauncher<String[]> registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions requestMultiplePermissions, Object o) {
        return null;
    }

    private class UserLocationListener implements LocationListener {

        private static final String LOCATION_SET_TO_LAT_LON = "IaruLocator set to lat/lon ";

        @Override
        public void onLocationChanged(final Location location) {
            final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putString(HOME_LAT, Double.toString(location.getLatitude()));
            editor.putString(HOME_LON, Double.toString(location.getLongitude()));
            final IaruLocator locator = new IaruLocator(location.getLatitude(), location.getLongitude());
            String homeLoc = locator.toMaidenhead();
            editor.putString(HOME_LOCATOR, homeLoc).commit();
            editor.apply();
            // TODO: probably don't need this any longer
            HamSatDroid.currentLocator = locator.toMaidenhead();
            String dialogString = "";
            if (location.hasAccuracy()) {
                dialogString = LOCATION_SET_TO_LAT_LON + Double.toString(location.getLatitude()) + SLASH
                        + Double.toString(location.getLongitude()) + ", location accuracy " + location.getAccuracy() + " meters";
            } else {
                dialogString = LOCATION_SET_TO_LAT_LON + Double.toString(location.getLatitude()) + SLASH
                        + Double.toString(location.getLongitude()) + ", unknown location accuracy";
            }
            new AlertDialog.Builder(context).setMessage(dialogString).setPositiveButton(OK, null).show();
            cancelLocationRequest();
        }

        @Override
        public void onProviderDisabled(final String provider) {
        }

        @Override
        public void onProviderEnabled(final String provider) {
        }

        @Override
        public void onStatusChanged(final String provider, final int status, final Bundle extras) {
        }
    }

    private class LocationDialogListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(final DialogInterface di, final int which) {
            cancelLocationRequest();
        }
    }

    private void cancelLocationRequest() {
        final LocationManager locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        trackingLocation = false;
        locMgr.removeUpdates(locationListener);
        locationProgressDialog.dismiss();
    }

    private class CalcPassTask extends AsyncTask<Integer, Integer, Long> {

        @Override
        protected Long doInBackground(final Integer... timeOffsetArray) {
            // do not do any UI work here
            final Integer timeOffset = timeOffsetArray[0];
            recalcPass(timeOffset);
            return (long) 0;
        }

        @Override
        protected void onProgressUpdate(final Integer... progress) {
        }

        @Override
        protected void onPostExecute(final Long result) {
            ((ProgressBar) findViewById(R.id.PassProgressBar)).setVisibility(View.INVISIBLE);
            bindPassView();
            ((TextView) findViewById(R.id.latlon_view)).setText(passHeader);
        }

        @Override
        protected void onPreExecute() {
            ((ProgressBar) findViewById(R.id.PassProgressBar)).setVisibility(View.VISIBLE);
        }
    }


    @SuppressWarnings("unchecked")
    void restoreElemFromFile() {
        FileInputStream fis;
        try {
            fis = openFileInput(BIN_ELEM_FILENAME);
            final ObjectInputStream is = new ObjectInputStream(fis);
            // create a list of Sats used for sorting
            final List<TLE> elemTmp = (ArrayList<TLE>) is.readObject();
            if (elemTmp != null) {
                HamSatDroid.setAllSatElems(elemTmp);
            }
            is.close();
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sortAllSatElemsByNameAscending() {
        Collections.sort(allSatElems, new Comparator<TLE>() {
            @Override
            public int compare(TLE tle, TLE t1) {
                return tle.getName().compareToIgnoreCase(t1.getName());
            }
        });
    }

    private void sortAllSatElemsByNameDescending() {
        Collections.sort(allSatElems, new Comparator<TLE>() {
            @Override
            public int compare(TLE tle, TLE t1) {
                return t1.getName().compareToIgnoreCase(tle.getName());
            }
        });
    }

    private void sortAllSatElemsByCatalogAsc() {
        Collections.sort(allSatElems, new Comparator<TLE>() {
            @Override
            public int compare(TLE tle, TLE t1) {
                return tle.getCatnum() - t1.getCatnum();
            }
        });
    }

    private void sortAllSatElemsByCatalogDesc() {
        Collections.sort(allSatElems, new Comparator<TLE>() {
            @Override
            public int compare(TLE tle, TLE t1) {
                return t1.getCatnum() - tle.getCatnum();
            }
        });
    }

    boolean loadElemFromFile() {
        boolean success = false;
        try {
            allSatElems = TLE.importSat(new FileInputStream(new File(elemfile)));
            success = true;
            bindSatList();
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    boolean loadElemFromNetwork() {
        boolean success = false;
        URL url;
        try {
            final String kepSource = HamSatDroid.getKepsSource();
            if (AMATEUR_AMSAT.equals(kepSource)) {
                url = new URL(ELEM_URL_AMATEUR_AMSAT);
            } else if (AMATEUR_CELESTRAK.equals(kepSource)) {
                url = new URL(ELEM_URL_AMATEUR_CELESTRAK);
            } else if (WEATHER_CELESTRAK.equals(kepSource)) {
                url = new URL(ELEM_URL_WEATHER_CELESTRAK);
            } else if (CUBESAT_CELESTRAK.equals(kepSource)) {
                url = new URL(ELEM_URL_CUBESAT_CELESTRAK);
            } else if (RESOURCES_CELESTRAK.equals(kepSource)) {
                url = new URL(ELEM_URL_RESOURCES_CELESTRAK);
            } else if (NEW_CELESTRAK.equals(kepSource)) {
                url = new URL(ELEM_URL_NEW_CELESTRAK);
            } else {
                throw new IllegalArgumentException("Unknown keplerian source[" + kepSource + "]");
            }
            // create a temporary list of Sats used for sorting
            final List<TLE> tmpSatElems = TLE.importSat(url.openStream());
            if (tmpSatElems != null) {
                allSatElems = tmpSatElems;
                // determine the user's satellite sorting setting and then display them sorted
                String SatelliteSortingMethod = AppConfig.getSatSortingMethod();
                switch (SatelliteSortingMethod) {
                    case "NAMEASC":
                        sortAllSatElemsByNameAscending();
                        break;
                    case "NAMEDESC":
                        sortAllSatElemsByNameDescending();
                        break;
                    case "CATDESC":
                        sortAllSatElemsByCatalogDesc();
                        break;
                    default:
                        sortAllSatElemsByCatalogAsc();
                }
                // mark it as successful.
                success = true;
            }
            allSatElems = tmpSatElems;
            success = true;
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        }
        return success;
    }

    void loadElemFromInternalFile() {
        try {
            allSatElems = TLE.importSat(getResources().openRawResource(R.raw.nasabare));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    void saveElemToFile() {
        FileOutputStream fos;
        if (allSatElems != null) {
            try {
                fos = openFileOutput(BIN_ELEM_FILENAME, MODE_PRIVATE);
                final ObjectOutputStream os = new ObjectOutputStream(fos);
                os.writeObject(allSatElems);
                os.close();
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class LoadElemNetTask extends AsyncTask<Integer, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(final Integer... timeOffset) {
            // do not do any UI work here
            return loadElemFromNetwork();
        }

        @Override
        protected void onProgressUpdate(final Integer... progress) {
        }

        @Override
        protected void onCancelled() {
            kepsProgressDialog.dismiss();
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            kepsProgressDialog.dismiss();
            if (result) {
                saveElemToFile();
                bindSatList();
                Toast.makeText(context, KEPS_UPDATED, Toast.LENGTH_LONG).show();
            } else {
                new AlertDialog.Builder(context).setMessage("Could not download file from network. Kept existing element data.")
                        .setPositiveButton(OK, null).show();
            }
        }

        @Override
        protected void onPreExecute() {
            kepsProgressDialog = new ProgressDialog(context);
            kepsProgressDialog.setMessage("Downloading keps...");
            kepsProgressDialog.setCancelable(false);
            kepsProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, CANCEL, new NetKepsDialogListener());
            kepsProgressDialog.show();
        }
    }

    private class NetKepsDialogListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(final DialogInterface di, final int which) {
            updateKepsTask.cancel(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu XML resource.
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        // get the Android build version
        final int androidVersion = Build.VERSION.SDK_INT;
        /* Build.VERSION_CODES.JELLY_BEAN_MR1 */
        if (androidVersion > 17 ) {
            menu.findItem(R.id.MENU_LOAD_ELEM).setVisible(false);
        }
        return true;
    }

    private void selectSpinnerItemByValue(Spinner spinner, Object value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.MENU_GET_GPS_LOCATION) {
            setLocationPreference(LocationManager.GPS_PROVIDER);
            return true;
        } else if (item.getItemId() == R.id.MENU_GET_NETWORK_LOCATION) {
            setLocationPreference(LocationManager.NETWORK_PROVIDER);
            return true;
        } else if (item.getItemId() == R.id.MENU_ENTER_LOCATION) {
            final Intent launchPreferencesIntent = new Intent().setClass(this, PrefHandling.class);
            startActivity(launchPreferencesIntent);
            return true;
        } else if (item.getItemId() == R.id.MENU_TIMELOCAL) {
            // User wants the app to display the local date and time.
            AppConfig.setUtcDateTime(false);
            // update the date/time above the "Change Start Time" button to Local date/time.
            updateStartTimeDisplay();
            // recalculate the pass list so that it displays the Date/Time in local date/time.
            final String selectedTime = (String) ((Spinner) findViewById(R.id.TimeSelectorSpinner)).getSelectedItem();
            if (selectedTime.equals("Next Pass Only")) {
                new CalcPassTask().execute(0);
            } else if (selectedTime.equals("6 hours")) {
                new CalcPassTask().execute(6);
            } else if (selectedTime.equals("12 hours")) {
                new CalcPassTask().execute(12);
            } else if (selectedTime.equals("24 hours")) {
                new CalcPassTask().execute(24);
            }
        } else if (item.getItemId() == R.id.MENU_TIMEUTC) {
            // User wants the app to display the UTC date and time.
            AppConfig.setUtcDateTime(true);
            // update the date/time above the "Change Start Time" button to UTC
            updateStartTimeDisplay();
            // recalculate the pass list so that it displays the Date/Time in UTC.
            final String selectedTime = (String) ((Spinner) findViewById(R.id.TimeSelectorSpinner)).getSelectedItem();
            if (selectedTime.equals("Next Pass Only")) {
                new CalcPassTask().execute(0);
            } else if (selectedTime.equals("6 hours")) {
                new CalcPassTask().execute(6);
            } else if (selectedTime.equals("12 hours")) {
                new CalcPassTask().execute(12);
            } else if (selectedTime.equals("24 hours")) {
                new CalcPassTask().execute(24);
            }
        } else if (item.getItemId() == R.id.MENU_UIDARK) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Toast.makeText(context, "Dark activated", Toast.LENGTH_LONG).show();
        } else if (item.getItemId() == R.id.MENU_UILIGHT) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Toast.makeText(context, "Light activated", Toast.LENGTH_LONG).show();
        } else if (item.getItemId() == R.id.MENU_SATSORTNAMEASC) {
            // User wants the app to sort the satellite list by name in asc order
            Spinner SatSelSpinner = ((Spinner) findViewById(R.id.SatelliteSelectorSpinner));
            Object selItemObj = SatSelSpinner.getSelectedItem();
            AppConfig.setSatSortingMethod("NAMEASC");
            sortAllSatElemsByNameAscending();
            selectSpinnerItemByValue(SatSelSpinner, selItemObj);
        } else if (item.getItemId() == R.id.MENU_SATSORTNAMEDESC) {
            // User wants the app to sort the satellite list by name in descending order
            Spinner SatSelSpinner = ((Spinner) findViewById(R.id.SatelliteSelectorSpinner));
            Object selItemObj = SatSelSpinner.getSelectedItem();
            AppConfig.setSatSortingMethod("NAMEDESC");
            sortAllSatElemsByNameDescending();
            selectSpinnerItemByValue(SatSelSpinner, selItemObj);
        } else if (item.getItemId() == R.id.MENU_SATSORTCATASC) {
            // User wants the app to sort the satellite list by catalog number in asc order
            Spinner SatSelSpinner = ((Spinner) findViewById(R.id.SatelliteSelectorSpinner));
            Object selItemObj = SatSelSpinner.getSelectedItem();
            AppConfig.setSatSortingMethod("CATASC");
            sortAllSatElemsByCatalogAsc();
            selectSpinnerItemByValue(SatSelSpinner, selItemObj);
        } else if (item.getItemId() == R.id.MENU_SATSORTCATDESC) {
            // User wants the app to sort the satellite list by catalog number in descending order
            Spinner SatSelSpinner = ((Spinner) findViewById(R.id.SatelliteSelectorSpinner));
            Object selItemObj = SatSelSpinner.getSelectedItem();
            AppConfig.setSatSortingMethod("CATDESC");
            sortAllSatElemsByCatalogDesc();
            selectSpinnerItemByValue(SatSelSpinner, selItemObj);
        } else if (item.getItemId() == R.id.MENU_LOAD_ELEM) {
            if (loadElemFromFile()) {
                saveElemToFile();
                Toast.makeText(context, KEPS_UPDATED, Toast.LENGTH_LONG).show();
            } else {
                new AlertDialog.Builder(context).setMessage(FILE + elemfile + " could not be found. Kept existing element data.")
                        .setPositiveButton(OK, null).show();
            }
            return true;
        } else if (item.getItemId() == R.id.MENU_DOWNLOAD_AMATEUR_AMSAT) {
            setKepsSource(AMATEUR_AMSAT);
            updateKepsTask = new LoadElemNetTask();
            updateKepsTask.execute(0);
            return true;
        } else if (item.getItemId() == R.id.MENU_DOWNLOAD_AMATEUR_CELESTRAK) {
            setKepsSource(AMATEUR_CELESTRAK);
            updateKepsTask = new LoadElemNetTask();
            updateKepsTask.execute(0);
            return true;
        } else if (item.getItemId() == R.id.MENU_DOWNLOAD_WEATHER_CELESTRAK) {
            setKepsSource(WEATHER_CELESTRAK);
            updateKepsTask = new LoadElemNetTask();
            updateKepsTask.execute(0);
            return true;
        } else if (item.getItemId() == R.id.MENU_DOWNLOAD_NEW_CELESTRAK) {
            setKepsSource(NEW_CELESTRAK);
            updateKepsTask = new LoadElemNetTask();
            updateKepsTask.execute(0);
            return true;
        } else if (item.getItemId() == R.id.MENU_DOWNLOAD_RESOURCES_CELESTRAK) {
            setKepsSource(RESOURCES_CELESTRAK);
            updateKepsTask = new LoadElemNetTask();
            updateKepsTask.execute(0);
            return true;
        } else if (item.getItemId() == R.id.MENU_DOWNLOAD_CUBESAT_CELESTRAK) {
            setKepsSource(CUBESAT_CELESTRAK);
            updateKepsTask = new LoadElemNetTask();
            updateKepsTask.execute(0);
            return true;
        } else if (item.getItemId() == R.id.MENU_HELP) {
            LinearLayout sk = (LinearLayout) findViewById(R.id.LLparent);
            Rect globalVisibilityRectangle = new Rect();
            sk.getGlobalVisibleRect(globalVisibilityRectangle);
            //
            int visibleHeight = globalVisibilityRectangle.right - globalVisibilityRectangle.left;
            int visibleWidth = globalVisibilityRectangle.bottom - globalVisibilityRectangle.top;

            int hhfg = getStatusBarHeight();
            Spinner sp = ((Spinner)findViewById(R.id.SatelliteSelectorSpinner));
            ViewGroup decoreView = (ViewGroup)sp.getRootView();
            int barSize = ((ViewGroup)decoreView.getChildAt(0)).getChildAt(0).getTop();


            new AlertDialog.Builder(context)
            .setMessage(getString(R.string.HelpString))
            .setPositiveButton("Dismiss", null)
            .show();
            // Commented the lines below since the website URL is no longer valid.
            //
            /*final Intent myIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://sites.google.com/site/hamsatdroid/installation-and-user-guide-1"));
            startActivity(myIntent);*/
            return true;
        } else if (item.getItemId() == R.id.MENU_ABOUT) {
            try {
                new AlertDialog.Builder(context)
                        .setMessage(
                                "Version " + this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName
                                        + "\n\n" + getString(R.string.AboutString)).setPositiveButton("Dismiss", null).show();
            } catch (final NameNotFoundException e) {
                e.printStackTrace();
            }
            return true;
        } else if (item.getItemId() == R.id.LAUNCH_GROUND_VIEW) {
            launchGroundView();
            return true;
        } else {
            //
        }
        return false;
    }

    public void createNotification(Context context, String satName, String aosDateTime) {
        // Creates the notification.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8 and up, introduced a new requirement of setting the channelId property by using a NotificationChannel.
            CharSequence name = CHANNEL_NAME;
            String description = CHANNEL_DESCRIPTION;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            // Create the NotificationChannel, but only on API 26+ because the NotificationChannel class is not in the Support Library.
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Create a notification manager to use for notifying the user of an upcoming satellite pass.
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            // Register the channel with the system. You can't change the importance or other notification behaviors after this.
            notificationManager.createNotificationChannel(channel);
            // create a notification builder and assign the various parameters.
            // NOTE that you MUST use an icon of some kind.  If you leave this line out the app will crash.
            Notification.Builder builder = new Notification.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle("Satellite " + satName + " Pass Alert!")
                    .setOngoing(false)
                    .setAutoCancel(true)
                    .setContentText("AOS @ " + aosDateTime);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                builder.setFlag(Notification.FLAG_INSISTENT, true);
                builder.setFlag(Notification.FLAG_AUTO_CANCEL, true);
            }
            // create an intent used to display this application when the user clicks on the notification pop-up.
            Intent notificationIntent = new Intent(context, HamSatDroid.class);
            PendingIntent contentIntent = getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            builder.setContentIntent(contentIntent);
            // display the notification.
            notificationManager.notify(1, builder.build());
        } else {
            // Create a notification manager to use for notifying the user of an upcoming satellite pass.
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // create an intent used to display this application when the user clicks on the notification pop-up.
            Intent notificationIntent = new Intent(context, HamSatDroid.class);
            PendingIntent contentIntent = getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            // create a notification builder and assign the various parameters.
            // NOTE that you MUST use an icon of some kind.  If you leave this line out the app will crash.
            Notification.Builder n = new Notification.Builder(context)
                    .setContentTitle("Satellite " + satName + " Pass Alert!")
                    .setContentText("AOS @ " + aosDateTime)
                    .setSmallIcon(R.drawable.icon)
                    .setContentIntent(contentIntent)
                    .setOngoing(false)
                    .setAutoCancel(true);
            // display the notification.
            notificationManager.notify(1, n.getNotification());
        }
    }

    public static Calendar toCalendar(Date date) {
        // converts a Date object to a Calendar object.
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    private void launchGroundView() {
        final Intent launchGroundViewIntent = new Intent().setClass(this, GroundView.class);
        startActivity(launchGroundViewIntent);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent me) {
        return gestureScanner.onTouchEvent(me);
    }

    @Override
    public boolean onDown(final MotionEvent arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
        if (e1.getX() - e2.getX() > SWIPE_MINDISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            launchGroundView();
        }
        return true;
    }

    @Override
    public void onLongPress(final MotionEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onShowPress(final MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp(final MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @return the allSatElems
     */
    public static final List<TLE> getAllSatElems() {
        return allSatElems;
    }

    /**
     * @param allSatElems the allSatElems to set
     */
    public static final void setAllSatElems(final List<TLE> allSatElems) {
        HamSatDroid.allSatElems = allSatElems;
    }

    /**
     * @param selectedPass the selectedPass to set
     */
    public static final void setSelectedPass(final SatPassTime selectedPass) {
        HamSatDroid.selectedPass = selectedPass;
    }

    /**
     * @param passes the passes to set
     */
    public static final void setPasses(final List<SatPassTime> passes) {
        HamSatDroid.passes = passes;
    }

    /**
     * @param groundStation the groundStation to set
     */
    public static final void setGroundStation(final GroundStationPosition groundStation) {
        HamSatDroid.groundStation = groundStation;
    }

    /**
     * @param passPredictor the passPredictor to set
     */
    public static final void setPassPredictor(final PassPredictor passPredictor) {
        HamSatDroid.passPredictor = passPredictor;
    }

    private static void setSelectedSatellite(final Satellite satellite) {
        HamSatDroid.selectedSatellite = satellite;
    }

    /**
     * @return the selectedPass
     */
    public static SatPassTime getSelectedPass() {
        return selectedPass;
    }

    /**
     * @return the groundStation
     */
    public static GroundStationPosition getGroundStation() {
        return groundStation;
    }

    /**
     * @return the selectedSatellite
     */
    public static Satellite getSelectedSatellite() {
        return selectedSatellite;
    }

    /**
     * @return the passPredictor
     */
    public static PassPredictor getPassPredictor() {
        return passPredictor;
    }

    /**
     * @return
     */
    public static List<SatPassTime> getPasses() {
        return HamSatDroid.passes;
    }

    /**
     * @param kepsSource the kepsSource to set
     */
    public static void setKepsSource(String kepsSource) {
        HamSatDroid.kepsSource = kepsSource;
    }

    /**
     * @return the kepsSource
     */
    public static String getKepsSource() {
        return kepsSource;
    }
}
