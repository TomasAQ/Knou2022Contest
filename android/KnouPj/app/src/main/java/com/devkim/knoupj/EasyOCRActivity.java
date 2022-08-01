package com.devkim.knoupj;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.devkim.knoupj.privateData.NetWorkPrivateInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EasyOCRActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class. getSimpleName();
    // View
    ImageView mImgVSelectImg;
    Button mBtnSearch;

    // 사진 요청 코드
    private static final int REQUEST_CODE = 0;
    // 보내는 사진 절대 경로
    String sendImgPath;

    RequestQueue queue;

    NetWorkPrivateInfo netWorkPrivateInfo = new NetWorkPrivateInfo();
    // View 초기화
    void init(){
        mImgVSelectImg = findViewById(R.id.img_v_select_img);
        mBtnSearch = findViewById(R.id.btn_search);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_ocractivity);

        init();
        mImgVSelectImg.setOnClickListener(this);
        mBtnSearch.setOnClickListener(this);

        imgSendPermissions();

        queue= Volley.newRequestQueue(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.img_v_select_img:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent , REQUEST_CODE);
                break;

            case R.id.btn_search:
                SendImgtoServer();
                break;
            default:
                Log.d(TAG, "onClick: default!!!");
                break;
        }
    }

    // 서버로 이미지 전송하는 로직
    void SendImgtoServer(){
        String msg = "android 전송";


        String serverUrl = netWorkPrivateInfo.getUrl();
//        serverUrl = serverUrl.replaceAll(" ", "%20");

        SimpleMultiPartRequest simpleMultiPartRequest = new SimpleMultiPartRequest(Request.Method.POST , serverUrl , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                Toast.makeText(MainActivity.this, "서버에 이미지 업로드 성공!!!", Toast.LENGTH_SHORT).show();
//                Log.d(TAG, "onResponse: response : "+response);

                splitData(response);

            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(EasyOCRActivity.this, "서버와 통신 중 오류 발생", Toast.LENGTH_SHORT).show();
            }
        });

        Log.d(TAG, "SendImgtoServer: sendImgPath : "+sendImgPath);
        simpleMultiPartRequest.addFile("img",sendImgPath);

        queue.add(simpleMultiPartRequest);
    }

    private void splitData(String response) {
        Intent intent = new Intent(getApplicationContext() , ResultActivity.class);
        intent.putExtra("textArray",response);
        startActivity(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE){
            if(resultCode == RESULT_OK){
                try {
                    Uri uri = data.getData();

                    // 절대 경로 얻을 수 있는 메소드 사용
                    sendImgPath = createCopyAndReturnRealPath(this, uri);
                    // 절대경로를 미리 확인해 볼 수  있는 Dialog
//                    new AlertDialog.Builder(this).setMessage(uri.toString()+"\n"+sendImgPath).create().show();

                    Glide.with(getApplicationContext()).load(sendImgPath).into(mImgVSelectImg);

                }catch (Exception e){
                    e.printStackTrace();
                }

            }else if(resultCode == RESULT_CANCELED){    // 취소시 호출할 행동 쓰기

            }
        }

    }

    // 절대경로 파악할 때 사용된 메소드
    @Nullable
    public static String createCopyAndReturnRealPath(@NonNull Context context, @NonNull Uri uri) {
        final ContentResolver contentResolver = context.getContentResolver();

        if (contentResolver == null)
            return null;

        // 파일 경로를 만듬
        String filePath = context.getApplicationInfo().dataDir + File.separator + System.currentTimeMillis();

        File file = new File(filePath);
        try {
            // 매개변수로 받은 uri 를 통해  이미지에 필요한 데이터를 불러 들인다.
            InputStream inputStream = contentResolver.openInputStream(uri);
            if (inputStream == null)
                return null;
            // 이미지 데이터를 다시 내보내면서 file 객체에  만들었던 경로를 이용한다.

            OutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0)
                outputStream.write(buf, 0, len);
            outputStream.close();

            inputStream.close();


        } catch (IOException ignore) {
            return null;
        }

        return file.getAbsolutePath();
    }


    // 동적 퍼미션
    private void imgSendPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 마시멜로우 버전과 같거나 이상이라면
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "외부 저장소 사용을 위해 읽기/쓰기 필요", Toast.LENGTH_SHORT).show();
                }

                requestPermissions(new String[]
                                {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                        2);  //마지막 인자는 체크해야될 권한 갯수

            } else {
                //Toast.makeText(this, "권한 승인되었음", Toast.LENGTH_SHORT).show();
            }
        }

    }
    // 권한 관련 로직
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 2 :
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED) //사용자가 허가 했다면
                {
                    Toast.makeText(this, "외부 메모리 읽기/쓰기 권한을 허용하였습니다.", Toast.LENGTH_SHORT).show();

                }else{//거부했다면
                    Toast.makeText(this, "외부 메모리 읽기/쓰기 권한을 제한하였습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}