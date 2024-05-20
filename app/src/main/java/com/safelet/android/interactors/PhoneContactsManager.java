package com.safelet.android.interactors;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.safelet.android.interactors.base.BaseManager;
import com.safelet.android.models.ContactModel;
import com.safelet.android.models.event.ContactsLoadedEvent;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This class is used to retrieve phone contacts and parse different types of phone numbers
 */
public class PhoneContactsManager extends BaseManager {

    private Map<String, ContactModel> cacheContactUsers = null;

    private String countryCode = "";

    private static PhoneContactsManager sInstance;

    public static PhoneContactsManager instance() {
        if (sInstance == null) {
            sInstance = new PhoneContactsManager();
        }
        return sInstance;
    }

    public boolean isContactsListOutdated() {
        return cacheContactUsers == null;
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    public void readPhoneContactsAsync(ContentResolver contentResolver) {
        new GetContactsAsync(contentResolver, countryCode).execute();
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    public String getPhoneNumber(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber = telephonyManager.getLine1Number();
        if (phoneNumber == null) {
            return "";
        }
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber numberProto = phoneNumberUtil.parse(phoneNumber, "");
            return "0" + numberProto.getNationalNumber();
        } catch (NumberParseException ignore) {
            return "";
        }
    }

    private Map<String, ContactModel> getPhoneContacts(ContentResolver contentResolver, String countryCode) {
        Uri contentUri = ContactsContract.Contacts.CONTENT_URI;
        Uri phoneContentUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        String[] projection = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER, ContactsContract.Contacts.PHOTO_ID};

        Map<String, ContactModel> phoneContacts = new HashMap<>();

        Cursor cursor = contentResolver.query(contentUri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                String contactPhotoId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
                Uri uriFile = null;
                if (!TextUtils.isEmpty(contactPhotoId)) {
                    uriFile = ContentUris.withAppendedId(contentUri, Long.parseLong(contactId));
                }

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {
                    projection = new String[]{ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER, ContactsContract.CommonDataKinds.Phone.NUMBER};

                    Cursor phoneCursor = contentResolver.query(phoneContentUri, projection, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{contactId}, null);
                    if (phoneCursor != null) {
                        while (phoneCursor.moveToNext()) {
                            String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
                            if (TextUtils.isEmpty(phoneNumber)) {
                                phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            }
                            if (!TextUtils.isEmpty(phoneNumber)) {
                                phoneNumber = normalizePhoneNumber(phoneNumber, countryCode);
                            }

                            if (!TextUtils.isEmpty(phoneNumber)) {
                                ContactModel contactModel = new ContactModel(name, phoneNumber, uriFile);
                                phoneContacts.put(phoneNumber, contactModel);
                            }
                        }

                        phoneCursor.close();
                    }
                }

            } while (cursor.moveToNext());
            cursor.close();
        }

        return phoneContacts;
    }

    private String normalizePhoneNumber(String phoneNumber, String defaultCountryCode) {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        String region = phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(defaultCountryCode.replace("+", ""))); // remove + sign if default country code contain it
        Phonenumber.PhoneNumber number;
        try {
            number = phoneNumberUtil.parse(phoneNumber, region);
        } catch (NumberParseException ignore) {
            String retryNumber = defaultCountryCode + phoneNumber;
            try {
                number = phoneNumberUtil.parse(retryNumber, region);
            } catch (NumberParseException ignore1) {
                return phoneNumber;
            }
        }
        return phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);
    }

    public Collection<ContactModel> getCacheContactContacts() {
        return cacheContactUsers.values();
    }

    public ContactModel getContact(String phone) {
        if (cacheContactUsers == null) {
            return null;
        }
        return cacheContactUsers.get(phone);
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    public void initCountryCode(Context context, String countryCode) {
        this.countryCode = countryCode;
        context.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, new ContactsContentObserver(context));
    }

    public void clear() {
        if (cacheContactUsers != null) {
            cacheContactUsers.clear();
            cacheContactUsers = null;
        }
    }

    private class ContactsContentObserver extends ContentObserver {

        private Context context;

        ContactsContentObserver(Context context) {
            super(null);
            this.context = context;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                readPhoneContactsAsync(context.getContentResolver());
            }
        }
    }

    private class GetContactsAsync extends AsyncTask<Void, Void, Map<String, ContactModel>> {

        private ContentResolver contentResolver;
        private String countryCode;

        GetContactsAsync(ContentResolver contentResolver, String countryCode) {
            this.contentResolver = contentResolver;
            this.countryCode = countryCode;
        }

        @Override
        protected Map<String, ContactModel> doInBackground(Void... params) {
            return getPhoneContacts(contentResolver, countryCode);
        }

        @Override
        protected void onPostExecute(Map<String, ContactModel> contacts) {
            super.onPostExecute(contacts);

            cacheContactUsers = contacts;

            EventBusManager.instance().postEvent(new ContactsLoadedEvent(contacts.values()));
        }
    }

    /**
     * Retrieves country code for current region corresponding to telephony service
     *
     * @param context  the context
     * @param callback the callback to return the country code in case that the country code is not
     *                 available for the CDMA networks
     * @return Country code as string if available or null in case that the country code is
     * requested from ip-api
     */
    public static String getCountryCodeForCurrentRegion(Context context, CountryCodeCallback callback) {
        if (!PhoneContactsManager.instance().countryCode.isEmpty()) {
            return PhoneContactsManager.instance().countryCode;
        }
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = null;
        if (tm.getNetworkType() != TelephonyManager.NETWORK_TYPE_CDMA) {
            countryCode = tm.getNetworkCountryIso();
            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = tm.getSimCountryIso();
            }
        }
        if (countryCode != null && !countryCode.isEmpty()) {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            return String.format("+%s", phoneUtil.getCountryCodeForRegion(
                    countryCode.toUpperCase(Locale.getDefault())));
        } else {
            new GetCountryCodeAsync(callback).execute((Void) null);
            return null;
        }
    }

    public static boolean isWhatsAppInstalled(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo info = packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    private static class GetCountryCodeAsync extends AsyncTask<Void, Void, String> {
        private final static String COUNTRY_CODE_KEY = "countryCode";
        private CountryCodeCallback listener;

        public GetCountryCodeAsync(CountryCodeCallback listener) {
            this.listener = listener;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                URI website = new URI("http://ip-api.com/json");
                request.setURI(website);
                HttpResponse response = httpclient.execute(request);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                StringBuilder builder = new StringBuilder();
                for (String line; (line = reader.readLine()) != null; ) {
                    builder.append(line).append("\n");
                }
                JSONObject responseAsJson = new JSONObject(builder.toString());
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                String countryCode = responseAsJson.getString(COUNTRY_CODE_KEY);
                return String.format("+%s", phoneUtil.getCountryCodeForRegion(
                        countryCode.toUpperCase(Locale.getDefault())));
            } catch (IOException | JSONException | URISyntaxException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String countryCode) {
            super.onPostExecute(countryCode);
            listener.onCountryCodeReceived(countryCode);
        }
    }

    public interface CountryCodeCallback {
        void onCountryCodeReceived(String countryCode);
    }


}
