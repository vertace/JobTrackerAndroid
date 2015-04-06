package com.tt.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.sstracker.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tt.data.Shared;
import com.tt.data.TaskListResponse;
import com.tt.data.TaskViewModel;
import com.tt.enumerations.ServerResult;
import com.tt.helpers.CustomHttpClient;
import com.tt.helpers.DatabaseHelper;
import com.tt.helpers.SstAlert;
import com.tt.jobtracker.DownloadFileAsync;
import com.tt.sync.SyncHelperExecution;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class TaskListFragment extends Fragment {
    DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
    ListView listView;
    GetTaskList taskRetriever;
    private FragmentTabHost mTabHost;
    private ProgressDialog m_ProgressDialog = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);

        mTabHost.addTab(mTabHost.newTabSpec("PendingListFragment").setIndicator("Pending List"),
                PendingListFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("DoneListFragment").setIndicator("Done List"),
                DoneListFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("UploadedListFragment").setIndicator("Uploaded List"),
                UploadedListFragment.class, null);

        setHasOptionsMenu(true);

        return mTabHost;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_sync:

                m_ProgressDialog = ProgressDialog.show(getActivity(),
                        "Please wait...", "Downloading task list...", true);

                taskRetriever = new GetTaskList(getActivity());
                taskRetriever.execute();
                return true;
            case R.id.action_search:

                break;

            case R.id.mnuMap:


                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
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