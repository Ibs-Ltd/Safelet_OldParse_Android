package com.safelet.android.fragments;


import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.safelet.android.R;
import com.safelet.android.adapters.NavigationDrawerListAdapter;
import com.safelet.android.callback.NavigationDrawerCallbacks;
import com.safelet.android.interactors.EventBusManager;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.interactors.callbacks.RetrievePictureCallback;
import com.safelet.android.models.UserModel;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.event.LogoutEvent;
import com.safelet.android.models.event.NewEventsReceivedEvent;
import com.safelet.android.models.event.bluetooth.DeviceConnectionStateChangedEvent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

public class NavigationDrawerFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String MENU_INDEX_SELECTED_KEY = "ndf.menuIndexSelected.key";

    private NavigationMenu menuSelected = NavigationMenu.HOME;
//    private NavigationMenu menuSelected;

    private DrawerLayout drawerLayout;
    private View fragmentContainerView;

    private NavigationDrawerCallbacks callback;
    private NavigationDrawerListAdapter mNavigationMenuAdapter;

    private final UserManager userManager = UserManager.instance();
    private ImageView imgUserPic;
    private TextView txtUserName;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MENU_INDEX_SELECTED_KEY, menuSelected.ordinal());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            int menuIndex = savedInstanceState.getInt(MENU_INDEX_SELECTED_KEY);
            menuSelected = NavigationMenu.getMenuWithoutBluetooth()[menuIndex];
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // New Added
        imgUserPic = view.findViewById(R.id.imgUserPic);
        txtUserName = view.findViewById(R.id.txtUserName);

        mNavigationMenuAdapter = new NavigationDrawerListAdapter(getActivity(), true);
        ListView drawerLv = view.findViewById(R.id.navigation_drawer_items_lv);
        drawerLv.setOnItemClickListener(this);
        drawerLv.setAdapter(mNavigationMenuAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBusManager.instance().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mNavigationMenuAdapter != null) {
            refreshMenuList();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBusManager.instance().unRegister(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NavigationMenu menuItem = mNavigationMenuAdapter.getItem(position);
        if (!menuSelected.equals(menuItem)) {
            callback.onNavigationDrawerItemSelected(menuItem);
        }
        drawerLayout.closeDrawer(fragmentContainerView);
    }

    public NavigationMenu getMenuSelected() {
        return menuSelected;
    }

    public void setMenuSelected(NavigationMenu menuItem) {
        menuSelected = menuItem;
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout, NavigationDrawerCallbacks callbacks) {
        fragmentContainerView = getActivity().findViewById(fragmentId);
        this.drawerLayout = drawerLayout;
        callback = callbacks;

        // set a custom shadow that overlays the main content when the drawer opens
        this.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    }

    public void toggleMenu() {
        if (isDrawerOpen()) {
            drawerLayout.closeDrawer(fragmentContainerView);
        } else {
            refreshMenuList();
            drawerLayout.openDrawer(fragmentContainerView);
        }

        // Set and Update User Pic and Username
        UserModel userModel = userManager.getUserModel();
        if (txtUserName != null)
            txtUserName.setText(userModel.getOriginalName());

        if (imgUserPic != null)
            if (userModel.getUserImageBitmap() != null) {
                imgUserPic.setImageBitmap(userModel.getUserImageBitmap());
            } else {
                userManager.getProfilePicture(new GetProfilePictureListener(this));
            }
    }

    public boolean isDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(fragmentContainerView);
    }

    public void refreshMenuList() {
        mNavigationMenuAdapter.refreshMenu(true);
        mNavigationMenuAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewEventsReceived(NewEventsReceivedEvent event) {
        if (mNavigationMenuAdapter != null) {
            refreshMenuList();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSafeletConnectionChanged(DeviceConnectionStateChangedEvent event) {
        if (mNavigationMenuAdapter != null) {
            refreshMenuList();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogout(LogoutEvent event) {
        EventBusManager.instance().unRegister(this);
    }

    private static class GetProfilePictureListener implements RetrievePictureCallback {
        private WeakReference<NavigationDrawerFragment> weakReference;

        GetProfilePictureListener(NavigationDrawerFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onRetrieveSuccess(Bitmap bitmap) {
            NavigationDrawerFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            if (fragment.imgUserPic != null && bitmap != null) {
                fragment.imgUserPic.setImageBitmap(bitmap);
            }
        }

        @Override
        public void onRetrieveFailed() {
        }
    }
}
