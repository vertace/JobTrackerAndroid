package com.tt.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TaskLineItemFragment extends ListFragment {

    OnTaskLineItemSelected mCallback;

    // Container Activity must implement this interface
    public interface OnTaskLineItemSelected {
        public void onTaskLineItemSelected(TaskLineItemViewModel taskLineItemViewModel);
    }

    TaskViewModel task;
    String notDoneReason;
    String value;
    private Uri fileUri;

    private ListAdapter mAdapter;

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
        taskName.setText(task.ShopName.toString());
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

            case R.id.action_task_notdone:
               final AlertDialog levelDialog;

// Strings to Show In Dialog with Radio Buttons
                final CharSequence[] items = {" Material Damaged "," Material not Reached "," Structure Damage "," No Space "};

                // Creating and Building the Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Select The Not Done Reason");
                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {

                   notDoneReason="";
                        switch (item) {
                            case 0:
                                 value="Material Damaged";
                                break;
                            case 1:
                                 value="Material not Reached";
                                break;
                            case 2:
                                 value="Structure Damage";
                                break;
                            case 3:
                                 value="No Space";
                                break;

                        }
                        notDoneReason=value;
                        notDoneTaskLineItem();
                        //levelDialog.dismiss();
                    }
                });

                levelDialog = builder.create();
                levelDialog.show();
             //   Toast.makeText(getActivity(), " Take Mi", Toast.LENGTH_LONG).show();
                break;



            default:
                return super.onOptionsItemSelected(item);
        }


        return false;
    }

    private void moveto_donetask() {
        MainActivity mainActivity = (MainActivity) getActivity();
        final DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
        Shared.TaskDetail = dbHelper.getTaskLineItems(" TaskID = " + String.valueOf(task.ID));
        Shared.SelectedTask=dbHelper.getTaskInfo(String.valueOf(task.ID));
        //  TaskLineItemViewModel task=dbHelper.getTaskLineItemInfo(String.valueOf(call));
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
                            TaskViewModel taskViewModel = Shared.SelectedTask;

                            taskViewModel.IsDone = true;
                            dbHelper.saveTask(taskViewModel, true);
                            Toast.makeText(getActivity(), "Moved to done task", Toast.LENGTH_LONG).show();
                            Intent myIntent = new Intent(getActivity(), MainActivity.class);
                            getActivity().startActivity(myIntent);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            Intent myIntent = new Intent(getActivity(), MainActivity.class);
                            getActivity().startActivity(myIntent);
                        }
                    })      //Do nothing on no
                    .show();

        }
        else
        {

            Toast.makeText(getActivity(), " Take Minimum Photo", Toast.LENGTH_LONG).show();
        }
    }
    private void notDoneTaskLineItem() {
         TaskLineItemViewModel selectedTaskLineItem=new TaskLineItemViewModel();
        MainActivity mainActivity = (MainActivity) getActivity();
        DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
        Shared.SelectedTask=dbHelper.getTaskInfo(String.valueOf(task.ID));
        selectedTaskLineItem.Uri = Uri.EMPTY;
        selectedTaskLineItem.Lat = String.valueOf(Shared.lat);
        selectedTaskLineItem.Lon = String.valueOf(Shared.lon);
        selectedTaskLineItem.SaveToDB = true;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        selectedTaskLineItem.Time = sdf.format(new Date());

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        selectedTaskLineItem.PhotoID = "NOT_DONE";
      /*  if (selectedTaskLineItem.OldImage == null
                || selectedTaskLineItem.OldImage.isEmpty()) {
            selectedTaskLineItem.OldTakenNow = true;
            selectedTaskLineItem.OldImage = "NOT_DONE";
        }*/
        selectedTaskLineItem.NewImage = "NOT_DONE";
        selectedTaskLineItem.NotDoneReason = notDoneReason;
        dbHelper.saveTaskLineItem(selectedTaskLineItem, true);
        Shared.TaskDetail = dbHelper.getTaskLineItems(" TaskID = "+ String.valueOf(selectedTaskLineItem.TaskID));
        TaskViewModel taskViewModel = Shared.SelectedTask;

        taskViewModel.IsDone = true;
        dbHelper.saveTask(taskViewModel, true);
        Toast.makeText(getActivity(), "Moved to done task", Toast.LENGTH_LONG).show();
        Intent myIntent = new Intent(getActivity(), MainActivity.class);
        getActivity().startActivity(myIntent);
      // getActivity().processFinish(null);
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

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        TaskLineItemViewModel taskLineItemViewModel = Shared.TaskDetail.get(position);
        mCallback.onTaskLineItemSelected(taskLineItemViewModel);

        super.onListItemClick(l, v, position, id);
    }

}
