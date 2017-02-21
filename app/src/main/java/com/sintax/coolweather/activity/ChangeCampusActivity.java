package com.three.classpie;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.three.classpie.util.Constant;
import com.three.classpie.util.SpUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.Call;

public class ChangeCampusActivity extends AppCompatActivity {
    Unbinder unbinder;
    @BindView(R.id.btn_back)
    ImageButton btnBack;
    @BindView(R.id.btn_save)
    Button btnSave;
    @BindView(R.id.edit_user_campus)
    EditText editCampus;
    private Context mContext;
    private String campusName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_campus);
        mContext = this;
        unbinder = ButterKnife.bind(this);
    }
    /*
   返回按钮
    */
    @OnClick(R.id.btn_back)
    public void back(View view) {
        finish();
    }

    /*
   保存按钮
    */
    @OnClick(R.id.btn_save)
    public void save(View view) {
        int flag = checkFormat();
        if(flag==0) {
            Toast.makeText(mContext, "学校名称不能小于2位", Toast.LENGTH_SHORT).show();
        }else if (flag==1){
            Toast.makeText(mContext, "学校未修改", Toast.LENGTH_SHORT).show();
        }else {
            OkHttpUtils.get()
                    .url(Constant.WEBROOT+"users/changeCampus")
                    .addParams("campus",campusName)
                    .addParams("uid", SpUtils.getInt("id")+"")
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            Toast.makeText(mContext, "网络连接不可用,请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onResponse(String response, int id) {
                            if(response.equals("1")){
                                Toast.makeText(mContext, "修改成功", Toast.LENGTH_SHORT).show();
                                SpUtils.putString("campus", campusName);
                                finish();
                            }else{
                                Toast.makeText(mContext, "修改失败,请稍后再试", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    private int checkFormat(){
        campusName=editCampus.getText().toString().trim();
        if(campusName.length()<2){
            return 0;
        }else if (campusName.equals(SpUtils.getString("campus"))){
            return 1;
        }
        return 2;
    }
}
