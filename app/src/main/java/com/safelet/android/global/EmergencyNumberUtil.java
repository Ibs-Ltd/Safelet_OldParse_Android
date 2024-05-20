package com.safelet.android.global;

import androidx.collection.SparseArrayCompat;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class EmergencyNumberUtil {

    private static final int INTERNATIONAL_EMERGENCY_NUMBER = 911;

    private static final SparseArrayCompat<Integer> EMERGENCY_NUMBERS = new SparseArrayCompat<>();

    static {
        EMERGENCY_NUMBERS.append(64, 111);
    }

    public static int getEmergencyNumber(String phoneNumber) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber number = phoneUtil.parse(phoneNumber, "");
            Integer emergencyNumber = EMERGENCY_NUMBERS.get(number.getCountryCode());
            if (emergencyNumber != null) {
                return emergencyNumber;
            }
        } catch (NumberParseException e) {
            e.printStackTrace();
        }
        return INTERNATIONAL_EMERGENCY_NUMBER;
    }

}
