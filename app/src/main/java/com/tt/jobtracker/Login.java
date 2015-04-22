package com.tt.jobtracker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Login extends Activity {

    DatabaseHelper dbHelper = new DatabaseHelper(this);
    EditText un, pw;
    GetEmployeeList employeeRetriever;
    private ProgressDialog m_ProgressDialog = null;
    SharedPreferences sh_Pref;
    SharedPreferences.Editor toEdit;
    String username, password;
    public Login() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utility.getLocation(this);

        setContentView(R.layout.activity_login);

        un = (EditText) findViewById(R.id.txtUsername);
        pw = (EditText) findViewById(R.id.txtPassword);
        SharedPreference();
    }
    public void SharedPreference() {
        sh_Pref = getSharedPreferences("Login Credentials", MODE_PRIVATE);
        toEdit = sh_Pref.edit();
        toEdit.putString("Username", un.getText().toString());
        toEdit.putString("Password", pw.getText().toString());
        toEdit.commit();
        String  user=sh_Pref.getString("Username","0");
        String pas=sh_Pref.getString("Password","0");
        EmployeeViewModel employee ;
        employee   = dbHelper.AuthenticateUser(user, pas);
        if(employee!=null)
        CheckDefaultLogin(employee);
    }
    public void btnLogin_click(View view) {

        m_ProgressDialog = ProgressDialog.show(Login.this, "Please wait...",
                "Logging  in...", true);

        // new LoginToServer().execute();
        EmployeeViewModel employee ;
       String  user=sh_Pref.getString("Username","0");
        String pas=sh_Pref.getString("Password","0");
        employee   = dbHelper.AuthenticateUser(un.getText().toString(), pw.getText()
                .toString());
        SharedPreference();
        m_ProgressDialog.dismiss();
        if (employee == null) {
            employeeRetriever = new GetEmployeeList(this);
            employeeRetriever.execute();
            employee = dbHelper.AuthenticateUser(un.getText().toString(), pw.getText()
                    .toString());
            if(employee!=null)
            {
                CheckDefaultLogin(employee);
            }
            else {
                SstAlert.Show(Login.this, "Login Failed",
                        "Wrong username/password");
            }
        } else {
            Shared.LoggedInUser = employee;


            Intent intent = new Intent(Login.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
            finish();
        }
    }
    public void CheckDefaultLogin( EmployeeViewModel employee) {
        Shared.LoggedInUser = employee;
        Intent intent = new Intent(Login.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
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
                return ServerResult.ConnectionFailed;
            } catch (Exception e) {
                return ServerResult.UnknownError;
            }
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected void onPostExecute(ServerResult result) {
            m_ProgressDialog.dismiss();

            switch (result) {
                case ConnectionFailed:
                    SstAlert.Show(Login.this, "No Internet",
                            "No internet connection");
                    break;
                case LoginFailed:
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
                    SstAlert.Show(Login.this, "Unknown Error", "Some error occured");
                    break;
                default:
                    break;

            }
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
                    m_ProgressDialog.dismiss();

                    break;
                case NoTasks:
                    SstAlert.Show(Login.this, "No Tasks", "No employees");
                    break;
                case UnknownError:
                    SstAlert.Show(Login.this, "Unknown Error",
                            "Some error occured");
                    break;
                default:
                    break;

            }
        }
    }


}
