package com.tt.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.sstracker.R;
import com.tt.data.TaskViewModel;

/**
 * Adapts NewsEntry objects onto views for lists
 */
public final class TaskListAdapter extends ArrayAdapter<TaskViewModel> {

    private final int taskListLayoutResource;
    Context mapContext;

    public TaskListAdapter(final Context context,
                           final int _taskListLayoutResource) {
        super(context, 0);
        mapContext = context;
        this.taskListLayoutResource = _taskListLayoutResource;
    }

    @Override
    public View getView(final int position, final View convertView,
                        final ViewGroup parent) {

        // We need to get the best view (re-used if possible) and then
        // retrieve its corresponding ViewHolder, which optimizes lookup
        // efficiency
        final View view = getWorkingView(convertView);
        final ViewHolder viewHolder = getViewHolder(view);
        final TaskViewModel entry = getItem(position);
        //viewHolder.imgMap.setTag(position);
        // Setting the title view is straightforward
        viewHolder.txtShopName.setText(entry.ShopName);
        viewHolder.txtShopAddress.setText(entry.ShopAddress);

        /**viewHolder.imgMap.setOnClickListener(new OnClickListener()
         {

         public void onClick(View v)
         {

         int pos=(Integer)v.getTag();
         int pos1=position;
         Shared.selectedShopAddress=entry.ShopAddress;
         if(Shared.selectedShopAddress==null||Shared.selectedShopAddress.isEmpty())
         {
         SstAlert.Show(mapContext, "No Address found",
         "No Shop Address Found");
         }
         else
         {
         Intent intent = new Intent(mapContext, mapactivity.class);

         mapContext.startActivity(intent);
         }
         }

         });
         */


        return view;
    }

    private View getWorkingView(final View convertView) {
        // The workingView is basically just the convertView re-used if possible
        // or inflated new if not possible
        View workingView = null;

        if (null == convertView) {
            final Context context = getContext();
            final LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            workingView = inflater.inflate(taskListLayoutResource, null);
        } else {
            workingView = convertView;
        }

        return workingView;
    }

    private ViewHolder getViewHolder(final View workingView) {
        // The viewHolder allows us to avoid re-looking up view references
        // Since views are recycled, these references will never change
        final Object tag = workingView.getTag();
        ViewHolder viewHolder = null;

        if (null == tag || !(tag instanceof ViewHolder)) {
            viewHolder = new ViewHolder();

            viewHolder.txtShopName = (TextView) workingView
                    .findViewById(R.id.tvShopName);
            viewHolder.txtShopAddress = (TextView) workingView
                    .findViewById(R.id.tvAddress);
            // viewHolder.imgMap=(ImageView)workingView.findViewById(R.id.imgmap);

            workingView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) tag;
        }

        return viewHolder;
    }

    /**
     * ViewHolder allows us to avoid re-looking up view references Since views
     * are recycled, these references will never change
     */
    private static class ViewHolder {
        public TextView txtShopName;
        public TextView txtShopAddress;
        //public ImageView imgMap;
    }

}