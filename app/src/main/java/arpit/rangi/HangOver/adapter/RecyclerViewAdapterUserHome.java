package arpit.rangi.HangOver.adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import arpit.rangi.HangOver.R;
import arpit.rangi.HangOver.message;

public class RecyclerViewAdapterUserHome extends RecyclerView.Adapter<RecyclerViewAdapterUserHome.ViewHolder> {
    private Context context;
    private List<Map<String, Object>> userList;

    public RecyclerViewAdapterUserHome(Context context, List<Map<String, Object>> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> data = userList.get(position);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(Objects.requireNonNull(data.get("receiver")).toString()).child("info").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() ) {
                    mDatabase.child(Objects.requireNonNull(data.get("receiver")).toString()).child("block").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot1) {
                            if (snapshot1.exists()){
                                if (Integer.parseInt((data.get("unread")).toString()) > 0) {
                                    holder.unread_msg_count.setText(Objects.requireNonNull(data.get("unread")).toString());
                                    holder.unread_msg_count.setVisibility(View.VISIBLE);
                                } else {
                                    holder.unread_msg_count.setVisibility(View.INVISIBLE);
                                }
                                holder.userName.setText("@user");
                                if (data.get("time") != null) {
                                    holder.name.setText("SecHat User");

                                    SimpleDateFormat sfd = new SimpleDateFormat("h:mm a", Locale.US);
                                    holder.time.setText(sfd.format(new Date((Long) data.get("time"))));
                                    if (Objects.requireNonNull(data.get("uid")).toString().equals(FirebaseAuth.getInstance().getUid())) {
                                        holder.about.setText(String.format("you: %s", Objects.requireNonNull(Objects.requireNonNull(data.get("last_msg")).toString())));
                                    } else {
                                        holder.about.setText(Objects.requireNonNull(data.get("last_msg")).toString());
                                    }
                                }
                            }else{
                                Map<String, Object> data1 = (Map<String, Object>) snapshot.getValue();
                                String image = Objects.requireNonNull(data1.get("image")).toString();
                                String name_user = Objects.requireNonNull(data1.get("name")).toString();
                                String username = "@" + Objects.requireNonNull(data1.get("username"));
                                if (Integer.parseInt((data.get("unread")).toString()) > 0) {
                                    holder.unread_msg_count.setText(Objects.requireNonNull(data.get("unread")).toString());
                                    holder.unread_msg_count.setVisibility(View.VISIBLE);
                                } else {
                                    holder.unread_msg_count.setVisibility(View.INVISIBLE);
                                }
                                holder.userName.setText(username);
                                holder.name.setText(name_user);
                                Picasso.get().load(image).placeholder(R.drawable.person).into(holder.dp);
                                if (data.get("time") != null) {
                                    SimpleDateFormat sfd = new SimpleDateFormat("h:mm a", Locale.US);
                                    holder.time.setText(sfd.format(new Date((Long) data.get("time"))));
                                    if (Objects.requireNonNull(data.get("uid")).toString().equals(FirebaseAuth.getInstance().getUid())) {
                                        holder.about.setText(String.format("you: %s", Objects.requireNonNull(Objects.requireNonNull(data.get("last_msg")).toString())));
                                    } else {
                                        holder.about.setText(Objects.requireNonNull(data.get("last_msg")).toString());
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }else{
                    if (Integer.parseInt((data.get("unread")).toString()) > 0) {
                        holder.unread_msg_count.setText(Objects.requireNonNull(data.get("unread")).toString());
                        holder.unread_msg_count.setVisibility(View.VISIBLE);
                    } else {
                        holder.unread_msg_count.setVisibility(View.INVISIBLE);
                    }
                    holder.userName.setText("@user");
                    holder.name.setText("SecHat User");
                    if (data.get("time") != null) {
                        SimpleDateFormat sfd = new SimpleDateFormat("h:mm a", Locale.US);
                        holder.time.setText(sfd.format(new Date((Long) data.get("time"))));
                        if (Objects.requireNonNull(data.get("uid")).toString().equals(FirebaseAuth.getInstance().getUid())) {
                            holder.about.setText(String.format("you: %s", Objects.requireNonNull(Objects.requireNonNull(data.get("last_msg")).toString())));
                        } else {
                            holder.about.setText(Objects.requireNonNull(data.get("last_msg")).toString());
                        }
                    }
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
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showPopupWindow(v);
                    v.setAlpha(0.3f);
                    return true;
                }
            });

            name = itemView.findViewById(R.id.user_display_name);
            userName = itemView.findViewById(R.id.user_name);
            time = itemView.findViewById(R.id.time);
            unread_msg_count = itemView.findViewById(R.id.unread_msg_count);
            about = itemView.findViewById(R.id.last_msg);
            dp = itemView.findViewById(R.id.dp);
        }
        private void showPopupWindow(View anchorView) {
            // Inflate the pop-up layout
            View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow, null);

            // Create a new instance of PopupWindow
            PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);

            // Find the button in the pop-up layout
            Button me = popupView.findViewById(R.id.popup_delete_me);
            Button both = popupView.findViewById(R.id.popup_delete_both);
            int position = this.getAbsoluteAdapterPosition();
            Map<String, Object> data = userList.get(position);

            // Set click listener for the button
            me.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase.getInstance().getReference().child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("chat").child(Objects.requireNonNull(data.get("receiver")).toString()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            FirebaseDatabase.getInstance().getReference().child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("msg").child(Objects.requireNonNull(data.get("receiver")).toString()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                        }
                    });
                    anchorView.setAlpha(1);
                    popupWindow.dismiss();
                }
            });
            both.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase.getInstance().getReference().child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("chat").child(Objects.requireNonNull(data.get("receiver")).toString()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            FirebaseDatabase.getInstance().getReference().child(Objects.requireNonNull(data.get("receiver")).toString()).child("chat").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    FirebaseDatabase.getInstance().getReference().child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("msg").child(Objects.requireNonNull(data.get("receiver")).toString()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            FirebaseDatabase.getInstance().getReference().child(Objects.requireNonNull(data.get("receiver")).toString()).child("msg").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                    anchorView.setAlpha(1);
                    popupWindow.dismiss();
                }
            });
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
            popupWindow.showAsDropDown(anchorView, (int) (anchorView.getWidth()*(0.5)), 50-anchorView.getHeight());
        }


        @Override
        public void onClick(View view) {
            int position = this.getAbsoluteAdapterPosition();
            Map<String, Object> data = userList.get(position);
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child(Objects.requireNonNull(data.get("receiver")).toString()).child("info").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Map<String, Object> data1 = (Map<String, Object>) snapshot.getValue();
                        assert data1 != null;
                        mDatabase.child(Objects.requireNonNull(data.get("receiver")).toString()).child("block").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                if (snapshot1.exists()){
                                    Intent intent = new Intent(context, message.class);
                                    intent.putExtra("uid", Objects.requireNonNull(data.get("receiver")).toString());
                                    intent.putExtra("token", "");
                                    intent.putExtra("username", "user");
                                    intent.putExtra("name", "SecHat User");
                                    intent.putExtra("image", "false");
                                    intent.putExtra("about", "");
                                    intent.putExtra("unread", Objects.requireNonNull(data.get("unread")).toString());
                                    intent.putExtra("receiver", Objects.requireNonNull(data.get("receiver")).toString());
                                    context.startActivity(intent);
                                }else{
                                    String image = Objects.requireNonNull(data1.get("image")).toString();
                                    String name_user = Objects.requireNonNull(data1.get("name")).toString();
                                    Intent intent = new Intent(context, message.class);
                                    intent.putExtra("uid", Objects.requireNonNull(data.get("receiver")).toString());
                                    intent.putExtra("token", Objects.requireNonNull(data1.get("token")).toString());
                                    intent.putExtra("username", Objects.requireNonNull(data1.get("username")).toString());
                                    intent.putExtra("name", name_user);
                                    intent.putExtra("about", data1.get("about").toString());
                                    intent.putExtra("image", image);
                                    intent.putExtra("unread", Objects.requireNonNull(data.get("unread")).toString());
                                    intent.putExtra("receiver", Objects.requireNonNull(data.get("receiver")).toString());
                                    context.startActivity(intent);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }else{
                        Intent intent = new Intent(context, message.class);
                        intent.putExtra("uid", Objects.requireNonNull(data.get("receiver")).toString());
                        intent.putExtra("token", "");
                        intent.putExtra("username", "user");
                        intent.putExtra("name", "SecHat User");
                        intent.putExtra("image", "false");
                        intent.putExtra("about", "");
                        intent.putExtra("unread", Objects.requireNonNull(data.get("unread")).toString());
                        intent.putExtra("receiver", Objects.requireNonNull(data.get("receiver")).toString());
                        context.startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

}
