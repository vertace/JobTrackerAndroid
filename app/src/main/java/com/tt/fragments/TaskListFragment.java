package com.tt.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tt.adapters.TaskListAdapter;
import com.tt.data.Shared;
import com.tt.data.TaskListResponse;
import com.tt.data.TaskViewModel;
import com.tt.enumerations.JobTrackerScreen;
import com.tt.enumerations.ServerResult;
import com.tt.helpers.CustomHttpClient;
import com.tt.helpers.DatabaseHelper;
import com.tt.helpers.SstAlert;
import com.tt.jobtracker.DownloadFileAsync;
import com.tt.jobtracker.MainActivity;
import com.tt.jobtracker.MapForMultipleShop;
import com.tt.jobtracker.MapForSingleShop;
import com.tt.jobtracker.R;
import com.tt.sync.SyncHelperExecution;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class TaskListFragment extends Fragment {
    DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
    //ListView listView;
    GetTaskList taskRetriever;
    private FragmentTabHost mTabHost;
    private ProgressDialog m_ProgressDialog = null;
    MainActivity mainActivity;
    View menuItemView;
    ListView listView,listview1;
    View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //rootView = inflater.inflate(R.layout.fragment_list_task_pending, container, false);

        mainActivity = (MainActivity) getActivity();
        mTabHost = new FragmentTabHost(mainActivity);
        mTabHost.setup(mainActivity, getChildFragmentManager(), R.id.realtabcontent);

        mTabHost.addTab(mTabHost.newTabSpec("PendingListFragment").setIndicator("Pending List"),
                PendingListFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("DoneListFragment").setIndicator("Done List"),
                DoneListFragment.class, null);
       // mTabHost.addTab(mTabHost.newTabSpec("UploadedListFragment").setIndicator("Uploaded List"),UploadedListFragment.class, null);

        setHasOptionsMenu(true);
        // mTabHost.setCurrentTab(0);
        mainActivity.CurrentScreen = JobTrackerScreen.TaskList;
        mainActivity.SetActionBarMenuItems();
        return mTabHost;
    }

    public void popup_window() {

        PopupMenu popupMenu = new PopupMenu(getActivity(), getActivity().findViewById(R.id.action_home_sort));
        popupMenu.getMenuInflater().inflate(R.menu.popupmenu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.shop:

                        Toast.makeText(getActivity().getApplicationContext(), "sort by shop", Toast.LENGTH_LONG).show();

                        MainActivity mainActivity = (MainActivity) getActivity();
                        DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);

                        String condition ="ORDER BY ShopName";
                        if (mainActivity.SearchText != "") {
                            condition = condition + " AND TaskRequest.ShopName like '%" + mainActivity.SearchText + "%'";
                        }
                        Shared.TaskList = dbHelper.getPendingTasks(condition);
                        /*listView = (ListView)rootView.FindViewById (R.id.list);
                        taskAdapter = new TaskListAdapter (rootView.Context, Resource.Layout.homescreen_list);
                        taskAdapter.AddAll (SunSignSharedProject.Token.TaskList);
                        listView.Adapter = taskAdapter;
                        TaskListAdapter taskListAdapter = new TaskListAdapter(getActivity(), R.layout.row_task);
                        //setListAdapter(taskListAdapter);
                        taskListAdapter.addAll(Shared.TaskList);
                      //  PendingListFragment.ShowTaskListsort();*/
                        break;

                    case R.id.branch:
                        Toast.makeText(getActivity().getApplicationContext(), "sort by branch", Toast.LENGTH_LONG).show();
                        break;

                    default:
                        break;

                }
                return true;
            }
        });
        popupMenu.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_sync:

                DownloadTasksFromServer();
                return true;
            case R.id.action_search:

                break;
            case R.id.action_home_sort:
                popup_window();
                break;

            case R.id.action_home_map:
                MainActivity mainActivity = (MainActivity) getActivity();
                String condition = " EmployeeID = " + String.valueOf(Shared.LoggedInUser.ID);

                DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
                Shared.TaskList = dbHelper.getPendingTasks(condition);
                Intent myIntent = new Intent(getActivity(), MapForMultipleShop.class);
                getActivity().startActivity(myIntent);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }

    public void DownloadTasksFromServer() {
        m_ProgressDialog = ProgressDialog.show(getActivity(),
                "Please wait...", "Downloading task list...", true);

        taskRetriever = new GetTaskList(getActivity());
        taskRetriever.execute();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTabHost = null;
    }


    public class GetTaskList extends AsyncTask<String, Integer, ServerResult> {
        Context context;

        public GetTaskList(Context _context) {
            context = _context;
        }

        protected ServerResult doInBackground(String... loginData) {

            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("EmployeeID",
                    Shared.LoggedInUser.ID));

            String response = null;
            try {
                response = CustomHttpClient.executeHttpPost(Shared.TaskListAPI,
                        postParameters);
                String res = response.toString();

                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();

                Type listType = new TypeToken<TaskListResponse>() {
                }.getType();
                TaskListResponse taskListResponse = gson.fromJson(res, listType);
                List<TaskViewModel> taskList = taskListResponse.TaskList;

                if (taskList == null || taskList.size() == 0) {
                    return ServerResult.NoTasks;
                } else {
                    Shared.TaskList = taskList;
                    Shared.OldImagesFile = taskListResponse.OldImagesFile;
                    return ServerResult.TaskListReceived;
                }
            } catch (UnknownHostException e) {
                return ServerResult.ConnectionFailed;
            } catch (Exception e) {
                return ServerResult.UnknownError;
            }
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(ServerResult result) {
            m_ProgressDialog.dismiss();

            switch (result) {
                case ConnectionFailed:
                    SstAlert.Show(getActivity(), "No Internet",
                            "You seem to have no internet connection");
                    break;
                case TaskListReceived:

                    m_ProgressDialog = ProgressDialog.show(getActivity(),
                            "Please wait...",
                            "Data downloaded. Adding to database...", true);

                    SyncHelperExecution syncHelper = new SyncHelperExecution(
                            context);
                    syncHelper
                            .addTasksToLocalDatabase((ArrayList<TaskViewModel>) Shared.TaskList);
                    m_ProgressDialog.dismiss();

                    DownloadFileAsync imageDownloader = new DownloadFileAsync(
                            context);
                    int lastDotPosition = Shared.OldImagesFile.lastIndexOf('/');
                    String zipFilename = Shared.OldImagesFile;
                    if (lastDotPosition > 0) {
                        zipFilename = zipFilename.substring(lastDotPosition + 1);
                    }
                    imageDownloader.execute(Shared.OldImagesFile, zipFilename);
                    //  mTabHost.onTabChanged("PendingListFragment");

//                    mTabHost.setCurrentTab(0);
                  /*  PendingListFragment fragment2 = new PendingListFragment();
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                   // fragmentTransaction.replace(R.id.fragment2, fragment2);
                    fragmentTransaction.commit();*/

                    break;
                case NoTasks:
                    SstAlert.Show(getActivity(), "No Tasks",
                            "No open tasks in your name");
                    break;
                case UnknownError:
                    SstAlert.Show(getActivity(), "Unknown Error",
                            "Some error occured");
                    break;
                default:
                    break;

            }
        }
    }
}