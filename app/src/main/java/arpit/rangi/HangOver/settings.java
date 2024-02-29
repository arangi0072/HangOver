package arpit.rangi.HangOver;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Map;
import java.util.Objects;

import arpit.rangi.HangOver.adapter.DBHandler;

public class settings extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ImageView image = findViewById(R.id.image);
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        Log.d("user", username);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        TextView privacy = findViewById(R.id.Privacy);
        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.VIEW",
                        Uri.parse("http://www.google.com"));
                settings.this.startActivity(intent);
            }
        });
        FirebaseApp.initializeApp(settings.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("info").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> data = (Map<String, Object>) snapshot.getValue();
                if (data != null && data.containsKey("image")) {
                    Picasso.get().load(Objects.requireNonNull(data.get("image")).toString()).placeholder(R.drawable.person).into(image);
                    image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            View popupView = LayoutInflater.from(settings.this).inflate(R.layout.imgae_view, null);
                            ImageView imageView1 = popupView.findViewById(R.id.user_image_pop);
                            Picasso.get().load(Objects.requireNonNull(data.get("image")).toString()).placeholder(R.drawable.person).into(imageView1);
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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        TextView delete = findViewById(R.id.delete_account);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View popupView = LayoutInflater.from(settings.this).inflate(R.layout.delete_popup, null);
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                findViewById(android.R.id.content).setAlpha(0.3f);
                boolean focusable = true;
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
                Button leave = popupView.findViewById(R.id.delete_popup);
                leave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ProgressBar progressBar = findViewById(R.id.progressBar8);
                        progressBar.setVisibility(View.VISIBLE);
                        popupWindow.dismiss();

                        FirebaseFirestore.getInstance().collection("username").document(username).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                FirebaseFirestore.getInstance().collection("emails").document(mAuth.getCurrentUser().getEmail()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        FirebaseDatabase.getInstance().getReference().child(mAuth.getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
//
                                                        mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                DBHandler dbHandler = new DBHandler(settings.this);
                                                                dbHandler.updatePersonName("login", "false");
                                                                dbHandler.close();
                                                                findViewById(android.R.id.content).setAlpha(1);
                                                                progressBar.setVisibility(View.INVISIBLE);
                                                                Toast.makeText(settings.this, "Account deleted!", Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(settings.this, MainActivity.class);
                                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                settings.this.startActivity(intent);
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
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
    }
    public void logout(View view){
        ProgressBar progressBar = findViewById(R.id.progressBar8);
        progressBar.setVisibility(View.VISIBLE);
        DBHandler dbHandler = new DBHandler(this);
        dbHandler.updatePersonName("login", "false");
        dbHandler.close();
        FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).update("token", "false").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("info").child("token").setValue("false").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mAuth.signOut();
                        mGoogleSignInClient.signOut();
                        progressBar.setVisibility(View.INVISIBLE);
                        Intent intent = new Intent(settings.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        settings.this.startActivity(intent);
                    }
                });
            }
        });



    }
    public void profile(View view){
        Intent intent = new Intent(settings.this, account_details.class);
        this.startActivity(intent);
    }
    public void privacy_settings(View view){
        Intent intent = new Intent(settings.this, privacy_settings.class);
        this.startActivity(intent);
    }
    public void back(View view){
        finish();
    }
}