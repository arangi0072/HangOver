package arpit.rangi.HangOver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.chrisbanes.photoview.BuildConfig;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arpit.rangi.HangOver.Module.Msg;
import arpit.rangi.HangOver.adapter.RecyclerViewAdapterMsg;

public class message extends AppCompatActivity {
    boolean text_focus;
    boolean reply;
    String reply_text;
    private int PAGE_SIZE = 25;
    File imageFile;
    String receiver;

    String lastMessageKey;
    DatabaseReference messagesRef;
    RecyclerViewAdapterMsg adapter;
    boolean image_selected = false;
    private final int gallery_code = 101;

    private String currentPhotoPath;
    private  boolean block;
    private  boolean you_block;

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 102;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        DatabaseReference scoresRef = FirebaseDatabase.getInstance().getReference(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())+"/"+receiver);
        scoresRef.keepSynced(true);
        Intent intent = getIntent();
        String uid = intent.getStringExtra("uid");
        receiver = intent.getStringExtra("receiver");
        String token = intent.getStringExtra("token");
        String about = intent.getStringExtra("about");
        String unread = intent.getStringExtra("unread");
        String username_1 = intent.getStringExtra("username");
        String image = intent.getStringExtra("image");
        String name = intent.getStringExtra("name");
        ImageView dp = findViewById(R.id.msg_dp);
        RecyclerViewAdapterMsg recyclerViewAdapter;
        EditText msg = findViewById(R.id.msg_text);
        TextView msgDate2 = findViewById(R.id.msgdate2);
        TextView msg_name = findViewById(R.id.msg_name);
        Intent intent1 = new Intent(this, TypingService.class);
        intent1.putExtra("receiver", receiver);
        this.startService(intent1);
        FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("block").child(receiver).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String blocked = snapshot.getValue(String.class);
                    you_block = blocked.equals("block");
                    if (blocked.equals("block")){
                        View layout = findViewById(R.id.linearLayout3);
                        layout.setVisibility(View.INVISIBLE);
                        TextView textView = findViewById(R.id.block_textview);
                        textView.setVisibility(View.VISIBLE);
                    }
                }else{
                    View layout = findViewById(R.id.linearLayout3);
                    layout.setVisibility(View.VISIBLE);
                    TextView textView = findViewById(R.id.block_textview);
                    textView.setVisibility(View.INVISIBLE);
                    you_block = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child(receiver).child("block").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String blocked = snapshot.getValue(String.class);
                    block = blocked.equals("block");
                }else{
                    block = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        assert image != null;
        ImageView camera = findViewById(R.id.camera_att);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(message.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(message.this,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION);
                } else {
                    openCamera();
                }
            }
        });
        ImageButton imageButton_block = findViewById(R.id.user_info);
        imageButton_block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(message.this, user_info.class);
                intent2.putExtra("uid", receiver);
                intent2.putExtra("username", username_1);
                intent2.putExtra("name", name);
                intent2.putExtra("image", image);
                intent2.putExtra("about", about);
                if (you_block){
                    intent2.putExtra("block", "true");
                    message.this.startActivity(intent2);
                }else{
                    intent2.putExtra("block", "false");
                    message.this.startActivity(intent2);
                }

            }
        });


        Picasso.get().load(Objects.requireNonNull(image)).placeholder(R.drawable.person).into(dp);
        dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View popupView = LayoutInflater.from(message.this).inflate(R.layout.imgae_view, null);
                ImageView imageView1 = popupView.findViewById(R.id.user_image_pop);
                Picasso.get().load(Objects.requireNonNull(image)).placeholder(R.drawable.person).into(imageView1);
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                findViewById(android.R.id.content).setAlpha(0.3f);
                boolean focusable = true;
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
                popupWindow.setOutsideTouchable(true);
                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        findViewById(android.R.id.content).setAlpha(1);
                        popupWindow.dismiss();
                    }
                });
            }
        });
        msg_name.setText(name);
        RecyclerView recyclerView = findViewById(R.id.msg_recycleView);



        ArrayList<Msg> msgArrayList = new ArrayList<>();
        recyclerView.setHasFixedSize(true);
//        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(message.this);
//        linearLayoutManager.setSmoothScrollbarEnabled(true);
//        linearLayoutManager.setStackFromEnd(true);
//        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerViewAdapter = new RecyclerViewAdapterMsg(recyclerView, message.this, msgArrayList, msgDate2, receiver);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapterMsg(recyclerView, message.this, msgArrayList, msgDate2, receiver);
        recyclerView.setAdapter(adapter);

        messagesRef = FirebaseDatabase.getInstance().getReference(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())+"/"+"msg"+"/"+receiver);
        Query query = messagesRef.orderByKey().limitToLast(25);
// Load initial messages
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                msgArrayList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Msg msg;
                    Map<String, Object> data = (Map<String, Object>) snapshot.getValue();
                    if (data.containsKey("msg")) {
                        if (data.containsKey("replay")) {
                            msg = new Msg(Objects.requireNonNull(data.get("uid")).toString(), Objects.requireNonNull(data.get("msg")), data.get("time"), Objects.requireNonNull(data.get("seen")).toString(), snapshot.getKey(), Objects.requireNonNull(data.get("msgType")).toString(), receiver, Objects.requireNonNull(data.get("replay")).toString());

                        } else {
                            msg = new Msg(Objects.requireNonNull(data.get("uid")).toString(), Objects.requireNonNull(data.get("msg")), data.get("time"), Objects.requireNonNull(data.get("seen")).toString(), snapshot.getKey(), Objects.requireNonNull(data.get("msgType")).toString(), receiver);
                        }
                        msgArrayList.add(msg);
                    }
                }
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(msgArrayList.size() - 1 );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("msg", "Error fetching messages", databaseError.toException());
            }
        });

// Implement scroll listener for pagination
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    // User has scrolled to the top
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    if (firstVisibleItemPosition == 0) {
                        // Load more messages
                        loadMoreMessages();
                    }
                }
            }
        });
        View replay_view = findViewById(R.id.replay_view);
        TextView replay_text = findViewById(R.id.reply_text);
        ImageButton replay_close = findViewById(R.id.reply_close);
        replay_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replay_view.setVisibility(View.GONE);
                replay_text.setText("");
                reply = false;
            }
        });


        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                adapter.notifyItemChanged(position);
                Msg item = adapter.getItem(position);
                reply = true;
                replay_view.setVisibility(View.VISIBLE);
                if (item.getMsgType().equals("image")){
                    replay_text.setText("image");
                    reply_text = "image";
                }else{
                    if (item.getMsg().toString().length() >= 60){
                        String re = item.getMsg().toString().substring(0, 60) + "...";
                        replay_text.setText(re);
                        reply_text = re;
                    }else {
                        replay_text.setText(item.getMsg().toString());
                        reply_text = item.getMsg().toString();
                    }
                }

            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        ImageView send = findViewById(R.id.send_msg);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        assert uid != null;
        assert receiver != null;

        mDatabase.child(receiver).child("info").child("last_seen").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Object last = (snapshot.getValue());
                    if (last != null) {
                        TextView last_seen = findViewById(R.id.last_seen);
                        mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("chat").child(receiver).child("typing").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                Object data = (snapshot1.getValue());
                                if (data != null && data.toString().equals("typing")) {
                                    last_seen.setText("typing...");
                                } else {
                                    if (last.equals("Online")) {
                                        last_seen.setText(last.toString());
                                        last_seen.setTextColor(Color.BLACK);
                                    } else {
                                        SimpleDateFormat sfd = new SimpleDateFormat("dd-MMM-yy h:mm a");
                                        last_seen.setText(String.format("Last seen %s", sfd.format(new Date((Long) last))));
                                        last_seen.setTextColor(Color.BLACK);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }else{
                    TextView last_seen = findViewById(R.id.last_seen);
                    last_seen.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//        FirebaseDatabase.getInstance().getReference(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())+"/"+"msg"+"/"+receiver).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                msgArrayList.clear();
//                for (DataSnapshot snapshot1 : snapshot.getChildren()){
//                    Map<String, Object> data = (Map<String, Object>) snapshot1.getValue();
//                    assert data != null;
//
//                    Msg msg10;
//                    if (data.containsKey("msg")) {
//                        if (data.containsKey("replay")) {
//                            msg10 = new Msg(Objects.requireNonNull(data.get("uid")).toString(), Objects.requireNonNull(data.get("msg")), data.get("time"), Objects.requireNonNull(data.get("seen")).toString(), snapshot1.getKey(), Objects.requireNonNull(data.get("msgType")).toString(), receiver, Objects.requireNonNull(data.get("replay")).toString());
//
//                        } else {
//                            msg10 = new Msg(Objects.requireNonNull(data.get("uid")).toString(), Objects.requireNonNull(data.get("msg")), data.get("time"), Objects.requireNonNull(data.get("seen")).toString(), snapshot1.getKey(), Objects.requireNonNull(data.get("msgType")).toString(), receiver);
//                        }
//
//                        msgArrayList.add(msg10);
//                    }
//                }
//                recyclerView.scrollToPosition(msgArrayList.size() - 1 );
//                recyclerViewAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

        FirebaseDatabase.getInstance().getReference(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())+"/"+"msg"+"/"+receiver).orderByChild("time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                msgArrayList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    Map<String, Object> data = (Map<String, Object>) snapshot1.getValue();
                    assert data != null;
                    Msg msg10;
                    if (data.containsKey("msg")) {
                        if (data.containsKey("replay")) {
                            msg10 = new Msg(Objects.requireNonNull(data.get("uid")).toString(), Objects.requireNonNull(data.get("msg")), data.get("time"), Objects.requireNonNull(data.get("seen")).toString(), snapshot1.getKey(), Objects.requireNonNull(data.get("msgType")).toString(), receiver, Objects.requireNonNull(data.get("replay")).toString());
                        } else {
                            msg10 = new Msg(Objects.requireNonNull(data.get("uid")).toString(), Objects.requireNonNull(data.get("msg")), data.get("time"), Objects.requireNonNull(data.get("seen")).toString(), snapshot1.getKey(), Objects.requireNonNull(data.get("msgType")).toString(), receiver);
                        }
                        msgArrayList.add(msg10);
                    }
                }
                recyclerView.scrollToPosition(msgArrayList.size() - 1 );
                recyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        View attach_doc = findViewById(R.id.attach);
        View attach = findViewById(R.id.attach_Layout);
        text_focus = false;
        msg.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    attach.setVisibility(View.GONE);
                    text_focus = true;
                }
            }
        });
        attach_doc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attach.setVisibility(View.VISIBLE);
                msg.clearFocus();
                if (text_focus){
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    text_focus = false;
                }
            }
        });
        msg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!username_1.equals("user")) {
                    if (msg.getText().toString().length() > 0) {
                        mDatabase.child(receiver).child("chat").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("typing").setValue("typing");
                    } else {
                        mDatabase.child(receiver).child("chat").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("typing").setValue("false");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        View send2 = findViewById(R.id.send_msg2);
        EditText msg2 = findViewById(R.id.msg_text2);
        send2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replay_view.setVisibility(View.GONE);
                replay_text.setText("");
                reply = false;
                String mGroupId = mDatabase.push().getKey();
                PhotoView image = findViewById(R.id.send_image);
                image.setDrawingCacheEnabled(true);
                image.buildDrawingCache();
                Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                ByteArrayOutputStream bass = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bass);
                byte[] data = bass.toByteArray();
                boolean x = !block;
                if (username_1.equals("user")){
                    x = false;
                }

                new ImageUploadService().execute(message.this, data, mGroupId, token, uid, msg2.getText().toString(), "@"+username_1, x);
                msg2.setText("");
                View card = findViewById(R.id.send_layout);
                card.setVisibility(View.INVISIBLE);
                image_selected = false;
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean emoji = false;
                String msg_text = msg.getText().toString().trim();
                String emojiPattern = "[\\p{So}]";
                Pattern pattern = Pattern.compile(emojiPattern);

                Matcher matcher = pattern.matcher(msg_text);
                Log.d("msg", "" + matcher.matches());
                if (msg_text.length() > 0 && msg_text.length() <= 3 && matcher.matches()) {
                    emoji = true;
                    Map<String, Object> msg1 = new HashMap<>();
                    if (uid.equals(FirebaseAuth.getInstance().getUid())) {
                        if (reply){
                            msg1.put("uid", FirebaseAuth.getInstance().getUid());
                            msg1.put("msg", msg_text);
                            msg1.put("time", ServerValue.TIMESTAMP);
                            msg1.put("seen", "seen");
                            msg1.put("msgType", "emoji");
                            msg1.put("replay", replay_text.getText().toString());
                        }else {
                            msg1.put("uid", FirebaseAuth.getInstance().getUid());
                            msg1.put("msg", msg_text);
                            msg1.put("time", ServerValue.TIMESTAMP);
                            msg1.put("seen", "seen");
                            msg1.put("msgType", "emoji");
                        }
                    }else {
                        if (reply){
                            msg1.put("uid", FirebaseAuth.getInstance().getUid());
                            msg1.put("msg", msg_text);
                            msg1.put("time", ServerValue.TIMESTAMP);
                            msg1.put("seen", "unseen");
                            msg1.put("msgType", "emoji");
                            msg1.put("replay", replay_text.getText().toString());
                        }else {
                            msg1.put("uid", FirebaseAuth.getInstance().getUid());
                            msg1.put("msg", msg_text);
                            msg1.put("time", ServerValue.TIMESTAMP);
                            msg1.put("seen", "unseen");
                            msg1.put("msgType", "emoji");
                        }
                    }

                    replay_view.setVisibility(View.GONE);
                    replay_text.setText("");
                    reply = false;
                    String mGroupId = mDatabase.push().getKey();
                    assert mGroupId != null;
                    mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("msg").child(uid).child(mGroupId).setValue(msg1).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!block ) {
                                if (!username_1.equals("user")) {
                                    mDatabase.child(uid).child("msg").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(mGroupId).setValue(msg1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            mDatabase.child(receiver).child("chat").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    RequestQueue requestQueue = Volley.newRequestQueue(message.this);
                                                    final String postUrl = "https://fcm.googleapis.com/fcm/send";
                                                    final String fcmServerKey = "AAAAqGC_3wI:APA91bEWt3sHT9i9indyYML7vLiVuu7xRvgR1rJeO8hcgGjpD4z6CJPRFBR-5aYX1DY1rKk-Up2nX0tgMuqtemPVVHwkdF1EcH3DGFJIJVTV2ZxKNFo35bZbHwoRQPNnSyMDOmJXHJgk";
                                                    JSONObject mainObj = new JSONObject();
                                                    try {
                                                        mainObj.put("to", token);
                                                        JSONObject notiObject = new JSONObject();
                                                        notiObject.put("title", "@" + username_1);
                                                        notiObject.put("body", "msg: " + msg_text);

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
                                                    data2.put("last_msg", msg_text);
                                                    data2.put("time", ServerValue.TIMESTAMP);
                                                    data2.put("uid", FirebaseAuth.getInstance().getUid());
                                                    mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("chat").child(receiver).updateChildren(data2);
                                                    if (!receiver.equals(FirebaseAuth.getInstance().getUid())) {
                                                        mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("chat").child(receiver).child("unread").runTransaction(new Transaction.Handler() {
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
                                                        mDatabase.child(receiver).child("chat").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).updateChildren(data2);
                                                        mDatabase.child(receiver).child("chat").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("unread").runTransaction(new Transaction.Handler() {
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
                                                        mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("chat").child(receiver).child("unread").runTransaction(new Transaction.Handler() {
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

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                        }
                                    });
                                }
                            }
                        }
                    });
                    msg.setText("");

                }
                if (msg_text.length() > 0 && !emoji) {
                    Map<String, Object> msg1 = new HashMap<>();

                    if (uid.equals(FirebaseAuth.getInstance().getUid())) {
                        if (reply){
                            msg1.put("uid", FirebaseAuth.getInstance().getUid());
                            msg1.put("msg", msg_text);
                            msg1.put("time", ServerValue.TIMESTAMP);
                            msg1.put("seen", "seen");
                            msg1.put("msgType", "txt");
                            msg1.put("replay", replay_text.getText().toString());
                        }else {
                            msg1.put("uid", FirebaseAuth.getInstance().getUid());
                            msg1.put("msg", msg_text);
                            msg1.put("time", ServerValue.TIMESTAMP);
                            msg1.put("seen", "seen");
                            msg1.put("msgType", "txt");
                        }
                    }else {
                        if (reply){
                            msg1.put("uid", FirebaseAuth.getInstance().getUid());
                            msg1.put("msg", msg_text);
                            msg1.put("time", ServerValue.TIMESTAMP);
                            msg1.put("seen", "unseen");
                            msg1.put("msgType", "txt");
                            msg1.put("replay", replay_text.getText().toString());
                        }else {
                            msg1.put("uid", FirebaseAuth.getInstance().getUid());
                            msg1.put("msg", msg_text);
                            msg1.put("time", ServerValue.TIMESTAMP);
                            msg1.put("seen", "unseen");
                            msg1.put("msgType", "txt");
                        }
                    }
                    replay_view.setVisibility(View.GONE);
                    replay_text.setText("");
                    reply = false;
                    String mGroupId = mDatabase.push().getKey();
                    assert mGroupId != null;
                    mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("msg").child(uid).child(mGroupId).setValue(msg1).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!block) {
                                if (!username_1.equals("user")) {
                                    mDatabase.child(uid).child("msg").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(mGroupId).setValue(msg1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            mDatabase.child(receiver).child("chat").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                    RequestQueue requestQueue = Volley.newRequestQueue(message.this);
                                                    final String postUrl = "https://fcm.googleapis.com/fcm/send";
                                                    final String fcmServerKey = "AAAAqGC_3wI:APA91bEWt3sHT9i9indyYML7vLiVuu7xRvgR1rJeO8hcgGjpD4z6CJPRFBR-5aYX1DY1rKk-Up2nX0tgMuqtemPVVHwkdF1EcH3DGFJIJVTV2ZxKNFo35bZbHwoRQPNnSyMDOmJXHJgk";
                                                    JSONObject mainObj = new JSONObject();
                                                    try {
                                                        mainObj.put("to", token);
                                                        JSONObject notiObject = new JSONObject();
                                                        notiObject.put("title", "@" + username_1);
                                                        notiObject.put("body", "msg: " + msg_text);

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
                                                    data2.put("last_msg", msg_text);
                                                    data2.put("time", ServerValue.TIMESTAMP);
                                                    data2.put("uid", FirebaseAuth.getInstance().getUid());
                                                    mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("chat").child(receiver).updateChildren(data2);
                                                    if (!receiver.equals(FirebaseAuth.getInstance().getUid())) {
                                                        mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("chat").child(receiver).child("unread").runTransaction(new Transaction.Handler() {
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
                                                        mDatabase.child(receiver).child("chat").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).updateChildren(data2);
                                                        mDatabase.child(receiver).child("chat").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("unread").runTransaction(new Transaction.Handler() {
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
                                                        mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("chat").child(receiver).child("unread").runTransaction(new Transaction.Handler() {
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

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    });
                    msg.setText("");
                }
            }
        });
        ImageView pick = findViewById(R.id.gallery_att);
        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(message.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES},111);
                Intent iGallery = new Intent(Intent.ACTION_PICK);
                iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                message.this.startActivityForResult(iGallery , gallery_code);
            }
        });
    }


    @Override
    public void onStart(){
        super.onStart();
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
    }

    public void onActivityResult(int request_code , int result_code , @NonNull Intent data) {
        try {
            super.onActivityResult(gallery_code, result_code, data);
            if (result_code == RESULT_OK) {
                if (request_code == gallery_code) {
                    View card = findViewById(R.id.send_layout);
                    card.setVisibility(View.VISIBLE);
                    PhotoView image = findViewById(R.id.send_image);
                    String picturePath =  getRealPathFromURI(data.getData());
                    imageFile = new File(picturePath);
                    if (imageFile.exists()) {
                        ExifInterface exif = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            exif = new ExifInterface(Objects.requireNonNull(imageFile));
                        }
                        assert exif != null;
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Objects.requireNonNull(data.getData()));
                        bitmap = rotateBitmap(bitmap, orientation);
                        image.setImageBitmap(bitmap);
                        image_selected = true;
                    }
                }
            } if (request_code == REQUEST_IMAGE_CAPTURE && result_code == RESULT_OK) {
                View card = findViewById(R.id.send_layout);
                card.setVisibility(View.VISIBLE);
                setPic();
                image_selected = true;
            }
        }
        catch (Exception e){
            Toast.makeText(message.this, "Please use low resolution Image!" , Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                return bitmap; // No rotation needed
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
    public String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void back(View view){
        if (image_selected){
            View card = findViewById(R.id.send_layout);
            card.setVisibility(View.INVISIBLE);
            image_selected = false;
        }else {
            finish();
        }
    }

    @SuppressLint("MissingSuperCall")
    public void onBackPressed(){
        if (image_selected){
            View card = findViewById(R.id.send_layout);
            card.setVisibility(View.INVISIBLE);
            image_selected = false;
        } else {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child(receiver).child("chat").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("typing").setValue("false");
            finish();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setPic() {
        PhotoView imageView = findViewById(R.id.send_image);

        if (imageView != null && currentPhotoPath != null) {
            try {
                // Decode the image file into a Bitmap without scaling for orientation check
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

                // Get the image orientation
                ExifInterface exif = new ExifInterface(currentPhotoPath);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);


                // Calculate the scale factor based on the orientation
                int targetW = imageView.getWidth();
                int targetH = imageView.getHeight();
                int photoW = (orientation == 90 || orientation == 270) ? bmOptions.outHeight : bmOptions.outWidth;
                int photoH = (orientation == 90 || orientation == 270) ? bmOptions.outWidth : bmOptions.outHeight;

                int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

                // Decode the image file into a Bitmap sized to fill the View with correct orientation
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor;

                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

                // Rotate the bitmap if needed
                bitmap = rotateBitmap(bitmap, orientation);

                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    deleteImageFile();
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Handle the exception appropriately
            }
        }
    }


    private void deleteImageFile() {
        File file = new File(currentPhotoPath);
        if (file.exists()) {
            file.delete();
        }
    }
    private void loadMoreMessages() {
        Query nextQuery = messagesRef.orderByChild("time").endAt(lastMessageKey).limitToLast(PAGE_SIZE + 5);
//        PAGE_SIZE += 1;
        nextQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean first = true;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (!first) {
                        Msg msg;
                        Map<String, Object> data = (Map<String, Object>) snapshot.getValue();
                        if (data.containsKey("msg")) {
                        if (data.containsKey("replay")) {
                            msg = new Msg(Objects.requireNonNull(data.get("uid")).toString(), Objects.requireNonNull(data.get("msg")), data.get("time"), Objects.requireNonNull(data.get("seen")).toString(), snapshot.getKey(), Objects.requireNonNull(data.get("msgType")).toString(), receiver, Objects.requireNonNull(data.get("replay")).toString());

                        } else {
                            msg = new Msg(Objects.requireNonNull(data.get("uid")).toString(), Objects.requireNonNull(data.get("msg")), data.get("time"), Objects.requireNonNull(data.get("seen")).toString(), snapshot.getKey(), Objects.requireNonNull(data.get("msgType")).toString(), receiver);
                        }
                            adapter.addMessages(msg);
                    }
                    } else {
                        first = false;
                        lastMessageKey = snapshot.getKey();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("msg", "Error fetching more messages", databaseError.toException());
            }
        });
    }

    @Override
    public void onDestroy() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(receiver).child("chat").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("typing").setValue("false");
        super.onDestroy();
    }
    @Override
    public void onPause() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(receiver).child("chat").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("typing").setValue("false");
        super.onPause();
    }
    @Override
    public void onStop() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(receiver).child("chat").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("typing").setValue("false");
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            }
        }
    }
}