package com.safelet.android.fragments;

import static android.app.Activity.RESULT_OK;
import static android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.parse.ParseFile;
import com.parse.ParseObject;
import com.safelet.android.R;
import com.safelet.android.activities.EditUserInformationActivity;
import com.safelet.android.activities.GuardianNetworkActivity;
import com.safelet.android.callback.NavigationDrawerCallbacks;
import com.safelet.android.global.PopDialog;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.interactors.callbacks.RetrievePictureCallback;
import com.safelet.android.models.UserModel;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.event.NewNotificationEvent;
import com.safelet.android.utils.Error;
import com.safelet.android.utils.LogoutHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

/**
 * My profile handling screen
 * <br/> Handles network requests and ui updating
 */
public class MyProfileFragment extends BaseFragment implements OnClickListener {

    private ImageView profilePictureImageView;
    private TextView userNameTextView, nameTextView, emailTextView, phoneTextView;
    private ImageView imgSafeletCommunity;

    private NavigationDrawerCallbacks parentDrawerCallback;
    private final UserManager userManager = UserManager.instance();
    private static final int JPEG_PERCENT_QUALITY = 90;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.parentDrawerCallback = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException ignore) {
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_profile_new, container, false);
    }

    @Override
    public void onViewCreated(View mView, Bundle savedInstanceState) {
        super.onViewCreated(mView, savedInstanceState);
        userNameTextView = mView.findViewById(R.id.my_profile_username_tv);
        profilePictureImageView = mView.findViewById(R.id.my_profile_profile_picture_iv);
        nameTextView = mView.findViewById(R.id.myProfileNameTv);
        emailTextView = mView.findViewById(R.id.myProfileEmailTv);
        phoneTextView = mView.findViewById(R.id.myProfilePhoneTv);

        LinearLayout layoutProfile = mView.findViewById(R.id.layoutProfile);
        layoutProfile.setOnClickListener(this);
        imgSafeletCommunity = mView.findViewById(R.id.imgSafeletCommunity);

        mView.findViewById(R.id.my_profile_change_picture_tv).setOnClickListener(this);
        mView.findViewById(R.id.myProfileNameLL).setOnClickListener(this);
        mView.findViewById(R.id.myProfileEmailLL).setOnClickListener(this);
        mView.findViewById(R.id.myProfilePhoneLL).setOnClickListener(this);
        mView.findViewById(R.id.myProfilePasswordLL).setOnClickListener(this);
        mView.findViewById(R.id.layoutGuardianNetwork).setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.my_profile_change_picture_tv) {
            if (getContext() != null)
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setFixAspectRatio(true).setAspectRatio(1, 1)
                        .setRequestedSize(1000, 1000).start(getContext(), this);
        } else if (v.getId() == R.id.myProfileNameLL) {
            editUserInfo(EditUserInformationActivity.UserInfo.NAME);
        } else if (v.getId() == R.id.myProfileEmailLL) {
            editUserInfo(EditUserInformationActivity.UserInfo.EMAIL);
        } else if (v.getId() == R.id.myProfilePhoneLL) {
            editUserInfo(EditUserInformationActivity.UserInfo.PHONE);
        } else if (v.getId() == R.id.myProfilePasswordLL) {
            editUserInfo(EditUserInformationActivity.UserInfo.PASSWORD);
        } else if (v.getId() == R.id.layoutProfile) {
            if (getContext() != null) {
                PopDialog.showDialogNew(getContext(), getString(R.string.home_dialog_logout_title)
                        , "Are you sure you want to delete your account?"
                        , "Delete"
                        , "Cancel"
                        , "Deletion Form",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (getContext() != null) {
                                    showLoading();
                                    UserManager.instance().deleteMyAccount(getContext(), userManager.getUserModel()
                                            , new DeleteMyAccountListener(MyProfileFragment.this));
                                }
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Uri uri = Uri.parse("https://d1gmxjp1u1wf9h.cloudfront.net/"); // missing 'http://' will cause crashed
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        });
            }
        } else if (v.getId() == R.id.layoutGuardianNetwork) {
            Intent intent = new Intent(getActivity(), GuardianNetworkActivity.class);
            startActivity(intent);
//            if (userManager.getUserModel().isCommunityMember()) {
//                if (getActivity() != null)
//                    ((HomeBaseActivity) getActivity()).addFragmentToRoot(new SafeletCommunityJoinedFragment());
//            } else {
//                if (getActivity() != null)
//                    ((HomeBaseActivity) getActivity()).addFragmentToRoot(new SafeletCommunityNotJoinedFragment());
//            }
        }
    }

    @Override
    public int getTitleResId() {
        return R.string.myprofile_title;
    }

    @Override
    public void onAlarmClicked() {
        parentDrawerCallback.onNavigationDrawerItemSelected(NavigationMenu.ALARM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
//                Uri resultUri = result.getUri();

                showLoading();

                Picasso.get().load(result.getUri()).fit().into(profilePictureImageView);
                Picasso.get().load(result.getUri()).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//                        bitmap.compress(CompressFormat.JPEG, JPEG_PERCENT_QUALITY, outputStream);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
//                        ParseFile file = new ParseFile(outputStream.toByteArray(), "image/jpeg");
//                        ParseFile file = new ParseFile("safelet", outputStream.toByteArray());
//                        ParseFile file = new ParseFile("safelet.png", outputStream.toByteArray(), "image/png");
                        ParseFile file = new ParseFile("safelet.png", outputStream.toByteArray());
                        UserManager.instance().getUserModel().setUserImage(file);
                        UserManager.instance().getUserModel().setUserImageBitmap(bitmap);
                        UserManager.instance().saveUser(null);
                        UserManager.instance().uploadUserImageFile(file, new UploadProfilePictureListener(MyProfileFragment.this));
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }


/*
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (result == null) {
                return;
            }

            showLoading();

            Picasso.get().load(result.getUri()).fit().into(profilePictureImageView);
            Picasso.get().load(result.getUri()).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    bitmap.compress(CompressFormat.JPEG, JPEG_PERCENT_QUALITY, outputStream);
                    ParseFile file = new ParseFile(outputStream.toByteArray(), "image/jpeg");

                    UserManager.instance().getUserModel().setUserImage(file);
                    UserManager.instance().getUserModel().setUserImageBitmap(bitmap);
                    UserManager.instance().saveUser(null);
                    UserManager.instance().uploadUserImageFile(new UploadProfilePictureListener(MyProfileFragment.this));
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
        }
*/
    }

    @Override
    protected void onNavigationDrawerItemSelected(NavigationMenu navigationMenu) {
        parentDrawerCallback.onNavigationDrawerItemSelected(navigationMenu);
    }

    @Override
    protected void onViewNotificationDialogClicked(Context context, NewNotificationEvent event) {
        handleNewNotificationEvent(event);
    }

    private void editUserInfo(EditUserInformationActivity.UserInfo userInfo) {
        Intent intent = new Intent(getActivity(), EditUserInformationActivity.class);
        intent.putExtra(EditUserInformationActivity.INFO_TYPE_KEY, userInfo);
        startActivity(intent);
    }

    private void initData() {
        // Updates screen with user data
        UserModel userModel = userManager.getUserModel();
        nameTextView.setText(userModel.getOriginalName());
        emailTextView.setText(userModel.getEmail());
        phoneTextView.setText(userModel.getPhoneNumber());

        // Check Guardian Network Safelet Community
        if (UserManager.instance().getUserModel().isCommunityMember()) {
            imgSafeletCommunity.setVisibility(View.VISIBLE);
        } else {
            imgSafeletCommunity.setVisibility(View.GONE);
        }

        userNameTextView.setText(userModel.getOriginalName());
        if (userModel.getUserImageBitmap() != null) {
            profilePictureImageView.setImageBitmap(userModel.getUserImageBitmap());
        } else {
            userManager.getProfilePicture(new GetProfilePictureListener(this));
        }
    }

    private static class GetProfilePictureListener implements RetrievePictureCallback {
        private WeakReference<MyProfileFragment> weakReference;

        GetProfilePictureListener(MyProfileFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onRetrieveSuccess(Bitmap bitmap) {
            MyProfileFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            if (fragment.profilePictureImageView != null && bitmap != null) {
                fragment.profilePictureImageView.setImageBitmap(bitmap);
            }
        }

        @Override
        public void onRetrieveFailed() {
            //No need to handle, if user doesn't have the image it will remain the default one
        }
    }

    private static class UploadProfilePictureListener implements OnResponseCallback {
        private WeakReference<MyProfileFragment> weakReference;

        UploadProfilePictureListener(MyProfileFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onSuccess(ParseObject object) {
            MyProfileFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.hideLoading();
        }

        @Override
        public void onFailed(Error error) {
            MyProfileFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.hideLoading();
            LogoutHelper.handleExpiredSession(fragment.getActivity(), error);
        }
    }

    // Delete My Account
    private static class DeleteMyAccountListener implements OnResponseCallback {
        private WeakReference<MyProfileFragment> weakReference;

        DeleteMyAccountListener(MyProfileFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onSuccess(ParseObject object) {
            MyProfileFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.hideLoading();
            // Account Delete Successful
            if (fragment.getContext() != null)
                LogoutHelper.logout(fragment.getContext());
        }

        @Override
        public void onFailed(Error error) {
            MyProfileFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.hideLoading();
            LogoutHelper.handleExpiredSession(fragment.getActivity(), error);
        }
    }
}