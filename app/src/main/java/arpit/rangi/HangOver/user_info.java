package arpit.rangi.HangOver;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class user_info extends AppCompatActivity {
    String block;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        String uid = intent.getStringExtra("uid");
        String name = intent.getStringExtra("name");
        String about = intent.getStringExtra("about");
        String image = intent.getStringExtra("image");
        block = intent.getStringExtra("block");
        TextView user = findViewById(R.id.info_username);
        TextView user_Name = findViewById(R.id.info_name);
        TextView user_about = findViewById(R.id.info_about);
        ImageView imageView = findViewById(R.id.imageView11);
        user.setText("@"+username);
        user_Name.setText("Name : "+name);
        user_about.setText("About : "+about);
        Log.d("uid", uid);
        Picasso.get().load(Objects.requireNonNull(image)).placeholder(R.drawable.person).into(imageView);

        Button button_block = findViewById(R.id.block);
        if (block.equals("true")){
            button_block.setText("Unblock");
        }
        button_block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (block.equals("true")) {
                    FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("block").child(uid).removeValue();
                    button_block.setText("Block");
                    Toast.makeText(user_info.this, "Unblocked!", Toast.LENGTH_SHORT).show();
                    block = "false";
                }else {
                    FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("block").child(uid).setValue("block");
                    button_block.setText("Unblock");
                    Toast.makeText(user_info.this, "Blocked!", Toast.LENGTH_SHORT).show();
                    block = "true";
                }
            }
        });


    }
    public void back(View view){
        finish();
    }

}