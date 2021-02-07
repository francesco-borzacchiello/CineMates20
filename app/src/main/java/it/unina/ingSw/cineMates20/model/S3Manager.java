package it.unina.ingSw.cineMates20.model;

import android.app.Activity;
import android.net.Uri;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import it.unina.ingSw.cineMates20.BuildConfig;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class S3Manager {

    private static final String AWS_ACCESS_KEY = BuildConfig.AWS_ACCESS_KEY,
                                AWS_SECRET_KEY = BuildConfig.AWS_SECRET_KEY,
                                S3_BUCKET_NAME = BuildConfig.S3_BUCKET_NAME;

    public static void uploadImage(@NotNull Activity activity, @NotNull Uri uriImage, @NotNull String userEmail) {
        AWSCredentials credentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY);

        AmazonS3Client s3 = new AmazonS3Client(credentials, Region.getRegion(Regions.EU_WEST_3));

        TransferUtility transferUtility =
                TransferUtility.builder().defaultBucket(S3_BUCKET_NAME)
                        .context(activity).s3Client(s3).build();

        try {
            //Conversione dell'estensione della foto a .jpg e upload
            transferUtility.upload("Img/" + userEmail + ".jpg", activity.getContentResolver().openInputStream(uriImage));
        } catch (FileNotFoundException e) { //Si prova a prendere la foto da un url
            Thread t = new Thread(()-> {
                try {
                    URL url = new URL(uriImage.toString());
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        connection.setDoInput(true);
                        connection.connect();
                    }

                    InputStream input = connection.getInputStream();

                    transferUtility.upload("Img/" + userEmail + ".jpg", input);
                } catch (Exception ignore) {
                    Utilities.stampaToast(activity, "Si è verificato un errore,\nriprova più tardi.");
                }
            });
            t.start();
        }
        catch(IOException e2) {
            Utilities.stampaToast(activity, "Si è verificato un errore,\nriprova più tardi.");
        }
    }

    @Nullable
    public static String getProfilePictureUrl(@NotNull String userEmail) {
        AWSCredentials credentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY);

        AmazonS3Client s3 = new AmazonS3Client(credentials, Region.getRegion(Regions.EU_WEST_3));

        String[] ret = new String[1];
        Thread t = new Thread(()-> {
            if(s3.doesObjectExist(S3_BUCKET_NAME, "Img/" + userEmail + ".jpg"))
                ret[0] = s3.getResourceUrl(S3_BUCKET_NAME, "Img/" + userEmail + ".jpg");
        });
        t.start();
        try {
            t.join();
        }catch(InterruptedException ignore){}

        return ret[0];
    }
}
