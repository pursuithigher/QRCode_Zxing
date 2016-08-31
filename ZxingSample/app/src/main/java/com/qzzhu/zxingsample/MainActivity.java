package com.qzzhu.zxingsample;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;
import com.thridpart.zxingview.OnScannerCompletionListener;
import com.thridpart.zxingview.ScannerView;
import com.thridpart.zxingview.decode.QRDecode;
import com.thridpart.zxingview.encode.QREncode;

public class MainActivity extends AppCompatActivity implements OnScannerCompletionListener {

    private ScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_encode);

        mScannerView = (ScannerView) findViewById(R.id.scanner_view);
        mScannerView.setOnScannerCompletionListener(this);

        mScannerView.setMediaResId(R.raw.beep);//设置扫描成功的声音
        mScannerView.setLaserFrameBoundColor(Color.YELLOW);
        mScannerView.setLaserColor(Color.YELLOW);
    }

    /**
     ********* necessary **************
     */
    @Override
    protected void onResume() {
        mScannerView.onResume();
        super.onResume();
    }

    /**
     ********* necessary **************
     */
    @Override
    protected void onPause() {
        mScannerView.onPause();
        super.onPause();
    }

    /**
     * see ScannerView public Func
     */
    private void ScanView_Useage(){
        //see ScannerView public func
        //such as reScan , set customStyle with QRCode scannerView
    }

    /**
     * create qrcode bitmap and decode qrcode func
     * 二维码 与 一维码
     */
    private void QRcodeImage(){
        //生成二维码
        Bitmap bitmap = QREncode.encodeQR(MainActivity.this,new QREncode.Builder()
                .setParsedResultType(ParsedResultType.TEXT)
                .setContents("hello") //这个方法用于二维码，最终转化为setEncodeContents方法
                .setColor(Color.RED)
                .build());

        //生成一维码
        Bitmap bitmap2 = QREncode.encodeQR(MainActivity.this,new QREncode.Builder()
                .setParsedResultType(ParsedResultType.TEXT)
                .setEncodeContents("hello") //这个方法用于一维码
                .setColor(Color.RED)
                .setBarcodeFormat(BarcodeFormat.CODE_128) //一维码编码方式
                .build());

        //更多格式见 BarcodeFormat.java

        //decode to result callback in
            // OnScannerCompletion(Result rawResult, ParsedResult parsedResult, Bitmap barcode)
        QRDecode.decodeQR(bitmap,MainActivity.this);
    }

    @Override
    public void OnScannerCompletion(Result rawResult, ParsedResult parsedResult, Bitmap barcode) {

    }
}
