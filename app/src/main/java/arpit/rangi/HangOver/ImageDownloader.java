package arpit.rangi.HangOver;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

public class ImageDownloader {

    public static void downloadImage(Context context, String imageUrl, String fileName) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle("Downloading Image");
        request.setDescription("Image download in progress");

        // Set the destination in the download folder
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/SecHat/image/"+fileName);

        // Enqueue the download and get the download ID
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        Toast.makeText(context, "Image Saved to Download Folder.", Toast.LENGTH_SHORT).show();
    }
}
