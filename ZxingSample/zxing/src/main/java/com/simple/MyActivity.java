package com.simple;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.R;
import com.google.zxing.Result;
import com.google.zxing.client.result.AddressBookParsedResult;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;
import com.google.zxing.client.result.URIParsedResult;
import com.thridpart.zxingview.OnScannerCompletionListener;
import com.thridpart.zxingview.ScannerView;
import com.thridpart.zxingview.common.Intents;
import com.thridpart.zxingview.decode.QRDecode;
import com.thridpart.zxingview.encode.QREncode;

/**
 * Created by qzzhu on 16-8-30.
 */

public class MyActivity extends Activity implements OnScannerCompletionListener {
    private ScannerView mScannerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_encode);

        mScannerView = (ScannerView) findViewById(R.id.scanner_view);
        mScannerView.setOnScannerCompletionListener(this);

        mScannerView.setMediaResId(R.raw.beep);//设置扫描成功的声音
        mScannerView.setLaserFrameBoundColor(Color.YELLOW);
        mScannerView.setLaserColor(Color.YELLOW);
    }

    @Override
    public void OnScannerCompletion(Result rawResult, ParsedResult parsedResult, Bitmap barcode) {
        if (rawResult == null) {
            Toast.makeText(this, "未发现二维码", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Bundle bundle = new Bundle();
        ParsedResultType type = parsedResult.getType();
        switch (type) {
            case ADDRESSBOOK:
                AddressBookParsedResult addressResult = (AddressBookParsedResult) parsedResult;
                bundle.putStringArray(Intents.AddressBookConnect.NAME, addressResult.getNames());
                bundle.putStringArray(Intents.AddressBookConnect.NUMBER, addressResult.getPhoneNumbers());
                bundle.putStringArray(Intents.AddressBookConnect.EMAIL, addressResult.getEmails());
                break;
            case PRODUCT:
                break;
            case URI:
                URIParsedResult uriParsedResult = (URIParsedResult) parsedResult;
                bundle.putString(Intents.URIContents.URI, uriParsedResult.getURI());
                break;
            case TEXT:
                bundle.putString(Intents.Scan.RESULT, rawResult.getText());
                break;
            case GEO:
                break;
            case TEL:
                break;
            case SMS:
                break;
        }
        onResultActivity(rawResult, type, bundle);
    }

    private void onResultActivity(Result rawResult, ParsedResultType type, Bundle bundle) {

    }

    @Override
    protected void onResume() {
        mScannerView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mScannerView.onPause();
        super.onPause();
    }
}
