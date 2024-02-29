package arpit.rangi.HangOver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ImageUploadService extends AsyncTask<Object, Integer, Void> {
    private static final String CHANNEL_ID = "image_upload_channel";
    @SuppressLint("StaticFieldLeak")
    Context context;
    Integer NOTIFICATION_ID = 1;
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (context == null) {
            return;
        }
        createNotificationChannel();
        showNotification();
    }

    @Override
    protected Void doInBackground(Object... params) {
        if (params.length >= 7) {
            context = (Context) params[0];

            byte[] byteArray = (byte[]) params[1];
            // Convert the byte array back to a Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            String file = (String) params[2];
            String token = (String) params[3];
            String uid = (String) params[4];
            String msg = (String) params[5];
            String username = (String) params[6];
            boolean block = (boolean) params[7];
            createNotificationChannel();
            showNotification();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                uploadBitmapToFirebaseStorage(context, bitmap, file, token, uid, msg, username, block);
            }
        }

        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void uploadBitmapToFirebaseStorage(Context context, Bitmap bitmap, String file, String token, String uid, String msg_text, String username_1, boolean block) {
        Log.d("image", "uploading2");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(FirebaseAuth.getInstance().getUid() + "/" + "files" + "/" + file + ".jpg");

        // Convert the Bitmap to a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // Create a notification to show progress
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default")
                .setSmallIcon(android.R.drawable.ic_menu_upload)
                .setContentTitle("Image Upload")
                .setContentText("Uploading in progress")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setProgress(100, 0, true);

        Notification notification = builder.build();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.POST_NOTIFICATIONS,}, 115);
        }
        NotificationManagerCompat.from(context).notify(1, notification);

        // Upload the byte array to Firebase Storage
        storageRef.putBytes(data)
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    // Publish progress to onProgressUpdate
                    publishProgress((int) progress);
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Map<String, Object> data = new HashMap<>();
                        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                        data.put("msg", msg_text);
                        storageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                data.put("url", task.getResult().toString());
                                Map<String, Object> msg1 = new HashMap<>();

                                if (uid.equals(FirebaseAuth.getInstance().getUid())) {
                                    msg1.put("uid", FirebaseAuth.getInstance().getUid());
                                    msg1.put("msg", data);
                                    msg1.put("time", ServerValue.TIMESTAMP);
                                    msg1.put("seen", "seen");
                                    msg1.put("msgType", "image");
                                }else{
                                    msg1.put("uid", FirebaseAuth.getInstance().getUid());
                                    msg1.put("msg", data);
                                    msg1.put("time", ServerValue.TIMESTAMP);
                                    msg1.put("seen", "unseen");
                                    msg1.put("msgType", "image");
                                }

                                mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("msg").child(uid).child(file).setValue(msg1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (block) {
                                            mDatabase.child(uid).child("msg").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(file).setValue(msg1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    RequestQueue requestQueue = Volley.newRequestQueue(context);
                                                    final String postUrl = "https://fcm.googleapis.com/fcm/send";
                                                    final String fcmServerKey = "AAAAqGC_3wI:APA91bEWt3sHT9i9indyYML7vLiVuu7xRvgR1rJeO8hcgGjpD4z6CJPRFBR-5aYX1DY1rKk-Up2nX0tgMuqtemPVVHwkdF1EcH3DGFJIJVTV2ZxKNFo35bZbHwoRQPNnSyMDOmJXHJgk";
                                                    JSONObject mainObj = new JSONObject();
                                                    try {
                                                        mainObj.put("to", token);
                                                        JSONObject notiObject = new JSONObject();
                                                        notiObject.put("title", "SecHat");
                                                        notiObject.put("body", "@" + username_1 + " : " + msg_text);

                                                        mainObj.put("notification", notiObject);


                                                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj, new Response.Listener<JSONObject>() {
                                                            @Override
                                                            public void onResponse(JSONObject response) {

                                                                // code run is got response

                                                            }
                                                        }, new Response.ErrorListener() {
                                                            @Override
                                                            public void onErrorResponse(VolleyError error) {
                                                                // code run is got error

                                                            }
                                                        }) {
                                                            @Override
                                                            public Map<String, String> getHeaders() throws AuthFailureError {
                                                                Map<String, String> header = new HashMap<>();
                                                                header.put("content-type", "application/json");
                                                                header.put("authorization", "key=" + fcmServerKey);
                                                                return header;


                                                            }
                                                        };
                                                        requestQueue.add(request);
                                                    } catch (JSONException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                    Map<String, Object> data2 = new HashMap<>();
                                                    data2.put("last_msg", "image");
                                                    data2.put("time", ServerValue.TIMESTAMP);
                                                    data2.put("uid", FirebaseAuth.getInstance().getUid());
                                                    mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("chat").child(uid).updateChildren(data2);
                                                    if (!uid.equals(FirebaseAuth.getInstance().getUid())) {
                                                        mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("chat").child(uid).child("unread").runTransaction(new Transaction.Handler() {
                                                            @NonNull
                                                            @Override
                                                            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                                                Integer currentValue = currentData.getValue(Integer.class);
                                                                if (currentValue == null) {
                                                                    currentData.setValue(0);
                                                                }
                                                                // Indicate that the transaction was successful
                                                                return Transaction.success(currentData);
                                                            }

                                                            @Override
                                                            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {

                                                            }
                                                        });
                                                        mDatabase.child(uid).child("chat").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).updateChildren(data2);
                                                        mDatabase.child(uid).child("chat").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("unread").runTransaction(new Transaction.Handler() {
                                                            @NonNull
                                                            @Override
                                                            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                                                Integer currentValue = currentData.getValue(Integer.class);
                                                                if (currentValue == null) {
                                                                    currentData.setValue(1);
                                                                } else {
                                                                    // Otherwise, increment the value
                                                                    currentData.setValue(currentValue + 1);
                                                                }

                                                                // Indicate that the transaction was successful
                                                                return Transaction.success(currentData);
                                                            }

                                                            @Override
                                                            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {

                                                            }
                                                        });
                                                    } else {
                                                        mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("chat").child(uid).child("unread").runTransaction(new Transaction.Handler() {
                                                            @NonNull
                                                            @Override
                                                            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                                                Integer currentValue = currentData.getValue(Integer.class);
                                                                if (currentValue == null) {
                                                                    currentData.setValue(0);
                                                                }
                                                                // Indicate that the transaction was successful
                                                                return Transaction.success(currentData);
                                                            }

                                                            @Override
                                                            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {

                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    updateNotification("Image upload failed", 0);
                });
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        // Update the progress in the notification
        if (values[0] == 100){
            removeNotification();
        }else {
            updateNotification("Uploading image...", values[0]);
        }
    }

    private void createNotificationChannel() {
        if (context == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // Handle the case where context is null or API level is less than Oreo
            return;
        }

        CharSequence name = "Image Upload Channel";
        String description = "Channel for image upload notifications";

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(description);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.createNotificationChannel(channel);
    }


    private void showNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Image Upload")
                .setContentText("Uploading image...")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true);

        // Create and set a progress bar
        builder.setProgress(100, 0, false);

        Notification notification = builder.build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.POST_NOTIFICATIONS,}, 115);
        }
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void updateNotification(String contentText, int progress) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Image Upload")
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true);

        // Update the progress bar
        builder.setProgress(100, progress, false);

        Notification notification = builder.build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.POST_NOTIFICATIONS,}, 115);
        }
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void removeNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
