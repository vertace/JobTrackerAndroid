package com.tt.data;

import java.io.Serializable;
import java.util.ArrayList;

public class TaskViewModel implements Serializable {

    public int ID;

    public int ShopID;
    public int EmployeeID;

    public String ShopName;
    public String ShopBranch;
    public String ShopRegion;
    public String ShopCity;
    public String ShopAddress;

    public String EmployeeName;
    public String TaskStatus;

    public boolean IsMeasurement;

    public ArrayList<TaskLineItemViewModel> TaskLineItemViewModelList;

    public String PhotoID;
    public String StartTime;
    public boolean ShopPhotoUploaded;

}
