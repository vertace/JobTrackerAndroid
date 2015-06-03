package com.tt.jobtracker;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.Toast;

import com.tt.data.Shared;
import com.tt.data.TaskLineItemViewModel;
import com.tt.data.TaskViewModel;
import com.tt.enumerations.JobTrackerScreen;
import com.tt.fragments.PendingListFragment;
import com.tt.fragments.RFEListFragment;
import com.tt.fragments.RfeTaskListFragment;
import com.tt.fragments.SettingsFragment;
import com.tt.fragments.TaskDetailFragment;
import com.tt.fragments.TaskLineItemDetailFragment;
import com.tt.fragments.TaskLineItemFragment;
import com.tt.fragments.TaskListFragment;
import com.tt.helpers.CameraHelper;
import com.tt.helpers.DatabaseHelper;

import java.util.logging.Handler;

/**
 * Created by BS-308 on 5/6/2015.
 */
public class Admin_MainActivity extends ActionBarActivity implements RfeTaskListFragment.OnTaskSelected,TaskDetailFragment.OnFragmentInteractionListener,TaskLineItemFragment.OnTaskLineItemSelected, TaskLineItemDetailFragment.OnTaskLineItemPhotoClickInitiated{
    public static FragmentManager fragmentManager;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mMenuTitles;
    private Menu menu;
    public TaskLineItemViewModel taskLineItemViewModel;
    public String SearchText;

    public JobTrackerScreen CurrentScreen = JobTrackerScreen.TaskList;
    Context context;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
        if (savedInstanceState == null) {
            selectItem(0);
        }
        Shared.onbackpress=true;
        fragmentManager = getSupportFragmentManager();
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
        mMenuTitles = getResources().getStringArray(R.array.admin_array_menu_main);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(this.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_RFE_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));


        SetActionBarMenuItems();
        return super.onCreateOptionsMenu(menu);
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

    public void onFragmentInteraction(Uri uri)
    {

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
                menu.setGroupVisible(R.id.actionbar_Admin_group_home, true);
                break;
            case TaskDetail:
                    menu.setGroupVisible(R.id.actionbar_group_taskdetail, false);
                break;
            case TaskLineItemDetail:
                    menu.setGroupVisible(R.id.actionbar_group_tasklineitem_detail, false);
                break;
            case Setting:
                break;
        }
    }
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
            // showTaskListMenu(true);
            fragment = new RFEListFragment();
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
            intent = new Intent(Admin_MainActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
        }



        if (fragment != null) {
            Bundle args = new Bundle();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment).commit();
        }
        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mMenuTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }
    public void onTaskSelected(TaskViewModel task)
    {
        Shared.hideMenu=String.valueOf(task.IsDone);


        if(task.IsMeasurement)
        {
            //call measurement fragment

            Shared.MeasurementTaskID = task.ID;
            Intent intent = new Intent(Admin_MainActivity.this,
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
