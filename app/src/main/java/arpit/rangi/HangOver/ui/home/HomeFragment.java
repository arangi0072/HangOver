package arpit.rangi.HangOver.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import arpit.rangi.HangOver.adapter.RecyclerViewAdapterUserHome;
import arpit.rangi.HangOver.databinding.FragmentHomeBinding;
import arpit.rangi.HangOver.search;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        View searchKey = binding.searchKey;



        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        DatabaseReference scoresRef = FirebaseDatabase.getInstance().getReference(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())+"/"+"chat");
        scoresRef.keepSynced(true);

        mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("info").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> data = (Map<String, Object>) snapshot.getValue();
                if (data != null && data.containsKey("image")) {
                    RecyclerView recyclerView = binding.userRecycle;
                    ArrayList<Map<String, Object>> ArrayList = new ArrayList<>();
                    recyclerView.setHasFixedSize(true);
                    final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(binding.getRoot().getContext());
                    recyclerView.setLayoutManager(linearLayoutManager);
                    RecyclerViewAdapterUserHome recyclerViewAdapter = new RecyclerViewAdapterUserHome(binding.getRoot().getContext(), ArrayList);
                    recyclerView.setAdapter(recyclerViewAdapter);
                    searchKey.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(binding.getRoot().getContext(), search.class);
                            intent.putExtra("username", Objects.requireNonNull(data.get("username")).toString());
                            binding.getRoot().getContext().startActivity(intent);
                        }
                    });

                    FirebaseDatabase.getInstance().getReference().child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("chat").orderByChild("time").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ArrayList.clear();
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                Map<String, Object> data2 = (Map<String, Object>) snapshot1.getValue();
                                assert data2 != null;

                                data2.put("username", data.get("username"));
                                data2.put("receiver", snapshot1.getKey());
                                if (data2.containsKey("last_msg")) {
                                    ArrayList.add(data2);
                                }
                            }
                            Collections.reverse(ArrayList);
                            recyclerViewAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    FirebaseDatabase.getInstance().getReference().child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("chat").orderByChild("time").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ArrayList.clear();
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                Map<String, Object> data2 = (Map<String, Object>) snapshot1.getValue();
                                assert data2 != null;
                                data2.put("username", data.get("username"));
                                data2.put("receiver", snapshot1.getKey());
                                if (data2.containsKey("last_msg")) {
                                    ArrayList.add(data2);
                                }
                            }
                            Collections.reverse(ArrayList);
                            recyclerViewAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return root;
    }
}