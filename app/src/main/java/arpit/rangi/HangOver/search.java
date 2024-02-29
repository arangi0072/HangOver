package arpit.rangi.HangOver;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import arpit.rangi.HangOver.Module.User;
import arpit.rangi.HangOver.adapter.RecyclerViewAdapterUser;

public class search extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerViewAdapterUser recyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        Log.d("user", ""+username);
        EditText searchText = findViewById(R.id.search_text);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        TextView not_found = findViewById(R.id.not_found);
        recyclerView = findViewById(R.id.search_recycle);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = searchText.getText().toString();
                if (text.length() == 0){
                    recyclerView.clearOnScrollListeners();
                    ArrayList<User> userArrayList = new ArrayList<>();
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(search.this));
                    recyclerViewAdapter = new RecyclerViewAdapterUser(search.this, userArrayList);
                    recyclerView.setAdapter(recyclerViewAdapter);
                } else {
                    db.collection("username").orderBy("username").whereGreaterThanOrEqualTo("username",text).limit(25).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            ArrayList<User> userArrayList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> data = document.getData();
                                User user = new User(Objects.requireNonNull(data.get("username")).toString(), Objects.requireNonNull(data.get("Name")).toString(), Objects.requireNonNull(data.get("about")).toString(), username, Objects.requireNonNull(data.get("image")).toString(), Objects.requireNonNull(data.get("userID")).toString(), Objects.requireNonNull(data.get("token")).toString());
                                userArrayList.add(user);
                            }
                            if (userArrayList.size() == 0) {
                                recyclerView.setHasFixedSize(true);
                                recyclerView.setLayoutManager(new LinearLayoutManager(search.this));
                                recyclerViewAdapter = new RecyclerViewAdapterUser(search.this, userArrayList);
                                recyclerView.setAdapter(recyclerViewAdapter);
                                recyclerView.setVisibility(View.INVISIBLE);
                                not_found.setVisibility(View.VISIBLE);
                            } else {
                                not_found.setVisibility(View.INVISIBLE);
                                recyclerView.setVisibility(View.VISIBLE);
                                recyclerView.setHasFixedSize(true);
                                recyclerView.setLayoutManager(new LinearLayoutManager(search.this));
                                recyclerViewAdapter = new RecyclerViewAdapterUser(search.this, userArrayList);
                                recyclerView.setAdapter(recyclerViewAdapter);
                            }
                        }
                    });
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    public void back(View view){
        finish();
    }
}