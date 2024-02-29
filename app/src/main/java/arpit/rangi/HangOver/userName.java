package arpit.rangi.HangOver;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import arpit.rangi.HangOver.adapter.DBHandler;
import arpit.rangi.HangOver.Module.User;

public class userName extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        ImageView imageView = findViewById(R.id.userNameImage);
        Button signup = findViewById(R.id.next);
        ImageView imageView2 = findViewById(R.id.userNameWrong);
        EditText userName = findViewById(R.id.username);
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Button verify = findViewById(R.id.verify);
        View layout = findViewById(R.id.edit_data_layout);
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(mAuth.getCurrentUser()).reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (mAuth.getCurrentUser().isEmailVerified()){
                            layout.setVisibility(View.INVISIBLE);
                            userName.setVisibility(View.VISIBLE);
                        }
                        else {
                            Toast.makeText(userName.this, "Not Verified!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        Objects.requireNonNull(mAuth.getCurrentUser()).reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!mAuth.getCurrentUser().isEmailVerified()){
                    layout.setVisibility(View.VISIBLE);
                    userName.setVisibility(View.INVISIBLE);
                    mAuth.getCurrentUser().sendEmailVerification()
                            .addOnCompleteListener(userName.this, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(userName.this, "Email Verification link is sent.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
        userName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (userName.getText().toString().length() > 3) {
                    progressBar.setVisibility(View.VISIBLE);
                    imageView2.setVisibility(View.INVISIBLE);
                    imageView.setVisibility(View.INVISIBLE);
                    signup.setVisibility(View.INVISIBLE);
                    db.collection("username").document(userName.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                progressBar.setVisibility(View.INVISIBLE);
                                imageView2.setVisibility(View.VISIBLE);
                                imageView.setVisibility(View.INVISIBLE);
                                signup.setVisibility(View.INVISIBLE);
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                imageView.setVisibility(View.VISIBLE);
                                imageView2.setVisibility(View.INVISIBLE);
                                signup.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }else {
                    signup.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    imageView.setVisibility(View.INVISIBLE);
                    imageView2.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    public void next(View view){
        EditText username = findViewById(R.id.username);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                Map<String, Object> data = new HashMap<>();
                data.put("userID", Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
                data.put("username", username.getText().toString().trim());
                data.put("Name", "");
                data.put("token", task.getResult());
                data.put("about", "");
                data.put("image", "false");
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child(Objects.requireNonNull(mAuth.getUid())).child("info").updateChildren(data);
                User user1 = new User(username.getText().toString(), "", "", "true", "false", FirebaseAuth.getInstance().getUid(), task.getResult());
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                db.collection("users").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).update(user1.toMap()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        db.collection("username").document(username.getText().toString()).set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                DBHandler dbHandler = new DBHandler(userName.this);
                                dbHandler.addNewCourse("true");
                                dbHandler.close();
                            }
                        });
                    }
                });

                Intent intent = new Intent(userName.this, userDetails.class);
                intent.putExtra("username", username.getText().toString());
                userName.this.startActivity(intent);
            }
        });

    }
    public void back(View view){
        finish();
    }
}