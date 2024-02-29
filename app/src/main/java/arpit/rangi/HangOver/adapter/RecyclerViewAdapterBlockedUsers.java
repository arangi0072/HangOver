package arpit.rangi.HangOver.adapter;


import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import arpit.rangi.HangOver.R;

public class RecyclerViewAdapterBlockedUsers extends RecyclerView.Adapter<RecyclerViewAdapterBlockedUsers.ViewHolder> {
    private Context context;
    private List<Map<String, Object>> userList;

    public RecyclerViewAdapterBlockedUsers(Context context, List<Map<String, Object>> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.block_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> data = userList.get(position);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(Objects.requireNonNull(data.get("uid")).toString()).child("info").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() ) {

                    Map<String, Object> data1 = (Map<String, Object>) snapshot.getValue();
                    String image = Objects.requireNonNull(data1.get("image")).toString();
                    String name_user = Objects.requireNonNull(data1.get("name")).toString();
                    String username = "@" + Objects.requireNonNull(data1.get("username"));
                    holder.userName.setText(username);
                    Picasso.get().load(image).placeholder(R.drawable.person).into(holder.dp);
                    holder.name.setText(name_user);
                }else{
                    holder.userName.setText("@user");
                    holder.name.setText("SecHat User");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView userName;
        public TextView name;
        public TextView about;
        public TextView time;
        public TextView unread_msg_count;
        public ImageView dp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            name = itemView.findViewById(R.id.user_display_name);
            userName = itemView.findViewById(R.id.user_name);
            dp = itemView.findViewById(R.id.dp);
        }
        private void showPopupWindow(View anchorView) {
            // Inflate the pop-up layout
            View popupView = LayoutInflater.from(context).inflate(R.layout.unblock_popup, null);

            // Create a new instance of PopupWindow
            PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);

            // Find the button in the pop-up layout
            Button me = popupView.findViewById(R.id.unblock);
            int position = this.getAbsoluteAdapterPosition();
            Map<String, Object> data = userList.get(position);
            me.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("block").child(data.get("uid").toString()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(context, "User Unblocked!", Toast.LENGTH_SHORT).show();
                            anchorView.setAlpha(1);
                            popupWindow.dismiss();
                        }
                    });
                }
            });

            // Set click listener for the button
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    anchorView.setAlpha(1);
                    popupWindow.dismiss();
                }
            });
            // Set focusable and animation style
            popupWindow.setFocusable(true);
            popupWindow.setAnimationStyle(R.anim.popup_animation);

            // Show the popup window at the calculated position
            popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
        }


        @Override
        public void onClick(View view) {
            showPopupWindow(view);
            view.setAlpha(0.3f);
        }
    }

}
