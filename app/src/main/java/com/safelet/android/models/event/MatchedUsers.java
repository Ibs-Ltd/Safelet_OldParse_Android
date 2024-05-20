package com.safelet.android.models.event;

import com.safelet.android.models.UserModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchedUsers {

    public static final String GUARDIANS = "parse_guardians";
    public static final String INVITED = "parse_invited";
    public static final String UNINVITED = "parse_uninvited";
    public static final String SMS_INVITED = "sms_invited";
    public static final String UNMATCHED = "unmatched";
    private HashMap<String, Object> data;

    public MatchedUsers(HashMap<String, Object> data) {
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    public Collection<UserModel> getGuardians() {
        return ((Map<String, UserModel>) data.get(GUARDIANS)).values();
    }

    @SuppressWarnings("unchecked")
    public Collection<UserModel> getInvited() {
        return ((Map<String, UserModel>) data.get(INVITED)).values();
    }

    @SuppressWarnings("unchecked")
    public Collection<UserModel> getUninvited() {
        return ((Map<String, UserModel>) data.get(UNINVITED)).values();
    }

    @SuppressWarnings("unchecked")
    public List<String> getSmsInvited() {
        return (List<String>) data.get(SMS_INVITED);
    }

    @SuppressWarnings("unchecked")
    public List<String> getUnmatched() {
        return (List<String>) data.get(UNMATCHED);
    }

}
