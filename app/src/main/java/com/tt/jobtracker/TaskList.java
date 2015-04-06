package com.tt.jobtracker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.example.sstracker.R;
import com.tt.adapters.TaskListAdapter;
import com.tt.data.Shared;
import com.tt.data.TaskViewModel;
import com.tt.helpers.DatabaseHelper;
import com.tt.helpers.SstAlert;

public class TaskList extends Activity {

    DatabaseHelper dbHelper = new DatabaseHelper(this);
    ListView listView;
    private ProgressDialog m_ProgressDialog = null;
    //GetEmployeeList employeeRetriever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_tasklist);

            m_ProgressDialog = ProgressDialog.show(TaskList.this,
                    "Please wait...", "Reading data from database...", true);
            // taskRetriever = new GetTaskList(this);
            // taskRetriever.execute();

            ShowTaskList();
            m_ProgressDialog.dismiss();
        } catch (Exception e) {
            // AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            // dialog.setTitle("MyException Occured");
            // dialog.setMessage(e.getMessage());
            // dialog.setNeutralButton("Cool", null);
            // dialog.create().show();
            // ShowTaskList();
            m_ProgressDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Shared.LoggedInUser.Name.equals("ADMIN")) {
            // Inflate the menu; this adds items to the action bar if it is
            // present.
            getMenuInflater().inflate(R.menu.adminmaster, menu);
        } else {
            getMenuInflater().inflate(R.menu.master, menu);
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.shopnotavailable, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.mnuTaskList:
                finish();
                // startActivity(getIntent());

                intent = new Intent(TaskList.this, TaskList.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);

                break;
            case R.id.mnuPendingMeasurement:
                finish();

                intent = new Intent(TaskList.this, PendingMeasurementUpload.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
                break;
            case R.id.mnuPendingExecution:
                finish();
                intent = new Intent(TaskList.this, PendingExecutionUpload.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
                break;
            case R.id.mnuClearPhone:
                int size = dbHelper.getMeasurementPhotos(
                        " MeasurementString not null").size();
                size += dbHelper.getTaskLineItems(" NewImage not null").size();
                if (size > 0) {
                    SstAlert.Show(TaskList.this, "Uploads Pending",
                            "There are pending uploads");
                } else {
                    dbHelper.ClearDatabase();
                }
                break;
            case R.id.mnuAdminSync:
                m_ProgressDialog = ProgressDialog.show(TaskList.this,
                        "Please wait...", "Downloading employee list...", true);

                //	employeeRetriever = new GetEmployeeList(this);
                //	employeeRetriever.execute();
                break;
            case R.id.mnuSync:

                m_ProgressDialog = ProgressDialog.show(TaskList.this,
                        "Please wait...", "Downloading task list...", true);

                break;
            case R.id.mnuAddShop:
                AddRunTimeShopForMeasurement();
                finish();
                // startActivity(getIntent());

                intent = new Intent(TaskList.this, TaskList.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
                break;

            case R.id.mnuMap:
                intent = new Intent(TaskList.this, mapactivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);

                break;

            case R.id.mnuAdminLogout:
            case R.id.mnuLogout:
                finish();
                Shared.LoggedInUser = null;
                intent = new Intent(TaskList.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }

    private void AddRunTimeShopForMeasurement() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        TaskViewModel task = new TaskViewModel();
        task.EmployeeID = Integer.parseInt(Shared.LoggedInUser.ID);
        task.EmployeeName = Shared.LoggedInUser.Name;
        task.IsMeasurement = true;
        int num = dbHelper.getNextDynamicShopID();
        task.ID = num;
        task.ShopID = num;
        task.ShopAddress = "";
        task.ShopBranch = "";
        task.ShopCity = "";
        task.ShopName = "";
        task.ShopRegion = "";
        task.ShopName = "TempS" + String.valueOf(num);
        dbHelper.insertTask(task);
    }

    public void ShowTaskList() {

        DatabaseHelper dbHelper = new DatabaseHelper(this);

        String condition = " EmployeeID = "
                + String.valueOf(Shared.LoggedInUser.ID);
        Shared.TaskList = dbHelper.getPendingTasks(condition);

        TaskListAdapter taskListAdapter = new TaskListAdapter(this,
                R.layout.row_task);

        listView = (ListView) findViewById(R.id.lstTasks);

        listView.setAdapter(taskListAdapter);

        taskListAdapter.addAll(Shared.TaskList);

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                finish();

                TaskViewModel taskViewmodel = (TaskViewModel) parent
                        .getItemAtPosition(position);

                Shared.SelectedTask = taskViewmodel;

                if (taskViewmodel.IsMeasurement) {
                    Shared.MeasurementTaskID = taskViewmodel.ID;
                    Intent intent = new Intent(TaskList.this,
                            TakeMeasurementList.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("TaskID", String.valueOf(taskViewmodel.ID));
                    intent.putExtra("ShopID",
                            String.valueOf(taskViewmodel.ShopID));
                    intent.putExtra("ShopName",
                            String.valueOf(taskViewmodel.ShopName));
                    intent.putExtra("ShopAddress",
                            String.valueOf(taskViewmodel.ShopAddress));
                    getApplicationContext().startActivity(intent);
                } else {

                    Intent intent = new Intent(TaskList.this, TaskDetail.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("TaskID", taskViewmodel.ID);
                    getApplicationContext().startActivity(intent);
                }
            }

        });
//		
//		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//	        @Override
//	        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
//	        	MenuInflater inflater = getMenuInflater();
//	    		inflater.inflate(R.menu.shopnotavailable, menu);
//				return false;
//	        }
//		});
    }


/**    public class GetEmployeeList extends
 AsyncTask<String, Integer, ServerResult> {
 Context context;

 public GetEmployeeList(Context _context) {
 context = _context;
 }

 protected ServerResult doInBackground(String... loginData) {

 String response = null;
 try {
 response = CustomHttpClient
 .executeHttpGet(Shared.EmployeeListAPI);
 String res = response.toString();

 GsonBuilder gsonBuilder = new GsonBuilder();
 Gson gson = gsonBuilder.create();
 Type listType = new TypeToken<ArrayList<EmployeeViewModel>>() {
 }.getType();
 List<EmployeeViewModel> employeeList = gson.fromJson(res,
 listType);

 if (employeeList == null || employeeList.size() == 0) {
 return ServerResult.NoTasks;
 } else {
 Shared.EmployeeList = employeeList;
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
 SstAlert.Show(TaskList.this, "No Internet",
 "You seem to have no internet connection");
 break;
 case TaskListReceived:
 // SstAlert.Show(TaskList.this, "Tasks", Shared.TaskList.size()
 // + " tasks received");
 // delegate.processFinish(result);
 m_ProgressDialog = ProgressDialog
 .show(TaskList.this,
 "Please wait...",
 "Employee list downloaded. Adding to database...",
 true);

 for (EmployeeViewModel employee : Shared.EmployeeList) {
 dbHelper.saveEmployee(employee);
 }
 m_ProgressDialog.dismiss();

 break;
 case NoTasks:
 SstAlert.Show(TaskList.this, "No Tasks", "No employees");
 break;
 case UnknownError:
 SstAlert.Show(TaskList.this, "Unknown Error",
 "Some error occured");
 break;
 default:
 break;

 }
 }
 }*/
}
