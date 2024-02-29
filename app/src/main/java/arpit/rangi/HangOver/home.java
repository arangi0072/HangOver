package arpit.rangi.HangOver;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Map;
import java.util.Objects;

import arpit.rangi.HangOver.databinding.ActivityHomeBinding;

public class home extends AppCompatActivity {

    private ActivityHomeBinding binding;
    public String image_url ;


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ActivityCompat.requestPermissions(home.this,new String[]{android.Manifest.permission.ACCESS_NOTIFICATION_POLICY, Manifest.permission.POST_NOTIFICATIONS, },111);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        ImageView image = myToolbar.findViewById(R.id.user_dp);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("info").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> data = (Map<String, Object>) snapshot.getValue();
                if (data != null && data.containsKey("image")) {
                    image_url = Objects.requireNonNull(data.get("image")).toString();
                    Picasso.get().load(image_url).placeholder(R.drawable.person).into(image);
                    ImageView imageView = binding.userDp;
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            View popupView = LayoutInflater.from(binding.getRoot().getContext()).inflate(R.layout.imgae_view, null);
                            ImageView imageView1 = popupView.findViewById(R.id.user_image_pop);
                            Picasso.get().load(Objects.requireNonNull(data.get("image")).toString()).placeholder(R.drawable.person).into(imageView1);
                            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                            binding.getRoot().setAlpha(0.3F);
                            boolean focusable = true;
                            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                            popupWindow.showAtLocation(binding.getRoot(), Gravity.CENTER, 0, 0);
                            popupWindow.setOutsideTouchable(true);
                            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                                @Override
                                public void onDismiss() {
                                    binding.getRoot().setAlpha(1);
                                    popupWindow.dismiss();
                                }
                            });
                        }
                    });
                    ImageView setting = myToolbar.findViewById(R.id.setting);
                    setting.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(binding.getRoot().getContext(), settings.class);
                            intent.putExtra("username", Objects.requireNonNull(data.get("username")).toString());
                            binding.getRoot().getContext().startActivity(intent);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        setSupportActionBar(myToolbar);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_home);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

    }

    @Override
    public void onStart(){
        super.onStart();
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
    }


}