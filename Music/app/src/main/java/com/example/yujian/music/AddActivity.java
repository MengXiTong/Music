package com.example.yujian.music;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class AddActivity extends AppCompatActivity {

    private EditText etAddName;
    private EditText etAddSinger;
    private EditText etAddAlbum;
    private EditText etAddPath;
    private Spinner spAddSound;
    private Button btnAddExecute;
    private Button btnAddCancel;
    private ArrayAdapter<String> adapter;
    private MyDatabaseHelper myDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        initView();
    }

    private void initView(){
        myDatabaseHelper = new MyDatabaseHelper(AddActivity.this);
        etAddName = (EditText) findViewById(R.id.et_add_name);
        etAddSinger = (EditText) findViewById(R.id.et_add_singer);
        etAddAlbum = (EditText) findViewById(R.id.et_add_album);
        etAddPath= (EditText) findViewById(R.id.et_add_path);
        spAddSound = (Spinner) findViewById(R.id.sp_add_sound);
        btnAddExecute = (Button) findViewById(R.id.btn_add_execute);
        btnAddCancel = (Button) findViewById(R.id.btn_add_cancel);
        btnAddExecute.setOnClickListener(new MyOnClickListener());
        btnAddCancel.setOnClickListener(new MyOnClickListener());
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,new String[]{"标准品质","高品质","无损品质"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAddSound.setAdapter(adapter);
    }

    class MyOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_add_execute:
                    if(TextUtils.isEmpty(etAddName.getText())||TextUtils.isEmpty(etAddSinger.getText())
                            ||TextUtils.isEmpty(etAddAlbum.getText())||TextUtils.isEmpty(etAddPath.getText())){
                        Toast.makeText(AddActivity.this,"请完善信息！",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Music music = new Music();
                        music.setName(etAddName.getText().toString());
                        music.setSinger(etAddSinger.getText().toString());
                        music.setAlbum(etAddAlbum.getText().toString());
                        music.setPath(etAddPath.getText().toString());
                        music.setSound((int) spAddSound.getSelectedItemId());
                        myDatabaseHelper.add(music);
                        Toast.makeText(AddActivity.this,"添加成功",Toast.LENGTH_SHORT).show();
                    }
                    AddActivity.this.finish();
                    break;
                case R.id.btn_add_cancel:
                    AddActivity.this.finish();
                    break;
                default:
                    break;
            }
        }
    }
}
