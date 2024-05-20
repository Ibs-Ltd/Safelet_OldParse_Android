package com.safelet.android.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.parse.ParseFile;
import com.safelet.android.R;
import com.safelet.android.activities.PhonePrefixListActivity;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.PhoneContactsManager;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.models.UserModel;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.event.NewNotificationEvent;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * Sign up user second fragment
 * <p/>
 * Created by Alin on 10/18/2015.
 */
public class SignupSecondFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "SignupSecondFragment";
    private static final String BITMAP_SAVED_KEY = "suf.bitmapSaved.key";
    private static final String COUNTRY_CODE_SAVED_KEY = "suf.countryCodeSaved.key";
    private static final String PREFIX_KEY = "prefix";
    private static final String DEFAULT_COUNTRY_CODE = "+0";
    private static final int PREFIX_REQUEST_CODE = 1001;
    private static final int SELECT_PICTURE_REQUEST_CODE = 1002;
    private static final int REQUEST_CODE_PHONE = 19;
    /**
     * User desired profile picture
     */
    private Bitmap pictureBitmap;
    private String countryCode = DEFAULT_COUNTRY_CODE;

    private EditText nameEditText;
    private TextView prefixTextView;
    private EditText phoneNumberEditText;
    private ImageView profilePictureImageView;
    private TextView nameAlertTextView;
    private TextView prefixAlertTextView;
    private TextView phoneAlertTextView;
    private RelativeLayout prefixRelativeLayout;

    private BaseActivity activity;

    private final UserManager userManager = UserManager.instance();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.activity = (BaseActivity) activity;
        } catch (ClassCastException e) {
            Log.e(TAG, "".concat("should be inflated in BaseActivity"));
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(BITMAP_SAVED_KEY)) {
                pictureBitmap = savedInstanceState.getParcelable(BITMAP_SAVED_KEY);
            }
            countryCode = savedInstanceState.getString(COUNTRY_CODE_SAVED_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_second, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profilePictureImageView = (ImageView) view.findViewById(R.id.register_upload_iv);
        TextView profilePictureTv = (TextView) view.findViewById(R.id.register_upload_tv);
        //Set text to be underlined
        profilePictureTv.setPaintFlags(profilePictureTv.getPaintFlags()
                | Paint.UNDERLINE_TEXT_FLAG);

        nameEditText = (EditText) view.findViewById(R.id.register_name_et);
        prefixTextView = (TextView) view.findViewById(R.id.register_prefix_et);
        prefixRelativeLayout = (RelativeLayout) view.findViewById(R.id.register_prefix_rl);
        phoneNumberEditText = (EditText) view.findViewById(R.id.register_phone_et);
        nameAlertTextView = (TextView) view.findViewById(R.id.register_name_alert_tv);
        prefixAlertTextView = (TextView) view.findViewById(R.id.register_prefix_alert_tv);
        phoneAlertTextView = (TextView) view.findViewById(R.id.register_phone_alert_tv);

        prefixRelativeLayout.setOnClickListener(this);
        view.findViewById(R.id.register_back_tv).setOnClickListener(this);
        view.findViewById(R.id.register_next_btn).setOnClickListener(this);
        view.findViewById(R.id.register_upload_rl).setOnClickListener(this);

        nameEditText.addTextChangedListener(new ScreenFieldsTextWatcher(this));
        phoneNumberEditText.addTextChangedListener(new ScreenFieldsTextWatcher(this));
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            phoneNumberEditText.setText(PhoneContactsManager.instance().getPhoneNumber(getContext()));
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_PHONE);
        }

        if (countryCode.equals(DEFAULT_COUNTRY_CODE)) {
            String currentCountryCode = PhoneContactsManager.getCountryCodeForCurrentRegion(getActivity(),
                    new GetCountryCodeListener(this));
            if (currentCountryCode == null) {
                showLoading();
            } else {
                countryCode = currentCountryCode;
            }
        }
        prefixTextView.setText(countryCode);
        //Sets ui to default state
        setScreenState(ScreenStates.StateDefault);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PREFIX_REQUEST_CODE) {
            try {
                //Parse prefix from Prefix activity
                countryCode = String.format("+%s", data.getStringExtra(PREFIX_KEY));
                prefixTextView.setText(countryCode);
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Error loading prefix");
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (result == null) {
                return;
            }

            showLoading();

            Picasso.get().load(result.getUri()).fit().into(profilePictureImageView);
            Picasso.get().load(result.getUri()).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    pictureBitmap = bitmap;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            hideLoading();
                        }
                    });
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PHONE && ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            phoneNumberEditText.setText(PhoneContactsManager.instance().getPhoneNumber(getContext()));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (pictureBitmap != null) {
            outState.putParcelable(BITMAP_SAVED_KEY, pictureBitmap);
        }
        outState.putString(COUNTRY_CODE_SAVED_KEY, countryCode);
    }

    @Override
    protected void onNavigationDrawerItemSelected(NavigationMenu navigationMenu) {
        // do nothing
    }

    @Override
    protected void onViewNotificationDialogClicked(Context context, NewNotificationEvent event) {
        // do nothing
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.register_prefix_rl) {
            Intent intent = new Intent(getActivity(), PhonePrefixListActivity.class);
            startActivityForResult(intent, PREFIX_REQUEST_CODE);
        } else if (v.getId() == R.id.register_back_tv) {
            userManager.logout();
            getActivity().onBackPressed();
        } else if (v.getId() == R.id.register_next_btn) {
            onNext();
        } else if (v.getId() == R.id.register_upload_rl) {
            onUpload();
        }
    }

    @Override
    public int getTitleResId() {
        return NO_TITLE;
    }

    /**
     * Display alert message for errors on name
     *
     * @param message Text message
     */
    private void setNameAlert(String message) {
        if (nameAlertTextView != null) {
            nameAlertTextView.setText(message);
        }
    }

    /**
     * Display alert message for errors on phone number prefix
     *
     * @param message Text message
     */
    private void setPrefixAlert(String message) {
        if (prefixAlertTextView != null) {
            prefixAlertTextView.setText(message);
        }
    }

    /**
     * Display alert message for errors on phone
     *
     * @param message Text message
     */
    private void setPhoneAlert(String message) {
        if (phoneAlertTextView != null) {
            phoneAlertTextView.setText(message);
        }
    }

    /**
     * Changes screen state based on ScreenStates enum
     *
     * @param screenState Screen state
     */
    private void setScreenState(ScreenStates screenState) {
        if (nameEditText == null) {
            return;
        }
        int paddingPasswordEt = nameEditText.getPaddingTop();
        switch (screenState) {
            case StateDefault:
                nameEditText.setBackgroundResource(R.drawable.input_box);
                prefixRelativeLayout.setBackgroundResource(R.drawable.input_box);
                phoneNumberEditText.setBackgroundResource(R.drawable.input_box);
                nameAlertTextView.setVisibility(View.GONE);
                prefixAlertTextView.setVisibility(View.GONE);
                phoneAlertTextView.setVisibility(View.GONE);
                nameEditText.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                prefixTextView.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                phoneNumberEditText.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                break;
            case StateServiceWrong:
                nameEditText.setBackgroundResource(R.drawable.input_box_accepted);
                prefixRelativeLayout.setBackgroundResource(R.drawable.input_box_small);
                phoneNumberEditText.setBackgroundResource(R.drawable.input_box_accepted);
                nameAlertTextView.setVisibility(View.GONE);
                prefixAlertTextView.setVisibility(View.GONE);
                phoneAlertTextView.setVisibility(View.VISIBLE);
                nameEditText.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                prefixTextView.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                phoneNumberEditText.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                break;
            case StateNameWrong:
                nameEditText.setBackgroundResource(R.drawable.input_box_rejected);
                nameAlertTextView.setVisibility(View.VISIBLE);
                prefixAlertTextView.setVisibility(View.GONE);
                phoneAlertTextView.setVisibility(View.GONE);
                nameEditText.setTextColor(getResources().getColor(R.color.red_alert_text));
                break;
            case StatePrefixWrong:
                nameEditText.setBackgroundResource(R.drawable.input_box_accepted);
                prefixRelativeLayout.setBackgroundResource(R.drawable.input_box_small);
                nameAlertTextView.setVisibility(View.GONE);
                prefixAlertTextView.setVisibility(View.VISIBLE);
                phoneAlertTextView.setVisibility(View.GONE);
                nameEditText.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                prefixTextView.setTextColor(getResources().getColor(R.color.red_alert_text));
                break;
            case StateNumberWrong:
                nameEditText.setBackgroundResource(R.drawable.input_box_accepted);
                prefixRelativeLayout.setBackgroundResource(R.drawable.input_box_small);
                phoneNumberEditText.setBackgroundResource(R.drawable.input_box_rejected);
                nameAlertTextView.setVisibility(View.GONE);
                prefixAlertTextView.setVisibility(View.GONE);
                phoneAlertTextView.setVisibility(View.VISIBLE);
                nameEditText.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                prefixTextView.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                phoneNumberEditText.setTextColor(getResources().getColor(R.color.red_alert_text));
                break;
            case StateOk:
                nameEditText.setBackgroundResource(R.drawable.input_box_accepted);
                prefixRelativeLayout.setBackgroundResource(R.drawable.input_box_small);
                phoneNumberEditText.setBackgroundResource(R.drawable.input_box_accepted);
                nameAlertTextView.setVisibility(View.GONE);
                prefixAlertTextView.setVisibility(View.GONE);
                phoneAlertTextView.setVisibility(View.GONE);
                nameEditText.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                prefixTextView.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                phoneNumberEditText.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                break;
            default:
                break;
        }
        //Restores padding
        nameEditText.setPadding(paddingPasswordEt, paddingPasswordEt, paddingPasswordEt, paddingPasswordEt);
        phoneNumberEditText.setPadding(paddingPasswordEt, paddingPasswordEt, paddingPasswordEt, paddingPasswordEt);
    }

    private void onNext() {
        Utils.hideKeyboard(getActivity());
        String name = nameEditText.getText().toString();
        String prefix = prefixTextView.getText().toString();
        String phone = phoneNumberEditText.getText().toString();
        name = name.trim();
        prefix = prefix.trim();
        phone = phone.trim();

        // User details validation
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(prefix) && (prefix.length() > 1) &&
                !TextUtils.isEmpty(phone) && phone.length() > 5) {
            // Validation completed successfully
            showLoading();
            UserModel userModel = userManager.getUserModel();
            if (pictureBitmap != null) {
                //sPictureBitmap is the Bitmap
                //calculate how many bytes our image consists of.
                int bytes = pictureBitmap.getByteCount();
                //or we can calculate bytes this way. Use a different value than 4 if you don't use 32bit images.
                //int bytes = sPictureBitmap.getWidth()*sPictureBitmap.getHeight()*4;

                ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
                pictureBitmap.copyPixelsToBuffer(buffer); //Move the byte data to the buffer
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                //byte[] array = buffer.array(); //Get the underlying array containing the data.
                // Convert picture to parse file
                ParseFile file = new ParseFile(os.toByteArray(), "image/jpg");
                userModel.setUserImageBitmap(pictureBitmap);
                userModel.setUserImage(file);
            }

            userModel.setName(name);
            userModel.setPhoneNumber(phone);
            userModel.setCountryPrefixCode(prefix);
            // Sets screen state to success and sends the user to next screen
            setScreenState(ScreenStates.StateOk);
            activity.addFragmentToBackStack(new SignupThirdFragment());
            // Saving user to server
        } else if (TextUtils.isEmpty(name)) {
            setScreenState(ScreenStates.StateNameWrong);
            setNameAlert(getString(R.string.signup_second_alert_message_namecannotbeempty));
        } else if (TextUtils.isEmpty(prefix) || prefix.length() == 1 || prefix.equals(DEFAULT_COUNTRY_CODE)) {
            setScreenState(ScreenStates.StatePrefixWrong);
            setPrefixAlert(getString(R.string.signup_second_alert_message_prefixcannotbeempty));
        } else if (TextUtils.isEmpty(phone)) {
            setScreenState(ScreenStates.StateNumberWrong);
            setPhoneAlert(getString(R.string.signup_second_alert_message_phonecannotbeempty));
        } else if (phone.length() <= 5) {
            setScreenState(ScreenStates.StateNumberWrong);
            setPhoneAlert(getString(R.string.signup_second_alert_message_phonenumberisshort));
        }
    }

    private void onUpload() {
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setFixAspectRatio(true).setAspectRatio(1, 1)
                .setRequestedSize(1000, 1000).start(getContext(), this);
    }

    /**
     * Parameters for controlling states of the screen
     */
    protected enum ScreenStates {
        StateDefault,
        StateNameWrong,
        StatePrefixWrong,
        StateNumberWrong,
        StateServiceWrong,
        StateOk
    }

    private static class ScreenFieldsTextWatcher implements TextWatcher {
        private WeakReference<SignupSecondFragment> weakReference;

        ScreenFieldsTextWatcher(SignupSecondFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            SignupSecondFragment fragment = weakReference.get();
            if (fragment == null) {
                return;
            }
            // Restores default colour as the user types
            if (fragment.nameEditText.getEditableText().length() == 0) {
                int paddingEmailEt = fragment.nameEditText.getPaddingTop();
                fragment.nameEditText.setBackgroundResource(R.drawable.input_box);
                fragment.nameEditText.setPadding(paddingEmailEt, paddingEmailEt, paddingEmailEt, paddingEmailEt);
                fragment.nameEditText.setTextColor(fragment.getResources().getColor(R.color.gray_input_color_text));
            }
            if (fragment.phoneNumberEditText.getEditableText().length() == 0) {
                int paddingPasswordEt = fragment.phoneNumberEditText.getPaddingTop();
                fragment.phoneNumberEditText.setBackgroundResource(R.drawable.input_box);
                fragment.phoneNumberEditText.setPadding(paddingPasswordEt, paddingPasswordEt, paddingPasswordEt, paddingPasswordEt);
                fragment.phoneNumberEditText.setTextColor(fragment.getResources().getColor(R.color.gray_input_color_text));
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            //No need to handle this
        }

        @Override
        public void afterTextChanged(Editable s) {
            //No need to handle this
        }
    }

    private static class GetCountryCodeListener implements PhoneContactsManager.CountryCodeCallback {
        private WeakReference<SignupSecondFragment> weakReference;

        GetCountryCodeListener(SignupSecondFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onCountryCodeReceived(String countryCode) {
            SignupSecondFragment fragment = weakReference.get();
            if (fragment == null) {
                return;
            }
            fragment.hideLoading();
            fragment.countryCode = countryCode;
            fragment.prefixTextView.setText(fragment.countryCode);
        }
    }
}
