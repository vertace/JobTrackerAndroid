package com.tt.data;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class TaskLineItemViewModel implements Serializable {
    public int ID;
    public int TaskID;

    public int ShopID;
    public int ShopWallID;

    public String ShopName;
    public String ShopWall;

    public String Instruction;

    public int WallID;

    public String OldImage;
    public String NewImage;

    public String Type;
    public String Width;
    public String Height;
    public String Side;
    public String Form;
    public String Photo;
    public String Status;
    public String Remove;
    public String ShopAddress;
    public String Lat;
    public String Lon;

    public String PhotoID;
    public String Time;
    public String Measurement;
    public Uri Uri;

    public boolean SaveToDB;
    public boolean OldTakenNow;

    public String NotDoneReason;

    public ArrayList<TaskLineItemPhotoViewModel> TaskLineItemPhotoViewModelList;

}
