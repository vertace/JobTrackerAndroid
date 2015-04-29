package com.tt.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tt.adapters.TaskDetailAdapter;
import com.tt.data.Shared;
import com.tt.data.TaskLineItemViewModel;
import com.tt.data.TaskViewModel;
import com.tt.enumerations.JobTrackerScreen;
import com.tt.helpers.DatabaseHelper;
import com.tt.jobtracker.MainActivity;
import com.tt.jobtracker.MapForSingleShop;
import com.tt.jobtracker.R;
import com.tt.jobtracker.TakeMeasurementList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TaskLineItemFragment extends ListFragment {

    OnTaskLineItemSelected mCallback;
    //onTaskLineItemShopPhoto mCallback;

    // Container Activity must implement this interface
    public interface OnTaskLineItemSelected {
        public void onTaskLineItemSelected(TaskLineItemViewModel taskLineItemViewModel);
        public void onTaskLineItemShopPhoto(TaskViewModel taskViewModel);
    }

    TaskViewModel task;
    TaskLineItemViewModel taskLineItemViewModel;

    private ListAdapter mAdapter;
String value;
    String notDoneReason;
    public TaskLineItemFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.task = (TaskViewModel) bundle.get("Task");
        }
        ShowTaskLineItems();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity mainActivity = (MainActivity) getActivity();
        View view = inflater.inflate(R.layout.fragment_list_tasklineitem, container, false);

        TextView taskName = (TextView) view.findViewById(R.id.ShopName);
        TextView taskAddress = (TextView) view.findViewById(R.id.ShopAddress);
        TextView branchname = (TextView) view.findViewById(R.id.Branch);
        branchname.setText(task.ShopBranch.toString());
        taskName.setText(task.Name.toString());
        taskAddress.setText(task.ShopAddress.toString());

        ShowTaskLineItems();
        setHasOptionsMenu(true);
        mainActivity.CurrentScreen = JobTrackerScreen.TaskDetail;
        mainActivity.SetActionBarMenuItems();

        super.onCreate(savedInstanceState);
        return view;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_task_done:
                moveto_donetask();
                return true;
            case R.id.action_task_shopmap:
                MainActivity mainActivity = (MainActivity) getActivity();
                DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
                Shared.SelectedTask=dbHelper.getTaskInfo(String.valueOf(task.ID));
                Intent myIntent = new Intent(getActivity(), MapForSingleShop.class);
                getActivity().startActivity(myIntent);

              /*MapSingleShopFragment fragment2 = new MapSingleShopFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.list_item, fragment2);
                fragmentTransaction.commit();*/
                break;

            case R.id.mnuMap:

                break;
            case R.id.action_task_notdone:
              TaskNotDone();
                break;
            case R.id.action_task_takephoto:
                if(task.IsShopPhoto)
                {
                    takephoto_TaskShopPhoto();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("No need Shop photo.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do things
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }

                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }

    private void moveto_donetask() {
        final SharedPreferences mainClassCall = getActivity().getSharedPreferences(Shared.MainClassCall, 0);
        MainActivity mainActivity = (MainActivity) getActivity();
        final DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
        Shared.TaskDetail = dbHelper.getTaskLineItems(" TaskID = " + String.valueOf(task.ID));
        Shared.SelectedTask=dbHelper.getTaskInfo(String.valueOf(task.ID));
        //  TaskLineItemViewModel task=dbHelper.getTaskLineItemInfo(String.valueOf(t));
        // final ArrayList<String> imageList = dbHelper.getAllTaskLineItemPhotoUri(String.valueOf(task.ID));

        //List <TaskLineItemViewModel> tasklineitems=dbHelper.getTaskLineItems(" TaskID = " + String.valueOf(task.ID));
        if(dbHelper.PhotoUploadCount(task.ID)<=0)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder
                    .setTitle("Alert")
                    .setMessage("Are you sure to mark this as complete?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Yes button clicked, do something
                            SharedPreferences.Editor editor = mainClassCall.edit();
                            editor.putString("mainClassCall", "True"); // Storing string
                            editor.commit();
                            TaskViewModel taskViewModel = Shared.SelectedTask;

                            taskViewModel.IsDone = true;

                            dbHelper.saveTask(taskViewModel, true);
                            Toast.makeText(getActivity(), " Move to Done list", Toast.LENGTH_LONG).show();
                            Intent myIntent = new Intent(getActivity(), MainActivity.class);
                            getActivity().startActivity(myIntent);
                        }
                    })
                    .setNegativeButton("No",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            SharedPreferences.Editor editor = mainClassCall.edit();
                            editor.putString("mainClassCall", "True"); // Storing string
                            editor.commit();
                            Intent myIntent = new Intent(getActivity(), MainActivity.class);
                            getActivity().startActivity(myIntent);
                        }
                    })						//Do nothing on no
                    .show();

        }
        else
        {

            Toast.makeText(getActivity(), " Take Minimum Photo", Toast.LENGTH_LONG).show();
        }
    }
public void TaskNotDone()
{
    AlertDialog levelDialog;

// Strings to Show In Dialog with Radio Buttons
    final CharSequence[] items = {" StructuralDamage "," NoSpace "," MaterialDamage "," MaterialMissing "};
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle("Select The Reason");
    builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int item) {


            switch(item)
            {
                case 0:
                  value="StructuralDamage";
                    break;
                case 1:
                    value="NoSpace";

                    break;
                case 2:
                    value="MaterialDamage";
                    break;
                case 3:
                    value="MaterialMissing";
                    break;

            }
            notDoneReason=value;
            notDoneTaskLineItem();
           // levelDialog.dismiss();
        }
    });
    levelDialog = builder.create();
    levelDialog.show();

}

    private void notDoneTaskLineItem() {
        final SharedPreferences mainClassCall = getActivity().getSharedPreferences(Shared.MainClassCall, 0);
        TaskLineItemViewModel selectedTaskLineItem=new TaskLineItemViewModel();
        selectedTaskLineItem.Uri = Uri.EMPTY;
        selectedTaskLineItem.Lat = String.valueOf(Shared.lat);
        selectedTaskLineItem.Lon = String.valueOf(Shared.lon);
        selectedTaskLineItem.SaveToDB = true;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        selectedTaskLineItem.Time = sdf.format(new Date());

        selectedTaskLineItem.PhotoID = "NOT_DONE";
    /*    if (selectedTaskLineItem.OldImage == null
                || selectedTaskLineItem.OldImage.isEmpty()) {
            selectedTaskLineItem.OldTakenNow = true;
            selectedTaskLineItem.OldImage = "NOT_DONE";
        }*/
        selectedTaskLineItem.NewImage = "NOT_DONE";
        selectedTaskLineItem.NotDoneReason = notDoneReason;
        MainActivity mainActivity = (MainActivity) getActivity();
        DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
        dbHelper.saveTaskLineItem(selectedTaskLineItem, true);
        Shared.SelectedTask=dbHelper.getTaskInfo(String.valueOf(task.ID));
        TaskViewModel taskViewModel = Shared.SelectedTask;
        taskViewModel.IsDone = true;
        dbHelper.saveTask(taskViewModel, true);

        Toast.makeText(getActivity(), " Move to Done list", Toast.LENGTH_LONG).show();
        SharedPreferences.Editor editor = mainClassCall.edit();
        editor.putString("mainClassCall", "True"); // Storing string
        editor.commit();
        Intent myIntent = new Intent(getActivity(), MainActivity.class);
        getActivity().startActivity(myIntent);

    }


    private void ShowTaskLineItems() {
        MainActivity mainActivity = (MainActivity) getActivity();
        DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
        mainActivity.CurrentScreen = JobTrackerScreen.TaskDetail;
        mainActivity.SetActionBarMenuItems();
        TaskDetailAdapter taskDetailAdapter = new TaskDetailAdapter(mainActivity, R.layout.row_tasklineitem);
        ListView lstTaskDetail = (ListView) getActivity().findViewById(R.id.lstTaskDetail);
        setListAdapter(taskDetailAdapter);
        Shared.TaskDetail = dbHelper.getTaskLineItems(" TaskID = "
                + String.valueOf(task.ID));

        taskDetailAdapter.addAll(Shared.TaskDetail);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnTaskLineItemSelected) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }
    public void takephoto_TaskShopPhoto() {
        mCallback.onTaskLineItemShopPhoto(task);

    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        TaskLineItemViewModel taskLineItemViewModel = Shared.TaskDetail.get(position);
        MainActivity mainActivity = (MainActivity) getActivity();
        DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
        TaskViewModel taskForShop=dbHelper.getTaskInfo(String.valueOf(task.ID));
        if(taskForShop.IsShopPhoto && taskForShop.PhotoID!=null) {
            mCallback.onTaskLineItemSelected(taskLineItemViewModel);
        }else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Take shop photo first.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

        super.onListItemClick(l, v, position, id);
    }

}
