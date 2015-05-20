package com.tt.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tt.adapters.MeasurementPhotoListAdapter;
import com.tt.adapters.TaskDetailAdapter;
import com.tt.data.MeasurementPhoto;
import com.tt.data.Shared;
import com.tt.data.TaskLineItemPhotoViewModel;
import com.tt.data.TaskLineItemViewModel;
import com.tt.data.TaskNotDoneViewModel;
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
    MainActivity mainActivity = (MainActivity) getActivity();
    DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
    // Container Activity must implement this interface
    public interface OnTaskLineItemSelected {
        public void onTaskLineItemSelected(TaskLineItemViewModel taskLineItemViewModel);
        public void onTaskLineItemShopPhoto(TaskViewModel taskViewModel);
    }
    String Items[];
    String Photocount[];
    TaskViewModel task;
    TaskLineItemViewModel taskLineItemViewModel;
    ListView listView;

    private ListAdapter mAdapter;
String value;
    String notDoneReason;
    public TaskLineItemFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        task=Shared.SelectedTask;
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.task = (TaskViewModel) bundle.get("Task");
        }
        Shared.SelectedTask=task;
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
        TextView minimumPhoto = (TextView) view.findViewById(R.id.MinimumPhoto);
        ImageView shopphoto=(ImageView) view.findViewById(R.id.ShopPhoto);
        branchname.setText(task.ShopBranch.toString());
        taskName.setText(task.Name.toString());
        taskAddress.setText(task.ShopAddress.toString());
        minimumPhoto.setText("Minimum Photo: "+task.MinimumPhoto);

        ShowTaskLineItems();
        setHasOptionsMenu(true);
        mainActivity.CurrentScreen = JobTrackerScreen.TaskDetail;
        mainActivity.SetActionBarMenuItems();
        super.onCreate(savedInstanceState);
        shopphoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                MainActivity mainActivity = (MainActivity) getActivity();
                DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
                Dialog settingsDialog = new Dialog(getActivity());
                settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                View viewDialog=getActivity().getLayoutInflater().inflate(R.layout.image_layout, null);
                settingsDialog.setContentView(viewDialog);
                ImageView imgShop=(ImageView)viewDialog.findViewById(R.id.ShopImage);
                TaskViewModel task=dbHelper.getTaskInfo(String.valueOf(Shared.SelectedTask.ID));
                Bitmap bmp = BitmapFactory.decodeFile(task.PhotoID);

                imgShop.setImageBitmap(bmp);
                settingsDialog.show();
            }
        });
        return view;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {

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
            case R.id.action_task_notdone_TaskLineItem:
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
        TaskLineItemViewModel tltest=dbHelper.getTaskLineItemInfo(String.valueOf(Shared.SelectedTask.ID));
        TaskLineItemPhotoViewModel test=dbHelper.TestVa(1);
        //  TaskLineItemViewModel task=dbHelper.getTaskLineItemInfo(String.valueOf(t));
        // final ArrayList<String> imageList = dbHelper.getAllTaskLineItemPhotoUri(String.valueOf(task.ID));

        //List <TaskLineItemViewModel> tasklineitems=dbHelper.getTaskLineItems(" TaskID = " + String.valueOf(task.ID));
        int value=dbHelper.PhotoUploadCount(task.ID);
        if(value<=0)
        {
            alertdailogue();
        }
        else
        {

            Toast.makeText(getActivity(), " Take Minimum Photo", Toast.LENGTH_LONG).show();
        }
    }
    public void alertdailogue()
    {
        MainActivity mainActivity = (MainActivity) getActivity();
        final DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
        final SharedPreferences mainClassCall = getActivity().getSharedPreferences(Shared.MainClassCall, 0);
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
public void TaskNotDone()
{
    AlertDialog levelDialog;

    List<TaskNotDoneViewModel> taskNotDone;
    MainActivity mainActivity = (MainActivity) getActivity();
    final DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
   taskNotDone=dbHelper.getAllTaskNotDone();
    Items=new String[taskNotDone.size()];
    for(int i=0;i<taskNotDone.size();i++)
    {
        Items[i]=new String();
      Items[i]=taskNotDone.get(i).title;
    }
  //  final CharSequence[] items = {" Structural Damage ", " NoSpace ", " MaterialDamage ", " MaterialMissing "};
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle("Select The Reason");
    builder.setSingleChoiceItems(Items, -1, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int item) {
                notDoneReason= Items[item];

            notDoneTaskLineItem();
           // levelDialog.dismiss();
        }
    });
    levelDialog = builder.create();
    levelDialog.show();

}

    private void notDoneTaskLineItem() {
        final SharedPreferences mainClassCall = getActivity().getSharedPreferences(Shared.MainClassCall, 0);
        MainActivity mainActivity = (MainActivity) getActivity();
        DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
        Shared.TaskDetail = dbHelper.getTaskLineItems(" TaskID = " + String.valueOf(task.ID));
        for(TaskLineItemViewModel tasklineitem:Shared.TaskDetail) {
            TaskLineItemPhotoViewModel taskLineItemPhotoViewModel = new TaskLineItemPhotoViewModel();
            taskLineItemPhotoViewModel.PhotoID = "NOT_DONE";
            taskLineItemPhotoViewModel.NotDoneReason = notDoneReason;
            taskLineItemPhotoViewModel.TaskLineItemID = tasklineitem.ID;
            taskLineItemPhotoViewModel.Lat = String.valueOf(Shared.lat);
            taskLineItemPhotoViewModel.Lon = String.valueOf(Shared.lon);
            taskLineItemPhotoViewModel.NotDone=true;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            taskLineItemPhotoViewModel.Time = sdf.format(new Date());
            dbHelper.saveTaskLineItemPhotos(taskLineItemPhotoViewModel);


            TaskLineItemViewModel selectedTaskLineItem = new TaskLineItemViewModel();
            selectedTaskLineItem.Uri = Uri.EMPTY;
            selectedTaskLineItem.Lat = String.valueOf(Shared.lat);
            selectedTaskLineItem.Lon = String.valueOf(Shared.lon);
            selectedTaskLineItem.SaveToDB = true;
            selectedTaskLineItem.Time = sdf.format(new Date());

            selectedTaskLineItem.PhotoID = "NOT_DONE";
    /*    if (selectedTaskLineItem.OldImage == null
                || selectedTaskLineItem.OldImage.isEmpty()) {
            selectedTaskLineItem.OldTakenNow = true;
            selectedTaskLineItem.OldImage = "NOT_DONE";
        }*/
            selectedTaskLineItem.NewImage = "NOT_DONE";
            selectedTaskLineItem.NotDoneReason = notDoneReason;

            dbHelper.saveTaskLineItem(selectedTaskLineItem, true);
        }
        Shared.SelectedTask=dbHelper.getTaskInfo(String.valueOf(task.ID));
        alertdailogue();
    }


    private void ShowTaskLineItems() {
        MainActivity mainActivity = (MainActivity) getActivity();
        DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
        String condition="TaskID="+task.ID;
      ArrayList<TaskLineItemViewModel> taskLineItem=dbHelper.getTaskLineItems(condition);
        Photocount=new String[taskLineItem.size()];
        for(int i=0;i<taskLineItem.size();i++)
        {
            Photocount[i]=new String();
            final ArrayList<String> imageList = dbHelper.getAllTaskLineItemPhotoUri(String.valueOf(taskLineItem.get(i).ID));
            int size=imageList.size();
            Photocount[i]=String.valueOf(size);
        }
        Shared.SharedPhotocount=Photocount;
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
        }
        else if(!taskForShop.IsShopPhoto)
        {
            mCallback.onTaskLineItemSelected(taskLineItemViewModel);
        }
        else
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
