package com.devkim.knoupj;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.devkim.knoupj.privateData.NetWorkPrivateInfo;

/*public class ResultActivity extends AppCompatActivity implements View.OnClickListener {*/
public class ResultActivity extends AppCompatActivity{
    TextView mTxtVResult;
    EditText mEdit_v_result;
    Button mBtnEdit;
    NetWorkPrivateInfo netWorkPrivateInfo = new NetWorkPrivateInfo();
    String mode ="View";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        init();

        Intent intent = getIntent();
        String dataValue = intent.getExtras().getString("textArray");

        String[] resultArray = dataValue.split(netWorkPrivateInfo.getSplitWord());
        dataValue = "";
        for(int i =0 ; i< resultArray.length ; i++){
            dataValue = dataValue + resultArray[i]+"\n";
        }

        mTxtVResult.setText(dataValue);

    }

    void init(){

        mTxtVResult = findViewById(R.id.txt_v_result);
        mEdit_v_result = findViewById(R.id.edit_v_result);
    }

/*    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_edit:
                mode = (mode.equals("View")) ? "Edit" : "View";

                if(mode.equals("View")){
                    String temp = mEdit_v_result.getText().toString();
                    mEdit_v_result.setVisibility(View.GONE);
                    mTxtVResult.setVisibility(View.VISIBLE);
                    mTxtVResult.setText(temp);
                }else if(mode.equals("Edit")){
                    String temp = mTxtVResult.getText().toString();
                    mTxtVResult.setVisibility(View.GONE);
                    mEdit_v_result.setVisibility(View.VISIBLE);
                    mEdit_v_result.setText(temp);
                }
                break;
        }
    }*/
}