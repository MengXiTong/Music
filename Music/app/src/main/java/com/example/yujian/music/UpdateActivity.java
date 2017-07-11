package com.example.yujian.music;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

public class UpdateActivity extends AppCompatActivity {

    private EditText etUpdateName;
    private EditText etUpdateSinger;
    private EditText etUpdateAlbum;
    private EditText etUpdatePath;
    private Spinner spUpdateSound;
    private Button btnUpdateExecute;
    private Button btnUpdateCancel;
    private ArrayAdapter<String> adapter;
    private MyDatabaseHelper myDatabaseHelper;
    private int index = -1;
    private Music music;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        initView();
    }

    private void initView(){
        myDatabaseHelper = new MyDatabaseHelper(UpdateActivity.this);
        etUpdateName = (EditText) findViewById(R.id.et_update_name);
        etUpdateSinger = (EditText) findViewById(R.id.et_update_singer);
        etUpdateAlbum = (EditText) findViewById(R.id.et_update_album);
        etUpdatePath= (EditText) findViewById(R.id.et_update_path);
        spUpdateSound = (Spinner) findViewById(R.id.sp_update_sound);
        btnUpdateExecute = (Button) findViewById(R.id.btn_update_execute);
        btnUpdateCancel = (Button) findViewById(R.id.btn_update_cancel);
        btnUpdateExecute.setOnClickListener(new MyOnClickListener());
        btnUpdateCancel.setOnClickListener(new MyOnClickListener());
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,new String[]{"标准品质","高品质","无损品质"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUpdateSound.setAdapter(adapter);
        Intent intent = getIntent();
        index = intent.getIntExtra("index",-1);
        List<Music> musicList = myDatabaseHelper.query();
        music = musicList.get(index);
        etUpdateName.setText(music.getName());
        etUpdateSinger.setText(music.getSinger());
        etUpdateAlbum.setText(music.getAlbum());
        etUpdatePath.setText(music.getPath());
        spUpdateSound.setSelection(music.getSound());
    }

    class MyOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_update_execute:
                    if(TextUtils.isEmpty(etUpdateName.getText())||TextUtils.isEmpty(etUpdateSinger.getText())
                            ||TextUtils.isEmpty(etUpdateAlbum.getText())||TextUtils.isEmpty(etUpdatePath.getText())){
                        Toast.makeText(UpdateActivity.this,"请完善信息！",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Music updateMusic = new Music();
                        updateMusic.setId(music.getId());
                        updateMusic.setName(etUpdateName.getText().toString());
                        updateMusic.setSinger(etUpdateSinger.getText().toString());
                        updateMusic.setAlbum(etUpdateAlbum.getText().toString());
                        updateMusic.setPath(etUpdatePath.getText().toString());
                        updateMusic.setSound((int) spUpdateSound.getSelectedItemId());
                        myDatabaseHelper.update(updateMusic);
                        Toast.makeText(UpdateActivity.this,"更新成功",Toast.LENGTH_SHORT).show();
                    }
                    UpdateActivity.this.finish();
                    break;
                case R.id.btn_update_cancel:
                    UpdateActivity.this.finish();
                    break;
                default:
                    break;
            }
        }
    }
}
