package com.safelet.android.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.parse.ParseFile;
import com.safelet.android.R;
import com.safelet.android.interactors.PhoneContactsManager;
import com.safelet.android.models.ContactModel;
import com.safelet.android.models.UserModel;
import com.safelet.android.models.enums.SmsInviteStatus;
import com.safelet.android.models.enums.UserRelationStatus;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Adapter for displaying preferred contacts in a invite guardians list {@link}
 *
 * @author catalin
 */
public class GuardiansContactsListAdapter extends BaseAdapter implements StickyListHeadersAdapter {
    private Context context;
    private String userObjectId;
    private List<UserModel> userModels = new ArrayList<>();
    private List<UserModel> filteredUserModels = new ArrayList<>();
    private Filter filter = new ItemFilter();
    private ActionListener actionListener;

    public GuardiansContactsListAdapter(Context context, String userObjectId) {
        this.context = context;
        this.userObjectId = userObjectId;
    }

    @Override
    public int getCount() {
        if (filteredUserModels == null) {
            return 0;
        }
        return filteredUserModels.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GuardianContactsViewHolder mViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.cell_guardian_contact, parent, false);
            mViewHolder = new GuardianContactsViewHolder();
            mViewHolder.contactImageView = convertView.findViewById(R.id.cell_guardian_contacts_image_iv);
            mViewHolder.nameTextView = convertView.findViewById(R.id.cell_guardian_contacts_name_tv);
            mViewHolder.numberTextView = convertView.findViewById(R.id.cell_guardian_contacts_number_tv);
            mViewHolder.statusTextView = convertView.findViewById(R.id.cell_guardian_contacts_status_tv);
            mViewHolder.inviteContactButton = convertView.findViewById(R.id.cell_guardian_invite_contact);
            mViewHolder.cancelInviteContactButton = convertView.findViewById(R.id.cell_guardian_cancel_invite_contact);
            mViewHolder.requestGuardianButton = convertView.findViewById(R.id.cell_guardian_request_guardian);
            mViewHolder.cancelGuardianRequestButton = convertView.findViewById(R.id.cell_guardian_cancel_guardian_request);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (GuardianContactsViewHolder) convertView.getTag();
        }

        final UserModel model = filteredUserModels.get(position);
        // Sets contact name
        mViewHolder.nameTextView.setText(model.getName());
        mViewHolder.numberTextView.setText(model.getPhoneNumber());

        ParseFile userImage = model.getUserImage();

        // Loads contact picture
        if (userImage != null) {
            Picasso.get().load(userImage.getUrl())
                    .resize((int) context.getResources().getDimension(R.dimen.invite_guardian_image_size),
                            (int) context.getResources().getDimension(R.dimen.invite_guardian_image_size))
                    .into(mViewHolder.contactImageView);
        } else {
            ContactModel contact = PhoneContactsManager.instance().getContact(model.getPhoneNumber());
            Uri photoUri = contact != null ? contact.getPhotoUri() : null;
            if (photoUri == null) {
                // Loads default contact image
                mViewHolder.contactImageView.setImageResource(R.drawable.generic_icon);
            } else {
                Picasso.get().load(photoUri)
                        .resize((int) context.getResources().getDimension(R.dimen.invite_guardian_image_size),
                                (int) context.getResources().getDimension(R.dimen.invite_guardian_image_size))
                        .placeholder(R.drawable.generic_icon)
                        .error(R.drawable.generic_icon)
                        .into(mViewHolder.contactImageView);
            }
        }

        mViewHolder.statusTextView.setTextColor(ContextCompat.getColor(context, android.R.color.primary_text_light));

        if (model.isMember()) {
            UserRelationStatus userRelationStatus = model.getUserRelationStatus();

            if (userRelationStatus == UserRelationStatus.PENDING) {
                mViewHolder.requestGuardianButton.setVisibility(View.GONE);
                mViewHolder.cancelGuardianRequestButton.setVisibility(View.VISIBLE);
                mViewHolder.statusTextView.setText(R.string.contact_status_guardian_invited);
            } else {
                if (userRelationStatus == UserRelationStatus.ACCEPTED) {
                    mViewHolder.requestGuardianButton.setVisibility(View.GONE);
                    mViewHolder.statusTextView.setText(R.string.contact_status_your_guardian);
                    mViewHolder.statusTextView.setTextColor(ContextCompat.getColor(context, R.color.status_green));
                } else {
                    mViewHolder.requestGuardianButton.setVisibility(View.VISIBLE);
                    mViewHolder.statusTextView.setText(R.string.contact_status_member);
                }
                mViewHolder.cancelGuardianRequestButton.setVisibility(View.GONE);
            }

            mViewHolder.inviteContactButton.setVisibility(View.GONE);
            mViewHolder.cancelInviteContactButton.setVisibility(View.GONE);
        } else {
            mViewHolder.requestGuardianButton.setVisibility(View.GONE);
            mViewHolder.cancelGuardianRequestButton.setVisibility(View.GONE);
            if (model.getSmsInviteStatus() == SmsInviteStatus.NONE) {
                mViewHolder.statusTextView.setText(R.string.contact_status_not_member);
                mViewHolder.inviteContactButton.setVisibility(View.VISIBLE);
                mViewHolder.cancelInviteContactButton.setVisibility(View.GONE);
            } else {
                mViewHolder.statusTextView.setText(R.string.contact_status_contact_invited);
                mViewHolder.inviteContactButton.setVisibility(View.GONE);
                mViewHolder.cancelInviteContactButton.setVisibility(View.VISIBLE);
            }
        }

        mViewHolder.inviteContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionListener != null) {
                    actionListener.onInviteContact(model);
                }
            }
        });
        mViewHolder.cancelInviteContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionListener != null) {
                    actionListener.onCancelInviteContact(model);
                }
            }
        });
        mViewHolder.requestGuardianButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionListener != null) {
                    actionListener.onRequestGuarding(model);
                }
            }
        });
        mViewHolder.cancelGuardianRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionListener != null) {
                    actionListener.onCancelGuardingRequest(model);
                }
            }
        });

        return convertView;
    }

    @Override
    public UserModel getItem(int position) {
        return filteredUserModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        // Handles letters for contact list
        HeaderViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.header_guardian_contacts, parent, false);
            holder = new HeaderViewHolder();
            holder.headerTextView = (TextView) convertView.findViewById(R.id.header_guardian_contacts_letter_tv);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }
        String userName = filteredUserModels.get(position).getName();
        String firstLetter = userName.substring(0, 1).toUpperCase();
        holder.headerTextView.setText(firstLetter);
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        UserModel userModel = filteredUserModels.get(position);
        String name = userModel.getName();
        return name.toUpperCase(Locale.ENGLISH).charAt(0);
    }

    public void clear() {
        userModels.clear();
        filteredUserModels.clear();
        notifyDataSetChanged();
    }

    /**
     * Sets data source for this adapter
     *
     * @param users List data
     */
    public void setItems(List<UserModel> users) {
        this.userModels = users;
        this.filteredUserModels = users;
        notifyDataSetChanged();
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public Filter getFilter() {
        return filter;
    }

    public interface ActionListener {
        void onInviteContact(UserModel userModel);

        void onCancelInviteContact(UserModel model);

        void onRequestGuarding(UserModel userModel);

        void onCancelGuardingRequest(UserModel userModel);
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            final List<UserModel> filteredList = new ArrayList<UserModel>();
            String filterableName;
            for (UserModel userModel : userModels) {
                filterableName = userModel.getName();
                if (filterableName.toLowerCase().contains(filterString)) {
                    filteredList.add(userModel);
                }
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredUserModels = (ArrayList<UserModel>) results.values;
            notifyDataSetChanged();
        }

    }

    private static class HeaderViewHolder {
        TextView headerTextView;
    }

    private static class GuardianContactsViewHolder {
        ImageView contactImageView;
        TextView nameTextView;
        TextView numberTextView;
        TextView statusTextView;
        Button inviteContactButton;
        Button cancelInviteContactButton;
        Button requestGuardianButton;
        Button cancelGuardianRequestButton;
    }
}
