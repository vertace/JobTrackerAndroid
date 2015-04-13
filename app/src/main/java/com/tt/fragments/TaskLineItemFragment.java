package com.tt.fragments;

import android.app.Activity;
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
import android.widget.Toast;

import com.tt.adapters.TaskDetailAdapter;
import com.tt.data.Shared;
import com.tt.data.TaskLineItemViewModel;
import com.tt.data.TaskViewModel;
import com.tt.enumerations.JobTrackerScreen;
import com.tt.helpers.DatabaseHelper;
import com.tt.jobtracker.MainActivity;
import com.tt.jobtracker.R;

import java.util.ArrayList;
import java.util.List;


public class TaskLineItemFragment extends ListFragment {

    OnTaskLineItemSelected mCallback;

    // Container Activity must implement this interface
    public interface OnTaskLineItemSelected {
        public void onTaskLineItemSelected(TaskLineItemViewModel taskLineItemViewModel);
    }

    TaskViewModel task;

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
                Fragment fragment = null;
                fragment = new MapSingleShopFragment();

              /*  MapSingleShopFragment fragment2 = new MapSingleShopFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.list_item, fragment2);
                fragmentTransaction.commit();*/
                break;

            case R.id.mnuMap:


                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }

    private void moveto_donetask() {
        int t=task.ID;
        MainActivity mainActivity = (MainActivity) getActivity();
        DatabaseHelper dbHelper = new DatabaseHelper(mainActivity);
        Shared.TaskDetail = dbHelper.getTaskLineItems(" TaskID = " + String.valueOf(task.ID));
        Shared.SelectedTask=dbHelper.getTaskInfo(String.valueOf(task.ID));
      //  TaskLineItemViewModel task=dbHelper.getTaskLineItemInfo(String.valueOf(t));
       // final ArrayList<String> imageList = dbHelper.getAllTaskLineItemPhotoUri(String.valueOf(task.ID));

        //List <TaskLineItemViewModel> tasklineitems=dbHelper.getTaskLineItems(" TaskID = " + String.valueOf(task.ID));

        TaskViewModel taskViewModel= Shared.SelectedTask;

        taskViewModel.IsPending=true;
        dbHelper.saveTask(taskViewModel, true);

        Toast.makeText(getActivity(), " Moved to done list", Toast.LENGTH_LONG).show();
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
