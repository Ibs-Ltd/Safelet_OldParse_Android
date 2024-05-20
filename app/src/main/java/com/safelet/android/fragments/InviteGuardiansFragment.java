package com.safelet.android.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;

import com.parse.ParseObject;
import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.adapters.GuardiansContactsListAdapter;
import com.safelet.android.global.PopDialog;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.EventsManager;
import com.safelet.android.interactors.PhoneContactsManager;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.models.UserModel;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.enums.SmsInviteStatus;
import com.safelet.android.models.enums.UserRelationStatus;
import com.safelet.android.models.event.ContactsLoadedEvent;
import com.safelet.android.models.event.NetworkAvailableEvent;
import com.safelet.android.models.event.NewNotificationEvent;
import com.safelet.android.utils.Error;
import com.safelet.android.utils.LogoutHelper;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import timber.log.Timber;

/**
 * Invite guardians selector
 * <p/>
 * Created by alin on 15.10.2015.
 */
public class InviteGuardiansFragment extends BaseFragment implements GuardiansContactsListAdapter.ActionListener {

    private static final int REQUEST_CODE_CONTACTS = 11;
    private GuardiansContactsListAdapter contactsAdapter;
    protected StickyListHeadersListView contactList;
    protected ProgressBar progressBar;
    protected TextView notFoundTextView;
    protected TextView loadingTextView;
    private MenuItem searchMenuItem;
    private MenuItem refreshMenuItem;
    private SearchView searchView;

    private final UserManager userManager = UserManager.instance();
    private final PhoneContactsManager phoneContactsManager = PhoneContactsManager.instance();
    private final EventsManager eventsManager = EventsManager.instance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Utils.hideKeyboard(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_invite_guardians, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.invite_guardians_menu, menu);
        refreshMenuItem = menu.findItem(R.id.reload);
        searchMenuItem = menu.findItem(R.id.search);
        if (contactsAdapter.getCount() == 0) {
            searchMenuItem.setEnabled(false); // set enable false in case the list of contacts is empty
        }
        searchView = (SearchView) searchMenuItem.getActionView();
//        if (searchMenuItem.isEnabled())
//            searchView.setOnQueryTextListener(new SearchTextListener(this));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String newText) {
                //Log.e("onQueryTextChange", "called");
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                onSearchContact(query);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.reload) {
            Utils.hideKeyboard(getActivity());
            if (Utils.isOnline()) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    loadContacts();
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_CONTACTS);
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadingTextView = view.findViewById(R.id.invite_guardians_loading_tv);
        RelativeLayout listContainerRl = view.findViewById(R.id.invite_guardians_act_list_container_rl);
        progressBar = view.findViewById(R.id.invite_guardians_act_progressbar_pb);
        notFoundTextView = view.findViewById(R.id.invite_guardians_act_no_results_tv);

        contactList = new StickyListHeadersListView(getActivity());
        contactList.setSelector(R.drawable.list_item_background_light);
        listContainerRl.addView(contactList);

        try {
            //Retrieves a reference for alert pop view if available
            this.alarmView = view.findViewById(R.id.globalAlertPopView);
        } catch (Exception e) {
            Timber.tag(BaseActivity.TAG.concat(" Error loading alert popup"));
        }

        //Contact click listener
        contactsAdapter = new GuardiansContactsListAdapter(getActivity(), userManager.getUserModel().getObjectId());
        contactsAdapter.setActionListener(this);
        contactList.setAdapter(contactsAdapter);
        reloadContacts();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CONTACTS && ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            phoneContactsManager.initCountryCode(getContext(), UserManager.instance().getUserModel().getCountryPrefixCode());
            loadContacts();
        }
    }

    @Override
    protected void onNavigationDrawerItemSelected(NavigationMenu navigationMenu) {
        // do nothing
    }

    @Override
    protected void onViewNotificationDialogClicked(Context context, NewNotificationEvent event) {
        goToHomeScreenWithNotification(event);
    }

    @Override
    public int getTitleResId() {
        return R.string.invite_guardians_title;
    }

    @Override
    public void onInviteContact(UserModel userModel) {
        sendInviteSMS(userModel);
    }

    @Override
    public void onCancelInviteContact(UserModel userModel) {
        showLoading();
        userManager.removeNonUserSmsInvitation(userModel.getPhoneNumber(), new SmsInvitationListener(userModel, InviteGuardiansFragment.this));
    }

    @Override
    public void onRequestGuarding(UserModel userModel) {
        showLoading();
        userManager.sendGuardianRequest(userModel, new GuardianInvitationListener(userModel, InviteGuardiansFragment.this));
    }

    @Override
    public void onCancelGuardingRequest(UserModel userModel) {
        showLoading();
        userManager.cancelGuardianRequest(userModel.getObjectId(), new GuardianInvitationListener(userModel, InviteGuardiansFragment.this));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInternetAvailable(NetworkAvailableEvent event) {
        super.onInternetAvailable(event);
        notFoundTextView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        if (contactList.getAdapter() == null || contactList.getAdapter().getCount() == 0) {
            reloadContacts();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContactsLoaded(ContactsLoadedEvent event) {
        userManager.getUsersForContacts(event.getContacts(), new ContactsReceivedListener(this));
    }

    private void reloadContacts() {
        Utils.hideKeyboard(getActivity());
        if (Utils.isOnline()) {
            if (phoneContactsManager.isContactsListOutdated()) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    loadContacts();
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_CONTACTS);
                }
            } else {
                userManager.getUsersForContacts(phoneContactsManager.getCacheContactContacts(), new ContactsReceivedListener(this));
            }
        } else {
            progressBar.setVisibility(View.GONE);
            notFoundTextView.setVisibility(View.VISIBLE);
            PopDialog.showDialog(getActivity(),
                    getString(R.string.invite_guardians_dialog_message_activeinternetconnectionrequirederror),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                            startActivity(intent);
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }
    }

    private void onSearchContact(CharSequence sequence) {
        if (contactsAdapter != null) {
            contactsAdapter.getFilter().filter(sequence);
            contactsAdapter.notifyDataSetChanged();
        }
    }

    private void onContactsFetched(List<UserModel> mergedContacts) {
        contactsAdapter.setItems(mergedContacts);
        if (searchView != null && !searchView.getQuery().toString().isEmpty()) {
            // in case user reload the list and the search view is not empty
            onSearchContact(searchView.getQuery());
        }
        progressBar.setVisibility(View.GONE);
        loadingTextView.setVisibility(View.GONE);
        if (searchMenuItem != null) { // check null in case we already have the list of contacts cached
            searchMenuItem.setEnabled(true);
        }
        if (refreshMenuItem != null) { // check null in case we already have the list of contacts cached
            refreshMenuItem.setEnabled(true);
        }
        hideLoading();
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    private void loadContacts() {
        progressBar.setVisibility(View.VISIBLE);
        loadingTextView.setVisibility(View.VISIBLE);
        if (refreshMenuItem != null)
            refreshMenuItem.setEnabled(false);
        contactsAdapter.clear();
        phoneContactsManager.clear();
        phoneContactsManager.readPhoneContactsAsync(getActivity().getContentResolver());
    }

    private void sendInviteSMS(UserModel userModel) {
        showLoading();

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + userModel.getPhoneNumber()));
        intent.putExtra("sms_body", getString(R.string.invite_guardians_text));
        startActivity(intent);

        userManager.createNonUserSmsInvitation(userModel.getPhoneNumber(),
                new SmsInvitationListener(userModel, InviteGuardiansFragment.this));
    }

    private void notifyDataChanged() {
        contactsAdapter.notifyDataSetChanged();
    }

    private static class SearchTextListener implements SearchView.OnQueryTextListener {
        private final WeakReference<InviteGuardiansFragment> weakReference;

        SearchTextListener(InviteGuardiansFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            InviteGuardiansFragment fragment = weakReference.get();
            if (fragment != null) {
                fragment.onSearchContact(newText);
            }
            return false;
        }
    }

    private static class ContactsReceivedListener implements UserManager.MyConnectionsCallback {

        private final WeakReference<InviteGuardiansFragment> weakReference;

        ContactsReceivedListener(InviteGuardiansFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onConnectionsReceived(List<UserModel> connections) {
            InviteGuardiansFragment fragment = weakReference.get();
            if (fragment == null) {
                return;
            }
            fragment.onContactsFetched(connections);
        }

        @Override
        public void onFailed(Error error) {
            InviteGuardiansFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.hideLoading();
            fragment.progressBar.setVisibility(View.GONE);
            fragment.loadingTextView.setVisibility(View.GONE);
            fragment.notFoundTextView.setVisibility(View.VISIBLE);
            PopDialog.showDialog(fragment.getActivity(), fragment.getString(R.string.invite_guardians_dialog_message_interneterror));
        }
    }

    private static class GuardianInvitationListener implements OnResponseCallback {

        private final UserModel userModel;

        private final WeakReference<InviteGuardiansFragment> weakReference;

        GuardianInvitationListener(UserModel userModel, InviteGuardiansFragment fragment) {
            this.userModel = userModel;
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onFailed(Error error) {
            InviteGuardiansFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.hideLoading();
            if (!LogoutHelper.handleExpiredSession(fragment.getActivity(), error)) {
                PopDialog.showDialog(fragment.getActivity(),
                        fragment.getString(R.string.invite_guardians_unabletosendinvitationdueto)
                                + error.getErrorMessage(fragment.getActivity()));
            }
        }

        @Override
        public void onSuccess(ParseObject object) {
            InviteGuardiansFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            if (userModel.getUserRelationStatus() == UserRelationStatus.NONE) {
                userModel.setUserRelationStatus(UserRelationStatus.PENDING);
            } else {
                userModel.setUserRelationStatus(UserRelationStatus.NONE);
            }
            fragment.notifyDataChanged();
            fragment.hideLoading();
        }
    }

    private static class SmsInvitationListener implements OnResponseCallback {

        private final UserModel userModel;

        private final WeakReference<InviteGuardiansFragment> weakReference;

        SmsInvitationListener(UserModel userModel, InviteGuardiansFragment fragment) {
            this.userModel = userModel;
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onSuccess(ParseObject object) {
            InviteGuardiansFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            if (userModel.getSmsInviteStatus() == SmsInviteStatus.NONE) {
                userModel.setSmsInviteStatus(SmsInviteStatus.INVITED);
            } else {
                userModel.setSmsInviteStatus(SmsInviteStatus.NONE);
            }
            fragment.notifyDataChanged();
            fragment.hideLoading();
            Utils.hideKeyboard(fragment.getActivity());
            PopDialog.showDialog(fragment.getActivity(),
                    fragment.getString(R.string.invite_guardians_title_success),
                    fragment.getString(R.string.invite_non_user_successfully));

        }

        @Override
        public void onFailed(Error error) {
            InviteGuardiansFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.hideLoading();
            if (!LogoutHelper.handleExpiredSession(fragment.getActivity(), error)) {
                PopDialog.showDialog(fragment.getActivity(),
                        fragment.getString(R.string.invite_guardians_title_success),
                        fragment.getString(R.string.invite_non_user_successfully));
            }
        }
    }
}
