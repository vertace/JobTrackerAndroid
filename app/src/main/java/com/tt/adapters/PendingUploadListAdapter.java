package com.tt.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.sstracker.R;
import com.tt.data.TaskLineItemViewModel;

/**
 * Adapts NewsEntry objects onto views for lists
 */
public final class PendingUploadListAdapter extends
        ArrayAdapter<TaskLineItemViewModel> {

    private final int taskDetailLayoutResource;

    public PendingUploadListAdapter(final Context context,
                                    final int _taskDetailLayoutResource) {
        super(context, 0);
        this.taskDetailLayoutResource = _taskDetailLayoutResource;
    }

    @Override
    public View getView(final int position, final View convertView,
                        final ViewGroup parent) {

        // We need to get the best view (re-used if possible) and then
        // retrieve its corresponding ViewHolder, which optimizes lookup
        // efficiency
        final View view = getWorkingView(convertView);
        final ViewHolder viewHolder = getViewHolder(view);
        final TaskLineItemViewModel entry = getItem(position);

        // Setting the title view is straightforward
        viewHolder.tvShopWall.setText(entry.ShopName);
        viewHolder.tvInstruction.setText(entry.ShopAddress);
        setRowColor(view, entry.NewImage);
        return view;
    }

    private void setRowColor(View view, String var) {
        if (var == null || var.isEmpty()) {
            view.setBackgroundResource(R.drawable.listpending_selector);
        } else {
            view.setBackgroundResource(R.drawable.listcompleted_selector);
        }
    }

    private View getWorkingView(final View convertView) {
        // The workingView is basically just the convertView re-used if possible
        // or inflated new if not possible
        View workingView = null;

        if (null == convertView) {
            final Context context = getContext();
            final LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            workingView = inflater.inflate(taskDetailLayoutResource, null);
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

            viewHolder.tvShopWall = (TextView) workingView
                    .findViewById(R.id.tvShopWall);
            viewHolder.tvInstruction = (TextView) workingView
                    .findViewById(R.id.tvInstruction);

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
        public TextView tvShopWall;
        public TextView tvInstruction;
    }

}