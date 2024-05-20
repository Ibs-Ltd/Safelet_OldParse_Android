package com.safelet.android.activities;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.interactors.CheckInManager;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.models.UserBean;
import com.safelet.android.models.UserModel;
import com.safelet.android.utils.Error;
import com.safelet.android.utils.ItemOffsetDecoration;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class IAmHereActivity extends BaseActivity {

    private List<UserModel> myGuardiansCache = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mIamHereAdapter;
    private double mLatitude, mLongitude;
    TextView no_data;
    boolean status = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_i_am_here_layout);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mLatitude = extras.getDouble("mLatitude", 0);
            mLongitude = extras.getDouble("mLongitude", 0);
        }

        Toolbar toolBarIAmHere = findViewById(R.id.toolBarIAmHere);
        toolBarIAmHere.setTitle("".concat("I'M HERE"));
        setSupportActionBar(toolBarIAmHere);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mRecyclerView = findViewById(R.id.recycler_view);
        no_data = findViewById(R.id.no_data);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(mContext, R.dimen.item_offset);
        mRecyclerView.addItemDecoration(itemDecoration);

        myGuardiansCache = UserManager.instance().getMyGuardiansCache();

        TextView txtShareLocation = findViewById(R.id.txtShareLocation);
        txtShareLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(status) {
                    if (mIamHereAdapter != null) {
                        List<UserBean> mSelectedGuardiansArr = mIamHereAdapter.getSelectedUser();
                        if (mSelectedGuardiansArr != null && !mSelectedGuardiansArr.isEmpty()) {
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
                                CheckInManager.instance().onShareLocationUser(
                                        ParseUser.getCurrentUser().getObjectId(),
                                        new ParseGeoPoint(mLatitude, mLongitude),
                                        getAddressLatLon(mLatitude, mLongitude),
                                        "Message", mSelectedUserArr, new onShareLocationListener());
                            } else {
                                Toast.makeText(mContext, "Login user not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(mContext, "Please select guardians", Toast.LENGTH_SHORT).show();
                        }
                    }
                }else {
                    Toast.makeText(mContext, "First please add guardian to use this feature.", Toast.LENGTH_SHORT).show();
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

    private class onShareLocationListener implements OnResponseCallback {

        @Override
        public void onSuccess(ParseObject object) {
            // Checkin completed successfully
            hideLoading();
            Toast.makeText(mContext, "Done", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed(Error error) {
            // Displays checkin action error
            hideLoading();
            Toast.makeText(mContext, error.getErrorMessage(mContext), Toast.LENGTH_SHORT).show();
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
                    UserBean mUser = new UserBean();
                    mUser.mUserId = myGuardiansCache.get(g).getObjectId();
                    mUser.mUserName = myGuardiansCache.get(g).getName();
                    ParseFile photoFile = myGuardiansCache.get(g).getUserImage();
                    mUser.mPhotoURL = photoFile != null ? photoFile.getUrl() : "";
                    mUser.isSelected = false;
                    mUserArray.add(mUser);
                }
                if (!mUserArray.isEmpty()) {
                    status =true;
                    no_data.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mIamHereAdapter = new RecyclerViewAdapter(mUserArray);
                    mRecyclerView.setAdapter(mIamHereAdapter);
                    mIamHereAdapter.notifyDataSetChanged();
                }else {
                    status =false;
                    mRecyclerView.setVisibility(View.GONE);
                    no_data.setVisibility(View.VISIBLE);
                }
            }else {
                status =false;
                mRecyclerView.setVisibility(View.GONE);
                no_data.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onFailed(Error error) {
            status =false;
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        private List<UserBean> mModelList;

        RecyclerViewAdapter(List<UserBean> modelList) {
            mModelList = modelList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_i_am_here_guardians_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
            final UserBean mUser = mModelList.get(position);
            if (mUser.mPhotoURL != null && !mUser.mPhotoURL.equals(""))
                Picasso.get()
                        .load(mUser.mPhotoURL)
                        .placeholder(R.drawable.generic_icon)
                        .noFade()
                        .error(R.drawable.generic_icon)
                        .into(holder.imgMyGuardian);

            holder.textView.setText(mUser.mUserName);

            holder.view.setBackgroundColor(mUser.isSelected ? ContextCompat.getColor(mContext, R.color.colorPrimary) : Color.WHITE);
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mUser.isSelected = !mUser.isSelected;
                    holder.view.setBackgroundColor(mUser.isSelected ? ContextCompat.getColor(mContext, R.color.colorPrimary) : Color.WHITE);
                    mModelList.set(holder.getAdapterPosition(), mUser);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mModelList == null ? 0 : mModelList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            private View view;
            private TextView textView;
            private ImageView imgMyGuardian;

            private MyViewHolder(View itemView) {
                super(itemView);
                view = itemView;
                textView = itemView.findViewById(R.id.text_view);
                imgMyGuardian = itemView.findViewById(R.id.imgMyGuardian);
            }
        }

        private List<UserBean> getSelectedUser() {
            List<UserBean> mSelectedUserArr = new ArrayList<>();
            for (int u = 0; u < mModelList.size(); u++) {
                if (mModelList.get(u).isSelected) {
                    mSelectedUserArr.add(mModelList.get(u));
                }
            }
            return mSelectedUserArr;
        }
    }
}
