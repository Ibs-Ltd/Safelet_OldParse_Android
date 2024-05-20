package com.safelet.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseFile;
import com.safelet.android.R;
import com.safelet.android.models.UserModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public final class MyConnectionsAdapter extends BaseAdapter {
    private Context mContext;
    private ConnectionsType connectionsType = ConnectionsType.GUARDIANS;
    private List<UserModel> usersList = new ArrayList<>();

    public MyConnectionsAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        if (ConnectionsType.GUARDIANS.equals(connectionsType)) {
            return usersList.size() + 1;
        }
        return usersList.size();
    }

    @Override
    public UserModel getItem(int index) {
        if (connectionsType.equals(ConnectionsType.GUARDIANS)) {
            return usersList.get(index - 1);
        }
        return usersList.get(index);
    }

    @Override
    public int getItemViewType(int position) {
        return connectionsType.equals(ConnectionsType.GUARDIANS) && position == 0 ? GuardiansViewType.ADD.ordinal() : GuardiansViewType.NORMAL.ordinal();
    }

    @Override
    public int getViewTypeCount() {
        return connectionsType.equals(ConnectionsType.GUARDIANS) ? GuardiansViewType.values().length : 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_myconnection, viewGroup, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.textView1);
            holder.photo = (ImageView) convertView.findViewById(R.id.imageView1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (getItemViewType(position) == GuardiansViewType.ADD.ordinal()) {
            holder.name.setText("");
            holder.photo.setImageResource(R.drawable.selector_add_icon);
        } else {
            UserModel model = getItem(position);
            // Displays connection
            holder.name.setText(model.getName());
            // Loads connection model image
            ParseFile photoFile = model.getUserImage();
            String photoUrl = photoFile != null ? photoFile.getUrl() : null;
            Picasso.get().load(photoUrl)
                    .placeholder(R.drawable.generic_icon)
                    .error(R.drawable.generic_icon).into(holder.photo);
        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void setItems(List<UserModel> usersList, ConnectionsType connectionsType) {
        this.connectionsType = connectionsType;
        this.usersList = usersList;
        notifyDataSetChanged();
    }

    public ConnectionsType getConnectionsType() {
        return connectionsType;
    }

    private static class ViewHolder {
        private TextView name;
        private ImageView photo;
    }

    public enum ConnectionsType {
        GUARDIANS, GUARDED
    }

    private enum GuardiansViewType {
        NORMAL, ADD
    }
}
