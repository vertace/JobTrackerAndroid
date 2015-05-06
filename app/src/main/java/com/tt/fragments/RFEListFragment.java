package com.tt.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tt.adapters.RfeListAdapter;
import com.tt.adapters.TaskListAdapter;
import com.tt.data.RfeViewModel;
import com.tt.data.Shared;
import com.tt.data.TaskListResponse;
import com.tt.data.TaskViewModel;
import com.tt.enumerations.JobTrackerScreen;
import com.tt.enumerations.ServerResult;
import com.tt.helpers.CustomHttpClient;
import com.tt.helpers.DatabaseHelper;
import com.tt.helpers.SstAlert;
import com.tt.jobtracker.Admin_MainActivity;
import com.tt.jobtracker.DownloadFileAsync;
import com.tt.jobtracker.MainActivity;
import com.tt.jobtracker.R;
import com.tt.sync.SyncHelperExecution;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by BS-308 on 5/6/2015.
 */
public class RFEListFragment extends Fragment {
    private ProgressDialog m_ProgressDialog = null;
   GetRfeList rfeListRetriever;
    DatabaseHelper dbHelper;
    ListView listView;
    Admin_MainActivity adminmainActivity;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_rfe_list, container, false);
        listView = (ListView) rootView.findViewById(R.id.RfeList);
        try {
            super.onCreate(savedInstanceState);

            adminmainActivity=(Admin_MainActivity) getActivity();
             dbHelper = new DatabaseHelper(adminmainActivity);


            SychRfe();

            m_ProgressDialog.dismiss();
        } catch (Exception e) {
            m_ProgressDialog.dismiss();
        }
        return rootView;
    }


    public void ShowRfeList() {

        m_ProgressDialog.dismiss();
        Shared.RfeList = dbHelper.getAllRfeList();
        RfeListAdapter rfeListAdapter = new RfeListAdapter(adminmainActivity, R.layout.row_rfe);
        rfeListAdapter.addAll(Shared.RfeList);
        listView.setAdapter(rfeListAdapter);

     //   Admin_MainActivity.CurrentScreen = JobTrackerScreen.TaskList;
       // mainActivity.SetActionBarMenuItems();
    }




    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void SychRfe() {
        m_ProgressDialog = ProgressDialog.show(getActivity(),
                "Please wait...", "Downloading task list...", true);

        GetRfeList obj = new GetRfeList(getActivity());
        obj.execute();
    }

    public class GetRfeList extends AsyncTask<String, Integer, ServerResult> {
        Context context;

        public GetRfeList(Context _context) {
            context = _context;
        }

        protected ServerResult doInBackground(String... loginData) {

            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();


            String response = null;
            try
            {
                response = CustomHttpClient.executeHttpPost(Shared.RfeListAPI,
                        postParameters);
                String res = response.toString();

                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();

                Type listType = new TypeToken<ArrayList<RfeViewModel>>() {
                }.getType();

                List<RfeViewModel> rfeList = gson.fromJson(res,
                        listType);

                if (rfeList == null || rfeList.size() == 0) {
                    return ServerResult.NoRfeList;
                } else {
                    Shared.RfeList = rfeList;
                    return ServerResult.RfeListReceived;
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
                case RfeListReceived:

                    m_ProgressDialog = ProgressDialog.show(getActivity(),
                            "Please wait...",
                            "Data downloaded. Adding to database...", true);
                  for(RfeViewModel rfe:Shared.RfeList)
                  {
                      dbHelper.insertRfeRequest(rfe);
                  }

                    m_ProgressDialog.dismiss();
                    ShowRfeList();

                    break;
                case NoRfeList:
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