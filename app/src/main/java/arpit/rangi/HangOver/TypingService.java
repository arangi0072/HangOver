package arpit.rangi.HangOver;

import android.app.Service;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TypingService extends Service {
    String receiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("receiver")) {
            receiver = intent.getStringExtra("receiver");
        }
        return START_STICKY;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> last = new HashMap<>();
        last.put("typing", "false");
        mDatabase.child(receiver).child("info").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(last);

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
        Map<String, Object> last = new HashMap<>();
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            last.put("typing", "false");
            mDatabase.child(receiver).child("info").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(last);

        }

    }

    public TypingService() {
        super();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> last = new HashMap<>();
        last.put("typing", "false");
        mDatabase.child(receiver).child("info").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(last);
        this.stopSelf();
    }
}
