package com.example.razerdemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOError;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Set;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends Activity {

    //-------------------------------- Variables ----------------------------------//

    // USB Connectivity
    private UsbService usbService;
    private MyHandler mHandler;

    Button setColor;
    SeekBar sliderRed;
    SeekBar sliderGreen;
    SeekBar sliderBlue;

    TextView valueRed;
    TextView valueGreen;
    TextView valueBlue;

    Switch liftLearn;

    //-------------------------------- USB Status Receiver ----------------------------------//
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };


    //-------------------------------- onCreate ----------------------------------//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new MyHandler(this);

        setColor = findViewById(R.id.set_color);

        sliderRed = findViewById(R.id.slider_red);
        sliderGreen = findViewById(R.id.slider_green);
        sliderBlue = findViewById(R.id.slider_blue);

        valueRed = findViewById(R.id.value_red);
        valueGreen = findViewById(R.id.value_green);
        valueBlue = findViewById(R.id.value_blue);

        //Slider Listeners
        sliderRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                valueRed.setText(String.format(Locale.CANADA,"%d",progress));
                updateSliderColor();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        sliderGreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                valueGreen.setText(String.format(Locale.CANADA,"%d",progress));
                updateSliderColor();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        sliderBlue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                valueBlue.setText(String.format(Locale.CANADA,"%d",progress));
                updateSliderColor();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //Tab Button Listeners
        final Button buttonRed = findViewById(R.id.button_red);
        buttonRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = "[RED12345]";
                if (usbService != null) { // if UsbService was correctly binded, Send data
                    usbService.write(data.getBytes());
                }
            }
        });

        final Button buttonGreen = findViewById(R.id.button_green);
        buttonGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = "[GREEN123]";
                if (usbService != null) { // if UsbService was correctly binded, Send data
                    usbService.write(data.getBytes());
                }
            }


        });

        final Button buttonBlue = findViewById(R.id.button_blue);
        buttonBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = "[BLUE1234]";
                if (usbService != null) { // if UsbService was correctly binded, Send data
                    usbService.write(data.getBytes());
                }
            }
        });

        final Button setColor = findViewById(R.id.set_color);
        setColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog(false);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }


    //-------------------------------- Serial Handler ----------------------------------//
    public class MyHandler extends android.os.Handler {
        private final WeakReference<MainActivity> mActivity;
        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);

        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    //mActivity.get().display.append(data);

                    //SCREEN SWITCHING BASED ON SERIAL DATA
                    if (data.contains("[")){
                        //mActivity.get().submitEvent("PushButton","1","Push Button 1 pressed");
                        //mActivity.get().pager.setCurrentItem(0);
                    }else if (data.contains("2")){
                        //mActivity.get().submitEvent("PushButton","2","Push Button 2 pressed");
                        //mActivity.get().pager.setCurrentItem(1);
                    }else if (data.contains("3")){
                        //mActivity.get().submitEvent("PushButton","3","Push Button 3 pressed");
                        //mActivity.get().pager.setCurrentItem(2);
                    }else if (data.contains("0")){
                        //mActivity.get().submitEvent("BigButton","0","Big Button pressed");
                    }
                    break;
            }
        }



        public void sendSerial(String msg){

            if (usbService != null) { // if UsbService was correctly binded, Send data
                try{
                    usbService.write(msg.getBytes());
                    Toast.makeText(getBaseContext(), msg.getBytes().toString(), Toast.LENGTH_SHORT).show();
                }catch(IOError e){
                    Toast.makeText(getBaseContext(), "ERROR - Device not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }

    }


    //-------------------------------- Color Picker ----------------------------------//
    private int currentColor;
    private void openDialog(boolean supportAlpha){
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, currentColor, supportAlpha, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                currentColor = color;
                setColor.setBackgroundColor(currentColor);
                String data = "[S#" + Integer.toHexString(color).substring(2) + "]";
                Toast.makeText(getApplicationContext(),data,Toast.LENGTH_LONG).show();
                if (usbService != null) { // if UsbService was correctly binded, Send data
                    usbService.write(data.getBytes());
                }
                valueRed.setText(Integer.toString(Color.red(color)));
                sliderRed.setProgress(Color.red(color));
                valueGreen.setText(Integer.toString(Color.green(color)));
                sliderGreen.setProgress(Color.green(color));
                valueBlue.setText(Integer.toString(Color.blue(color)));
                sliderBlue.setProgress(Color.blue(color));

            }
        });
        dialog.show();
    }

    //-------------------------------- Slider Updater ----------------------------------//
    public void updateSliderColor(){
        int r = sliderRed.getProgress();
        int g = sliderGreen.getProgress();
        int b = sliderBlue.getProgress();
        String colorString = String.format("%02X", r & 0xFF) + String.format("%02X", g & 0xFF) + String.format("%02X", b & 0xFF);
        setColor.setBackgroundColor(Color.rgb(r,g,b));
        String data = "[S#"+ colorString + "]";
        Toast.makeText(getApplicationContext(),data,Toast.LENGTH_SHORT).show();
        if (usbService != null) { // if UsbService was correctly binded, Send data
            usbService.write(data.getBytes());
        }
    }
}
