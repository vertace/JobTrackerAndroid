package com.tt.jobtracker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.example.sstracker.R;
import com.tt.adapters.PendingUploadListAdapter;
import com.tt.data.TaskLineItemViewModel;
import com.tt.enumerations.ServerResult;
import com.tt.helpers.AsycResponse.AsyncResponse;
import com.tt.helpers.DatabaseHelper;
import com.tt.helpers.SstAlert;

import java.util.ArrayList;

public class PendingExecutionUpload extends Activity implements AsyncResponse {

    DatabaseHelper dbHelper = new DatabaseHelper(this);
    PendingUploadListAdapter pendingUploadListAdapter;
    ListView listView;
    private ProgressDialog m_ProgressDialog = null;
    private ProgressDialog m_ProgressDialogStart = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_execution);
        ShowPendingList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.master, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.mnuTaskList:
                intent = new Intent(PendingExecutionUpload.this, TaskList.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
                break;
            case R.id.mnuPendingMeasurement:
                intent = new Intent(PendingExecutionUpload.this,
                        PendingMeasurementUpload.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
                break;
            case R.id.mnuPendingExecution:
                finish();
                startActivity(getIntent());
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }

    public void ShowPendingList() {
        if (m_ProgressDialog != null)
            m_ProgressDialog.dismiss();
        ArrayList<TaskLineItemViewModel> pendingList = LoadAllPendingUploads();
        if (pendingList.size() != 0) {

            m_ProgressDialogStart = ProgressDialog.show(
                    PendingExecutionUpload.this, "Please wait...",
                    "Uploading photos. " + pendingList.size() + " remaining",
                    true);
            UploadAllPendingItems(pendingList);

            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1,
                                        int arg2, long arg3) {
                    // TODO Auto-generated method stub

                }

            });
        }

    }

    private void UploadAllPendingItems(
            ArrayList<TaskLineItemViewModel> pendingList) {
        for (TaskLineItemViewModel taskLineItem : pendingList) {
            ImageUploadAsync async = new ImageUploadAsync(this, this);
            async.execute(taskLineItem);
        }
    }

    ArrayList<TaskLineItemViewModel> LoadAllPendingUploads() {
        if (m_ProgressDialogStart != null)
            m_ProgressDialogStart.dismiss();

        if (m_ProgressDialog != null)
            m_ProgressDialog.dismiss();

        pendingUploadListAdapter = new PendingUploadListAdapter(this,
                R.layout.row_tasklineitem);

        listView = (ListView) findViewById(R.id.lstPendingUploadTasks);

        listView.setAdapter(pendingUploadListAdapter);
        ArrayList<TaskLineItemViewModel> pendingList = dbHelper
                .getTaskLineItems(" NewImage not null AND NotDoneReason is null");

        ArrayList<TaskLineItemViewModel> pendingShopPhotos = dbHelper
                .getPendingShopPhotos();
        pendingList.addAll(pendingShopPhotos);


        pendingUploadListAdapter.addAll(pendingList);
        if (pendingList.size() != 0)
            m_ProgressDialog = ProgressDialog.show(PendingExecutionUpload.this,
                    "Please wait...", "Uploading photos. " + pendingList.size()
                            + " remaining", true);
        else {
            if (m_ProgressDialog != null)
                m_ProgressDialog.dismiss();
        }

        return pendingList;
    }

    @Override
    public void processFinish(ServerResult result) {
        if (m_ProgressDialogStart != null)
            m_ProgressDialogStart.dismiss();

        if (m_ProgressDialog != null)
            m_ProgressDialog.dismiss();
        switch (result) {
            case ConnectionFailed:
                SstAlert.Show(PendingExecutionUpload.this, "No Internet",
                        "No internet connection");
                break;
            case UploadSuccess:

                LoadAllPendingUploads();
                break;
            case UnknownError:
                SstAlert.Show(PendingExecutionUpload.this, "Unknown Error",
                        "Some error occured");
                break;
            default:
                break;

        }
    }
}
