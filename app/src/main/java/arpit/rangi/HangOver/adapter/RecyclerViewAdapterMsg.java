package arpit.rangi.HangOver.adapter;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import arpit.rangi.HangOver.ImageLoader;
import arpit.rangi.HangOver.Module.Msg;
import arpit.rangi.HangOver.R;
import arpit.rangi.HangOver.view_image;


public class RecyclerViewAdapterMsg extends RecyclerView.Adapter {
    private Context context;
    private final List<Msg> msgList;
    String uid;
    TextView msgDate2;
    RecyclerView recyclerView;
    int send = 1;
    int receive = 2;

    public RecyclerViewAdapterMsg(RecyclerView recyclerView, Context context, List<Msg> userList, TextView msgDate2, String uid) {
        this.context = context;
        this.msgList = userList;
        this.msgDate2 = msgDate2;
        this.uid = uid;
        this.recyclerView = recyclerView;
    }

    public void addMessages(Msg data){
        msgList.add(data);
    }
    public Msg getItem(int position) {
        return msgList.get(position);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == send){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.send_msg, parent, false);
            return new SendViewHolder(view, context, msgList, uid);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recieve_msg, parent, false);
            return new ReceiveViewHolder(view, context, msgList, uid);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Msg msg = msgList.get(position);
        Msg msg2 = null;
        if(position != 0){
            msg2 = msgList.get(position - 1);
        }
        SimpleDateFormat sfd1 = new SimpleDateFormat("dd-MMM-yyyy");
        SimpleDateFormat sfd  = new SimpleDateFormat("h:mm a");

        if (holder.getClass() == SendViewHolder.class){
            if (!Objects.isNull(msg.getReplay())){
                ((SendViewHolder) holder).replay_text.setText(msg.getReplay());
                ((SendViewHolder) holder).replay_view.setVisibility(View.VISIBLE);
            }else {
                ((SendViewHolder) holder).replay_view.setVisibility(View.GONE);
            }
            switch (msg.getMsgType()) {
                case "txt":
                    ((SendViewHolder) holder).image.setVisibility(View.GONE);
                    ((SendViewHolder) holder).msg.setText(msg.getMsg().toString());
                    ((SendViewHolder) holder).layout.setBackgroundResource(R.drawable.out_msg);
                    Linkify.addLinks(((SendViewHolder) holder).msg, Linkify.WEB_URLS);
                    ((SendViewHolder) holder).msg.setTextSize(18);
                    break;
                case "emoji":
                    ((SendViewHolder) holder).image.setVisibility(View.GONE);
                    ((SendViewHolder) holder).msg.setTextSize(50);
                    ((SendViewHolder) holder).msg.setText(msg.getMsg().toString());
                    ((SendViewHolder) holder).layout.setBackgroundResource(0);
                    break;
                case "image":
                    ((SendViewHolder) holder).msg.setTextSize(18);
                    Map<String, Object> data = (Map<String, Object>) msg.getMsg();
                    ((SendViewHolder) holder).layout.setBackgroundResource(R.drawable.out_msg);
                    ((SendViewHolder) holder).msg.setText(Objects.requireNonNull(data.get("msg")).toString());
                    Linkify.addLinks(((SendViewHolder) holder).msg, Linkify.WEB_URLS);
                    ImageLoader.loadImage(context, Objects.requireNonNull(data.get("url")).toString(), ((SendViewHolder) holder).image);
                    ((SendViewHolder) holder).image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, view_image.class);
                            intent.putExtra("url", Objects.requireNonNull(data.get("url")).toString());
                            intent.putExtra("msg", Objects.requireNonNull(data.get("msg")).toString());
                            intent.putExtra("file_name", msg.getMsgId());
                            context.startActivity(intent);
                        }
                    });
                    ((SendViewHolder) holder).image.setVisibility(View.VISIBLE);
                    break;
            }
            if (msg.getSeen().equals("seen")) {
                ((SendViewHolder) holder).unseen.setVisibility(View.INVISIBLE);
                ((SendViewHolder) holder).seen.setVisibility(View.VISIBLE);
            }else {
                ((SendViewHolder) holder).seen.setVisibility(View.INVISIBLE);
                ((SendViewHolder) holder).unseen.setVisibility(View.VISIBLE);
            }
            ((SendViewHolder)holder).time.setText(sfd.format(new Date((Long) msg.getTime())));
            Msg msg3;
            try {
                msg3 = msgList.get(getTopItemPositionOnScreen());
            }catch (Exception e){
                msg3 = msgList.get(position);
            }
            msgDate2.setText(sfd1.format(new Date((Long) msg3.getTime())));
            msgDate2.setVisibility(View.VISIBLE);
            if (position != 0) {
                if (!sfd1.format(new Date((Long) msg.getTime())).equals(sfd1.format(new Date((Long) msg2.getTime())))) {
                    ((SendViewHolder) holder).date.setText(sfd1.format(new Date((Long) msg.getTime())));
                    ((SendViewHolder) holder).date.setVisibility(View.VISIBLE);
                } else {
                    ((SendViewHolder) holder).date.setVisibility(View.GONE);
                }
            }else{
                ((SendViewHolder) holder).date.setText(sfd1.format(new Date((Long) msg.getTime())));
                ((SendViewHolder) holder).date.setVisibility(View.VISIBLE);
            }
        } else {
            if (!Objects.isNull(msg.getReplay())){
                ((ReceiveViewHolder) holder).replay_text.setText(msg.getReplay());
                ((ReceiveViewHolder) holder).replay_view.setVisibility(View.VISIBLE);
            }else {
                ((ReceiveViewHolder) holder).replay_view.setVisibility(View.GONE);
            }
            if (msg.getSeen().equals("unseen")) {
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("chat").child(msg.getUid()).child("unread").runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        Integer currentValue = currentData.getValue(Integer.class);
                        if (currentValue == null) {
                            currentData.setValue(0);
                        } else if (currentValue >= 0) {
                            // Otherwise, increment the value
                            currentData.setValue(currentValue - 1);
                        }

                        // Indicate that the transaction was successful
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {

                    }
                });
                mDatabase.child(msg.getUid()).child("msg").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(msg.getMsgId()).child("seen").setValue("seen").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mDatabase.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("msg").child(msg.getUid()).child(msg.getMsgId()).child("seen").setValue("seen").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
                    }
                });

            }
            switch (msg.getMsgType()) {
                case "txt":
                    ((ReceiveViewHolder) holder).image.setVisibility(View.GONE);
                    ((ReceiveViewHolder) holder).layout.setBackgroundResource(R.drawable.in_msg);
                    ((ReceiveViewHolder)holder).msg.setText(msg.getMsg().toString());
                    ((ReceiveViewHolder) holder).msg.setTextSize(18);
                    Linkify.addLinks(((ReceiveViewHolder) holder).msg, Linkify.WEB_URLS);
                    break;
                case "emoji":
                    ((ReceiveViewHolder) holder).image.setVisibility(View.GONE);
                    ((ReceiveViewHolder) holder).msg.setTextSize(50);
                    ((ReceiveViewHolder)holder).msg.setText(msg.getMsg().toString());
                    ((ReceiveViewHolder) holder).layout.setBackgroundResource(0);
                    break;
                case "image":
                    ((ReceiveViewHolder) holder).msg.setTextSize(18);
                    Map<String, Object> data = (Map<String, Object>) msg.getMsg();
                    ((ReceiveViewHolder) holder).layout.setBackgroundResource(R.drawable.in_msg);
                    ((ReceiveViewHolder) holder).msg.setText(Objects.requireNonNull(data.get("msg")).toString());
                    Linkify.addLinks(((ReceiveViewHolder) holder).msg, Linkify.WEB_URLS);
                    ImageLoader.loadImage(context, Objects.requireNonNull(data.get("url")).toString(), ((ReceiveViewHolder) holder).image);
                    ((ReceiveViewHolder) holder).image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, view_image.class);
                            intent.putExtra("url", Objects.requireNonNull(data.get("url")).toString());
                            intent.putExtra("msg", Objects.requireNonNull(data.get("msg")).toString());
                            intent.putExtra("file_name", msg.getMsgId());
                            context.startActivity(intent);
                        }
                    });
                    ((ReceiveViewHolder) holder).image.setVisibility(View.VISIBLE);
                    break;
            }
            ((ReceiveViewHolder)holder).time.setText(sfd.format(new Date((Long) msg.getTime())));
            Msg msg3;
            try {
                msg3 = msgList.get(getTopItemPositionOnScreen());
            }catch (Exception e){
                msg3 = msgList.get(position);
            }
            msgDate2.setText(sfd1.format(new Date((Long) msg3.getTime())));
            msgDate2.setVisibility(View.VISIBLE);
            if (position !=0 ) {
                if (!sfd1.format(new Date((Long) msg.getTime())).equals(sfd1.format(new Date((Long) msg2.getTime())))) {
                    ((ReceiveViewHolder) holder).date.setText(sfd1.format(new Date((Long) msg.getTime())));
                    ((ReceiveViewHolder) holder).date.setVisibility(View.VISIBLE);
                } else {
                    ((ReceiveViewHolder) holder).date.setVisibility(View.GONE);
                }
            }else {
                ((ReceiveViewHolder) holder).date.setText(sfd1.format(new Date((Long) msg.getTime())));
                ((ReceiveViewHolder) holder).date.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public int getItemViewType(int position){
        if ( msgList.get(position).getUid().equals(FirebaseAuth.getInstance().getUid())){
            return send;
        } else {
            return receive;
        }
    }
    public int getTopItemPositionOnScreen() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        Log.d("msg", ""+layoutManager);
        if (layoutManager != null) {
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
            View firstVisibleItem = layoutManager.findViewByPosition(firstVisibleItemPosition);
            if (firstVisibleItem != null) {
                return  firstVisibleItemPosition;
            }
        }
        return 1;
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    public static class ReceiveViewHolder extends RecyclerView.ViewHolder{
        TextView msg;
        TextView time;

        View replay_view;
        TextView replay_text;
        TextView date;
        ImageView image;
        View layout;


        public ReceiveViewHolder(@NonNull View itemView, Context context, List<Msg> msgList, String uid) {
            super(itemView);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showPopupWindow(v, context, msgList, uid);
                    v.setAlpha(0.3f);
                    return true;
                }
            });

            replay_view = itemView.findViewById(R.id.replay_msg_view);
            replay_text = itemView.findViewById(R.id.replay_msg_text);
            msg = itemView.findViewById(R.id.msg);
            time = itemView.findViewById(R.id.time);
            date = itemView.findViewById(R.id.msgdate);
            image = itemView.findViewById(R.id.msgImage);
            layout = itemView.findViewById(R.id.rec_msg_layout);
        }
        private void showPopupWindow(View anchorView, Context context, List<Msg> msgList, String uid) {
            // Inflate the pop-up layout
            View popupView = LayoutInflater.from(context).inflate(R.layout.msg_pop_up, null);

            // Create a new instance of PopupWindow
            PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);

            // Find the button in the pop-up layout
            Button me = popupView.findViewById(R.id.popup_delete_me);
            Button copy = popupView.findViewById(R.id.msg_copy);
            Button both = popupView.findViewById(R.id.popup_delete_both);
            int position = getAbsoluteAdapterPosition();
            Msg data = msgList.get(position);
            copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = data.getMsg().toString();

                    // Copy text to clipboard
                    if (data.getMsgType().toString().equals("image")){
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Copied Text", "Image");
                        clipboard.setPrimaryClip(clip);
                    }else {
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Copied Text", text);
                        clipboard.setPrimaryClip(clip);
                    }

                    // Show toast message
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                    anchorView.setAlpha(1);
                    popupWindow.dismiss();
                }
            });


            // Set click listener for the button
            me.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase.getInstance().getReference().child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("msg").child(Objects.requireNonNull(uid)).child(data.getMsgId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });
                    anchorView.setAlpha(1);
                    popupWindow.dismiss();
                }
            });
            both.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase.getInstance().getReference().child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("msg").child(Objects.requireNonNull(uid)).child(data.getMsgId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            FirebaseDatabase.getInstance().getReference().child(Objects.requireNonNull(uid)).child("msg").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(data.getMsgId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
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
            if (position <= 3){
                popupWindow.showAsDropDown(anchorView, (int) (anchorView.getWidth() / 3), - anchorView.getHeight());
            }else{
                popupWindow.showAsDropDown(anchorView, (int) (anchorView.getWidth() / 3), 20 - anchorView.getHeight() );
            }

        }
    }
    public static class SendViewHolder extends RecyclerView.ViewHolder {
        TextView msg;
        View replay_view;
        TextView replay_text;
        TextView time;
        TextView date;
        ImageView seen;
        ImageView image;
        View layout;
        ImageView unseen;
        public SendViewHolder(@NonNull View itemView, Context context, List<Msg> msgList, String uid) {
            super(itemView);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showPopupWindow(v, context, msgList, uid);
                    v.setAlpha(0.3F);
                    return true;
                }
            });
            replay_view = itemView.findViewById(R.id.replay_msg_view);
            replay_text = itemView.findViewById(R.id.replay_msg_text);
            msg = itemView.findViewById(R.id.msg);
            time = itemView.findViewById(R.id.time);
            seen = itemView.findViewById(R.id.seen);
            date = itemView.findViewById(R.id.msgdate);
            image = itemView.findViewById(R.id.msgImage);
            unseen = itemView.findViewById(R.id.unseen);
            layout = itemView.findViewById(R.id.send_msg_layout);
        }
        private void showPopupWindow(View anchorView, Context context, List<Msg> msgList, String uid) {
            // Inflate the pop-up layout
            View popupView = LayoutInflater.from(context).inflate(R.layout.msg_pop_up, null);

            // Create a new instance of PopupWindow
            PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);

            // Find the button in the pop-up layout
            Button me = popupView.findViewById(R.id.popup_delete_me);
            Button copy = popupView.findViewById(R.id.msg_copy);
            Button both = popupView.findViewById(R.id.popup_delete_both);
            int position = getAbsoluteAdapterPosition();
            Msg data = msgList.get(position);
            popupWindow.setOutsideTouchable(true);
            copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = data.getMsg().toString();

                    // Copy text to clipboard
                    if (data.getMsgType().toString().equals("image")){
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Copied Text", "Image");
                        clipboard.setPrimaryClip(clip);
                    }else {
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Copied Text", text);
                        clipboard.setPrimaryClip(clip);
                    }

                    // Show toast message
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                }
            });

            // Set click listener for the button
            me.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase.getInstance().getReference().child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("msg").child(Objects.requireNonNull(uid)).child(data.getMsgId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });
                    anchorView.setAlpha(1);
                    popupWindow.dismiss();

                }
            });
            both.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase.getInstance().getReference().child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("msg").child(Objects.requireNonNull(uid)).child(data.getMsgId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            FirebaseDatabase.getInstance().getReference().child(Objects.requireNonNull(uid)).child("msg").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(data.getMsgId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
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
            if (position <= 3){
                popupWindow.showAsDropDown(anchorView, (int) (anchorView.getWidth() / 3), - anchorView.getHeight() );
            }else{
                popupWindow.showAsDropDown(anchorView, (int) (anchorView.getWidth() / 3), 20 - anchorView.getHeight() );
            }
        }
        public interface SwipeListener {
            static void onSwipeLeft(int position) {

            }
        }


    }


}
