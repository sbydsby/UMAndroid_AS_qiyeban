package com.sheca.umandroid.companyCert;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sheca.umandroid.R;
import com.sheca.umandroid.companyCert.album.PictureSelector;
import com.sheca.umandroid.companyCert.album.compress.Luban;
import com.sheca.umandroid.companyCert.album.config.PictureConfig;
import com.sheca.umandroid.companyCert.album.entity.LocalMedia;
import com.sheca.umandroid.companyCert.album.tools.Constant;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.custle.dyrz.utils.Utils.getCurrentTime;

/**
 * @author shaoboyang
 * @time 2019/8/6 10:41
 * @descript 申请印章--上传公章
 */
public class SealApplyStep1 extends Activity {
    //    @BindView(R.id.cl_title)
//    ConstraintLayout mClTitle;
    @BindView(R.id.iv_back)
    ImageView mIvBack;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.tv_seal_photo)
    TextView mTvSealPhoto;
    @BindView(R.id.tv_seal_album)
    TextView mTvSealAlbum;
//    @BindView(R.id.timeline)
//    TimeLineView timeLineView;


    private final int START_CROP_IMAGE_REQUEST = 888;

    String originPath = "";
    String newPath = "";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seal_apply_step1);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {

        mTvTitle.setText("选择印章图片");
//        timeLineView.setStep3Constraint();
    }

    @OnClick({R.id.iv_back, R.id.tv_seal_photo, R.id.tv_seal_album})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_seal_photo:


                PictureSelector.create(SealApplyStep1.this)
                        .openCamera(PictureConfig.TYPE_IMAGE)
                        .forResult(PictureConfig.CHOOSE_REQUEST);

                break;
            case R.id.tv_seal_album:

                goPhotoAlbum();

//                initPictureSelector(PictureMimeType.ofImage());
                break;

        }

    }

    public static final int CHOOSE_ALBUM = 111;

    //激活相册操作
    private void goPhotoAlbum() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_ALBUM);
    }


    /**
     * 打开相册初始化,回传数据在onActivityResult方法中
     *
     * @param chooseMode 打开的类型
     */
    public void initPictureSelector(int chooseMode) {
        PictureSelector.create(this)
                .openGallery(chooseMode)// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()
                .theme(R.style.picture_default_style)// 主题样式设置 具体参考 libray中values/styles
                .maxSelectNum(1)// 最大图片选择数量
                .minSelectNum(1)// 最小选择数量
                .imageSpanCount(4)// 每行显示个数
                .selectionMode(PictureConfig.SINGLE)// 多选 or 单选 PictureConfig.MULTIPLE : PictureConfig.SINGLE
                .previewImage(true)// 是否可预览图片
                .previewVideo(true)// 是否可预览视频
                .enablePreviewAudio(false)// 是否预览音频
//                .compressGrade(Luban.THIRD_GEAR)// luban压缩档次，默认3档 Luban.FIRST_GEAR、Luban.CUSTOM_GEAR
                .isCamera(true)// 是否显示拍照按钮
                .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                .setOutputCameraPath(Constant.IMAGE_CACHE)// 自定义拍照保存路径
                .compress(true)// 是否压缩
                .compressMode(PictureConfig.LUBAN_COMPRESS_MODE)//系统自带 or 鲁班压缩 PictureConfig.SYSTEM_COMPRESS_MODE or LUBAN_COMPRESS_MODE
//                //.sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                .glideOverride(200, 200)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                .isGif(false)// 是否显示gif图片
                .openClickSound(false)// 是否开启点击声音
//                .selectionMedia(selectList)// 是否传入已选图片
//                //.previewEggs(false)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
//                .compressGrade(Luban.CUSTOM_GEAR)
                .compressGrade(Luban.CUSTOM_GEAR)
                .compressMaxKB(1024)//压缩最大值kb compressGrade()为Luban.CUSTOM_GEAR有效
                .minimumCompressSize(500) //add by tanhaiqin, 图片大小 <= 500KB(数字可变) 不需要压缩
//                //.compressWH() // 压缩宽高比 compressGrade()为Luban.CUSTOM_GEAR有效
//                //.videoQuality()// 视频录制质量 0 or 1
                .videoSecond(5 * 60)//显示多少秒以内的视频
//                //.recordVideoSecond()//录制视频秒数 默认60秒
                .forResult(PictureConfig.CHOOSE_REQUEST);
    }

    /**
     * 处理 PictureSelectorActivity.java 返回的数据
     * 注意 图片压缩 已经是在picture lib中处理， 界面仅仅是展示获取的LocalMedia数据，不做再次压缩！
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 图片选择,共用一个数据通道:返回时图片，可能为列表，视频只能有一个
                    List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                    for (int i = 0; i < selectList.size(); i++) {
                        final Uri uri;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            File mAvatarFile = new File(selectList.get(i).getPath());
                            uri = FileProvider.getUriForFile(this, getPackageName() + ".fileProvider", mAvatarFile);
                        } else {
                            uri = Uri.parse("file://" + selectList.get(i).getPath());
                        }
                        startPhoneCrop(uri);

                    }
                    break;

                case CHOOSE_ALBUM:
                     Uri uri;

                    uri=data.getData();
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        File mAvatarFile = new File(data.getData());
//                        uri = FileProvider.getUriForFile(this, getPackageName() + ".fileProvider", mAvatarFile);
//                    } else {
//                        String path = uri.getPath();
//                        uri = Uri.parse("file://" + selectList.get(i).getPath());
//                    }
                    startPhoneCrop(uri);


                    break;


                case START_CROP_IMAGE_REQUEST:
                    Intent intent = new Intent(SealApplyStep1.this, SealApplyStep3.class);
                    intent.putExtra("PicData", newPath);
                    intent.putExtra("single", false);
//                    intent.putExtra("certSn",getIntent().getStringExtra("certSn"));
                    startActivity(intent);
                    break;
            }
        }
    }

    /**
     * 调用系统裁剪的方法
     */
    private void startPhoneCrop(Uri uri) {

        Uri outputUri = Uri.fromFile(new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "sheca_seal_pic" + "_" + getCurrentTime("yyyyMMddHHmmssSSS") + ".png"));
        newPath = outputUri.getPath();
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        //是否可裁剪
        intent.putExtra("corp", "true");
        intent.putExtra("scale", true);
        //裁剪器高宽比
        intent.putExtra("aspectY", 1);
        intent.putExtra("aspectX", 1);
        //设置裁剪框高宽
        //图片控制在1M以下，否则intent会溢出
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        //返回数据
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

//        intent.setDataAndType(uri, "image/*");
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.setDataAndType(uri, "image/*");  //设置数据源,必须是由FileProvider创建的ContentUri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);//设置输出  不需要ContentUri,否则失败
        } else {
            intent.setDataAndType(uri, "image/*");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        }

        startActivityForResult(intent, START_CROP_IMAGE_REQUEST);
    }

}
