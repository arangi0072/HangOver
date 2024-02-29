package arpit.rangi.HangOver;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Map;
import java.util.Objects;

import arpit.rangi.HangOver.adapter.DBHandler;


public class loginPage extends AppCompatActivity {
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // [END config_signin]

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        TextView forget = findViewById(R.id.forget_password);
        forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(loginPage.this, forgot_password.class);
                loginPage.this.startActivity(intent);
            }
        });
        EditText pass = findViewById(R.id.Password);
        EditText email = findViewById(R.id.Email);
        Button login = findViewById(R.id.login);
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!email.getText().toString().isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
                    email.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_mark_email_read_24, 0, R.drawable.baseline_done_24, 0);
                }
                else{
                    email.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_mark_email_read_24, 0, R.drawable.baseline_clear_24, 0);
                    login.setVisibility(View.INVISIBLE);
                }
                if (!email.getText().toString().isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches() && pass.getText().toString().length()>=6 ){
                    login.setVisibility(View.VISIBLE);
                }else{
                    login.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (pass.getText().toString().length()>=6 && !email.getText().toString().isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
                    login.setVisibility(View.VISIBLE);
                } else{
                    login.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        CheckBox checkBox = findViewById(R.id.show_password);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else {
                    pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });


    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                ProgressBar progressBar = findViewById(R.id.progressBar3);
                progressBar.setVisibility(View.INVISIBLE);
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }
    // [END onactivityresult]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            assert user != null;
                            db.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Map<String, Object> data = document.getData();
                                        assert data != null;
                                        if (Objects.equals(data.get("account"), "false")){
                                            Intent intent = new Intent(loginPage.this, userName.class);
                                            loginPage.this.startActivity(intent);
                                            loginPage.this.finish();
                                        } else {
                                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                                @Override
                                                public void onComplete(@NonNull Task<String> task) {
                                                    db.collection("users").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).update("token", task.getResult());
                                                    db.collection("username").document(Objects.requireNonNull(data.get("username")).toString()).update("token", task.getResult());
                                                    DBHandler dbHandler = new DBHandler(loginPage.this);
                                                    dbHandler.addNewCourse("true");
                                                    dbHandler.close();
                                                    Intent intent = new Intent(loginPage.this, home.class);
                                                    loginPage.this.startActivity(intent);
                                                    loginPage.this.finish();
                                                }
                                            });
                                        }

                                    }else {
                                        mAuth.signOut();
                                        mGoogleSignInClient.signOut();
                                        user.delete();
                                        Toast.makeText(loginPage.this, "No account found .", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            updateUI();
                        }
                    }
                });
    }
    // [END auth_with_google]

    public void signIn(View view) {
        ProgressBar progressBar = findViewById(R.id.progressBar3);
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void updateUI() {

    }
    public void back(View view){
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
        this.finish();
    }
    public void signIn_email(View view) {
        ProgressBar progressBar = findViewById(R.id.progressBar3);
        progressBar.setVisibility(View.VISIBLE);
        EditText email = findViewById(R.id.Email);
        EditText pass = findViewById(R.id.Password);
        String email_text = email.getText().toString();
        String password = pass.getText().toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (!email_text.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email_text).matches()) {
            db.collection("emails").document(email_text).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        mAuth.signInWithEmailAndPassword(email_text, password)
                                .addOnCompleteListener(loginPage.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d(TAG, "signInWithEmail:success");
                                            progressBar.setVisibility(View.INVISIBLE);
                                            db.collection("users").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task1) {
                                                    DocumentSnapshot document1 = task1.getResult();
                                                    Map<String, Object> data = document1.getData();
                                                    assert data != null;
                                                    if (Objects.equals(data.get("account"), "false")) {
                                                        Intent intent = new Intent(loginPage.this, userName.class);
                                                        loginPage.this.startActivity(intent);
                                                        loginPage.this.finish();
                                                    } else {
                                                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<String> task) {
                                                                db.collection("users").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).update("token", task.getResult());
                                                                db.collection("username").document(Objects.requireNonNull(data.get("username")).toString()).update("token", task.getResult());
                                                                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                                                                mDatabase.child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).child("info").child("token").setValue("false");
                                                                DBHandler dbHandler = new DBHandler(loginPage.this);
                                                                dbHandler.addNewCourse("true");
                                                                dbHandler.close();

                                                                Intent intent = new Intent(loginPage.this, home.class);
                                                                loginPage.this.startActivity(intent);
                                                                loginPage.this.finish();
                                                            }
                                                        });
                                                    }

                                                }
                                            });
                                            updateUI();
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                                            pass.setError("Wrong password!");
                                            pass.getError();
                                            progressBar.setVisibility(View.INVISIBLE);
                                            updateUI();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(loginPage.this, "No account found .", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else {
            email.setError("Enter valid email");
            email.getError();
        }
        // [END sign_in_with_email]
    }
    public void signup(View view){
        Intent intent = new Intent(loginPage.this, SignupActivity.class);
        this.startActivity(intent);
        finish();
    }
}