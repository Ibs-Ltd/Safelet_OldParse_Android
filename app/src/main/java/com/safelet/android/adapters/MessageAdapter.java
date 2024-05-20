package com.safelet.android.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.safelet.android.R;
import com.safelet.android.models.Message;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MessageAdapter extends ArrayAdapter<Message> {

    public MessageAdapter(@NonNull Context context) {
        super(context, -1);
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType().ordinal();
    }

    @Override
    public int getViewTypeCount() {
        return Message.Type.values().length;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final View view;
        final ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(getItemViewType(position) == Message.Type.INCOMING.ordinal() ?
                    R.layout.message_received_item : R.layout.message_sent_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.image = (ImageView) view.findViewById(R.id.image);
            viewHolder.name = (TextView) view.findViewById(R.id.name);
            viewHolder.message = (TextView) view.findViewById(R.id.text);
            viewHolder.date = (TextView) view.findViewById(R.id.date);

            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        Message message = getItem(position);

        viewHolder.message.setText(message.getText());
        viewHolder.date.setText(DateFormat.getTimeInstance(SimpleDateFormat.SHORT).format(new Date(message.getTimestamp())));
        viewHolder.name.setText(message.getSender().getName());

        if (!TextUtils.isEmpty(message.getSender().getImage())) {
            Picasso.get().load(message.getSender().getImage())
                    .resize((int) getContext().getResources().getDimension(R.dimen.material_layout_avatar),
                            (int) getContext().getResources().getDimension(R.dimen.material_layout_avatar))
                    .placeholder(R.drawable.generic_icon)
                    .error(R.drawable.generic_icon).into(viewHolder.image);
        } else {
            viewHolder.image.setImageResource(R.drawable.generic_icon);
        }

        return view;
    }

    private class ViewHolder {
        ImageView image;
        TextView name;
        TextView message;
        TextView date;
    }
}
