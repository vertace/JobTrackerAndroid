package com.tt.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.tt.data.EmployeeViewModel;
import com.tt.data.MeasurementPhoto;
import com.tt.data.TaskLineItemPhotoViewModel;
import com.tt.data.TaskLineItemViewModel;
import com.tt.data.TaskViewModel;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String LOGCAT = null;
    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context applicationcontext) {
        super(applicationcontext, "SSTrackerDB.db", null, 1);
        Log.d(LOGCAT, "Created");
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String query;
        query = "CREATE TABLE TaskRequest ( 	TaskID  INTEGER PRIMARY KEY,"
                + "ShopID INTEGER," + "ShopName TEXT," + "ShopBranch TEXT,"
                + "ShopRegion TEXT," + "ShopCity TEXT," + "ShopAddress TEXT,"
                + "EmployeeName TEXT," + "EmployeeID INTEGER,"
                + "TaskStatus TEXT," + "IsMeasurement INTEGER,"
                + "PhotoID TEXT," + "StartTime TEXT,"
                + "ShopPhotoUploaded INTEGER)";
        database.execSQL(query);
        Log.d(LOGCAT, "TaskRequest Created");

        query = "CREATE TABLE TaskLineItemRequest (ID INTEGER PRIMARY KEY,"
                + "TaskID TEXT," + "ShopID TEXT," + "ShopWallID TEXT,"
                + "ShopName TEXT," + "ShopWall TEXT," + "Instruction TEXT,"
                + "WallID TEXT," + "OldImage TEXT," + "NewImage TEXT,"
                + "Type TEXT," + "Width TEXT," + "Height TEXT," + "Side TEXT,"
                + "Form TEXT," + "Photo TEXT," + "Status TEXT,"
                + "Remove TEXT," + "Lat TEXT," + "Lon TEXT," + "PhotoID TEXT,"
                + "Measurement TEXT," + "Time TEXT, OldTakenNow INT,"
                + "NotDoneReason TEXT ," + "ShopAddress TEXT)";
        database.execSQL(query);
        Log.d(LOGCAT, "TaskLineItemRequest Created");

        query = "CREATE TABLE TaskLineItemPhoto (ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "TaskLineItemID TEXT, PhotoID TEXT, Time TEXT, Lat TEXT, Lon TEXT)";
        database.execSQL(query);
        Log.d(LOGCAT, "TaskLineItemPhoto Created");

        query = "CREATE TABLE MeasurementPhoto (ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "TaskID TEXT, PhotoID TEXT ,MeasurementString TEXT, Time TEXT, ShopID TEXT, Lat TEXT, Lon TEXT,ShopName TEXT,ShopAddress TEXT)";
        database.execSQL(query);
        Log.d(LOGCAT, "MeasurementPhoto Created");

        query = "CREATE TABLE Employee (EmpID  INTEGER PRIMARY KEY,"
                + "Name INTEGER," + "Username TEXT," + "Password TEXT)";
        database.execSQL(query);
        Log.d(LOGCAT, "Employee Created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int version_old,
                          int current_version) {

        if (version_old == 1 && current_version == 2) {
            String query;
            query = "ALTER TABLE TaskLineItemRequest ADD COLUMN NotDoneReason TEXT";
            database.execSQL(query);
        }
    }

    public EmployeeViewModel AuthenticateUser(String username, String password) {
        EmployeeViewModel user = null;

        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM Employee where username='"
                + username + "' AND password='" + password + "'";
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                user = new EmployeeViewModel();
                user.ID = cursor.getString(0);
                user.Name = cursor.getString(1);
                user.Username = cursor.getString(2);
            } while (cursor.moveToNext());
        }
        database.close();
        return user;
    }

    public void insertEmployee(EmployeeViewModel employee) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("EmpID", employee.ID);
        values.put("Name", employee.Name);
        values.put("Username", employee.Username);
        values.put("Password", employee.Password);
        database.insert("Employee", null, values);
        database.close();
    }

    public int updateEmployee(EmployeeViewModel employee) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("EmpID", employee.ID);
        values.put("Name", employee.Name);
        values.put("Username", employee.Username);
        values.put("Password", employee.Password);
        int result = database.update("Employee", values, "EmpID" + " = ?",
                new String[]{String.valueOf(employee.ID)});
        database.close();
        return result;
    }

    public void deleteEmployee(String id) {
        Log.d(LOGCAT, "delete");
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM Employee where EmpID=" + id;
        Log.d("query", deleteQuery);
        database.execSQL(deleteQuery);
        database.close();
    }

    public ArrayList<TaskLineItemPhotoViewModel> getAllTaskLineItemPhotos(String taskLineItemID) {
        ArrayList<TaskLineItemPhotoViewModel> taskLineItemPhotoList = new ArrayList<TaskLineItemPhotoViewModel>();
        String selectQuery = "SELECT * FROM TaskLineItemPhoto where TaskLineItemID = " + taskLineItemID;
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                TaskLineItemPhotoViewModel taskLineItemPhoto = new TaskLineItemPhotoViewModel();

                taskLineItemPhoto.ID = Integer.parseInt(cursor.getString(0));
                taskLineItemPhoto.TaskLineItemID = Integer.parseInt(cursor.getString(1));
                taskLineItemPhoto.PhotoID = cursor.getString(2);
                taskLineItemPhoto.Time = cursor.getString(3);
                taskLineItemPhoto.Lat = cursor.getString(4);
                taskLineItemPhoto.Lon = cursor.getString(5);
                taskLineItemPhotoList.add(taskLineItemPhoto);
            } while (cursor.moveToNext());
        }
        database.close();
        return taskLineItemPhotoList;
    }

    public ArrayList<String> getAllTaskLineItemPhotoUri(String taskLineItemID) {
        ArrayList<String> taskLineItemPhotoUriList = new ArrayList<String>();
        String selectQuery = "SELECT PhotoID FROM TaskLineItemPhoto where TaskLineItemID = " + taskLineItemID;
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                taskLineItemPhotoUriList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        database.close();
        return taskLineItemPhotoUriList;
    }

    public void insertTaskLineItemPhoto(TaskLineItemPhotoViewModel taskLineItemPhoto) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TaskLineItemID", taskLineItemPhoto.TaskLineItemID);
        values.put("PhotoID", taskLineItemPhoto.PhotoID);
        values.put("Time", taskLineItemPhoto.Time);
        values.put("Lat", taskLineItemPhoto.Lat);
        values.put("Lon", taskLineItemPhoto.Lon);
        database.insert("TaskLineItemPhoto", null, values);
        database.close();
    }


    public void deleteTaskLineItemPhoto(String id) {
        Log.d(LOGCAT, "delete");
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM TaskLineItemPhoto where ID=" + id;
        Log.d("query", deleteQuery);
        database.execSQL(deleteQuery);
        database.close();
    }

    public void insertTaskLineItem(TaskLineItemViewModel taskLineItem) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ID", taskLineItem.ID);
        values.put("Form", taskLineItem.Form);
        values.put("Height", taskLineItem.Height);
        values.put("Instruction", taskLineItem.Instruction);
        values.put("Lat", taskLineItem.Lat);
        values.put("Lon", taskLineItem.Lon);
        values.put("NewImage", taskLineItem.NewImage);
        values.put("OldImage", taskLineItem.OldImage);
        values.put("Photo", taskLineItem.Photo);
        values.put("PhotoID", taskLineItem.PhotoID);
        values.put("Remove", taskLineItem.Remove);
        values.put("ShopName", taskLineItem.ShopName);
        values.put("ShopWall", taskLineItem.ShopWall);
        values.put("Side", taskLineItem.Side);
        values.put("Status", taskLineItem.Status);
        values.put("OldTakenNow", taskLineItem.OldTakenNow ? 1 : 0);
        values.put("NotDoneReason", taskLineItem.NotDoneReason);
        values.put("Time", taskLineItem.Time);
        values.put("Type", taskLineItem.Type);
        values.put("Width", taskLineItem.Width);
        values.put("ShopID", taskLineItem.ShopID);
        values.put("ShopWallID", taskLineItem.ShopWallID);
        values.put("TaskID", taskLineItem.TaskID);
        values.put("WallID", taskLineItem.WallID);
        values.put("Measurement", taskLineItem.Measurement);
        values.put("ShopAddress", taskLineItem.ShopAddress);
        database.insert("TaskLineItemRequest", null, values);
        database.close();
    }

    public int updateTaskLineItem(TaskLineItemViewModel taskLineItem,
                                  boolean includePhoneUpdates) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Form", taskLineItem.Form);
        values.put("Height", taskLineItem.Height);
        values.put("Instruction", taskLineItem.Instruction);

        values.put("Photo", taskLineItem.Photo);

        values.put("Remove", taskLineItem.Remove);
        values.put("ShopName", taskLineItem.ShopName);
        values.put("ShopWall", taskLineItem.ShopWall);
        values.put("Side", taskLineItem.Side);
        values.put("Status", taskLineItem.Status);

        values.put("Type", taskLineItem.Type);
        values.put("Width", taskLineItem.Width);
        values.put("ShopID", taskLineItem.ShopID);
        values.put("ShopWallID", taskLineItem.ShopWallID);
        values.put("TaskID", taskLineItem.TaskID);
        values.put("WallID", taskLineItem.WallID);
        values.put("ShopAddress", taskLineItem.ShopAddress);

        values.put("OldTakenNow", taskLineItem.OldTakenNow ? 1 : 0);
        if (includePhoneUpdates) {

            values.put("NewImage", taskLineItem.NewImage);
            values.put("OldImage", taskLineItem.OldImage);

            values.put("PhotoID", taskLineItem.PhotoID);
            values.put("Time", taskLineItem.Time);
            values.put("Measurement", taskLineItem.Measurement);
            values.put("Lat", taskLineItem.Lat);
            values.put("Lon", taskLineItem.Lon);
            values.put("NotDoneReason", taskLineItem.NotDoneReason);
        }
        int result = database.update("TaskLineItemRequest", values, "ID"
                + " = ?", new String[]{String.valueOf(taskLineItem.ID)});
        database.close();
        return result;
    }

    public void deleteTaskLineItem(String id) {
        Log.d(LOGCAT, "delete");
        DeleteRelatedJobPhoto(id);
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM TaskLineItemRequest where ID=" + id;
        Log.d("query", deleteQuery);
        database.execSQL(deleteQuery);
        database.close();

    }

    private void DeleteRelatedJobPhoto(String taskLineitemId) {
        TaskLineItemViewModel taskLineItemViewModel = getTaskLineItemInfo(taskLineitemId);
        PhotoDeleteHelper.DeletePhoto(taskLineItemViewModel.OldImage);
        PhotoDeleteHelper.DeletePhoto(taskLineItemViewModel.NewImage);
    }

    public ArrayList<TaskLineItemViewModel> getAllTaskLineItems() {
        ArrayList<TaskLineItemViewModel> taskLineItemList = new ArrayList<TaskLineItemViewModel>();
        String selectQuery = "SELECT * FROM TaskLineItemRequest";
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                TaskLineItemViewModel taskLineItem = new TaskLineItemViewModel();

                taskLineItem.ID = Integer.parseInt(cursor.getString(0));
                taskLineItem.Form = cursor.getString(14);
                taskLineItem.Height = cursor.getString(12);
                taskLineItem.Instruction = cursor.getString(6);
                taskLineItem.Lat = cursor.getString(18);
                taskLineItem.Lon = cursor.getString(19);
                taskLineItem.NewImage = cursor.getString(9);
                taskLineItem.OldImage = cursor.getString(8);
                taskLineItem.Photo = cursor.getString(15);
                taskLineItem.PhotoID = cursor.getString(20);
                taskLineItem.Remove = cursor.getString(17);
                taskLineItem.ShopName = cursor.getString(4);
                taskLineItem.ShopWall = cursor.getString(5);
                taskLineItem.Side = cursor.getString(13);
                taskLineItem.Status = cursor.getString(16);
                taskLineItem.Time = cursor.getString(22);
                taskLineItem.Type = cursor.getString(10);
                taskLineItem.Width = cursor.getString(11);
                taskLineItem.ShopID = Integer.parseInt(cursor.getString(2));
                taskLineItem.ShopWallID = Integer.parseInt(cursor.getString(3));
                taskLineItem.TaskID = Integer.parseInt(cursor.getString(1));
                taskLineItem.WallID = Integer.parseInt(cursor.getString(7));
                taskLineItem.Measurement = cursor.getString(21);
                taskLineItem.OldTakenNow = cursor.getString(23).equals("1") ? true : false;
                taskLineItem.ShopAddress = cursor.getString(25);
                taskLineItem.NotDoneReason = cursor.getString(24);
                taskLineItemList.add(taskLineItem);
            } while (cursor.moveToNext());
        }
        database.close();
        return taskLineItemList;
    }

    public TaskLineItemViewModel getTaskLineItemInfo(String id) {
        TaskLineItemViewModel taskLineItem = new TaskLineItemViewModel();
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM TaskLineItemRequest where ID='"
                + id + "'";
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                taskLineItem.ID = Integer.parseInt(cursor.getString(0));
                taskLineItem.Form = cursor.getString(14);
                taskLineItem.Height = cursor.getString(12);
                taskLineItem.Instruction = cursor.getString(6);
                taskLineItem.Lat = cursor.getString(18);
                taskLineItem.Lon = cursor.getString(19);
                taskLineItem.NewImage = cursor.getString(9);
                taskLineItem.OldImage = cursor.getString(8);
                taskLineItem.Photo = cursor.getString(15);
                taskLineItem.PhotoID = cursor.getString(20);
                taskLineItem.Remove = cursor.getString(17);
                taskLineItem.ShopName = cursor.getString(4);
                taskLineItem.ShopWall = cursor.getString(5);
                taskLineItem.Side = cursor.getString(13);
                taskLineItem.Status = cursor.getString(16);
                taskLineItem.Time = cursor.getString(22);
                taskLineItem.Type = cursor.getString(10);
                taskLineItem.Width = cursor.getString(11);
                taskLineItem.ShopID = Integer.parseInt(cursor.getString(2));
                taskLineItem.ShopWallID = Integer.parseInt(cursor.getString(3));
                taskLineItem.TaskID = Integer.parseInt(cursor.getString(1));
                taskLineItem.WallID = Integer.parseInt(cursor.getString(7));
                taskLineItem.Measurement = cursor.getString(21);
                taskLineItem.OldTakenNow = cursor.getString(23).equals("1") ? true
                        : false;
                taskLineItem.NotDoneReason = cursor.getString(24);
                taskLineItem.ShopAddress = cursor.getString(25);
            } while (cursor.moveToNext());
        }
        database.close();
        return taskLineItem;
    }

    public ArrayList<TaskLineItemViewModel> getTaskLineItems(String condition) {
        ArrayList<TaskLineItemViewModel> taskLineItemList = new ArrayList<TaskLineItemViewModel>();
        String selectQuery = "SELECT * FROM TaskLineItemRequest where "
                + condition;
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                TaskLineItemViewModel taskLineItem = new TaskLineItemViewModel();

                taskLineItem.ID = Integer.parseInt(cursor.getString(0));
                taskLineItem.Form = cursor.getString(14);
                taskLineItem.Height = cursor.getString(12);
                taskLineItem.Instruction = cursor.getString(6);
                taskLineItem.Lat = cursor.getString(18);
                taskLineItem.Lon = cursor.getString(19);
                taskLineItem.NewImage = cursor.getString(9);
                taskLineItem.OldImage = cursor.getString(8);
                taskLineItem.Photo = cursor.getString(15);
                taskLineItem.PhotoID = cursor.getString(20);
                taskLineItem.Remove = cursor.getString(17);
                taskLineItem.ShopName = cursor.getString(4);
                taskLineItem.ShopWall = cursor.getString(5);
                taskLineItem.Side = cursor.getString(13);
                taskLineItem.Status = cursor.getString(16);
                taskLineItem.Time = cursor.getString(22);
                taskLineItem.Type = cursor.getString(10);
                taskLineItem.Width = cursor.getString(11);
                taskLineItem.ShopID = Integer.parseInt(cursor.getString(2));
                taskLineItem.ShopWallID = Integer.parseInt(cursor.getString(3));
                taskLineItem.TaskID = Integer.parseInt(cursor.getString(1));
                taskLineItem.WallID = Integer.parseInt(cursor.getString(7));
                taskLineItem.Measurement = cursor.getString(21);
                taskLineItem.ShopAddress = cursor.getString(25);
                taskLineItem.OldTakenNow = cursor.getString(23).equals("1") ? true
                        : false;
                taskLineItem.NotDoneReason = cursor.getString(24);

                taskLineItemList.add(taskLineItem);
            } while (cursor.moveToNext());
        }
        database.close();
        return taskLineItemList;
    }

    public void insertTask(TaskViewModel task) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TaskID", task.ID);
        values.put("EmployeeName", task.EmployeeName);
        values.put("ShopAddress", task.ShopAddress);
        values.put("ShopBranch", task.ShopBranch);
        values.put("ShopName", task.ShopName);
        values.put("EmployeeID", task.EmployeeID);
        values.put("ShopRegion", task.ShopRegion);
        values.put("TaskStatus", task.TaskStatus);
        values.put("ShopID", task.ShopID);
        values.put("IsMeasurement", task.IsMeasurement ? 1 : 0);
        database.insert("TaskRequest", null, values);
        database.close();
    }

    public int updateTask(TaskViewModel task, boolean includePhoneUpdates) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("EmployeeName", task.EmployeeName);
        values.put("ShopAddress", task.ShopAddress);
        values.put("ShopBranch", task.ShopBranch);
        values.put("ShopName", task.ShopName);
        values.put("EmployeeID", task.EmployeeID);
        values.put("ShopRegion", task.ShopRegion);
        values.put("TaskStatus", task.TaskStatus);
        values.put("ShopID", task.ShopID);
        values.put("IsMeasurement", task.IsMeasurement ? 1 : 0);
        if (includePhoneUpdates) {
            values.put("StartTime", task.StartTime);
            values.put("PhotoID", task.PhotoID);
            values.put("ShopPhotoUploaded", task.ShopPhotoUploaded ? 1 : 0);
        }
        int result = database.update("TaskRequest", values, "TaskID" + " = ?",
                new String[]{String.valueOf(task.ID)});
        database.close();
        return result;
    }

    public void deleteTask(String id) {
        Log.d(LOGCAT, "delete");
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM TaskRequest where TaskID=" + id;
        Log.d("query", deleteQuery);
        database.execSQL(deleteQuery);
        database.close();
    }

    public ArrayList<TaskViewModel> getAllTasks() {
        ArrayList<TaskViewModel> taskList = new ArrayList<TaskViewModel>();
        String selectQuery = "SELECT * FROM TaskRequest order by ShopName";
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                TaskViewModel task = new TaskViewModel();

                task.ID = Integer.parseInt(cursor.getString(0));
                String temp = cursor.getString(1);
                if (temp == null || temp.isEmpty())
                    task.ShopID = 0;
                else
                    task.ShopID = Integer.parseInt(temp);
                task.ShopName = cursor.getString(2);
                task.ShopBranch = cursor.getString(3);
                task.ShopRegion = cursor.getString(4);
                task.ShopCity = cursor.getString(5);
                task.ShopAddress = cursor.getString(6);
                task.EmployeeName = cursor.getString(7);
                task.EmployeeID = Integer.parseInt(cursor.getString(8));
                task.TaskStatus = cursor.getString(9);
                task.IsMeasurement = Integer.parseInt(cursor.getString(10)) == 1 ? true
                        : false;
                task.PhotoID = cursor.getString(11);
                task.StartTime = cursor.getString(12);
                temp = cursor.getString(13);
                if (temp == null || temp.isEmpty())
                    task.ShopPhotoUploaded = false;
                else
                    task.ShopPhotoUploaded = Integer.parseInt(temp) == 1 ? true
                            : false;
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        database.close();
        return taskList;
    }

    public TaskViewModel getTaskInfo(String id) {
        TaskViewModel task = new TaskViewModel();
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM TaskRequest where TaskID='" + id
                + "'";
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                task.ID = Integer.parseInt(cursor.getString(0));
                String temp = cursor.getString(1);
                if (temp == null || temp.isEmpty())
                    task.ShopID = 0;
                else
                    task.ShopID = Integer.parseInt(temp);
                task.ShopName = cursor.getString(2);
                task.ShopBranch = cursor.getString(3);
                task.ShopRegion = cursor.getString(4);
                task.ShopCity = cursor.getString(5);
                task.ShopAddress = cursor.getString(6);
                task.EmployeeName = cursor.getString(7);
                task.EmployeeID = Integer.parseInt(cursor.getString(8));
                task.TaskStatus = cursor.getString(9);
                task.IsMeasurement = Integer.parseInt(cursor.getString(10)) == 1 ? true
                        : false;
                task.PhotoID = cursor.getString(11);
                task.StartTime = cursor.getString(12);
                temp = cursor.getString(13);
                if (temp == null || temp.isEmpty())
                    task.ShopPhotoUploaded = false;
                else
                    task.ShopPhotoUploaded = Integer.parseInt(temp) == 1 ? true
                            : false;
            } while (cursor.moveToNext());
        }
        database.close();
        return task;
    }

    public ArrayList<TaskViewModel> getTasks(String condition) {
        ArrayList<TaskViewModel> taskList = new ArrayList<TaskViewModel>();

        String selectQuery = "SELECT * FROM TaskRequest where " + condition
                + "  order by ShopName";
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                TaskViewModel task = new TaskViewModel();

                task.ID = Integer.parseInt(cursor.getString(0));
                String temp = cursor.getString(1);
                if (temp == null || temp.isEmpty())
                    task.ShopID = 0;
                else
                    task.ShopID = Integer.parseInt(temp);

                task.ShopName = cursor.getString(2);
                task.ShopBranch = cursor.getString(3);
                task.ShopRegion = cursor.getString(4);
                task.ShopCity = cursor.getString(5);
                task.ShopAddress = cursor.getString(6);
                task.EmployeeName = cursor.getString(7);
                task.EmployeeID = Integer.parseInt(cursor.getString(8));
                task.TaskStatus = cursor.getString(9);
                task.IsMeasurement = Integer.parseInt(cursor.getString(10)) == 1 ? true
                        : false;
                task.PhotoID = cursor.getString(11);
                task.StartTime = cursor.getString(12);
                temp = cursor.getString(13);
                if (temp == null || temp.isEmpty())
                    task.ShopPhotoUploaded = false;
                else
                    task.ShopPhotoUploaded = Integer.parseInt(temp) == 1 ? true
                            : false;
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        database.close();
        return taskList;
    }

    public ArrayList<TaskViewModel> getPendingTasks(String condition) {
        ArrayList<TaskViewModel> taskList = new ArrayList<TaskViewModel>();
        try {
            String selectQuery = "SELECT DISTINCT TaskRequest.* FROM TaskRequest  JOIN TaskLineItemRequest on TaskLineItemRequest.TaskID = TaskRequest.TaskID "
                    + " WHERE "
                    + condition
                    + " AND TaskRequest.IsMeasurement = 0 Order by ShopName";
            SQLiteDatabase database = this.getReadableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    TaskViewModel task = new TaskViewModel();

                    task.ID = Integer.parseInt(cursor.getString(0));
                    String temp = cursor.getString(1);
                    if (temp == null || temp.isEmpty())
                        task.ShopID = 0;
                    else
                        task.ShopID = Integer.parseInt(temp);

                    task.ShopName = cursor.getString(2);
                    task.ShopBranch = cursor.getString(3);
                    task.ShopRegion = cursor.getString(4);
                    task.ShopCity = cursor.getString(5);
                    task.ShopAddress = cursor.getString(6);
                    task.EmployeeID = Integer.parseInt(cursor.getString(8));
                    task.EmployeeName = cursor.getString(7);
                    task.TaskStatus = cursor.getString(9);
                    task.IsMeasurement = Integer.parseInt(cursor.getString(10)) == 1 ? true
                            : false;
                    task.PhotoID = cursor.getString(11);
                    task.StartTime = cursor.getString(12);

                    temp = cursor.getString(13);
                    if (temp == null || temp.isEmpty())
                        task.ShopPhotoUploaded = false;
                    else
                        task.ShopPhotoUploaded = Integer.parseInt(temp) == 1 ? true
                                : false;
                    taskList.add(task);
                } while (cursor.moveToNext());
            }
            database.close();
            ArrayList<TaskViewModel> measurementTaskList = getPendingMeasurementTasks(condition);
            taskList.addAll(measurementTaskList);
        } catch (Exception e) {
            String m = e.getLocalizedMessage();
        }
        return taskList;
    }

    public ArrayList<TaskViewModel> getPendingMeasurementTasks(String condition) {
        ArrayList<TaskViewModel> taskList = new ArrayList<TaskViewModel>();
        String selectQuery = "SELECT DISTINCT TaskRequest.* FROM TaskRequest "
                + " WHERE " + condition
                + " AND TaskRequest.IsMeasurement = 1 Order by ShopName";
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                TaskViewModel task = new TaskViewModel();

                task.ID = Integer.parseInt(cursor.getString(0));
                String temp = cursor.getString(1);
                if (temp == null || temp.isEmpty())
                    task.ShopID = 0;
                else
                    task.ShopID = Integer.parseInt(temp);

                task.ShopName = cursor.getString(2);
                task.ShopBranch = cursor.getString(3);
                task.ShopRegion = cursor.getString(4);
                task.ShopCity = cursor.getString(5);
                task.ShopAddress = cursor.getString(6);
                task.EmployeeID = Integer.parseInt(cursor.getString(8));
                task.EmployeeName = cursor.getString(7);
                task.TaskStatus = cursor.getString(9);
                task.IsMeasurement = Integer.parseInt(cursor.getString(10)) == 1 ? true
                        : false;
                task.PhotoID = cursor.getString(11);
                task.StartTime = cursor.getString(12);

                temp = cursor.getString(13);
                if (temp == null || temp.isEmpty())
                    task.ShopPhotoUploaded = false;
                else
                    task.ShopPhotoUploaded = Integer.parseInt(temp) == 1 ? true
                            : false;
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        database.close();
        return taskList;
    }

    public long insertMeasurementPhoto(MeasurementPhoto measurementPhoto) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // values.put("ID", measurementPhoto.ID);
        values.put("TaskID", measurementPhoto.TaskID);
        values.put("PhotoID", measurementPhoto.PhotoID);
        values.put("MeasurementString", measurementPhoto.MeasurementString);
        values.put("Time", measurementPhoto.Time);
        values.put("ShopID", measurementPhoto.ShopID);
        values.put("Lat", measurementPhoto.Lat);
        values.put("Lon", measurementPhoto.Lon);
        values.put("ShopName", measurementPhoto.ShopName);
        values.put("ShopAddress", measurementPhoto.ShopAddress);
        long id = database.insert("MeasurementPhoto", null, values);
        database.close();
        return id;
    }

    public int updateMeasurementPhoto(MeasurementPhoto measurementPhoto,
                                      boolean includePhoneUpdates) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TaskID", measurementPhoto.TaskID);
        values.put("PhotoID", measurementPhoto.PhotoID);
        values.put("MeasurementString", measurementPhoto.MeasurementString);
        values.put("Time", measurementPhoto.Time);
        values.put("ShopID", measurementPhoto.ShopID);

        values.put("Lat", measurementPhoto.Lat);
        values.put("Lon", measurementPhoto.Lon);

        values.put("ShopName", measurementPhoto.ShopName);
        values.put("ShopAddress", measurementPhoto.ShopAddress);
        int result = database.update("MeasurementPhoto", values, "ID" + " = ?",
                new String[]{String.valueOf(measurementPhoto.ID)});
        database.close();
        return measurementPhoto.ID;
    }

    public void deleteMeasurementPhoto(String id) {


        String ID[] = id.split(",");
        Log.d(LOGCAT, "delete");
        int value = Integer.parseInt(ID[0]);
        if (value > 0) {
            DeleteRelatedMeasurementPhoto(ID[0]);
            SQLiteDatabase database = this.getWritableDatabase();
            String deleteQuery = "DELETE FROM MeasurementPhoto where ID=" + ID[0];
            Log.d("query", deleteQuery);
            database.execSQL(deleteQuery);
            //database.close();
            String deleteTaskQuery = "DELETE FROM TaskRequest where TaskID=" + ID[1];//Shared.MeasurementTaskID;
            Log.d("query", deleteTaskQuery);
            database.execSQL(deleteTaskQuery);
            database.close();
        }


    }

    private void DeleteRelatedMeasurementPhoto(String id) {
        MeasurementPhoto photo = getMeasurementPhotoInfo(id);
        PhotoDeleteHelper.DeletePhoto(photo.PhotoID);
    }

    public ArrayList<MeasurementPhoto> getAllMeasurementPhotos() {
        ArrayList<MeasurementPhoto> measurementPhotoList = new ArrayList<MeasurementPhoto>();
        String selectQuery = "SELECT * FROM MeasurementPhoto";
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                MeasurementPhoto measurementPhoto = new MeasurementPhoto();

                measurementPhoto.ID = Integer.parseInt(cursor.getString(0));
                measurementPhoto.Time = cursor.getString(4);
                measurementPhoto.MeasurementString = cursor.getString(3);
                measurementPhoto.PhotoID = cursor.getString(2);
                measurementPhoto.TaskID = Integer.parseInt(cursor.getString(1));
                measurementPhoto.ShopID = cursor.getString(5);
                measurementPhoto.Lat = cursor.getString(6);
                measurementPhoto.Lon = cursor.getString(7);
                measurementPhoto.ShopName = cursor.getString(8);
                measurementPhoto.ShopAddress = cursor.getString(9);
                measurementPhotoList.add(measurementPhoto);
            } while (cursor.moveToNext());
        }
        database.close();
        return measurementPhotoList;
    }

    public MeasurementPhoto getMeasurementPhotoInfo(String id) {
        MeasurementPhoto measurementPhoto = new MeasurementPhoto();
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM MeasurementPhoto where ID='" + id
                + "'";
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                measurementPhoto.ID = Integer.parseInt(cursor.getString(0));
                measurementPhoto.Time = cursor.getString(4);
                measurementPhoto.MeasurementString = cursor.getString(3);
                measurementPhoto.PhotoID = cursor.getString(2);
                measurementPhoto.TaskID = Integer.parseInt(cursor.getString(1));
                measurementPhoto.ShopID = cursor.getString(5);
                measurementPhoto.Lat = cursor.getString(6);
                measurementPhoto.Lon = cursor.getString(7);
                measurementPhoto.ShopName = cursor.getString(8);
                measurementPhoto.ShopAddress = cursor.getString(9);
            } while (cursor.moveToNext());
        }
        database.close();
        return measurementPhoto;
    }

    public ArrayList<MeasurementPhoto> getMeasurementPhotos(String condition) {
        ArrayList<MeasurementPhoto> measurementPhotoList = new ArrayList<MeasurementPhoto>();
        String selectQuery = "SELECT * FROM MeasurementPhoto where "
                + condition;
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                MeasurementPhoto measurementPhoto = new MeasurementPhoto();

                measurementPhoto.ID = Integer.parseInt(cursor.getString(0));
                measurementPhoto.Time = cursor.getString(4);
                measurementPhoto.MeasurementString = cursor.getString(3);
                measurementPhoto.PhotoID = cursor.getString(2);
                measurementPhoto.TaskID = Integer.parseInt(cursor.getString(1));
                measurementPhoto.ShopID = cursor.getString(5);
                measurementPhoto.Lat = cursor.getString(6);
                measurementPhoto.Lon = cursor.getString(7);
                measurementPhoto.ShopName = cursor.getString(8);
                measurementPhoto.ShopAddress = cursor.getString(9);
                measurementPhotoList.add(measurementPhoto);
            } while (cursor.moveToNext());
        }
        database.close();
        return measurementPhotoList;
    }

    public long saveMeasurementPhoto(MeasurementPhoto measurementPhoto,
                                     boolean includePhoneUpdates) {
        String selectQuery = "SELECT * FROM MeasurementPhoto where ID = "
                + measurementPhoto.ID;
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        int count = cursor.getCount();
        database.close();
        if (count == 0) {
            return insertMeasurementPhoto(measurementPhoto);
        } else {
            return updateMeasurementPhoto(measurementPhoto, includePhoneUpdates);
        }
    }

    public void saveEmployee(EmployeeViewModel employee) {
        String selectQuery = "SELECT * FROM Employee where EmpID = "
                + employee.ID;
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        int count = cursor.getCount();
        database.close();
        if (count == 0) {
            insertEmployee(employee);
        } else {
            updateEmployee(employee);
        }
    }

    public void saveTask(TaskViewModel task, boolean includePhoneUpdates) {
        String selectQuery = "SELECT * FROM TaskRequest where TaskID = "
                + task.ID;
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        int count = cursor.getCount();
        database.close();
        if (count == 0) {
            insertTask(task);
        } else {
            updateTask(task, includePhoneUpdates);
        }
    }

    public void saveTaskLineItem(TaskLineItemViewModel taskLineItem,
                                 boolean includePhoneUpdates) {
        String selectQuery = "SELECT * FROM TaskLineItemRequest where ID = "
                + taskLineItem.ID;
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        int count = cursor.getCount();
        database.close();
        if (count == 0) {
            insertTaskLineItem(taskLineItem);
        } else {
            updateTaskLineItem(taskLineItem, includePhoneUpdates);
        }
    }

    public ArrayList<TaskLineItemViewModel> getPendingShopPhotos() {
        ArrayList<TaskLineItemViewModel> taskLineItemList = new ArrayList<TaskLineItemViewModel>();
        String selectQuery = "SELECT * FROM TaskRequest where (ShopPhotoUploaded is null OR ShopPhotoUploaded = 0) AND PhotoID is not null";
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                TaskLineItemViewModel taskLineItem = new TaskLineItemViewModel();

                taskLineItem.ID = Integer.parseInt(cursor.getString(0));
                taskLineItem.PhotoID = cursor.getString(11);
                taskLineItem.Time = cursor.getString(12);
                taskLineItem.NewImage = "";
                taskLineItem.ShopName = cursor.getString(3);
                taskLineItem.ShopAddress = cursor.getString(7);
                taskLineItemList.add(taskLineItem);
            } while (cursor.moveToNext());
        }
        database.close();
        return taskLineItemList;
    }

    public int shopPhotoUploaded(String taskId) {
        DeleteRelatedShopPhoto(taskId);
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ShopPhotoUploaded", 1);
        int result = database.update("TaskRequest", values, "TaskID" + " = ?",
                new String[]{taskId});
        database.close();
        return result;
    }

    public int WallPhotoMissing(String taskLineItemID) {
        DeleteRelatedShopPhoto(taskLineItemID);
        SQLiteDatabase database = this.getWritableDatabase();
        //onUpgrade(database,1,2);
        ContentValues values = new ContentValues();
        values.put("NotDoneReason", "MISSING_WALL_PHOTO");
        int result = database.update("TaskLineItemRequest", values, "ID" + " = ?",
                new String[]{taskLineItemID});
        database.close();
        return result;
    }

    private void DeleteRelatedShopPhoto(String taskLineItemID) {
        TaskLineItemViewModel taskLineItemViewModel = getTaskLineItemInfo(taskLineItemID);
        TaskViewModel taskViewModel = getTaskInfo(String.valueOf(taskLineItemViewModel.TaskID));
        PhotoDeleteHelper.DeletePhoto(taskViewModel.PhotoID);
    }

    public void ClearDatabase() {
        SQLiteDatabase database = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM MeasurementPhoto";
        database.execSQL(deleteQuery);
        deleteQuery = "DELETE FROM TaskLineItemRequest";
        database.execSQL(deleteQuery);
        deleteQuery = "DELETE FROM TaskRequest";
        database.execSQL(deleteQuery);
        database.close();

    }

    public int getNextDynamicShopID() {
        int result = 0;
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT MIN(TaskID) FROM TaskRequest";
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                result = cursor.getInt(0);
            } while (cursor.moveToNext());
        }
        database.close();
        if (result >= 0)
            return -1;
        else
            return result - 1;
    }
}