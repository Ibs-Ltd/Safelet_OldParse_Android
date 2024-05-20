package com.safelet.android.interactors;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.safelet.android.interactors.callbacks.OnFirebaseCallback;
import com.safelet.android.models.Message;
import com.safelet.android.models.User;
import com.safelet.android.models.UserModel;
import com.safelet.android.models.event.FirebaseInitializeEvent;
import com.safelet.android.models.event.FirebaseMessageEvent;
import com.safelet.android.utils.Error;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private static final String MESSAGES = "messages";
    private static final String TIMESTAMP = "timestamp";

    private static MessageManager sInstance;

    private DatabaseReference databaseReference;

    private Map<String, MessageListener> listeners = new HashMap<>();

    public static MessageManager instance() {
        if (sInstance == null) {
            sInstance = new MessageManager();
        }
        return sInstance;
    }

    public void authenticate() {
        if (!isAuthenticated()) {
            UserManager.instance().getFirebaseToken(new OnFirebaseCallback() {
                @Override
                public void onSuccess(String token) {
                    FirebaseAuth.getInstance().signInWithCustomToken(token).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                EventBus.getDefault().postSticky(new FirebaseInitializeEvent());
                            }
                        }
                    });
                }

                @Override
                public void onFailed(Error error) {
                }
            });
        } else {
            EventBus.getDefault().postSticky(new FirebaseInitializeEvent());
        }
    }

    public void sendMessage(String alarmId, String message) {
        UserModel currentUser = UserManager.instance().getUserModel();
        getDatabaseReference().child(alarmId).push().setValue(new Message(message, new User(currentUser.getObjectId(),
                currentUser.getOriginalName(), currentUser.getPhoneNumber(), currentUser.getImageUrl())));
    }

    public void subscribe(String alarmId) {
        MessageListener listener = new MessageListener();
        listeners.put(alarmId, listener);
        getDatabaseReference().child(alarmId).orderByChild(TIMESTAMP).addChildEventListener(listener);
    }

    public void unSubscribe(String alarmId) {
        MessageListener listener = listeners.get(alarmId);
        if (listener != null) {
            getDatabaseReference().child(alarmId).removeEventListener(listener);
        }
        listeners.remove(alarmId);
    }

    public void deleteMessages(String alarmId) {
        unSubscribe(alarmId);
        getDatabaseReference().child(alarmId).removeValue();
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
    }

    private DatabaseReference getDatabaseReference() {
        if (databaseReference == null) {
            databaseReference = FirebaseDatabase.getInstance().getReference(MESSAGES);
        }
        return databaseReference;
    }

    private boolean isAuthenticated() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null && !currentUser.isAnonymous();
    }

    private class MessageListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String value) {
            EventBus.getDefault().post(new FirebaseMessageEvent(dataSnapshot.getValue(Message.class)));
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String value) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String value) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    }
}
