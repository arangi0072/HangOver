package arpit.rangi.HangOver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import arpit.rangi.HangOver.adapter.DBHandler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        DBHandler dbHandler = new DBHandler(this);
        dbHandler.readCourses();
        dbHandler.close();
        if (currentUser!= null) {
            if (Objects.equals(dbHandler.readCourses(), "true")) {
                Intent intent = new Intent(MainActivity.this, home.class);
                MainActivity.this.startActivity(intent);
            } else {
                try {
                    FirebaseDatabase.getInstance().setPersistenceEnabled(true);
                    FirebaseDatabase.getInstance().setPersistenceCacheSizeBytes(100000000);
                }catch (Exception e){
                    Log.d("error", e.toString());
                }
                Intent intent = new Intent(MainActivity.this, userName.class);
                MainActivity.this.startActivity(intent);
            }
            MainActivity.this.finish();
        }
    }
    public void loginFrag(View view){
        Intent intent = new Intent(this, loginPage.class);
        this.startActivity(intent);
        MainActivity.this.finish();
    }
    public void signupFrag(View view){
        Intent intent = new Intent(this, SignupActivity.class);
        this.startActivity(intent);
        MainActivity.this.finish();
    }
}