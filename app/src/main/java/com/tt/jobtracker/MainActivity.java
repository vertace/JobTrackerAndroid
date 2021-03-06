/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tt.jobtracker;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ApplicationErrorReport;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tt.data.Shared;
import com.tt.data.TaskLineItemPhotoViewModel;
import com.tt.data.TaskLineItemViewModel;
import com.tt.data.TaskViewModel;
import com.tt.enumerations.JobTrackerScreen;
import com.tt.enumerations.ServerResult;
import com.tt.fragments.DoneListFragment;
import com.tt.fragments.MapSingleShopFragment;
import com.tt.fragments.PendingListFragment;
import com.tt.fragments.SettingsFragment;
import com.tt.fragments.TaskDetailFragment;
import com.tt.fragments.TaskLineItemDetailFragment;
import com.tt.fragments.TaskLineItemFragment;
import com.tt.fragments.TaskListFragment;
import com.tt.helpers.CameraHelper;
import com.tt.helpers.DatabaseHelper;
import com.tt.helpers.Utility;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This example illustrates a common usage of the DrawerLayout widget in the
 * Android support library.
 * <p/>
 * <p>
 * When a navigation (left) drawer is present, the host activity should detect
 * presses of the action bar's Up affordance as a signal to open and close the
 * navigation drawer. The ActionBarDrawerToggle facilitates this behavior. Items
 * within the drawer should fall into one of two categories:
 * </p>
 * <p/>
 * <ul>
 * <li><strong>View switches</strong>. A view switch follows the same basic
 * policies as list or tab navigation in that a view switch does not create
 * navigation history. This pattern should only be used at the root activity of
 * a task, leaving some form of Up navigation active for activities further down
 * the navigation hierarchy.</li>
 * <li><strong>Selective Up</strong>. The drawer allows the user to choose an
 * alternate parent for Up navigation. This allows a user to jump across an
 * app's navigation hierarchy at will. The application should treat this as it
 * treats Up navigation from a different task, replacing the current task stack
 * using TaskStackBuilder or similar. This is the only form of navigation drawer
 * that should be used outside of the root activity of a task.</li>
 * </ul>
 * <p/>
 * <p>
 * Right side drawers should be used for actions, not navigation. This follows
 * the pattern established by the Action Bar that navigation should be to the
 * left and actions to the right. An action should be an operation performed on
 * the current contents of the window, for example enabling or disabling a data
 * overlay on top of the current content.
 * </p>
 */
public class MainActivity extends ActionBarActivity implements PendingListFragment.OnTaskSelected, DoneListFragment.OnTaskSelected, TaskDetailFragment.OnFragmentInteractionListener, TaskLineItemFragment.OnTaskLineItemSelected, TaskLineItemDetailFragment.OnTaskLineItemPhotoClickInitiated {
    public static FragmentManager fragmentManager;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mMenuTitles;
    private Menu menu;
    String stringLongitude;
    String stringLatitude;


    public String SearchText;

    LocationListener mlocListener;
    Context context;
    String regid;
    GoogleCloudMessaging gcm;
    public static final String PROPERTY_FbUserID = "fbUserID";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    public TaskLineItemViewModel taskLineItemViewModel;
DatabaseHelper dbHelper=new DatabaseHelper(this);
    public JobTrackerScreen CurrentScreen = JobTrackerScreen.TaskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
        if (savedInstanceState == null) {
            selectItem(0);
        }
        Shared.onbackpress=true;
       context = getApplicationContext();
        regid = getRegistrationId(context);
        Shared.LoggedInUser.GcmRegID = regid;
        if (regid.isEmpty()) {
            registerInBackground();
            GetRegID obj = new GetRegID(this);
            obj.execute();
        }

        final SharedPreferences taskSyncLogin = getSharedPreferences(Shared.TaskSync, 0);
        String taskDownloadLogin= taskSyncLogin.getString("tasksync", null);
        if(taskDownloadLogin=="True") {

        }

        Shared.admin_mian_activity=false;
        final SharedPreferences mainClassCall = getSharedPreferences(Shared.MainClassCall, 0);
        String status= mainClassCall.getString("mainClassCall", null);
        if(status!="True") {
            startService(new Intent(this, BackgroundService.class));

        }else if(status=="true")
        {
            SharedPreferences.Editor editor = mainClassCall.edit();
            editor.putString("mainClassCall", null); // Storing string
            editor.commit();
        }
        fragmentManager = getSupportFragmentManager();


    }


  /* private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ApplicationErrorReport.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassname())) {
                return true;
            }
        }
        return false;
    }*/



    private void storeRegistrationId(Context context, String regId,
                                     String fbUserId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_FbUserID, fbUserId);
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            return "";
        }
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
                Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            return "";
        }
//		sendRegistrationIdToBackend(registrationId, Shared.LoggedInUser.ID);

        return registrationId;
    }

    private SharedPreferences getGcmPreferences(Context context) {
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(Shared.GCM_SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    Shared.LoggedInUser.GcmRegID = regid;
                    //sendRegistrationIdToBackend(regid, Shared.LoggedInUser.ID);


                    storeRegistrationId(context, regid, Shared.LoggedInUser.ID);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {

            }
        }.execute(null, null, null);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public class GetRegID extends AsyncTask<String, Integer, ServerResult> {
        Context context;

        public GetRegID(Context _context) {
            context = _context;

        }

        public GetRegID(AsyncTask<Void, Void, String> asyncTask) {
            // TODO Auto-generated constructor stub
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected ServerResult doInBackground(String... params) {

            try {
                HttpURLConnection conn;
                String boundary = "*****";
                DataOutputStream dos = null;
                int serverResponseCode = 0;
                List<NameValuePair> postValue = new ArrayList<NameValuePair>(2);
                postValue.add(new BasicNameValuePair("RegID", String.valueOf(Shared.LoggedInUser.GcmRegID)));
                postValue.add(new BasicNameValuePair("UserID", String.valueOf(Shared.LoggedInUser.ID)));
                URL url = new URL(Shared.SaveRegistrationID + "?" + Utility.getQuery(postValue));

                // Open a HTTP connection to the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + boundary);
                int code = conn.getResponseCode();

                //	OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                //	dos = new DataOutputStream(conn.getOutputStream());
//				 writer.write("message=" + message);


                serverResponseCode = conn.getResponseCode();

                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                ex.printStackTrace();

            } catch (Exception e) {

                e.printStackTrace();

            }
            return ServerResult.LoginSuccess;
        }

        protected void onPostExecute(ServerResult result) {

        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        setContentView(R.layout.activity_main);
        SetNavigationDrawer();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            SearchText = intent.getStringExtra(SearchManager.QUERY);
        } else {
            SearchText = "";
        }
    }


    private void SetNavigationDrawer() {
        mTitle = mDrawerTitle = getTitle();
        mMenuTitles = getResources().getStringArray(R.array.array_menu_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, mMenuTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open, /* "open drawer" description for accessibility */
                R.string.drawer_close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(this.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));

       /* ImageView tickImg=(ImageView) menu.findItem(R.id.action_task_done);
        if(Shared.SelectedTask!=null)
        {
            int value = dbHelper.PhotoUploadCount(Shared.SelectedTask.ID);
            if(value==0)
            {
                tickImg.setImageResource(R.drawable.done);
                tickImg.setFocusable(true);
                tickImg.setFocusableInTouchMode(true);

            }
            else
            {
                tickImg.setFocusable(false);
                tickImg.setFocusableInTouchMode(false);
                tickImg.setImageResource(R.drawable.ic_launcher);
            }
        }*/

        SetActionBarMenuItems();
        return super.onCreateOptionsMenu(menu);
    }

    public void showTaskListMenu(boolean showMenu)
    {

    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content
        // view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);

        //menu.findItem(R.id.mnuSync).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.action_logout:
                // create intent to perform web search for this planet
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, getSupportActionBar().getTitle());
                // catch event that there's no activity to handle intent
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, R.string.app_name, Toast.LENGTH_LONG)
                            .show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void selectItem(int position) {

        // update the main content by replacing fragments
        Fragment fragment = null;

        if (position == 0) {
            showTaskListMenu(true);
            fragment = new TaskListFragment();
        } else if (position == 1) {
            fragment = new SettingsFragment();

        } else if (position == 2) {

            final SharedPreferences username = getApplicationContext().getSharedPreferences(Shared.Username, 0);
            final SharedPreferences password = getApplicationContext().getSharedPreferences(Shared.Password, 0);
            SharedPreferences.Editor editor = username.edit();
            editor.putString("Loginuser",null); // Storing string
            editor.commit();
            SharedPreferences.Editor editor1 = password.edit();
            editor1.putString("LoginPass",null); // Storing string
            editor1.commit();
            Intent intent;
            finish();
            Shared.LoggedInUser = null;
            intent = new Intent(MainActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
        }

        if (fragment != null) {
            Bundle args = new Bundle();
            fragment.setArguments(args);

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment).commit();
        }
        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mMenuTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            selectItem(position);
        }
    }

    public void SetActionBarMenuItems() {
        if (menu == null)
            return;

        menu.setGroupVisible(R.id.actionbar_group_home, false);
        menu.setGroupVisible(R.id.actionbar_group_taskdetail, false);
        menu.setGroupVisible(R.id.actionbar_group_tasklineitem_detail, false);
        menu.setGroupVisible(R.id.actionbar_Admin_group_home, false);


        switch (CurrentScreen) {
            case TaskList:
               /* if(Shared.hideMenu=="true")
                {
                    menu.setGroupVisible(R.id.actionbar_group_home, false);
                }
                else {*/
                menu.setGroupVisible(R.id.actionbar_group_home, true);
                //}
                break;
            case TaskDetail:
                if( Shared.hideMenu=="true")
                {
                    menu.setGroupVisible(R.id.actionbar_group_taskdetail, false);

                }
                else {

                    menu.setGroupVisible(R.id.actionbar_group_taskdetail, true);
                    if(Shared.SelectedTask!=null) {
                        int value = dbHelper.PhotoUploadCount(Shared.SelectedTask.ID);
                        if (value <= 0) {
                            MenuItem item = menu.findItem(R.id.action_task_done);
                            item.setVisible(true);
                            this.invalidateOptionsMenu();
                        }
                        else {
                            MenuItem item = menu.findItem(R.id.action_task_done);
                            item.setVisible(false);
                            this.invalidateOptionsMenu();
                        }
                    }
                }
                break;
            case TaskLineItemDetail:
                if( Shared.hideMenu=="true")
                {
                    menu.setGroupVisible(R.id.actionbar_group_tasklineitem_detail, false);
                }
                else {
                    menu.setGroupVisible(R.id.actionbar_group_tasklineitem_detail, true);
                }
                break;
            case Setting:
                break;
        }
    }

    public void onTaskSelected(TaskViewModel task)
    {
        Shared.hideMenu=String.valueOf(task.IsDone);


        if(task.IsMeasurement)
        {
            //call measurement fragment

            Shared.MeasurementTaskID = task.ID;
            Intent intent = new Intent(MainActivity.this,
                    TakeMeasurementList.class);
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            Shared.SelectedTask=dbHelper.getTaskInfo(String.valueOf(task.ID));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("TaskID", String.valueOf(task.ID));
            intent.putExtra("ShopID",
                    String.valueOf(task.ShopID));
            intent.putExtra("ShopName",
                    String.valueOf(task.ShopName));
            intent.putExtra("ShopAddress",
                    String.valueOf(task.ShopAddress));
            intent.putExtra("IsDone",
                    String.valueOf(task.IsDone));
            getApplicationContext().startActivity(intent);

            // Intent myIntent = new Intent(MainActivity.this, TakeMeasurement.class);
            //myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // myIntent.putExtra("PhotoID",taskLineItemViewModel.PhotoID);
            // getApplicationContext().startActivity(myIntent);
            // finish();

        }

        else {
            //call execution fragment

            Fragment fragment = new TaskDetailFragment();
            if (fragment != null) {
                Bundle args = new Bundle();
                args.putSerializable("Task", task);
                fragment.setArguments(args);

                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragment).addToBackStack("tasklist").commit();
            }
        }
    }


    TaskLineItemDetailFragment taskLineItemDetailFragment;

    @Override
    public void onTaskLineItemSelected(TaskLineItemViewModel taskLineItemViewModel) {

        taskLineItemDetailFragment = new TaskLineItemDetailFragment();
        if (taskLineItemDetailFragment != null) {
            Bundle args = new Bundle();
            args.putSerializable("TaskLineItem", taskLineItemViewModel);
            taskLineItemDetailFragment.setArguments(args);

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, taskLineItemDetailFragment).addToBackStack("taskdetail").commit();
        }

    }


    public void onTaskLineItemShopPhoto(TaskViewModel taskViewModel) {
        tvm=taskViewModel;
        tlvm = taskLineItemViewModel;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = CameraHelper.getOutputMediaFileUri(CameraHelper.MEDIA_TYPE_IMAGE); // create
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the
        startActivityForResult(intent, CAPTURE_TASKLINEITEM_SHOP_IMAGE_ACTIVITY_REQUEST_CODE);
    }


    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAPTURE_TASKLINEITEM_IMAGE_ACTIVITY_REQUEST_CODE = 200;
    private static final int CAPTURE_TASKLINEITEM_SHOP_IMAGE_ACTIVITY_REQUEST_CODE = 300;
    private Uri fileUri;
    private TaskLineItemViewModel tlvm;
    private TaskViewModel tvm;

    @Override
    public void onTaskLineItemPhotoClickInitiated(TaskLineItemViewModel taskLineItemViewModel) {

        tlvm = taskLineItemViewModel;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = CameraHelper.getOutputMediaFileUri(CameraHelper.MEDIA_TYPE_IMAGE); // create
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the
        startActivityForResult(intent, CAPTURE_TASKLINEITEM_IMAGE_ACTIVITY_REQUEST_CODE);

    }


  /*  @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (fileUri != null) {
            Shared.FileUri=fileUri;
            outState.putString("cameraImageUri", fileUri.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("cameraImageUri")) {
            fileUri = Uri.parse(savedInstanceState.getString("cameraImageUri"));
        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) { // if the intent data is not null, use it
//                    Toast.makeText(this, "Image saved to:\n" + data.getData(),
//                            Toast.LENGTH_LONG).show();

                } else { // Use the fileUri global variable
//                    Toast.makeText(this, "Image saved to:\n" + fileUri,
//                            Toast.LENGTH_LONG).show();
                }

            } else if (resultCode == RESULT_CANCELED) {
//                Toast.makeText(this, "Camera cancelled", Toast.LENGTH_LONG)
//                        .show();
            } else {
                // Image capture failed, advise user
            }
        }
        if (requestCode == CAPTURE_TASKLINEITEM_SHOP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
//                Toast.makeText(this, "Image saved to:\n" + fileUri,
//                        Toast.LENGTH_LONG).show();
                AddTaskShopPhoto();
                //taskLineItemDetailFragment.updateImageAdapter();
                //markTaskStart();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the video capture
            } else {
                // Video capture failed, advise user
            }
        }

        if (requestCode == CAPTURE_TASKLINEITEM_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
//                Toast.makeText(this, "Image saved to:\n" + fileUri,
//                        Toast.LENGTH_LONG).show();
                AddTaskLineItemPhoto();
                //Intent myIntent = new Intent(MainActivity.this, TakeMeasurement.class);
                //myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //myIntent.putExtra("PhotoID",taskLineItemViewModel.PhotoID);
                //getApplicationContext().startActivity(myIntent);
                //finish();

                taskLineItemDetailFragment.updateImageAdapter();
                //markTaskStart();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the video capture
            } else {
                // Video capture failed, advise user
            }
        }
    }

    private void AddTaskLineItemPhoto() {

        GPSTracker gpsTracker = new GPSTracker(this);
        if (gpsTracker.getIsGPSTrackingEnabled()) {
            stringLatitude = String.valueOf(gpsTracker.latitude);
            stringLongitude = String.valueOf(gpsTracker.longitude);
        }

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        TaskLineItemPhotoViewModel taskLineItemPhotoViewModel = new TaskLineItemPhotoViewModel();
        taskLineItemPhotoViewModel.PhotoID = fileUri.getPath();
        taskLineItemPhotoViewModel.TaskLineItemID = tlvm.ID;
        taskLineItemPhotoViewModel.Lat = stringLatitude;
        taskLineItemPhotoViewModel.Lon = stringLongitude;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        taskLineItemPhotoViewModel.Time = sdf.format(new Date());
        dbHelper.insertTaskLineItemPhoto(taskLineItemPhotoViewModel);
        updateTaskLineItemPhotoCount();
        //processFinish(null);
    }
    private void AddTaskShopPhoto() {
        GPSTracker gpsTracker = new GPSTracker(this);
        if (gpsTracker.getIsGPSTrackingEnabled()) {

            stringLatitude = String.valueOf(gpsTracker.latitude);
            stringLongitude = String.valueOf(gpsTracker.longitude);

        }
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        TaskViewModel taskViewModel = dbHelper.getTaskInfo(String.valueOf(tvm.ID));
        // TaskLineItemViewModel taskLineItemViewModel=new TaskLineItemViewModel();
        // TaskLineItemPhotoViewModel taskLineItemPhotoViewModel = new TaskLineItemPhotoViewModel();
        taskViewModel.PhotoID = fileUri.getPath();
        taskViewModel.ID = tvm.ID;
        taskViewModel.ActualLat=stringLatitude;
        taskViewModel.ActualLon=stringLongitude;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        taskViewModel.StartTime = sdf.format(new Date());
        taskViewModel.IsDone=false;
        dbHelper.saveTask(taskViewModel, true);



        //updateTaskLineItemPhotoCount();
        //processFinish(null);
    }

    private void updateTaskLineItemPhotoCount()
    {

        int photocount;
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        TaskLineItemViewModel taskilneItem=new TaskLineItemViewModel();
        TaskLineItemViewModel taskbeforePhotoCount= dbHelper.getTaskLineItemInfo(String.valueOf(tlvm.ID));
        photocount=taskbeforePhotoCount.TaskLineItemPhotoCount;
        taskilneItem.TaskLineItemPhotoCount=photocount+1;
        int result=dbHelper.updateTaskLineItem(taskilneItem,true);
        int cv=result;
    }
    private static long back_pressed;
    @Override
    public void onBackPressed() {
        if (Shared.onbackpress==true) {
            if (back_pressed + 2000 > System.currentTimeMillis()) {
                //    super.onBackPressed();
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
            } else {
                Toast.makeText(getBaseContext(), "Press once again to exit!", Toast.LENGTH_SHORT).show();
                back_pressed = System.currentTimeMillis();
            }
        }
        else
        {
            super.onBackPressed();
        }
    }

}