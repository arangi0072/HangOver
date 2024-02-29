package arpit.rangi.HangOver;

import android.app.Service;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Objects;

public class MyService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("info").child("last_seen").setValue("Online");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("info").child("last_seen").setValue("Online");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("info").child("last_seen").setValue(ServerValue.TIMESTAMP);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("info").child("last_seen").setValue(ServerValue.TIMESTAMP);
        }
    }

    public MyService() {
        super();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("info").child("last_seen").setValue(ServerValue.TIMESTAMP);
        this.stopSelf();
    }
}
