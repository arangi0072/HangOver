package arpit.rangi.HangOver;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import arpit.rangi.HangOver.adapter.RecyclerViewAdapterBlockedUsers;

public class blocked_users extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_users);
        RecyclerView recyclerView = findViewById(R.id.blocked_users_recycleview);
        ArrayList<Map<String, Object>> ArrayList = new ArrayList<>();
        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        RecyclerViewAdapterBlockedUsers recyclerViewAdapter = new RecyclerViewAdapterBlockedUsers(this, ArrayList);
        recyclerView.setAdapter(recyclerViewAdapter);
        FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("block").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    ArrayList.clear();
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Map<String, Object> data2 = new HashMap<>();
                        data2.put("uid", snapshot1.getKey());
                        ArrayList.add(data2);
                    }
                    Collections.reverse(ArrayList);
                    recyclerViewAdapter.notifyDataSetChanged();
                }else{
                    ArrayList.clear();
                    recyclerViewAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void back(View view){
        finish();
    }

}