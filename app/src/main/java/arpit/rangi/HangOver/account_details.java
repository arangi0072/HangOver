package arpit.rangi.HangOver;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class account_details extends AppCompatActivity {
    private final int gallery_code = 101;
    String image_present = "false";
    String token;
    boolean user;
    File imageFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);
        ImageView image = findViewById(R.id.user_image);
        TextView name = findViewById(R.id.name);
        TextView about = findViewById(R.id.about1);
        TextView email = findViewById(R.id.email2);

        TextView username = findViewById(R.id.user_name);
        FirebaseApp.initializeApp(account_details.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(Objects.requireNonNull(mAuth.getUid())).child("info").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> data = (Map<String, Object>) snapshot.getValue();
                assert data != null;
                image_present = Objects.requireNonNull(data.get("image")).toString();
                if (!image_present.equals("false")){
                    Picasso.get().load(data.get("image").toString()).placeholder(R.drawable.person).into(image);
                }
                name.setText(Objects.requireNonNull(data.get("name")).toString());
                about.setText(Objects.requireNonNull(data.get("about")).toString());
                token = Objects.requireNonNull(data.get("token")).toString();
                email.setText(mAuth.getCurrentUser().getEmail());
                username.setText(Objects.requireNonNull(data.get("username")).toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        ImageView username_change = findViewById(R.id.username_change);
        ImageView edit_name = findViewById(R.id.edit_name);
        ImageView edit_about = findViewById(R.id.edit_about);
        EditText edit_data = findViewById(R.id.edit_data);
        EditText edit_name1 = findViewById(R.id.edit_name1);
        EditText edit_about1 = findViewById(R.id.edit_about1);
        View view = findViewById(R.id.edit_data_layout);
        View view_name = findViewById(R.id.edit_name_layout);
        View view_about = findViewById(R.id.edit_about_layout);
        ImageView save = findViewById(R.id.change_key);
        ImageView save_name = findViewById(R.id.change_name);
        ImageView save_about = findViewById(R.id.change_about);
        save.setVisibility(View.VISIBLE);
        Animation aniSlide = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slid_up);
        username_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view_name.setVisibility(View.INVISIBLE);
                view_about.setVisibility(View.INVISIBLE);
                edit_data.setText(username.getText().toString());
                view.setVisibility(View.VISIBLE);
                view.startAnimation(aniSlide);
            }
        });
        edit_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view_about.setVisibility(View.INVISIBLE);
                view.setVisibility(View.INVISIBLE);
                edit_name1.setText(name.getText().toString());
                view_name.setVisibility(View.VISIBLE);
                view_name.startAnimation(aniSlide);
            }
        });
        edit_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view_name.setVisibility(View.INVISIBLE);
                view.setVisibility(View.INVISIBLE);
                edit_about1.setText(about.getText().toString());
                view_about.setVisibility(View.VISIBLE);
                view_about.startAnimation(aniSlide);
            }
        });
        edit_data.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edit_data.getText().toString().equals(username.getText().toString())){
                    save.setVisibility(View.VISIBLE);
                }
                else if (edit_data.getText().toString().length() > 0){
                    save.setVisibility(View.VISIBLE);
                    db.collection("username").document(edit_data.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                user=false;
                                save.setVisibility(View.INVISIBLE);
                            } else {
                                user=true;
                                save.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                } else {
                    save.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        ImageView close = findViewById(R.id.close_button);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View card = findViewById(R.id.user_image_view);
                card.setVisibility(View.INVISIBLE);
            }
        });
        Button done = findViewById(R.id.done_image);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View card = findViewById(R.id.user_image_view);
                card.setVisibility(View.INVISIBLE);
                ProgressBar progressBar = findViewById(R.id.progressBar2);
                progressBar.setVisibility(View.VISIBLE);
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                ImageView image2 = findViewById(R.id.change_image);
                image2.setDrawingCacheEnabled(true);
                image2.buildDrawingCache();
                Bitmap bitmap = ((BitmapDrawable) image2.getDrawable()).getBitmap();
                ByteArrayOutputStream bass = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bass);
                if (!image_present.equals("false")){
                    storageRef.child(mAuth.getCurrentUser().getUid()).child("dp.jpg").delete();
                }
                byte[] data = bass.toByteArray();
                String filename = mAuth.getCurrentUser().getUid() +"/"+ "dp.jpg";
                StorageReference mountainImagesRef = storageRef.child(filename);
                UploadTask uploadTask = mountainImagesRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mountainImagesRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                mDatabase.child(Objects.requireNonNull(mAuth.getUid())).child("info").child("image").setValue(task.getResult().toString());
                                db.collection("users").document(mAuth.getCurrentUser().getUid()).update("image", task.getResult().toString());
                                db.collection("username").document(username.getText().toString()).update("image", task.getResult().toString());
                                progressBar.setVisibility(View.INVISIBLE);
                                Picasso.get().load(task.getResult().toString()).placeholder(R.drawable.person).into(image);
                            }
                        });
                    }
                });
            }
        });
        Animation aniSlide1 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_down);
        save_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child(Objects.requireNonNull(mAuth.getUid())).child("info").child("name").setValue(edit_name1.getText().toString());
                db.collection("users").document(mAuth.getCurrentUser().getUid()).update("name", edit_name1.getText().toString());
                db.collection("username").document(username.getText().toString()).update("Name", edit_name1.getText().toString());
                name.setText(edit_name1.getText().toString());
                view_name.startAnimation(aniSlide1);
                view_name.setVisibility(View.INVISIBLE);
            }
        });
        save_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child(Objects.requireNonNull(mAuth.getUid())).child("info").child("about").setValue(edit_about1.getText().toString());
                db.collection("users").document(mAuth.getCurrentUser().getUid()).update("about", edit_about1.getText().toString());
                db.collection("username").document(username.getText().toString()).update("about", edit_about1.getText().toString());
                about.setText(edit_about1.getText().toString());
                view_about.startAnimation(aniSlide1);
                view_about.setVisibility(View.INVISIBLE);
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (user) {
                        Map<String, String> data = new HashMap<>();
                        data.put("Name", name.getText().toString());
                        data.put("about", about.getText().toString());
                        data.put("token", token);
                        data.put("userID", mAuth.getCurrentUser().getUid());
                        data.put("image", image_present);
                        data.put("username", edit_data.getText().toString());
                        mDatabase.child(Objects.requireNonNull(mAuth.getUid())).child("info").child("username").setValue(edit_data.getText().toString());
                        db.collection("users").document(mAuth.getCurrentUser().getUid()).update("username", edit_data.getText().toString());
                        db.collection("username").document(edit_data.getText().toString()).set(data);
                        db.collection("username").document(username.getText().toString()).delete();
                        username.setText(edit_data.getText().toString());
                    }
                view.startAnimation(aniSlide1);
                view.setVisibility(View.INVISIBLE);
            }
        });
        view.setOnTouchListener(new OnSwipeTouchListener(account_details.this) {
            public void onSwipeTop() {
            }
            public void onSwipeRight() {
            }
            public void onSwipeLeft() {
            }
            public void onSwipeBottom() {
                view.startAnimation(aniSlide1);
                view.setVisibility(View.INVISIBLE);
            }

        });
        view_name.setOnTouchListener(new OnSwipeTouchListener(account_details.this) {
            public void onSwipeTop() {
            }
            public void onSwipeRight() {
            }
            public void onSwipeLeft() {
            }
            public void onSwipeBottom() {
                view_name.startAnimation(aniSlide1);
                view_name.setVisibility(View.INVISIBLE);
            }

        });
        view_about.setOnTouchListener(new OnSwipeTouchListener(account_details.this) {
            public void onSwipeTop() {
            }
            public void onSwipeRight() {
            }
            public void onSwipeLeft() {
            }
            public void onSwipeBottom() {
                view_about.startAnimation(aniSlide1);
                view_about.setVisibility(View.INVISIBLE);
            }

        });
        ImageView pick = findViewById(R.id.pick_image2);
        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(account_details.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_MEDIA_IMAGES},111);
                Intent iGallery = new Intent(Intent.ACTION_PICK);
                iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                account_details.this.startActivityForResult(iGallery , gallery_code);
            }
        });
    }
    public void onActivityResult(int request_code , int result_code , @NonNull Intent data) {
        try {
            super.onActivityResult(gallery_code, result_code, data);
            if (result_code == RESULT_OK) {
                if (request_code == gallery_code) {
                    View card = findViewById(R.id.user_image_view);
                    card.setVisibility(View.VISIBLE);
                    ImageView image = findViewById(R.id.change_image);
                    String picturePath =  getRealPathFromURI(data.getData());
                    imageFile = new File(picturePath);
                    if (imageFile.exists()) {
                        Glide.with(account_details.this)
                                .load(imageFile)
                                .into(image);
                        boolean image_updated = true;
                    }
                }
            }
        }
        catch (Exception e){
            Toast.makeText(account_details.this, "Please use low resolution Image!" , Toast.LENGTH_LONG).show();
        }
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
        finish();
    }
}