package com.tt.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.example.sstracker.R;
import com.tt.adapters.ImageAdapter;
import com.tt.data.TaskLineItemViewModel;
import com.tt.helpers.DatabaseHelper;
import com.tt.jobtracker.FullScreenImageViewActivity;

import java.util.ArrayList;

public class TaskLineItemDetailFragment extends Fragment {

    private OnTaskLineItemPhotoClickInitiated mCallback;
    ImageAdapter adapter;

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

        Button button = (Button) view.findViewById(R.id.btnTakePhoto);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onTaskLineItemPhotoClickInitiated(taskLineItemViewModel);
            }

        });


        GridView gridview = (GridView) view.findViewById(R.id.gridview);
        adapter = new ImageAdapter(getActivity());
        gridview.setAdapter(adapter);
        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());

        final ArrayList<String> imageList = dbHelper.getAllTaskLineItemPhotoUri(String.valueOf(taskLineItemViewModel.ID));
        adapter.addAll(imageList);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), FullScreenImageViewActivity.class);
                intent.putExtra("ImageList", imageList.toArray());
                intent.putExtra("Position", position);
                startActivity(intent);
            }
        });

        super.onCreate(savedInstanceState);
        return view;
    }

    public void updateImageAdapter() {
        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
        final ArrayList<String> imageList = dbHelper.getAllTaskLineItemPhotoUri(String.valueOf(taskLineItemViewModel.ID));
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
