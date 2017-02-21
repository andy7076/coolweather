package com.three.classpie;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;
import com.three.classpie.util.Constant;
import com.three.classpie.util.ProgressDialogUtils;
import com.three.classpie.util.SpUtils;
import com.three.classpie.util.UriToPath;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;
import me.nereo.multi_image_selector.MultiImageSelector;
import okhttp3.Call;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static com.soundcloud.android.crop.Crop.getOutput;

public class ChangeIconActivity extends AppCompatActivity {
    Unbinder unbinder;
    @BindView(R.id.btn_back)
    ImageButton btnBack;
    @BindView(R.id.btn_save)
    Button btnSave;
    @BindView(R.id.chooseIcon)
    Button chooseIcon;
    @BindView(R.id.img_icon)
    CircleImageView imgIcon;

    private Context mContext;
    private static final String TAG = "ChangeIconActivity";

    private static final int REQUEST_IMAGE = 2;
    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    protected static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102;
    private ArrayList<String> mSelectPath;
    private String fileName;

    Uri destination = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_icon);
        unbinder = ButterKnife.bind(this);
        mContext = ChangeIconActivity.this;
        mSelectPath = new ArrayList<>();
    }

    /**
     * 返回按钮
     *
     * @param view
     */
    @OnClick(R.id.btn_back)
    public void back(View view) {
        finish();
    }

    @OnClick(R.id.chooseIcon)
    public void chooseIcon(View view) {
        pickImage();
    }

    @OnClick(R.id.btn_save)
    public void save(View view) {
        if (destination == null) {
            Toast.makeText(this, "请先从本地选择要修改的头像", Toast.LENGTH_SHORT).show();
        } else {
            ProgressDialogUtils.showProgressDialog(mContext, "正在上传头像");
            createFileName();
            OkHttpUtils.post()
                    .addFile("file", fileName,
                            new File(UriToPath.getRealFilePath(mContext, destination)))
                    .addParams("uid", SpUtils.getInt("id") + "")
                    .url(Constant.WEBROOT + "users/uploadIcon")
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            Toast.makeText(ChangeIconActivity.this, "网络连接不可用,请稍后重试", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            if ("success".equals(response)) {
                                Toast.makeText(ChangeIconActivity.this, "头像已上传", Toast.LENGTH_SHORT).show();
                                SpUtils.putString("picUrl", fileName);
                                ProgressDialogUtils.closeProgressDialog();
                                finish();
                            }
                        }
                    });
        }
    }

    private void pickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
                && ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(READ_EXTERNAL_STORAGE,
                    getString(R.string.mis_permission_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        } else {
            MultiImageSelector selector = MultiImageSelector.create(ChangeIconActivity.this);
            selector.showCamera(true);
            selector.single();
            selector.origin(mSelectPath);
            selector.start(ChangeIconActivity.this, REQUEST_IMAGE);
        }
    }

    private void requestPermission(final String permission, String rationale, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.mis_permission_dialog_title)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.mis_permission_dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(ChangeIconActivity.this, new String[]{permission}, requestCode);
                        }
                    })
                    .setNegativeButton(R.string.mis_permission_dialog_cancel, null)
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_READ_ACCESS_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                mSelectPath.clear();
                mSelectPath.addAll(data.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT));
                destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
                Uri sourse = Uri.fromFile(new File(mSelectPath.get(0)));
                Crop.of(sourse, destination).asSquare().start(this);
            }
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            try {
                Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(),
                        Uri.fromFile(new File(UriToPath.getRealFilePath(mContext, getOutput(result)))));
                imgIcon.setImageBitmap(bmp);
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void createFileName() {
        fileName = System.currentTimeMillis() + ".png";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
