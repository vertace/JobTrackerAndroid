package com.tt.sync;

import android.content.Context;

import com.tt.data.TaskLineItemViewModel;
import com.tt.data.TaskViewModel;
import com.tt.helpers.DatabaseHelper;
import com.tt.helpers.SstAlert;
import com.tt.jobtracker.ImageUploadAsync;

import java.util.ArrayList;

public class SyncHelperExecution {
    DatabaseHelper dbHelper;
    String TaskIDList, TaskLineItemIDList;
    int TotalItemsToUpload;
    int UploadedCount;
    private Context context;

    public SyncHelperExecution(Context _context) {
        context = _context;
        dbHelper = new DatabaseHelper(context);
    }

    public void addTasksToLocalDatabase(ArrayList<TaskViewModel> tasklist) {
        getTasskIdsFromTaskList(tasklist);
        TaskIDList = (String) TaskIDList
                .subSequence(1, TaskIDList.length() - 1);
        for (TaskViewModel task : tasklist) {
            dbHelper.saveTask(task, false);
            if (task.TaskLineItemViewModelList != null
                    && task.TaskLineItemViewModelList.size() > 0)
                for (TaskLineItemViewModel taskLineItem : task.TaskLineItemViewModelList) {
                    taskLineItem.ShopName = task.ShopName;
                    taskLineItem.ShopAddress = task.ShopAddress;
                    dbHelper.saveTaskLineItem(taskLineItem, false);
                }

        }
    }

    public void sendCompletedTasksToServer() {
        String condition = " PhotoID not null";
        ArrayList<TaskLineItemViewModel> taskLineItemList = dbHelper
                .getTaskLineItems(condition);
        TotalItemsToUpload = taskLineItemList.size();
        if (TotalItemsToUpload == 0) {
            SstAlert.Show(context, "Upload", "All pending photos uploaded");
        } else {
            for (TaskLineItemViewModel taskLineItem : taskLineItemList) {
                ImageUploadAsync imageUploader = new ImageUploadAsync(context, null);
                imageUploader.execute(taskLineItem);
            }
        }
    }

    public void getTasskIdsFromTaskList(ArrayList<TaskViewModel> tasklist) {

        TaskIDList = TaskLineItemIDList = "";
        for (TaskViewModel task : tasklist) {
            TaskIDList = TaskIDList + "," + task.ID;
            if (task.TaskLineItemViewModelList != null)
                for (TaskLineItemViewModel taskLineItem : task.TaskLineItemViewModelList) {
                    TaskLineItemIDList = TaskLineItemIDList + ","
                            + taskLineItem.ID;
                }
        }
    }
}
