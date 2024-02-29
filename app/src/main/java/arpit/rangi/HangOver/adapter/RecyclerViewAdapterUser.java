package arpit.rangi.HangOver.adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import arpit.rangi.HangOver.Module.User;
import arpit.rangi.HangOver.R;
import arpit.rangi.HangOver.message;

public class RecyclerViewAdapterUser extends RecyclerView.Adapter<RecyclerViewAdapterUser.ViewHolder> {
    private Context context;
    private List<User> userList;

    public RecyclerViewAdapterUser(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        String username =  "@"+user.getUsername();
        holder.userName.setText(username);
        holder.name.setText(user.getName());
        holder.about.setText(user.getAbout());
        if (!user.getImage().equals("false")){
            Picasso.get().load(Objects.requireNonNull(user.getImage())).placeholder(R.drawable.person).into(holder.dp);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView userName;
        public TextView name;
        public TextView about;
        public ImageView dp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            name = itemView.findViewById(R.id.user_name);
            userName = itemView.findViewById(R.id.search_username);
            about = itemView.findViewById(R.id.search_about);
            dp = itemView.findViewById(R.id.dp);
        }

        @Override
        public void onClick(View view) {
            int position = this.getAdapterPosition();
            User user = userList.get(position);
            Intent intent = new Intent(context, message.class);
            intent.putExtra("uid", user.getUid());
            intent.putExtra("receiver", user.getUid());
            intent.putExtra("username", user.getAccount());
            intent.putExtra("unread", '0');
            intent.putExtra("token", user.getToken());
            intent.putExtra("name", user.getName());
            intent.putExtra("image", user.getImage());
            context.startActivity(intent);
        }
    }

}
