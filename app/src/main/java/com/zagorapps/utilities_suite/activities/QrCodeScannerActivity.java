package com.zagorapps.utilities_suite.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.zagorapps.utilities_suite.R;
import com.zagorapps.utilities_suite.utils.view.ActivityUtils;

import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QrCodeScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler
{
    private ZXingScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_scanner);

        ViewGroup scannerContainer = (ViewGroup) this.findViewById(R.id.scannerContainer);

        ArrayList<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QR_CODE);

        this.scannerView = new ZXingScannerView(this);
        this.scannerView.setFormats(formats);
        this.scannerView.setResultHandler(this);

        scannerContainer.addView(this.scannerView);
        this.scannerView.startCamera();
    }

    public void onPause()
    {
        super.onPause();

        this.scannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result)
    {
        this.scannerView.stopCamera();

        Intent intent = new Intent();
        intent.putExtra("qr_result", result.getText());

        ActivityUtils.finish(this, RESULT_OK, intent);
    }
}