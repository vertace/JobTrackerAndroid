package com.tt.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tt.data.RfeViewModel;
import com.tt.data.TaskViewModel;
import com.tt.jobtracker.R;

/**
 * Created by BS-308 on 5/6/2015.
 */
public final  class RfeListAdapter extends ArrayAdapter<RfeViewModel> {

    private final int rfeListLayoutResource;
    Context mapContext;

    public RfeListAdapter(final Context context,
                           final int _rfeListLayoutResource) {
        super(context, 0);
        mapContext = context;
        this.rfeListLayoutResource = _rfeListLayoutResource;
    }
    @Override
    public View getView(final int position, final View convertView,
                        final ViewGroup parent) {

        // We need to get the best view (re-used if possible) and then
        // retrieve its corresponding ViewHolder, which optimizes lookup
        // efficiency
        final View view = getWorkingView(convertView);
        final ViewHolder viewHolder = getViewHolder(view);
        final RfeViewModel entry = getItem(position);
        //viewHolder.imgMap.setTag(position);
        // Setting the title view is straightforward
       /*  TaskViewModel showUploadModel=Shared.ShowProgressForselectedTask;
        if(entry.IsDone && entry.ID==showUploadModel.ID) {
            viewHolder.txtStatus.setText("Up");
        }*/
        viewHolder.txtRfeName.setText(entry.FullName);

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

            workingView = inflater.inflate(rfeListLayoutResource, null);
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

            viewHolder.txtRfeName = (TextView) workingView
                    .findViewById(R.id.tvRfeName);

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
        public TextView txtRfeName;
        public TextView txtCode;

    }
}
