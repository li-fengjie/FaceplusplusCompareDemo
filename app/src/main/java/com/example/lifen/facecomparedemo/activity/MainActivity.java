package com.example.lifen.facecomparedemo.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lifen.facecomparedemo.R;
import com.megvii.cloud.http.CommonOperate;
import com.megvii.cloud.http.Response;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

/**
 * 人脸对比 1：1
 *
 * @author LiFen
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE1 = 11;
    private static final int REQUEST_CODE2 = 12;
    ImageView mImageView1;
    ImageView mImageView2;
    Button mCompareBtn;
    TextView mResultText;
    private String mImgBase641;
    private String mImgBase642;
    String key = "PhFbAYQAbXkwUOUTEYVuT1xVc_BUdZrx";//api_key
    String secret = "feySAROf9nUwbJeVcMIiYMDRzLRSUkpF";//api_secret
    private final static int i = 100;
    private Handler handler =   new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == i){
                mResultText.setText((String)msg.obj);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView1 = (ImageView) findViewById(R.id.img1);
        mImageView2 = (ImageView) findViewById(R.id.img2);
        mCompareBtn = (Button) findViewById(R.id.compareBtn);
        mResultText = (TextView) findViewById(R.id.resultBtn);
        if(TextUtils.isEmpty(key) || TextUtils.isEmpty(secret)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("please enter key and secret");
            builder.setTitle("");
            builder.show();
            return;
        }
        mImageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAlbumActivity(REQUEST_CODE1);
            }
        });
        mImageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAlbumActivity(REQUEST_CODE2);
            }
        });
        mCompareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCompare();
            }
        });
    }

    private void startCompare() {
        if ("".equals(mImgBase641) || mImgBase641 == null || "".equals(mImgBase642) || mImgBase642 == null) {
            Toast.makeText(this, "请选择图片再比对", Toast.LENGTH_SHORT).show();
            return;
        }
        mResultText.setText("比对中...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "run() called");
                CommonOperate commonOperate = new CommonOperate(key, secret, false);
                try{
                    Response compare = commonOperate.compare(null, null, null, mImgBase641,
                            null, null, null, mImgBase642);
                    String res = new String(compare.getContent());
                    Message msg = new Message();
                    msg.what = i;
                    msg.obj = res;
                    handler.sendMessage(msg);
                }catch (Exception e){
                    Log.i(TAG, "startCompare: " +e.toString());
                }
            }
        }).start();
    }

    private void startAlbumActivity(int requestCode) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;
        Uri uri = data.getData();
        Log.e("uri", uri.toString());
        ContentResolver cr = this.getContentResolver();
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                /* 将Bitmap设定到ImageView */
        } catch (FileNotFoundException e) {
            Log.e("Exception", e.getMessage(), e);
        }
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE1) {
            mImageView1.setImageBitmap(bitmap);
            mImgBase641 = bitmapToBase64(bitmap);
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE2) {
            mImageView2.setImageBitmap(bitmap);
            mImgBase642 = bitmapToBase64(bitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bStream);
        return Base64.encodeToString(bStream.toByteArray(), 0);
    }
}
