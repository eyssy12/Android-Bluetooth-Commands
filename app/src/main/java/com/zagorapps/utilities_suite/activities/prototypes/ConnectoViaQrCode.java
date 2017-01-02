package com.zagorapps.utilities_suite.activities.prototypes;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.zagorapps.utilities_suite.R;
import com.zagorapps.utilities_suite.utils.view.ActivityUtils;

import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ConnectoViaQrCode extends AppCompatActivity implements ZXingScannerView.ResultHandler
{
    private ZXingScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connecto_via_qr_code);

        // TODO: move permission request code in main activity on menu item click
//        if (ContextCompat.checkSelfPermission(
//                        ConnectoViaQrCode.this,
//                        Manifest.permission.CAMERA)
//                        != PackageManager.PERMISSION_GRANTED)
//                {
//                    // Should we show an explanation?
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(ConnectoViaQrCode.this, Manifest.permission.CAMERA))
//                    {
//                        // Show an explanation to the user *asynchronously* -- don't block
//                        // this thread waiting for the user's response! After the user
//                        // sees the explanation, try again to request the permission.
//                    }
//                    else
//                    {
//                        // No explanation needed, we can request the permission.
//
//                        ActivityCompat.requestPermissions(ConnectoViaQrCode.this, new String[]{Manifest.permission.CAMERA}, 12345);
//                    }
//                }
//                else
//                {
//                    scannerView.startCamera();
//                }

        ViewGroup scannerContainer = (ViewGroup) this.findViewById(R.id.scannerContainer);

        ArrayList<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QR_CODE);

        this.scannerView = new ZXingScannerView(this);
        this.scannerView.setFormats(formats);
        this.scannerView.setResultHandler(this);

        scannerContainer.addView(this.scannerView);
        this.scannerView.startCamera();
    }

    // TODO: add to main activity
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case 12345: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    scannerView.startCamera();
                }
                else
                {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }

    public void onPause()
    {
        super.onPause();

        this.scannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result)
    {
        Intent intent = new Intent();
        intent.putExtra("qr_result", result.getText());

        ActivityUtils.finish(this, RESULT_OK, intent);
    }
}