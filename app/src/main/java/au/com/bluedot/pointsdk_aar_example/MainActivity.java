package au.com.bluedot.pointsdk_aar_example;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;
import java.util.Map;

import au.com.bluedot.application.model.Proximity;
import au.com.bluedot.point.ApplicationNotificationListener;
import au.com.bluedot.point.ServiceStatusListener;
import au.com.bluedot.point.net.engine.BDError;
import au.com.bluedot.point.net.engine.BeaconInfo;
import au.com.bluedot.point.net.engine.FenceInfo;
import au.com.bluedot.point.net.engine.LocationInfo;
import au.com.bluedot.point.net.engine.ServiceManager;
import au.com.bluedot.point.net.engine.ZoneInfo;

import static android.app.Notification.PRIORITY_MAX;

public class MainActivity extends AppCompatActivity implements
        ServiceStatusListener,
        ApplicationNotificationListener {

    private ServiceManager mServiceManager;

    //TODO: Configure these values
    private String mApiKey = "<api key>";
    private boolean mRestartMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get an instance of ServiceManager
        mServiceManager = ServiceManager.getInstance(this);

        //Setup the notification icon to display when a notification action is triggered
        mServiceManager.setNotificationIDResourceID(R.drawable.ic_launcher_foreground);

        // Android O handling - Set the foreground Service Notification which will fire only if running on Android O and above
        Intent actionIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mServiceManager.setForegroundServiceNotification(createNotification(getString(R.string.foreground_notification_title), getString(R.string.foreground_notification_text), true, Notification.CATEGORY_SERVICE), false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mServiceManager.sendAuthenticationRequest(mApiKey, this, mRestartMode);
    }


    private Notification createNotification(String title, String content, boolean onGoing, String category) {
        Intent actionIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT );

        String channelId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = "Bluedot" + getString(R.string.app_name);
            String channelName = "Bluedot Service"  + getString(R.string.app_name);
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW);
                notificationChannel.enableLights(false);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableVibration(false);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            Notification.Builder notification = new Notification.Builder(getApplicationContext(), channelId)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setStyle(new Notification.BigTextStyle().bigText(content))
                    .setOngoing(onGoing)
                    .setCategory(category)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent);

            return notification.build();
        } else {

            NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext())
                    .setContentTitle(title)
                    .setContentText(content)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                    .setOngoing(onGoing)
                    .setCategory(category)
                    .setPriority(PRIORITY_MAX)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent);

            return notification.build();
        }
    }

    @Override
    public void onCheckIntoFence(FenceInfo fenceInfo, ZoneInfo zoneInfo, LocationInfo locationInfo, Map<String, String> map, boolean b) {
        // This method will be called if the device checked into a Fence
        // and the zone contains a custom action
        // Using a handler to pass Runnable into UI thread to interact with UI elements
    }

    @Override
    public void onCheckedOutFromFence(FenceInfo fenceInfo, ZoneInfo zoneInfo, int i, Map<String, String> map) {
        // This method will be called when a device leaves the Fence that was checked into. Only applies to zones flagged as checkout enabled on the backend.
    }

    @Override
    public void onCheckIntoBeacon(BeaconInfo beaconInfo, ZoneInfo zoneInfo, LocationInfo locationInfo, Proximity proximity, Map<String, String> map, boolean b) {
        // This will be called if the trigger happened on a Beacon
        // and the zone contains a custom action
        // Using runOnUiThread to interact with UI Elements within UI Thread
    }

    @Override
    public void onCheckedOutFromBeacon(BeaconInfo beaconInfo, ZoneInfo zoneInfo, int i, Map<String, String> map) {
        // This method will be called when a device Checks out of a Beacon
    }

    @Override
    public void onBlueDotPointServiceStartedSuccess() {
        // This is called when BlueDotPointService started successfully
        // Your app logic can start from here
        mServiceManager.subscribeForApplicationNotification(this);
    }

    @Override
    public void onBlueDotPointServiceStop() {
        // This will be called when BlueDotPointService has been stopped.
        // Your app could release resources that use the BlueDotPointService
    }

    @Override
    public void onBlueDotPointServiceError(final BDError bdError) {
        // This gives you details if BlueDotPointService encounters error.
        // Human-readable string of bdError.getReason() can be useful to analyse the cause of the error.
        // Boolean bdError.isFatal() identifies if error is fatal and BlueDotPointService is no more functional;
        // onBlueDotPointServiceStop() callback is invoked next in such a case.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(bdError.isFatal() ? "Error" : "Notice")
                        .setMessage(bdError.getReason())
                        .setPositiveButton("OK", null).create().show();
            }
        });
    }

    @Override
    public void onRuleUpdate(List<ZoneInfo> list) {
        //  Passively receive Zones information
        // Optional
    }
}
