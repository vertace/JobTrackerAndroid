package com.tt.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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
import com.tt.jobtracker.Admin_MainActivity;
import com.tt.jobtracker.MainActivity;
import com.tt.jobtracker.R;
import com.tt.sync.SyncHelperExecution;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class RfeTaskListFragment extends ListFragment
{
    private ProgressDialog m_ProgressDialog = null;

    OnTaskSelected mCallback;

    // Container Activity must implement this interface
    public interface OnTaskSelected {
        public void onTaskSelected(TaskViewModel task);
    }



    public RfeTaskListFragment()
    {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_list_task_pending, container, false);

        try {
            super.onCreate(savedInstanceState);

            m_ProgressDialog = ProgressDialog.show(getActivity(),
                    "Please wait...", "Reading data from database...", true);

            ShowTaskList(rootView);
            m_ProgressDialog.dismiss();
        } catch (Exception e) {
            m_ProgressDialog.dismiss();
        }
        return rootView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnTaskSelected) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnTaskSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        TaskViewModel task = Shared.TaskList.get(position);
        mCallback.onTaskSelected(task);

        super.onListItemClick(l, v, position, id);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void ShowTaskList(View rootView) {
        Admin_MainActivity mainActivity = (Admin_MainActivity) getActivity();
        DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
        final SharedPreferences oderByTaskList = getActivity().getApplicationContext().getSharedPreferences(Shared.OrderByTask, 0);
        String orderBySearch= oderByTaskList.getString("sorting", null);
        String condition;

            Shared.TaskList = dbHelper.getAllTasks();

            TaskListAdapter taskListAdapter = new TaskListAdapter(mainActivity, R.layout.row_task);
            setListAdapter(taskListAdapter);
            taskListAdapter.addAll(Shared.TaskList);

            mainActivity.CurrentScreen = JobTrackerScreen.TaskList;
            mainActivity.SetActionBarMenuItems();
        }

}