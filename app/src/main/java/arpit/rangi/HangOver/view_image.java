package arpit.rangi.HangOver;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;

public class view_image extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        String image = getIntent().getStringExtra("url");
        String msg = getIntent().getStringExtra("msg");
        String file_name = getIntent().getStringExtra("file_name");
        TextView msg_txt = findViewById(R.id.image_msg);
        PhotoView imageView = findViewById(R.id.send_image);
        ImageLoader.loadImage(this, image, imageView);
        msg_txt.setText(msg);
        ImageView download = findViewById(R.id.download_image);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(view_image.this, "Downloading...", Toast.LENGTH_SHORT).show();
                ImageDownloader.downloadImage(view_image.this, image, file_name+".jpg");

            }
        });
    }
}