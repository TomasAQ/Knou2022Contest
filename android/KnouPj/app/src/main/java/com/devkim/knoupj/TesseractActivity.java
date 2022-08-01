package com.devkim.knoupj;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TesseractActivity extends AppCompatActivity implements View.OnClickListener{

    Bitmap image;   // 사용되는 이미지
    private TessBaseAPI mTess;  //Tess API reference
    String datapath="";  // 데이터 경로
    TextView OCRTextView;   // OCR 결과출력 뷰
    ImageView imgView;
    String sendImgPath;
    RadioButton rg_btn1 , rg_btn2;
    RadioGroup radioGroup;
    // 사진 요청 코드
    private static final int REQUEST_CODE = 0;
    // Tesseract API 언어 세팅
    String lang = "kor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tesseract);

        OCRTextView = findViewById(R.id.OCRTextView);
        imgView = findViewById(R.id.imageView);
        rg_btn1 = findViewById(R.id.rg_btn1);
        rg_btn2 = findViewById(R.id.rg_btn2);
        radioGroup = findViewById(R.id.radioGroup);

        imgView.setOnClickListener(this);
        radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener);

        // 언어파일 경로
        datapath = getFilesDir()+ "/tesseract/";

        // 트레이닝 데이터가 카피되어 있는지 체크
        checkFile(new File(datapath + "tessdata/"));

        // OCR 세팅
        mTess = new TessBaseAPI();
        mTess.init(datapath , lang);
    }

    //라디오 그룹 클릭 리스너
    RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            if(i == R.id.rg_btn1){
                lang ="kor";
                // 언어파일 경로
                datapath = getFilesDir()+ "/tesseract/";
                // 트레이닝 데이터가 카피되어 있는지 체크
                checkFile(new File(datapath + "tessdata/"));

                mTess = new TessBaseAPI();
                mTess.init(datapath , "kor");
            }
            else if(i == R.id.rg_btn2){
                lang ="eng";
                // 언어파일 경로
                datapath = getFilesDir()+ "/tesseract/";
                // 트레이닝 데이터가 카피되어 있는지 체크
                checkFile(new File(datapath + "tessdata/"));

                mTess = new TessBaseAPI();
                mTess.init(datapath , "eng");
            }
        }
    };

    private void checkFile(File file) {
        // 디렉토리 없으면 만들고 파일 카피
        if(!file.exists() && file.mkdirs()){
            copyFiles();
        }

        // 디렉토리 존재 파일 카피
        if(file.exists()){
            String datafilePath = datapath+"tessdata/"+lang+".traineddata";
            File datafile = new File(datafilePath);
            if(!datafile.exists()){
                copyFiles();
            }
        }

    }

    public void processImage(View view){
        String OCRresult = null;
        BitmapDrawable drawable = (BitmapDrawable) imgView.getDrawable();
        image = drawable.getBitmap();
        mTess.setImage(image);
        OCRresult = mTess.getUTF8Text();
        OCRTextView.setText(OCRresult);
    }

    /**
     *  언어 파일 복사
     */
    private void copyFiles() {
        try {
            String filePath = datapath +"tessdata/"+lang+".traineddata";
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open("tessdata/"+lang+".traineddata");
            OutputStream outputStream = new FileOutputStream(filePath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read= inputStream.read(buffer)) != -1){
                outputStream.write(buffer,0,read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
            Log.d("IOException ", "copyFiles: IOException ");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageView:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent , REQUEST_CODE);
                break;
        }
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

                    Glide.with(getApplicationContext()).load(sendImgPath).into(imgView);

                }catch (Exception e){
                    e.printStackTrace();
                }

            }else if(resultCode == RESULT_CANCELED){    // 취소시 호출할 행동 쓰기

            }
        }
    }

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

}