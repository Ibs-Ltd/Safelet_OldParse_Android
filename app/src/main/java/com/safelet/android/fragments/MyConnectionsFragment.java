package com.safelet.android.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.parse.ParseObject;
import com.safelet.android.R;
import com.safelet.android.activities.InviteGuardiansActivity;
import com.safelet.android.activities.LastCheckinActivity;
import com.safelet.android.adapters.MyConnectionsAdapter;
import com.safelet.android.callback.NavigationDrawerCallbacks;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.models.UserModel;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.event.NewNotificationEvent;
import com.safelet.android.utils.Error;
import com.safelet.android.utils.LogoutHelper;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

public class MyConnectionsFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    //    private static final String NEW_NOTIFICATION_KEY = "mcf.newNotification.key";
    private NavigationDrawerCallbacks parentDrawerCallback;

    private GridView myConnectionsGridView;
    private MyConnectionsAdapter myConnectionsAdapter;

    private List<UserModel> myGuardiansCache = null;
    private List<UserModel> imGuardingCache = null;

    private View noGuardiansView;
    private View noGuardView;
    private TextView myGuardiansTab;
    private TextView guardianTab;
    private Button checkIn;
    private ProgressBar progressBar;
    private ScreenState screenState = ScreenState.Loading;
    private AlertDialog contactDialog;


//    private NewNotificationEvent notificationEvent = null;

//    public static MyConnectionsFragment newInstance(NewNotificationEvent newNotificationEvent) {
//        MyConnectionsFragment fragment = new MyConnectionsFragment();
//        Bundle bundle = new Bundle();
//        bundle.putParcelable(NEW_NOTIFICATION_KEY, newNotificationEvent);
//        fragment.setArguments(bundle);
//        return fragment;
//    }

//    public static MyConnectionsFragment newInstance() {
//        return new MyConnectionsFragment();
//    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.parentDrawerCallback = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException ignore) {
            // should not happen
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            notificationEvent = getArguments().getParcelable(NEW_NOTIFICATION_KEY);
//        }
        myGuardiansCache = UserManager.instance().getMyGuardiansCache();
        imGuardingCache = UserManager.instance().getImGuardingCache();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_myconnections, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // List adapters initializations
        initTheView(view);
        onMyGuardians();
    }

    @Override
    public void onStart() {
        super.onStart();
        onRefreshMyConnections(myGuardiansCache == null || imGuardingCache == null);
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (notificationEvent != null) {
//            handleNewNotificationEvent(notificationEvent);
//            notificationEvent = null;
//        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.check_in) {
            onCheckIn();
        } else if (v.getId() == R.id.add_guardian_button) {
            onAddGuardians();
        } else if (v.getId() == R.id.my_guardians_tab) {
            if (!screenState.equals(ScreenState.Loading)) {
                onMyGuardians();
            }
        } else if (v.getId() == R.id.guarding_tab) {
            if (!screenState.equals(ScreenState.Loading)) {
                onImGuarding();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0 && myConnectionsAdapter.getConnectionsType().equals(MyConnectionsAdapter.ConnectionsType.GUARDIANS)) {
            onAddGuardians();
        } else {
            onMyGuardianClicked(myConnectionsAdapter.getItem(position));
        }
    }

    @Override
    public int getTitleResId() {
        return R.string.connections_title;
    }

    @Override
    public void onAlarmClicked() {
        parentDrawerCallback.onNavigationDrawerItemSelected(NavigationMenu.ALARM);
    }

    @Override
    protected void onNavigationDrawerItemSelected(NavigationMenu navigationMenu) {
        parentDrawerCallback.onNavigationDrawerItemSelected(navigationMenu);
        if (!navigationMenu.equals(NavigationMenu.MY_CONNECTIONS)) {
            parentDrawerCallback.onNavigationDrawerItemSelected(navigationMenu);
        }
    }

    @Override
    protected void onViewNotificationDialogClicked(Context context, NewNotificationEvent event) {
        handleNewNotificationEvent(event);
    }

    private void initTheView(@NonNull View view) {
        TextView tv = view.findViewById(R.id.my_connections_noguardians_label_title_tv);
        tv.setText(Html.fromHtml(getString(R.string.connections_you_have_no_guardians_label)));

        view.findViewById(R.id.my_guardians_tab).setOnClickListener(this);
        view.findViewById(R.id.guarding_tab).setOnClickListener(this);
        view.findViewById(R.id.add_guardian_button).setOnClickListener(this);
        view.findViewById(R.id.check_in).setOnClickListener(this);

        myConnectionsAdapter = new MyConnectionsAdapter(getActivity());

        noGuardiansView = view.findViewById(R.id.no_guardians_view);
        noGuardView = view.findViewById(R.id.no_guard_view);
        progressBar = view.findViewById(R.id.my_connections_pb);

        myGuardiansTab = view.findViewById(R.id.my_guardians_tab);
        myGuardiansTab.setSelected(true);
        guardianTab = view.findViewById(R.id.guarding_tab);

        checkIn = view.findViewById(R.id.check_in);
        myConnectionsGridView = view.findViewById(R.id.myConnectionsGridView);

        myConnectionsGridView.setAdapter(myConnectionsAdapter);
        myConnectionsGridView.setOnItemClickListener(this);
    }

    /**
     * Changes the user interface state of the screen
     *
     * @param state Type of the screen
     */
    private void setScreenState(ScreenState state) {
        if (progressBar == null) { // FIXME: 15.12.2015
            return;
        }
        screenState = state;
        switch (screenState) {
            case MyGuardiansSelected:
                myGuardiansTab.setSelected(true);
                guardianTab.setSelected(false);
                progressBar.setVisibility(View.GONE);
                noGuardiansView.setVisibility(View.GONE);
                noGuardView.setVisibility(View.GONE);
                checkIn.setVisibility(View.VISIBLE);
                myConnectionsGridView.setVisibility(View.VISIBLE);
                break;

            case ImGuardingSelected:
                myGuardiansTab.setSelected(false);
                guardianTab.setSelected(true);
                progressBar.setVisibility(View.GONE);
                noGuardiansView.setVisibility(View.GONE);
                noGuardView.setVisibility(View.GONE);
                checkIn.setVisibility(View.VISIBLE);
                myConnectionsGridView.setVisibility(View.VISIBLE);
                break;

            case MyGuardiansSelectedNoGuardians:
                myGuardiansTab.setSelected(true);
                guardianTab.setSelected(false);
                progressBar.setVisibility(View.GONE);
                checkIn.setVisibility(View.VISIBLE);
                noGuardiansView.setVisibility(View.VISIBLE);
                noGuardView.setVisibility(View.GONE);
                myConnectionsGridView.setVisibility(View.GONE);
                break;

            case ImGuardingSelectedNotGuarding:
                myGuardiansTab.setSelected(false);
                guardianTab.setSelected(true);
                progressBar.setVisibility(View.GONE);
                noGuardiansView.setVisibility(View.GONE);
                noGuardView.setVisibility(View.VISIBLE);
                checkIn.setVisibility(View.VISIBLE);
                myConnectionsGridView.setVisibility(View.GONE);
                break;

            case Loading:
                progressBar.setVisibility(View.VISIBLE);
                checkIn.setVisibility(View.GONE);
                noGuardiansView.setVisibility(View.GONE);
                noGuardView.setVisibility(View.GONE);
                myConnectionsGridView.setVisibility(View.GONE);
                break;

            default:
                break;
        }
    }

    private void showActionChooserFor(final UserModel model) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.connections_actionsforuser_label) + model.getName());
        List<String> actionItems;
        if (myGuardiansTab.isSelected()) {
            actionItems = Arrays.asList(getString(R.string.connections_seelastcheckin_title),
                    getString(R.string.connections_removeguardian_label),
                    getString(R.string.connections_cancel_label));
        } else {
            actionItems = Arrays.asList(getString(R.string.connections_seelastcheckin_title),
                    getString(R.string.connections_stopguarding_label),
                    getString(R.string.connections_cancel_label));
        }
        builder.setItems(actionItems.toArray(new CharSequence[actionItems.size()]),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            onSeeLastCheckin(model);
                        } else if (which == 1 && myGuardiansTab.isSelected()) {
                            onRemoveGuardian(model);
                        } else if (which == 1) {
                            onStopGuarding(model);
                        } else {
                            dialog.cancel();
                        }
                    }
                }).create().show();
    }

    /**
     * Retrieves connections from server
     */
    private void onRefreshMyConnections(boolean showLoading) {
        if (showLoading) {
            // Shows loading
            setScreenState(ScreenState.Loading);
        }
        // Retrieve data call
        if (UserManager.instance().isUserLoggedIn()) {
            UserManager.instance().getGuardiansForUser(new OnConnectionsReceiveCallback(this, true));
        }
    }

    /**
     * This is called when the user press My guardians tab button
     */
    private void onMyGuardians() {
        if (myGuardiansCache == null) {
            return;
        }
        myConnectionsAdapter.setItems(myGuardiansCache, MyConnectionsAdapter.ConnectionsType.GUARDIANS);
        if (myConnectionsAdapter.getCount() <= 1) {
            setScreenState(ScreenState.MyGuardiansSelectedNoGuardians);
        } else {
            setScreenState(ScreenState.MyGuardiansSelected);
        }
    }

    /**
     * This is called when the user press Im guarding tab button
     */
    private void onImGuarding() {
        if (imGuardingCache == null) {
            return;
        }
        myConnectionsAdapter.setItems(imGuardingCache, MyConnectionsAdapter.ConnectionsType.GUARDED);
        if (myConnectionsAdapter.getCount() > 0) {
            setScreenState(ScreenState.ImGuardingSelected);
        } else {
            setScreenState(ScreenState.ImGuardingSelectedNotGuarding);
        }
    }

    /**
     * This is called when the user press Alarm button
     */
    private void onCheckIn() {
        // Navigate to alarm screen
        parentDrawerCallback.onNavigationDrawerItemSelected(NavigationMenu.CHECK_IN);
    }

    /**
     * This is called when the user press the add guardians button
     */
    private void onAddGuardians() {
        // Navigate to add guardians from screen
//        Intent intent = new Intent(getActivity(), InviteGuardiansActivity.class);
//        startActivity(intent);
        // Todo : This is for Check
//        if (Utils.isOnline()) {
//            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
//                Intent intent = new Intent(getActivity(), InviteGuardiansActivity.class);
//                startActivity(intent);
//            } else {
//                showExplanationDialog(requireContext());
//            }
//        }
        showExplanationDialog(requireContext());
    }

    /**
     * This is the dialog to ask user permission for contacts
     * */

//    private void showExplanationDialog(Context context) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//
//        builder.setTitle("Why We Need Access to Your Contacts");
//        builder.setMessage("We value your privacy and want to explain why we need access to your contacts:\n\n" +
//                "1. Contact Information :\n\nWe upload and store your phone contacts' information securely to provide you with the option to invite your friends and family as guardians in case of emergencies. This includes names, phone numbers, and email addresses.\n\n" +
//                "2. Location Data :\n\nSafelet may access your device's location data to share your real-time location with your All guardians when you trigger an SOS Alarm. This is essential for your safety.\n\n" +
//                "3. Emergency Information :\n\nTo assist first responders, we may collect and transmit vital information about you and emergency contacts, as provided by you.\n\n" +
//
//                "This data collection occurs in the following scenarios:\n" +
//                        "\n" +
//                        "        - When you choose to add friends from your contact list as guardians.\n" +
//                        "        - When you activate an SOS Alarm, sharing your location and emergency information with your selected guardians.\n\n" +
//
//                "4. Your privacy and security :\n\n" +
//                "Your privacy and security are our top priorities. We do not share this data with any third parties.\n\n" +
//
//                "By using Safelet, you agree to the collection, transmission, syncing, and storage of the data mentioned above for the purpose of enhancing your safety.\n\n" +
//                "Please note that your consent is required before Safelet can start collecting or accessing this data. You can provide your consent by continuing to use the app.\n\n" +
//                "If you have any concerns or questions about our data practices, please contact our support team at googleplay@safelet.com");
//
//        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                dialog.dismiss();
//                Intent intent = new Intent(getActivity(), InviteGuardiansActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                dialog.dismiss();
//            }
//        });
//
//        builder.setNeutralButton("Privacy Policy", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                // Open the privacy policy page
//                dialog.dismiss();
//                openPrivacy();
//            }
//        });
//
//        // Create the AlertDialog
//        AlertDialog dialog = builder.create();
//
//        // Customize the dialog's appearance
//        dialog.getWindow().setBackgroundDrawableResource(R.color.material_red_50); // Customize background color
//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialogInterface) {
//                // Customize button colors
//                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.material_green_500));
//                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.material_red_300));
//                dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.black_translucent));
//            }
//        });
//
//        // Show the dialog
//        dialog.show();
//
////        builder.create().show();
//    }


    @SuppressLint("SetTextI18n")
    private void showExplanationDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.CustomAlertDialogTheme));

        // Inflate the custom layout for the dialog
        View dialogView = LayoutInflater.from(context).inflate(R.layout.custom_alert_layout, null);
        builder.setView(dialogView);

        // Customize the title and message text (if needed)
        TextView titleTextView = dialogView.findViewById(R.id.dialog_title);
        titleTextView.setText("Why We Need Access to Your Contacts");

        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);

        String messageText = "We value your privacy and want to explain why we need access to your contacts:<br><br>" +
                "<b>1. Contact Information :</b><br><br>" +
                "We upload and store your phone contacts' information securely to provide you with the option to invite your friends and family as guardians in case of emergencies. This includes names, phone numbers, and email addresses.<br><br>" +
                "<b>2. Location Data :</b><br><br>" +
                "Safelet may access your device's location data to share your real-time location with your All guardians when you trigger an SOS Alarm. This is essential for your safety.<br><br>" +
                "<b>3. Emergency Information :</b><br><br>" +
                "To assist first responders, we may collect and transmit vital information about you and emergency contacts, as provided by you.<br><br>" +

                "<b>Note :</b> Users Contact Lists are uploaded to our secure Heroku Server at <b>https://safelet.herokuapp.com</b> while maintaining stringent data privacy and security measures.<br><br>"+

                "This data collection occurs in the following scenarios:<br><br>" +
                "<ul>" +
                "<li>When you choose to add friends from your contact list as guardians.</li>" +
                "<li>When you activate an SOS Alarm, sharing your location and emergency information with your selected guardians.</li>" +
                "</ul>" +

                "<b>4. Your privacy and security :</b><br><br>" +
                "Your privacy and security are our top priorities. We do not share this data with any third parties.<br><br>" +

                "By using Safelet, you agree to the collection, transmission, syncing, and storage of the data mentioned above for the purpose of enhancing your safety.<br><br>" +
                "Please note that your consent is required before Safelet can start collecting or accessing this data. You can provide your consent by continuing to use the app.<br><br>" +
                "If you have any concerns or questions about our data practices, please contact our support team at googleplay@safelet.com";

        Spanned spannedMessage = Html.fromHtml(messageText);
        messageTextView.setText(spannedMessage);

//        messageTextView.setText("We value your privacy and want to explain why we need access to your contacts:\n\n" +
//                "1. Contact Information :\n\nWe upload and store your phone contacts' information securely to provide you with the option to invite your friends and family as guardians in case of emergencies. This includes names, phone numbers, and email addresses.\n\n" +
//                "2. Location Data :\n\nSafelet may access your device's location data to share your real-time location with your All guardians when you trigger an SOS Alarm. This is essential for your safety.\n\n" +
//                "3. Emergency Information :\n\nTo assist first responders, we may collect and transmit vital information about you and emergency contacts, as provided by you.\n\n" +
//
//                "This data collection occurs in the following scenarios:\n" +
//                "\n" +
//                "        - When you choose to add friends from your contact list as guardians.\n" +
//                "        - When you activate an SOS Alarm, sharing your location and emergency information with your selected guardians.\n\n" +
//
//                "4. Your privacy and security :\n\n" +
//                "Your privacy and security are our top priorities. We do not share this data with any third parties.\n\n" +
//
//                "By using Safelet, you agree to the collection, transmission, syncing, and storage of the data mentioned above for the purpose of enhancing your safety.\n\n" +
//                "Please note that your consent is required before Safelet can start collecting or accessing this data. You can provide your consent by continuing to use the app.\n\n" +
//                "If you have any concerns or questions about our data practices, please contact our support team at googleplay@safelet.com");


        // Customize buttons and other UI elements as needed

        // Find custom buttons by their IDs
        Button customOKButton = dialogView.findViewById(R.id.positive_button);
        Button customCancelButton = dialogView.findViewById(R.id.negative_button);
        Button customPrivacyPolicyButton = dialogView.findViewById(R.id.neutral_button);

        // Set custom button click actions
        customOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Customize the action for the OK button
                // For example, dismiss the dialog and start an activity
                contactDialog.dismiss();
                Intent intent = new Intent(getActivity(), InviteGuardiansActivity.class);
                startActivity(intent);
            }
        });

        customCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Customize the action for the Cancel button
                // For example, dismiss the dialog
                contactDialog.dismiss();
            }
        });

        customPrivacyPolicyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Customize the action for the Privacy Policy button
                // For example, open the privacy policy page
                contactDialog.dismiss();
                openPrivacy();
            }
        });

        // Create and show the AlertDialog
        contactDialog = builder.create();
        contactDialog.show();
    }

    private void openPrivacy() {
//        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_url))));
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_url1))));
    }


    private void onMyGuardianClicked(UserModel guardian) {
        // Show options for guardian contact
        showActionChooserFor(guardian);
    }

    /**
     * This is called when user wants to see last check-in of the contact
     *
     * @param model Contact to be seen
     */
    private void onSeeLastCheckin(UserModel model) {
        // Navigate to Checkin screen
        Intent intent = new Intent(getActivity(), LastCheckinActivity.class);
        intent.putExtra(LastCheckinActivity.KEY_MODEL_CONN, model.getObjectId());
        startActivity(intent);
    }

    /**
     * This is called when user wants to remove a guardian
     *
     * @param model Contact to be removed
     */
    private void onRemoveGuardian(UserModel model) {
        setScreenState(ScreenState.Loading);
        UserManager.instance().removeGuardian(model, new RemoveGuardianListener(this, true));
    }

    private void onStopGuarding(UserModel model) {
        setScreenState(ScreenState.Loading);
        UserManager.instance().removeImGuardian(model, new RemoveGuardianListener(this, false));
    }

    @Override
    protected void onDismissNotificationDialogClicked(NewNotificationEvent event) {
        switch (event.getNotificationType()) {
            case SEND_INVITATION:
            case ACCEPT_INVITATION:
            case REJECT_INVITATION:
            case CANCEL_INVITATION:
            case REMOVE_CONNECTION_GUARDED:
            case REMOVE_CONNECTION_GUARDIAN:
                onRefreshMyConnections(true);
                break;
        }
    }

    /**
     * Describes screen possible states
     */
    protected enum ScreenState {
        MyGuardiansSelected,
        ImGuardingSelected,
        MyGuardiansSelectedNoGuardians,
        ImGuardingSelectedNotGuarding,
        Loading
    }

    private static class OnConnectionsReceiveCallback implements UserManager.MyConnectionsCallback {
        private WeakReference<MyConnectionsFragment> weakReference;
        private boolean isMyGuardians = true;

        OnConnectionsReceiveCallback(MyConnectionsFragment fragment, boolean isMyGuardians) {
            weakReference = new WeakReference<>(fragment);
            this.isMyGuardians = isMyGuardians;
        }

        @Override
        public void onConnectionsReceived(List<UserModel> connections) {
            MyConnectionsFragment fragment = weakReference.get();
            if (fragment != null && fragment.isAdded()) {
                if (isMyGuardians) {
                    fragment.myGuardiansCache = connections;
                } else {
                    fragment.imGuardingCache = connections;
                }
                if (isMyGuardians) {
                    UserManager.instance().getGuardedUsers(new OnConnectionsReceiveCallback(fragment, false));
                } else {
                    reloadData();
                }
            }
        }

        @Override
        public void onFailed(Error error) {
            reloadData();
        }

        private void reloadData() {
            MyConnectionsFragment fragment = weakReference.get();
            if (fragment != null && fragment.isAdded()) {
                if (fragment.myGuardiansTab.isSelected()) {
                    fragment.onMyGuardians();
                } else if (fragment.guardianTab.isSelected()) {
                    fragment.onImGuarding();
                }
            }
        }
    }

    private static class RemoveGuardianListener implements OnResponseCallback {
        private WeakReference<MyConnectionsFragment> weakReference;
        private boolean isMyGuardians = true;

        RemoveGuardianListener(MyConnectionsFragment fragment, boolean isMyGuardians) {
            weakReference = new WeakReference<>(fragment);
            this.isMyGuardians = isMyGuardians;
        }

        @Override
        public void onSuccess(ParseObject object) {
            MyConnectionsFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            if (isMyGuardians) {
                fragment.myGuardiansCache = UserManager.instance().getMyGuardiansCache();
                fragment.onMyGuardians();
            } else {
                fragment.imGuardingCache = UserManager.instance().getImGuardingCache();
                fragment.onImGuarding();
            }
        }

        @Override
        public void onFailed(Error error) {
            MyConnectionsFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            if (isMyGuardians) {
                fragment.onMyGuardians();
            } else {
                fragment.onImGuarding();
            }
            LogoutHelper.handleExpiredSession(fragment.getActivity(), error);
        }
    }
}
