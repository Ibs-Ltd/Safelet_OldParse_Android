package com.safelet.android.models.event;

import com.safelet.android.models.UserModel;
import com.safelet.android.utils.Error;

import java.util.List;


public class ParticipantsReceivedEvent extends BaseEvent {

    private List<UserModel> users;

    private boolean updateParticipants;

    public ParticipantsReceivedEvent(List<UserModel> users, boolean updateParticipants) {
        this.users = users;
        this.updateParticipants = updateParticipants;
    }

    public ParticipantsReceivedEvent(Error error, boolean updateParticipants) {
        super(error);
        this.updateParticipants = updateParticipants;
    }

    public List<UserModel> getParticipants() {
        return users;
    }

    public boolean isUpdateParticipants() {
        return updateParticipants;
    }
}
