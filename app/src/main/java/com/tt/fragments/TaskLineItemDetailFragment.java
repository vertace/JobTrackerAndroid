package com.tt.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.tt.adapters.ImageAdapter;
import com.tt.data.Shared;
import com.tt.data.TaskLineItemImageListViewModel;
import com.tt.data.TaskLineItemNotDoneViewModel;
import com.tt.data.TaskLineItemPhotoViewModel;
import com.tt.data.TaskLineItemViewModel;
import com.tt.enumerations.JobTrackerScreen;
import com.tt.helpers.DatabaseHelper;
import com.tt.jobtracker.Admin_MainActivity;
import com.tt.jobtracker.FullScreenImageViewActivity;
import com.tt.jobtracker.MainActivity;
import com.tt.jobtracker.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskLineItemDetailFragment extends Fragment {

    String value;
    String notDoneReason;
    private OnTaskLineItemPhotoClickInitiated mCallback;
    ImageAdapter adapter;
    TextView Walltype;
    TextView WallDetail;
    TextView PhotoCount;
    AlertDialog levelDialog;
    String Items[];
    MainActivity mainActivity = (MainActivity) getActivity();
    final DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
    public static ArrayList<String> imageArray;


    public interface OnTaskLineItemPhotoClickInitiated {
        void onTaskLineItemPhotoClickInitiated(TaskLineItemViewModel taskLineItemViewModel);

    }

    TaskLineItemViewModel taskLineItemViewModel;

    public TaskLineItemDetailFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.taskLineItemViewModel = (TaskLineItemViewModel) bundle.get("TaskLineItem");
        }


    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_tasklineitem, container, false);
        Shared.onbackpress=false;
        if(Shared.admin_mian_activity==false) {
            MainActivity mainActivity = (MainActivity) getActivity();
            DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);

            Walltype = (TextView) view.findViewById(R.id.wallType);
            WallDetail = (TextView) view.findViewById(R.id.wallDetail);
            PhotoCount = (TextView) view.findViewById(R.id.status);
            TaskLineItemViewModel tasklineitemviewmodel = new TaskLineItemViewModel();
            final ArrayList<String> imageListsize = dbHelper.getAllTaskLineItemPhotoUri(String.valueOf(taskLineItemViewModel.ID));
            int size = imageListsize.size();
            tasklineitemviewmodel = dbHelper.getTaskLineItemInfo(String.valueOf(taskLineItemViewModel.ID));
            Walltype.setText(tasklineitemviewmodel.Type.toString());
            // Walltype.setText(String.valueOf(tasklineitemviewmodel.Type));
            WallDetail.setText(tasklineitemviewmodel.Instruction.toString());
            if (size > 0) {
                if (imageListsize.get(0).equals("NOT_DONE")) {
                    PhotoCount.setText("");
                } else {
                    PhotoCount.setText(size + "/" + Shared.SelectedTask.MinimumPhoto);
                }
            } else {
                PhotoCount.setText(size + "/" + Shared.SelectedTask.MinimumPhoto);
            }


//        Button button = (Button) view.findViewById(R.id.btnTakePhoto);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mCallback.onTaskLineItemPhotoClickInitiated(taskLineItemViewModel);
//            }
//
//        });

            GridView gridview = (GridView) view.findViewById(R.id.gridview);
            adapter = new ImageAdapter(mainActivity);
            gridview.setAdapter(adapter);
            final ArrayList<String> imageList = dbHelper.getAllTaskLineItemPhotoUri(String.valueOf(taskLineItemViewModel.ID));
            imageArray=imageList;
            Shared.imagelisttasklineitemdetail=imageList;
            adapter.addAll(imageList);

           /* gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), FullScreenImageViewActivity.class);
                    intent.putExtra("ImageList", imageList.toArray());
                    intent.putExtra("Position", position);
                    startActivity(intent);
                }
            });*/
            mainActivity.CurrentScreen = JobTrackerScreen.TaskLineItemDetail;
            mainActivity.SetActionBarMenuItems();
        }
        else
        {
            Admin_MainActivity mainActivity = (Admin_MainActivity) getActivity();
            DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
            Walltype = (TextView) view.findViewById(R.id.wallType);
            WallDetail = (TextView) view.findViewById(R.id.wallDetail);
            PhotoCount = (TextView) view.findViewById(R.id.status);
            TaskLineItemViewModel tasklineitemviewmodel = new TaskLineItemViewModel();
            int size = Shared.TaskLineitemImageList.size();
            tasklineitemviewmodel = dbHelper.getTaskLineItemInfo(String.valueOf(taskLineItemViewModel.ID));
            Walltype.setText(tasklineitemviewmodel.Type.toString());
            // Walltype.setText(String.valueOf(tasklineitemviewmodel.Type));
            WallDetail.setText(tasklineitemviewmodel.Instruction.toString());
            PhotoCount.setText(size + "/" + Shared.SelectedTask.MinimumPhoto);
            GridView gridview = (GridView) view.findViewById(R.id.gridview);
            adapter = new ImageAdapter(mainActivity);
            gridview.setAdapter(adapter);
            int i=0;
           imageArray=new ArrayList<String>();
           for(TaskLineItemImageListViewModel taskLineItemImage:Shared.TaskLineitemImageList) {

                String url ="http://sunsigns.blob.core.windows.net/cdn/Images/WallImages/"+Shared.TaskLineitemImageList.get(0).ShopID+"/_"+Shared.TaskLineitemImageList.get(0).ShopWallID+"/Thumb_"+taskLineItemImage.ImageName;
               imageArray.add(url);
                adapter.add(url);
            }
          /*  gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Dialog settingsDialog = new Dialog(getActivity());
                    View viewDialog = getActivity().getLayoutInflater().inflate(R.layout.image_layout, null);
                    settingsDialog.setContentView(viewDialog);
                    ImageView imgShop = (ImageView) viewDialog.findViewById(R.id.ShopImage);
                    UrlImageViewHelper.setUrlDrawable(imgShop, "http://sunsigns.blob.core.windows.net/cdn/Images/WallImages/"+Shared.TaskLineitemImageList.get(0).ShopID+"/_"+Shared.TaskLineitemImageList.get(0).ShopWallID+"/Thumb_"+Shared.TaskLineitemImageList.get(position).ImageName);
                    settingsDialog.show();
                }
            });*/
            mainActivity.CurrentScreen = JobTrackerScreen.TaskLineItemDetail;
            mainActivity.SetActionBarMenuItems();
        }
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        return view;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_tasklineitem_takephoto:
                takephoto_TaskDeatilLineItem();
                return true;
            case R.id.action_task_notdone:
                TaskNotDone();
                break;

            case R.id.mnuMap:


                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }
    public void takephoto_TaskDeatilLineItem() {
        mCallback.onTaskLineItemPhotoClickInitiated(taskLineItemViewModel);
    }

    public void TaskNotDone()
    {


// Strings to Show In Dialog with Radio Buttons
        List<TaskLineItemNotDoneViewModel> taskNotDone;
        MainActivity mainActivity = (MainActivity) getActivity();
        final DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
        taskNotDone=dbHelper.getAllTaskLineItemNotDone();
        Items=new String[taskNotDone.size()];
        for(int i=0;i<taskNotDone.size();i++)
        {
            Items[i]=new String();
            Items[i]=taskNotDone.get(i).LineItemtitle;
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
        MainActivity mainActivity = (MainActivity) getActivity();
        DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
        TaskLineItemPhotoViewModel taskLineItemPhotoViewModel = new TaskLineItemPhotoViewModel();
        taskLineItemPhotoViewModel.PhotoID = "NOT_DONE";
        taskLineItemPhotoViewModel.NotDoneReason = notDoneReason;
        taskLineItemPhotoViewModel.TaskLineItemID = taskLineItemViewModel.ID;
        taskLineItemPhotoViewModel.Lat = String.valueOf(Shared.lat);
        taskLineItemPhotoViewModel.Lon = String.valueOf(Shared.lon);
        taskLineItemPhotoViewModel.NotDone=true;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        taskLineItemPhotoViewModel.Time = sdf.format(new Date());
        dbHelper.saveTaskLineItemPhotos(taskLineItemPhotoViewModel);

        TaskLineItemViewModel selectedTaskLineItem=dbHelper.getTaskLineItemInfo(String.valueOf(taskLineItemViewModel.ID));
        selectedTaskLineItem.Uri = Uri.EMPTY;
        selectedTaskLineItem.ID=taskLineItemViewModel.ID;
        selectedTaskLineItem.TaskID=Shared.SelectedTask.ID;
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
        levelDialog.dismiss();
        alertdailogue();


      /*  TaskLineItemFragment taskLineItemFragment = new TaskLineItemFragment();
        Bundle args = new Bundle();
        args.putSerializable("Task", Shared.SelectedTask);
        taskLineItemFragment.setArguments(args);*/

       /* TaskLineItemFragment taskLineItemFragment = new TaskLineItemFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, taskLineItemFragment);
                fragmentTransaction.commit();
        */

    }
    public void alertdailogue()
    {
        final SharedPreferences mainClassCall = getActivity().getSharedPreferences(Shared.MainClassCall, 0);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle("Alert")
                .setMessage("Are you sure to want to do this?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Yes button clicked, do something
                        Toast.makeText(getActivity(), " Reason Added", Toast.LENGTH_LONG).show();
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
    public void updateImageAdapter() {
        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
        final ArrayList<String> imageList = dbHelper.getAllTaskLineItemPhotoUri(String.valueOf(taskLineItemViewModel.ID));
        imageArray=imageList;
        adapter.clear();
        adapter.addAll(imageList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnTaskLineItemPhotoClickInitiated) activity;
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

}
