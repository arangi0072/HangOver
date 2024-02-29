package arpit.rangi.HangOver;

import android.content.Intent;
import android.net.Uri;
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

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        mAuth = FirebaseAuth.getInstance();
        EditText Pass = findViewById(R.id.Password);
        EditText Pass2 = findViewById(R.id.Password2);
        EditText email = findViewById(R.id.Email);
        Button signup = findViewById(R.id.signup);
        TextView privacy = findViewById(R.id.Privacy_policy);
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
                    signup.setVisibility(View.INVISIBLE);
                }
                if (!email.getText().toString().isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches() && Pass2.getText().toString().length()>=6 && Pass.getText().toString().equals(Pass2.getText().toString())){
                    signup.setVisibility(View.VISIBLE);
                }else{
                    signup.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.VIEW",
                        Uri.parse("http://www.google.com"));
                SignupActivity.this.startActivity(intent);
            }
        });
        Pass2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Pass2.getText().toString().length()>=6 && Pass.getText().toString().equals(Pass2.getText().toString()) && !email.getText().toString().isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
                    signup.setVisibility(View.VISIBLE);
                } else {
                    signup.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        Pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Pass2.getText().toString().length()>=6 && Pass.getText().toString().equals(Pass2.getText().toString()) && !email.getText().toString().isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
                    signup.setVisibility(View.VISIBLE);
                } else {
                    signup.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        CheckBox checkBox = findViewById(R.id.show_pass);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    Pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    Pass2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else {
                    Pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    Pass2.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

    }
    public void login(View view){
        Intent intent = new Intent(SignupActivity.this, loginPage.class);
        this.startActivity(intent);
        finish();
    }
    public void back(View view){
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
        this.finish();
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {

    }
    public void emailSignup(View view){
        ProgressBar progressBar = findViewById(R.id.progressBar5);
        progressBar.setVisibility(View.VISIBLE);
        mAuth = FirebaseAuth.getInstance();
        EditText email = findViewById(R.id.Email);
        String emailToText = email.getText().toString();
        EditText Pass = findViewById(R.id.Password);
        String passToText = Pass.getText().toString();
        EditText Pass2 = findViewById(R.id.Password2);
        String pass2ToText = Pass2.getText().toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (!emailToText.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailToText).matches()) {
            if(passToText.length()>=6) {
                if (passToText.equals(pass2ToText)) {
                    db.collection("emails").document(emailToText).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Toast.makeText(SignupActivity.this, "Account already exist.", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }else{
                                mAuth.createUserWithEmailAndPassword(emailToText, passToText)
                                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    // Sign in success, update UI with the signed-in user's information
                                                    Log.d(TAG, "createUserWithEmail:success");
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    FirebaseUser user = mAuth.getCurrentUser();
                                                    updateUI(user);
                                                    Map<String, String> data2 = new HashMap<>();
                                                    data2.put("userID", Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
                                                    Map<String, String> data3 = new HashMap<>();
                                                    data3.put("username", "");
                                                    data3.put("name", "");
                                                    data3.put("about", "");
                                                    data3.put("account", "false");
                                                    data3.put("image", "false");
                                                    data3.put("uid", mAuth.getCurrentUser().getUid());
                                                    data3.put("token", "");
                                                    data3.put("email", mAuth.getCurrentUser().getEmail());
                                                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                                    db.collection("users").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).set(data3).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            db.collection("emails").document(Objects.requireNonNull(mAuth.getCurrentUser().getEmail())).set(data2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    Intent intent = new Intent(SignupActivity.this, userName.class);
                                                                    SignupActivity.this.startActivity(intent);
                                                                }
                                                            });
                                                        }
                                                    });

                                                } else {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    // If sign in fails, display a message to the user.
                                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                                    Toast.makeText(SignupActivity.this, "Account already exit on this email.",
                                                            Toast.LENGTH_SHORT).show();
                                                    updateUI(null);
                                                }
                                            }
                                        });
                            }
                        }
                    });
                } else {
                    Pass2.setError("Password didn't match");
                    Pass2.getError();
                }
            }else {
                Pass.setError("Password has minimum length of 6");
                Pass.getError();
            }
        }else {
            email.setError("Enter valid email");
            email.getError();
        }
    }
}