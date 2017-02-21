package com.three.classpie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.three.classpie.entity.Announce;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AnnounceDetailActivity extends AppCompatActivity {

    private Unbinder unbinder;
    @BindView(R.id.title_announce)
    TextView annTitle;
    @BindView(R.id.time_announce)
    TextView annTime;
    @BindView(R.id.detail_announce)
    TextView annDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announce_detail);
        unbinder = ButterKnife.bind(this);
        //获取从公告列表item传过来的数据
        Intent intent = getIntent();
        Announce ann = intent.getParcelableExtra("ann");
        annTitle.setText(ann.getTitle());
        annTime.setText(ann.getDate());
        annDetail.setText(ann.getContent());
    }

    @OnClick(R.id.btn_back)
    public void onBack(View v){
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

}
