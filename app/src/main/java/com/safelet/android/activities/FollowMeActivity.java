package com.safelet.android.activities;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.global.PreferencesManager;
import com.safelet.android.interactors.CheckInManager;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.interactors.callbacks.OnStringResponseCallback;
import com.safelet.android.models.UserBean;
import com.safelet.android.models.UserModel;
import com.safelet.android.utils.Error;
import com.safelet.android.utils.ItemOffsetDecoration;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class FollowMeActivity extends BaseActivity {

    private List<UserModel> myGuardiansCache = new ArrayList<>();
    private RecyclerView rvFollowMe;
    private FollowMeAdapter mFollowMeAdapter;
    private double mLatitude, mLongitude;
    TextView no_data;
    boolean status = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_me_layout);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mLatitude = extras.getDouble("mLatitude", 0);
            mLongitude = extras.getDouble("mLongitude", 0);
        }

        Toolbar toolBarFollowMe = findViewById(R.id.toolBarFollowMe);
        toolBarFollowMe.setTitle("".concat("FOLLOW ME"));
        setSupportActionBar(toolBarFollowMe);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        rvFollowMe = findViewById(R.id.rvFollowMe);
        no_data = findViewById(R.id.no_data);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        rvFollowMe.setHasFixedSize(true);
        rvFollowMe.setLayoutManager(gridLayoutManager);

        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(mContext, R.dimen.item_offset);
        rvFollowMe.addItemDecoration(itemDecoration);

        myGuardiansCache = UserManager.instance().getMyGuardiansCache();

        TextView txtStartFollowMe = findViewById(R.id.txtStartFollowMe);
        txtStartFollowMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status) {
                    if (mFollowMeAdapter != null) {
                        List<UserBean> mSelectedGuardiansArr = mFollowMeAdapter.getSelectedUser();
                        if (mSelectedGuardiansArr != null && !mSelectedGuardiansArr.isEmpty()) {
                            Timber.tag(TAG).d("mSelectedGuardiansArr Size: ".concat(String.valueOf(mSelectedGuardiansArr.size())));
                            List<String> mSelectedUserArr = new ArrayList<>();
                            for (UserBean userModel : mSelectedGuardiansArr) {
                                String mUserId = userModel.mUserId;
                                if (mUserId != null && !mUserId.isEmpty()) {
                                    mSelectedUserArr.add(mUserId);
                                    Timber.tag(TAG).d(" Selected mUserId ".concat(mUserId));
                                }
                            }

                            if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().getObjectId() != null) {
                                showLoading();
                                CheckInManager.instance().onStartFollowMeToMultipleGuardians(
                                        ParseUser.getCurrentUser().getObjectId(),
                                        new ParseGeoPoint(mLatitude, mLongitude),
                                        getAddressLatLon(mLatitude, mLongitude),
                                        mSelectedUserArr, new onStartFollowMeListener());
                            } else {
                                Toast.makeText(mContext, "Login user not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(mContext, "Please select guardians", Toast.LENGTH_SHORT).show();
                        }
                    }
                }else {
                    Toast.makeText(FollowMeActivity.this, "First please add guardian to use this feature.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getAddressLatLon(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Timber.tag(TAG).w(" Location Address ".concat(strReturnedAddress.toString()));
            } else {
                Timber.tag(TAG).w(" No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Timber.tag(TAG).w(" Cannot get Address!");
        }
        return strAdd;
    }

    private class onStartFollowMeListener implements OnStringResponseCallback {

        @Override
        public void onFailed(Error error) {
            // Displays Follow Me action error
            hideLoading();
            Toast.makeText(mContext, error.getErrorMessage(mContext), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess(String mResponse) {
            // Follow Me completed successfully
            Timber.d("".concat(BaseActivity.TAG.concat("mResponse: ").concat(mResponse)));
            hideLoading();
//            Toast.makeText(mContext, "Done", Toast.LENGTH_SHORT).show();
            if (!mResponse.equals("")) {
                PreferencesManager.instance(getApplicationContext()).setFollowMeaObjectId(mResponse);
                finish();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (UserManager.instance().isUserLoggedIn()) {
            UserManager.instance().getGuardiansForUser(new OnConnectionsReceiveCallback());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class OnConnectionsReceiveCallback implements UserManager.MyConnectionsCallback {

        @Override
        public void onConnectionsReceived(List<UserModel> connections) {
            myGuardiansCache = connections;
            if (myGuardiansCache != null && !myGuardiansCache.isEmpty()) {
                status =true;
                List<UserBean> mUserArray = new ArrayList<>();
                for (int g = 0; g < myGuardiansCache.size(); g++) {
                    UserModel model = myGuardiansCache.get(g);
                    ParseFile photoFile = model.getUserImage();
                    String photoUrl = photoFile != null ? photoFile.getUrl() : "";
                    mUserArray.add(new UserBean(model.getName(), model.getObjectId(), photoUrl, false));
                }

                if (!mUserArray.isEmpty()) {
                    status =true;
                    no_data.setVisibility(View.GONE);
                    rvFollowMe.setVisibility(View.VISIBLE);
                    mFollowMeAdapter = new FollowMeAdapter(mUserArray);
                    rvFollowMe.setAdapter(mFollowMeAdapter);
                    mFollowMeAdapter.notifyDataSetChanged();
                }else {
                    status =false;
                    rvFollowMe.setVisibility(View.GONE);
                    no_data.setVisibility(View.VISIBLE);
                }
            }else {
                status =false;
                rvFollowMe.setVisibility(View.GONE);
                no_data.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onFailed(Error error) {
            status =false;
        }
    }

    public class FollowMeAdapter extends RecyclerView.Adapter<FollowMeAdapter.MyViewHolder> {

        private List<UserBean> mUserArray;

        FollowMeAdapter(List<UserBean> mUserArray) {
            this.mUserArray = mUserArray;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_i_am_here_guardians_item, parent, false);
            return new FollowMeAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final FollowMeAdapter.MyViewHolder holder, final int position) {
            final UserBean mUserData = mUserArray.get(position);
            if (mUserData.mPhotoURL != null && !mUserData.mPhotoURL.equals(""))
                Picasso.get()
                        .load(mUserData.mPhotoURL)
                        .placeholder(R.drawable.generic_icon)
                        .noFade()
                        .error(R.drawable.generic_icon)
                        .into(holder.imgMyGuardian);

            holder.textView.setText(mUserData.mUserName);

            holder.view.setBackgroundColor(mUserData.isSelected ? ContextCompat.getColor(mContext, R.color.colorPrimary) : Color.WHITE);
//            holder.view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                }
//            });
        }

        @Override
        public int getItemCount() {
            return mUserArray == null ? 0 : mUserArray.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            private View view;
            private TextView textView;
            private ImageView imgMyGuardian;

            private MyViewHolder(final View itemView) {
                super(itemView);
                view = itemView;
                textView = itemView.findViewById(R.id.text_view);
                imgMyGuardian = itemView.findViewById(R.id.imgMyGuardian);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UserBean mUserData = mUserArray.get(getAdapterPosition());
                        if (mUserData.isSelected) {
                            mUserData.isSelected = false;
                            view.setBackgroundColor(Color.WHITE);
                        } else {
                            mUserData.isSelected = true;
                            view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                        }
                        mUserArray.set(getAdapterPosition(), mUserData);
                        notifyDataSetChanged();
                    }
                });
            }
        }

        private List<UserBean> getSelectedUser() {
            List<UserBean> mSelectedUserArr = new ArrayList<>();
            for (int u = 0; u < mUserArray.size(); u++) {
                if (mUserArray.get(u).isSelected) {
                    mSelectedUserArr.add(mUserArray.get(u));
                }
            }
            return mSelectedUserArr;
        }
    }

}
