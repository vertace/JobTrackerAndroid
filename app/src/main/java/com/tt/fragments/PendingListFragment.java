package com.tt.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.sstracker.R;
import com.tt.adapters.TaskListAdapter;
import com.tt.data.Shared;
import com.tt.data.TaskViewModel;
import com.tt.helpers.DatabaseHelper;
import com.tt.jobtracker.MainActivity;


public class PendingListFragment extends ListFragment {

    private ProgressDialog m_ProgressDialog = null;

    OnTaskSelected mCallback;

    // Container Activity must implement this interface
    public interface OnTaskSelected {
        public void onTaskSelected(TaskViewModel task);
    }


    public PendingListFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

    public void ShowTaskList(View rootView) {
        MainActivity mainActivity = (MainActivity) getActivity();
        DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);

        String condition = " EmployeeID = "
                + String.valueOf(Shared.LoggedInUser.ID);
        if (mainActivity.SearchText != "") {
            condition = condition + " AND TaskRequest.ShopName like '%" + mainActivity.SearchText + "%'";
        }
        Shared.TaskList = dbHelper.getPendingTasks(condition);

        TaskListAdapter taskListAdapter = new TaskListAdapter(mainActivity, R.layout.row_task);
        setListAdapter(taskListAdapter);
        taskListAdapter.addAll(Shared.TaskList);
    }


}