package arpit.rangi.HangOver;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.checkerframework.checker.nullness.qual.NonNull;

public class forgot_password extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        EditText email = findViewById(R.id.forgot_password);
        Button next = findViewById(R.id.next);
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!email.getText().toString().isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
                    email.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_mark_email_read_24, 0, R.drawable.baseline_done_24, 0);
                    next.setVisibility(View.VISIBLE);
                }
                else{
                    email.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_mark_email_read_24, 0, R.drawable.baseline_clear_24, 0);
                    next.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(forgot_password.this, "Link is sent to your email.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    public void back(View view){
        finish();
    }
}