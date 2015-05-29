package com.tt.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tt.adapters.MeasurementPhotoListAdapter;
import com.tt.adapters.TaskDetailAdapter;
import com.tt.data.MeasurementPhoto;
import com.tt.data.RfeViewModel;
import com.tt.data.Shared;
import com.tt.data.TaskLineItemImageListViewModel;
import com.tt.data.TaskLineItemPhotoViewModel;
import com.tt.data.TaskLineItemViewModel;
import com.tt.data.TaskListResponse;
import com.tt.data.TaskNotDoneViewModel;
import com.tt.data.TaskViewModel;
import com.tt.enumerations.JobTrackerScreen;
import com.tt.enumerations.ServerResult;
import com.tt.helpers.CustomHttpClient;
import com.tt.helpers.DatabaseHelper;
import com.tt.helpers.SstAlert;
import com.tt.jobtracker.Admin_MainActivity;
import com.tt.jobtracker.MainActivity;
import com.tt.jobtracker.MapForSingleShop;
import com.tt.jobtracker.R;
import com.tt.jobtracker.TakeMeasurementList;
import com.tt.sync.SyncHelperExecution;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TaskLineItemFragment extends ListFragment {

    OnTaskLineItemSelected mCallback;
    //onTaskLineItemShopPhoto mCallback;
    MainActivity mainActivity;

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
    TaskViewModel taskforshopphoto;

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
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        ShowTaskLineItems();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_tasklineitem, container, false);
        if(Shared.admin_mian_activity==true) {
            Admin_MainActivity mainActivity = (Admin_MainActivity) getActivity();
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
                    Admin_MainActivity mainActivity = (Admin_MainActivity) getActivity();
                    DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
                    taskforshopphoto=dbHelper.getTaskInfo(String.valueOf(Shared.SelectedTask.ID));
                    if(taskforshopphoto.PhotoID!=null) {
                        Dialog settingsDialog = new Dialog(getActivity());
                        settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                        View viewDialog = getActivity().getLayoutInflater().inflate(R.layout.image_layout, null);
                        settingsDialog.setContentView(viewDialog);
                        ImageView imgShop = (ImageView) viewDialog.findViewById(R.id.ShopImage);
                        Bitmap bmp = BitmapFactory.decodeFile(taskforshopphoto.PhotoID);

                        imgShop.setImageBitmap(bmp);
                        settingsDialog.show();
                    }
                    else {
                        if (Shared.SelectedTask.ShopPhoto != null) {
                            // Toast.makeText(getActivity(), " No Shop photo", Toast.LENGTH_LONG).show();
                            Dialog settingsDialog = new Dialog(getActivity());
                            View viewDialog = getActivity().getLayoutInflater().inflate(R.layout.image_layout, null);
                            settingsDialog.setContentView(viewDialog);
                            ImageView imgShop = (ImageView) viewDialog.findViewById(R.id.ShopImage);
                            try {
                                URL url = new URL("http://sunsigns.blob.core.windows.net/cdn/Images/ShopImages/" + Shared.SelectedTask.ShopPhoto);
                                //try this url = "http://0.tqn.com/d/webclipart/1/0/5/l/4/floral-icon-5.jpg"
                                HttpGet httpRequest = null;

                                httpRequest = new HttpGet(url.toURI());

                                HttpClient httpclient = new DefaultHttpClient();
                                HttpResponse response = (HttpResponse) httpclient
                                        .execute(httpRequest);

                                HttpEntity entity = response.getEntity();
                                BufferedHttpEntity b_entity = new BufferedHttpEntity(entity);
                                InputStream input = b_entity.getContent();

                                Bitmap bitmap = BitmapFactory.decodeStream(input);

                                imgShop.setImageBitmap(bitmap);
                                settingsDialog.show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            Toast.makeText(getActivity(), " No Shop photo", Toast.LENGTH_LONG).show();
                        }
                    }

                }
            });
        }
        else {
            MainActivity mainActivity = (MainActivity) getActivity();
            TextView taskName = (TextView) view.findViewById(R.id.ShopName);
            TextView taskAddress = (TextView) view.findViewById(R.id.ShopAddress);
            TextView branchname = (TextView) view.findViewById(R.id.Branch);
            TextView minimumPhoto = (TextView) view.findViewById(R.id.MinimumPhoto);
            ImageView shopphoto = (ImageView) view.findViewById(R.id.ShopPhoto);
            branchname.setText(task.ShopBranch.toString());
            taskName.setText(task.Name.toString());
            taskAddress.setText(task.ShopAddress.toString());
            minimumPhoto.setText("Minimum Photo: " + task.MinimumPhoto);

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
                    taskforshopphoto = dbHelper.getTaskInfo(String.valueOf(Shared.SelectedTask.ID));
                    if (taskforshopphoto.PhotoID != null) {
                        Dialog settingsDialog = new Dialog(getActivity());
                        settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                        View viewDialog = getActivity().getLayoutInflater().inflate(R.layout.image_layout, null);
                        settingsDialog.setContentView(viewDialog);
                        ImageView imgShop = (ImageView) viewDialog.findViewById(R.id.ShopImage);
                        Bitmap bmp = BitmapFactory.decodeFile(taskforshopphoto.PhotoID);

                        imgShop.setImageBitmap(bmp);
                        settingsDialog.show();
                    } else {
                        Toast.makeText(getActivity(), " No Shop photo", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
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

        if(Shared.admin_mian_activity==true) {
            Admin_MainActivity mainActivity = (Admin_MainActivity) getActivity();
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
            Shared.TaskDetail = dbHelper.getTaskLineItems(" TaskID = " + String.valueOf(task.ID));
            taskDetailAdapter.addAll(Shared.TaskDetail);
        }
        else
        {
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
            Shared.TaskDetail = dbHelper.getTaskLineItems(" TaskID = " + String.valueOf(task.ID));
            taskDetailAdapter.addAll(Shared.TaskDetail);
        }

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
        Shared.TaskLineItem=taskLineItemViewModel;
        if (Shared.admin_mian_activity == true) {
            GetRFeImageList obj = new GetRFeImageList(getActivity());
            obj.execute();
        } else {
            MainActivity mainActivity = (MainActivity) getActivity();
            DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
            TaskViewModel taskForShop = dbHelper.getTaskInfo(String.valueOf(task.ID));
            if (taskForShop.IsShopPhoto && taskForShop.PhotoID != null) {
                mCallback.onTaskLineItemSelected(taskLineItemViewModel);
            } else if (!taskForShop.IsShopPhoto) {
                mCallback.onTaskLineItemSelected(taskLineItemViewModel);
            } else {
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
    public class GetRFeImageList extends AsyncTask<String, Integer, ServerResult> {
        Context context;

        public GetRFeImageList(Context _context) {
            context = _context;
        }

        protected ServerResult doInBackground(String... loginData) {
            StringBuilder sbResult = new StringBuilder();
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("TasklineItemID", String.valueOf(Shared.TaskLineItem.ID)));
            postParameters.add(new BasicNameValuePair("ShopID",String.valueOf(Shared.SelectedTask.ShopID)));

            String response = null;
            try
            {
                response = CustomHttpClient.executeHttpPost(Shared.RfeImageListtAPI,
                        postParameters);
                String res = response.toString();
                sbResult.append(res);
                String value=res.replace("\n", "").replace("\r", "");
              if(value.equals("No Photo"))
                {
                    return ServerResult.NoPhoto;
                 }
                else {
                  GsonBuilder gsonBuilder = new GsonBuilder();
                  Gson gson = gsonBuilder.create();

                  Type listType = new TypeToken<ArrayList<TaskLineItemImageListViewModel>>() {
                  }.getType();

                  List<TaskLineItemImageListViewModel> taskserverImageList = gson.fromJson(res, listType);
                  ;

                  if (taskserverImageList == null || taskserverImageList.size() == 0) {
                      return ServerResult.NoTasks;
                  } else {
                      Shared.TaskLineitemImageList = taskserverImageList;
                      return ServerResult.TaskListReceived;
                  }
              }
            }catch (UnknownHostException e) {
                return ServerResult.ConnectionFailed;
            } catch (Exception e) {
                return ServerResult.UnknownError;
            }
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(ServerResult result) {
            // m_ProgressDialog.dismiss();

            switch (result) {
                case ConnectionFailed:
                    SstAlert.Show(getActivity(), "No Internet",
                            "You seem to have no internet connection");
                    break;
                case TaskListReceived:
                    mCallback.onTaskLineItemSelected(Shared.TaskLineItem);

                    break;
                case NoTasks:
                    SstAlert.Show(getActivity(), "No Tasks",
                            "No open tasks in your name");
                    break;
                case UnknownError:
                    SstAlert.Show(getActivity(), "Unknown Error",
                            "Some error occured");
                    break;
                case NoPhoto:
                    SstAlert.Show(getActivity(), "Empty",
                            "No Photos for this line Item");
                default:
                    break;

            }
        }
    }
}
