package com.tt.data;

import java.util.List;

public class EmployeeViewModel {
    public String ID;
    public String Name;
    public String Address;
    public String Remarks;
    public String Mobile1;
    public String Mobile2;
    public String Password;
    public String Username;
    public String GcmRegID;
    public String RegID;
    public boolean IsAdmin;
    public List<TaskNotDoneViewModel> taskNotDoneReasonList;
    public List<TaskLineItemNotDoneViewModel> taskLineItemNotDoneReasonList;
}
