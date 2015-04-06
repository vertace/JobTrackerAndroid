package com.tt.data;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class Shared {


    //public static String WebAPI = "http://sunsigns.azurewebsites.net/";
    //public static String WebAPI = "http://emtracker.specialeffect.co.in/";
    //public static String WebAPI = "http://192.168.0.126:1989/";
    public static String WebAPI = "http://jobtrack.azurewebsites.net/";
    public static String LoginAPI = WebAPI + "/Employee/Login";
    public static String TaskListAPI = WebAPI + "/Task/GetTasksForEmployeeNew";
    public static String TaskDetailAPI = WebAPI + "/Task/GetTaskDetail";
    public static String UploadAPI = WebAPI + "Task/UploadImageAzure";
    public static String UploadMeasurementPhotoAPI = WebAPI + "Task/UploadMeasurementImage";
    public static String GetMeasurementPhotoListAPI = WebAPI + "Task/GetMeasurementPhoto";

    public static String SaveMeasurementAPI = WebAPI + "Measurement/SaveMeasurementNew";

    public static String EmployeeListAPI = WebAPI + "Employee/AllEmployees";
    public static String UploadShopPhotoAPI = WebAPI + "Task/UploadShopPhotoAzure";


    public static EmployeeViewModel LoggedInUser;
    public static List<TaskViewModel> TaskList;

    public static List<TaskLineItemViewModel> TaskDetail;

    public static double lat;
    public static double lon;
    public static int MeasurementTaskID;
    public static Bitmap MeasurementBitmap;

    public static List<MeasurementPhoto> MeasurementPhotoList;

    public static List<EmployeeViewModel> EmployeeList;

    public static TaskViewModel SelectedTask;

    public static String OldImagesFile;

    public static long sharedCheckinID;

    public static ArrayList<String> html_instructions;

    public static String selectedShopAddress;

    static {
        LoggedInUser = new EmployeeViewModel();
        LoggedInUser.ID = "2104";
    }

    public static String GetLocationString() {
        return String.valueOf(lat) + ":" + String.valueOf(lon);
    }
}
