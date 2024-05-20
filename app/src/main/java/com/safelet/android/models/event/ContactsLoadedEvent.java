package com.safelet.android.models.event;

import com.safelet.android.models.ContactModel;

import java.util.Collection;

public class ContactsLoadedEvent {

    private Collection<ContactModel> contacts;

    public ContactsLoadedEvent(Collection<ContactModel> contacts) {
        this.contacts = contacts;
    }

    public Collection<ContactModel> getContacts() {
        return contacts;
    }
}
