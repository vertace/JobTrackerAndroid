package com.tt.jobtracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tt.data.TaskLineItemNotDoneViewModel;
import com.tt.data.TaskNotDoneViewModel;
import com.tt.data.TaskViewModel;
import com.tt.jobtracker.R;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tt.data.EmployeeViewModel;
import com.tt.data.Shared;
import com.tt.enumerations.ServerResult;
import com.tt.helpers.CustomHttpClient;
import com.tt.helpers.DatabaseHelper;
import com.tt.helpers.SstAlert;
import com.tt.helpers.Utility;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class Login extends Activity {

    DatabaseHelper dbHelper = new DatabaseHelper(this);
    EditText un, pw;
    GetEmployeeList employeeRetriever;
    public ProgressDialog m_ProgressDialog = null;
    SharedPreferences sh_Pref;
    SharedPreferences.Editor toEdit;
    String username, password;
    public Login() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utility.getLocation(this);

        setContentView(R.layout.login);
        un = (EditText) findViewById(R.id.txtUsername);
        pw = (EditText) findViewById(R.id.txtPassword);

        final SharedPreferences username = getApplicationContext().getSharedPreferences(Shared.Username, 0);
        final SharedPreferences password = getApplicationContext().getSharedPreferences(Shared.Password, 0);
        String username1= username.getString("Loginuser", null); // getting String
        String password1= password.getString("LoginPass", null);
        //   username1="bal";
        // password1="123";
        if(username1!=null && password1!=null)
        {
            EmployeeViewModel employee ;
            employee   = dbHelper.AuthenticateUser(username1, password1);
            CheckDefaultLogin(employee);
            //String userValue=
        }
        // this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        //SharedPreference();
    }
    private void LoginProcess( )
    {
        EmployeeViewModel employee = dbHelper.AuthenticateUser(un.getText().toString(), pw.getText()
                .toString());
        if(employee!=null)
        {
            final SharedPreferences username = getApplicationContext().getSharedPreferences(Shared.Username, 0);
            final SharedPreferences password = getApplicationContext().getSharedPreferences(Shared.Password, 0);

            SharedPreferences.Editor editor = username.edit();
            editor.putString("Loginuser",un.getText().toString() ); // Storing string
            editor.commit();
            SharedPreferences.Editor editor1 = password.edit();
            editor1.putString("LoginPass", pw.getText().toString()); // Storing string
            editor1.commit();
            CheckDefaultLogin(employee);
        }
        else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Wrong username/password!")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(Login.this, Login.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(intent);
                            finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            //SstAlert.Show(Login.this, "Login Failed","Wrong username/password");

        }
    }
    public void btnLogin_click(View view) {
       String user= un.getText().toString();
        String pass= pw.getText().toString();
if(!user.isEmpty() && !pass.isEmpty()) {
// new LoginToServer().execute();
    InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
    inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);

    final SharedPreferences username = getApplicationContext().getSharedPreferences(Shared.Username, 0);
    final SharedPreferences password = getApplicationContext().getSharedPreferences(Shared.Password, 0);
    EmployeeViewModel employee;
    // String  user=sh_Pref.getString("Username",null);
    employee = dbHelper.AuthenticateUser(un.getText().toString().toLowerCase(), pw.getText().toString());
    //  SharedPreference();-
    m_ProgressDialog = ProgressDialog.show(Login.this, "Please wait...", "Logging  in...");
    // m_ProgressDialog= ProgressDialog.show(Login.this,"Please wait...", "Logging  in...", true, false);
    final SharedPreferences taskSync = getApplicationContext().getSharedPreferences(Shared.TaskSync, 0);
    SharedPreferences.Editor editors = taskSync.edit();

    editors.putString("tasksync", "True"); // Storing string
    editors.commit();
    Shared.sychIntiallyTasks = true;
    if (employee == null) {

        employeeRetriever = new GetEmployeeList(this);
        employeeRetriever.execute();
        //  m_ProgressDialog.dismiss();

    } else {
        SharedPreferences.Editor editor = username.edit();
        editor.putString("Loginuser", un.getText().toString()); // Storing string
        editor.commit();
        SharedPreferences.Editor editor1 = password.edit();
        editor1.putString("LoginPass", pw.getText().toString()); // Storing string
        editor1.commit();
        //   m_ProgressDialog.dismiss();
        CheckDefaultLogin(employee);
    }
}
        else
{
    Toast.makeText(getApplicationContext(), " Username or password is empty!!!", Toast.LENGTH_LONG).show();
}
    }
    public void CheckDefaultLogin( EmployeeViewModel employee) {

        Shared.LoggedInUser = employee;
        if(Shared.LoggedInUser.IsAdmin==true)
        {
            Intent intent = new Intent(Login.this, Admin_MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
        }
        else {
            Intent intent = new Intent(Login.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
        }
        finish();
    }
    public void btnSync_click(View view) {
        m_ProgressDialog = ProgressDialog.show(Login.this,
                "Please wait...", "Downloading employee list...", true);

        employeeRetriever = new GetEmployeeList(this);
        employeeRetriever.execute();
        m_ProgressDialog.dismiss();
    }

    private class LoginToServer extends AsyncTask<String, Integer, ServerResult> {
        protected ServerResult doInBackground(String... loginData) {

            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("Username", un.getText()
                    .toString()));
            postParameters.add(new BasicNameValuePair("Password", pw.getText()
                    .toString()));

            String response = null;
            try {
                response = CustomHttpClient.executeHttpPost(Shared.LoginAPI,
                        postParameters);
                String res = response.toString();
                res = res.replaceAll("\\s+", "");

                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                EmployeeViewModel employee = gson.fromJson(res,
                        EmployeeViewModel.class);

                if (employee == null) {
                    return ServerResult.LoginFailed;
                } else {
                    Shared.LoggedInUser = employee;
                    return ServerResult.LoginSuccess;
                }
            } catch (UnknownHostException e) {
                // m_ProgressDialog.dismiss();
                return ServerResult.ConnectionFailed;
            } catch (Exception e) {
                // m_ProgressDialog.dismiss();
                return ServerResult.UnknownError;
            }
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected void onPostExecute(ServerResult result) {
            // m_ProgressDialog.dismiss();

            switch (result) {
                case ConnectionFailed:
                    m_ProgressDialog.dismiss();
                    SstAlert.Show(Login.this, "No Internet",
                            "No internet connection");
                    break;
                case LoginFailed:
                    m_ProgressDialog.dismiss();
                    SstAlert.Show(Login.this, "Login Failed",
                            "Wrong username/password");
                    break;
                case LoginSuccess:
                    Intent intent = new Intent(Login.this, TaskList.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                    finish();
                    break;
                case UnknownError:
                    m_ProgressDialog.dismiss();
                    SstAlert.Show(Login.this, "Unknown Error", "Some error occured");
                    break;
                default:
                    break;
            }
            m_ProgressDialog.dismiss();
        }
    }

    private class GetEmployeeList extends
            AsyncTask<String, Integer, ServerResult> {
        Context context;

        public GetEmployeeList(Context _context) {
            context = _context;
        }

        protected ServerResult doInBackground(String... loginData) {

            String response = null;

            try {

             //   HttpGet httpGet = new HttpGet(Shared.EmployeeListAPI);
             //   HttpParams httpParameters = new BasicHttpParams();
// Set the timeout in milliseconds until a connection is established.
// The default value is zero, that means the timeout is not used.
          //      int timeoutConnection = 30000;
           //     HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
// Set the default socket timeout (SO_TIMEOUT)
// in milliseconds which is the timeout for waiting for data.
           //     int timeoutSocket = 50000;
           //     HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

           //     DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
             //   HttpResponse responses = (BasicHttpResponse) httpClient.execute(httpGet);
            //    HttpEntity entity = responses.getEntity();

                response = CustomHttpClient
                        .executeHttpGet(Shared.EmployeeListAPI);
                String res = response.toString();
                //  m_ProgressDialog.show();
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
                //  m_ProgressDialog.dismiss();
                return ServerResult.ConnectionFailed;
            } catch (Exception e) {
                //  m_ProgressDialog.dismiss();
                return ServerResult.UnknownError;
            }
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(ServerResult result) {
            // m_ProgressDialog.dismiss();

            switch (result) {
                case ConnectionFailed:
                    m_ProgressDialog.dismiss();
                    SstAlert.Show(Login.this, "No Internet",
                            "You seem to have no internet connection");
                    break;
                case TaskListReceived:
                    // SstAlert.Show(TaskList.this, "Tasks", Shared.TaskList.size()
                    // + " tasks received");
                    // delegate.processFinish(result);
                    m_ProgressDialog = ProgressDialog
                            .show(Login.this,
                                    "Please wait...",
                                    "Employee list downloaded. Adding to database...",
                                    true);

                    for (EmployeeViewModel employee : Shared.EmployeeList) {
                        dbHelper.saveEmployee(employee);
                    }
                    if(Shared.sychIntiallyTasks=true) {
                        dbHelper.deleteTaskNotDone();
                        dbHelper.deleteTaskLineItemNotDone();
                        // List<TaskNotDoneViewModel> taskNotDone = Shared.EmployeeList.get(0).taskNotDoneReasonList;
                        //List<TaskLineItemNotDoneViewModel> taskLineItemNotDone = Shared.EmployeeList.get(0).taskLineItemNotDoneReasonList;
                        for(TaskNotDoneViewModel tasknotdone:Shared.EmployeeList.get(0).taskNotDoneReasonList)
                        {
                            dbHelper.insertTaskNotDoneReason(tasknotdone);
                        }
                        for(TaskLineItemNotDoneViewModel taskLineItemNotDone:Shared.EmployeeList.get(0).taskLineItemNotDoneReasonList)
                        {
                            dbHelper.insertTaskLineItemNotDoneReason(taskLineItemNotDone);
                        }

                    }
                    LoginProcess();
                    //m_ProgressDialog.dismiss();
                    break;
                case NoTasks:
                    m_ProgressDialog.dismiss();
                    SstAlert.Show(Login.this, "No Tasks", "No employees");
                    break;
                case UnknownError:
                    m_ProgressDialog.dismiss();
                    SstAlert.Show(Login.this, "Unknown Error",
                            "Some error occured");
                    break;
                default:
                    break;

            }
        }
    }


}
