package arpit.rangi.HangOver;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Objects;

public class userDetails extends AppCompatActivity {
    private final int gallery_code = 101;
    private boolean image_updated = false;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        ImageView pick = findViewById(R.id.pickIamge);
        Intent intent2 = getIntent();
        username = intent2.getStringExtra("username");
        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(userDetails.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_EXTERNAL_STORAGE},111);
                Intent iGallery = new Intent(Intent.ACTION_PICK);
                iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                userDetails.this.startActivityForResult(iGallery , gallery_code);
            }
        });
    }
    public void done(View view){
        EditText name = findViewById(R.id.username);
        EditText about = findViewById(R.id.about);
        ScrollView scrollView = findViewById(R.id.scrollView3);
        scrollView.setVisibility(View.INVISIBLE);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        ImageView userPhoto = findViewById(R.id.userPhoto);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String nameText = name.getText().toString();
        String aboutText = about.getText().toString();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(Objects.requireNonNull(mAuth.getUid())).child("info").child("name").setValue(nameText);
        mDatabase.child(Objects.requireNonNull(mAuth.getUid())).child("info").child("about").setValue(aboutText);
        db.collection("users").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).update("name", nameText);
        db.collection("users").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).update("about", aboutText);
        db.collection("username").document(username).update("Name", nameText);
        db.collection("username").document(username).update("about", aboutText);
        if (image_updated){

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            userPhoto.setDrawingCacheEnabled(true);
            userPhoto.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) userPhoto.getDrawable()).getBitmap();
            ByteArrayOutputStream bass = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bass);
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
                            db.collection("username").document(username).update("image",task.getResult().toString());
                            db.collection("users").document(mAuth.getCurrentUser().getUid()).update("image", task.getResult().toString());
                        }
                    });
                    Intent intent = new Intent(userDetails.this, home.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    userDetails.this.startActivity(intent);
                }
            });
        }
        else {
            Intent intent = new Intent(userDetails.this, home.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            userDetails.this.startActivity(intent);
        }
    }
    public void back(View view){
        finish();
    }
    public void onActivityResult(int request_code , int result_code , @NonNull Intent data) {
        try {
            super.onActivityResult(gallery_code, result_code, data);
            if (result_code == RESULT_OK) {
                if (request_code == gallery_code) {
                    ImageView image = findViewById(R.id.userPhoto);
                    String picturePath =  getRealPathFromURI(data.getData());
                    File imageFile = new File(picturePath);
                    if (imageFile.exists()) {
                        Glide.with(userDetails.this)
                                .load(imageFile)
                                .into(image);
                        image_updated = true;
                    }
                }
            }
        }
        catch (Exception e){
            Toast.makeText(userDetails.this, "Please use low resolution Image!" , Toast.LENGTH_LONG).show();
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
}