package com.tt.data;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class Shared {


    //public static String WebAPI = "http://sunsigns.azurewebsites.net/";
    //public static String WebAPI = "http://emtracker.specialeffect.co.in/";
   // public static String WebAPI = "http://192.168.0.108:1989/";
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


    public static String SaveRegistrationID=WebAPI+"Task/SaveRegistrationID";

    public static EmployeeViewModel LoggedInUser;
    public static List<TaskViewModel> TaskList;

    public static List<TaskLineItemViewModel> TaskDetail;
    public static MeasurementPhoto MeasurementUploadPhoto;

    public static TaskLineItemPhotoViewModel SelecteduploadTasklineitemPhotos;

    public static double lat;
    public static double lon;
    public static int MeasurementTaskID;
    public static Bitmap MeasurementBitmap;

    public static List<MeasurementPhoto> MeasurementPhotoList;

    public static List<EmployeeViewModel> EmployeeList;

    public static TaskViewModel SelectedTask;

    public static String OldImagesFile;

    public static String OrderByTask;

    public static long sharedCheckinID;

    public static ArrayList<String> html_instructions;
    public static ArrayList<String>  end_address;
    public static String selectedShopAddress;

    public static String sharedprefs_uploadstatus="Uploadonwifi";

    public static String sharedprefs_switchstatus="WifiOnOff";

    public static String Username;
    public static String Password;
    public static String TaskSync;
    public static String MainClassCall;
    public static String UploadResponse;

    public static String GCM_SENDER_ID = "85852809473";
    public static String hideMenu;
    static {
        LoggedInUser = new EmployeeViewModel();
        LoggedInUser.ID = "2104";
    }

    public static String GetLocationString() {
        return String.valueOf(lat) + ":" + String.valueOf(lon);
    }
}
