package au.com.bluedot.pointsdk_aar_example;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
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

public class MainActivity extends AppCompatActivity implements
        ServiceStatusListener,
        ApplicationNotificationListener {

    private ServiceManager mServiceManager;

    //TODO: Configure these values
    private String mEmail = "<email>";
    private String mApiKey = "<api key>";
    private String mPackageName = "<package name>";
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
        mServiceManager.setForegroundServiceNotification(R.drawable.ic_launcher_foreground,
                getString(R.string.foreground_notification_title),
                getString(R.string.foreground_notification_text), pendingIntent, false);

        mServiceManager.sendAuthenticationRequest(mPackageName, mApiKey, mEmail, this, mRestartMode);
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
