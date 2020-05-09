package com.sheca.umandroid.companyCert.album;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sheca.umandroid.R;
import com.sheca.umandroid.companyCert.album.adapter.PictureAlbumDirectoryAdapter;
import com.sheca.umandroid.companyCert.album.adapter.PictureImageGridAdapter;
import com.sheca.umandroid.companyCert.album.config.PictureConfig;
import com.sheca.umandroid.companyCert.album.config.PictureMimeType;
import com.sheca.umandroid.companyCert.album.decoration.GridSpacingItemDecoration;
import com.sheca.umandroid.companyCert.album.dialog.CustomDialog;
import com.sheca.umandroid.companyCert.album.entity.EventEntity;
import com.sheca.umandroid.companyCert.album.entity.LocalMedia;
import com.sheca.umandroid.companyCert.album.entity.LocalMediaFolder;
import com.sheca.umandroid.companyCert.album.model.LocalMediaLoader;
import com.sheca.umandroid.companyCert.album.observable.ImagesObservable;
import com.sheca.umandroid.companyCert.album.permissions.RxPermissions;
import com.sheca.umandroid.companyCert.album.rxbus2.RxBus;
import com.sheca.umandroid.companyCert.album.rxbus2.Subscribe;
import com.sheca.umandroid.companyCert.album.rxbus2.ThreadMode;
import com.sheca.umandroid.companyCert.album.tools.AttrsUtils;
import com.sheca.umandroid.companyCert.album.tools.Constant;
import com.sheca.umandroid.companyCert.album.tools.DebugUtil;
import com.sheca.umandroid.companyCert.album.tools.LightStatusBarUtils;
import com.sheca.umandroid.companyCert.album.tools.PictureFileUtils;
import com.sheca.umandroid.companyCert.album.tools.ScreenUtils;
import com.sheca.umandroid.companyCert.album.tools.StringUtils;
import com.sheca.umandroid.companyCert.album.widget.FolderPopWindow;
import com.sheca.umandroid.companyCert.album.widget.PhotoPopupWindow;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;

//import static com.bumptech.glide.util.Preconditions.checkNotNull;

public class PictureSelectorActivity extends PictureBaseActivity implements View.OnClickListener,
        PictureAlbumDirectoryAdapter.OnItemClickListener,
        PictureImageGridAdapter.OnPhotoSelectChangedListener,
        PhotoPopupWindow.OnItemClickListener {

    private final static String TAG = PictureSelectorActivity.class.getSimpleName();

    private TextView picture_title;
    private TextView tv_empty;
    private TextView picture_id_preview;
    private TextView tv_PlayPause;
    private TextView tv_musicStatus;
    private TextView tv_musicTotal;
    private TextView tv_musicTime;

    private RelativeLayout rl_picture_title;

    //add by tan
    private TextView pictureOk;

    private LinearLayout pictureOriginalContainer;
    private TextView checkBoxOriginalPicture;
    private TextView tvOriginalPicture;
    private boolean isOriginal = false;

    private LinearLayout pictureConfirmContainer;
    private Drawable previewDisableDrawable;
    private Drawable previewEnableDrawable;

    private Drawable topBarConfirmDisableDrawable;
    private Drawable topBarConfirmEnableDrawable;

//    private Drawable originalPictureDisableDrawable;
//    private Drawable originalPictureEnableDrawable;
    //end

    private PictureImageGridAdapter adapter;
    private List<LocalMedia> images = new ArrayList<>();
    private List<LocalMediaFolder> foldersList = new ArrayList<>();
    private FolderPopWindow folderWindow;
    private Animation animation = null;
    private boolean anim = false;
    private int preview_textColor, complete_textColor;
    private RxPermissions rxPermissions;
    private PhotoPopupWindow popupWindow;
    private LocalMediaLoader mediaLoader;
    private MediaPlayer mediaPlayer;
    private SeekBar musicSeekBar;
    private boolean isPlayAudio = false;
    private CustomDialog audioDialog;
    private int audioH;

    //EventBus 3.0 回调
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventBus(EventEntity obj) {
        switch (obj.what) {
            case PictureConfig.UPDATE_FLAG:
                // 预览时勾选图片更新回调
                List<LocalMedia> selectImages = obj.medias;
                anim = selectImages.size() > 0;
                int position = obj.position;
                DebugUtil.i(TAG, "刷新下标::" + position);
                adapter.bindSelectImages(selectImages);
                adapter.notifyItemChanged(position);
                break;
            case PictureConfig.PREVIEW_DATA_FLAG:
                List<LocalMedia> medias = obj.medias;
                if (medias.size() > 0) {
                    // 取出第1个判断是否是图片，视频和图片只能二选一，不必考虑图片和视频混合
                    String pictureType = medias.get(0).getPictureType();
                    if (isCompress && pictureType.startsWith(PictureConfig.IMAGE)) {
                        compressImage(medias);
                    } else {
                        onResult(medias);
                    }
                }
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RxBus.getDefault().isRegistered(this)) {
            RxBus.getDefault().register(this);
        }
        rxPermissions = new RxPermissions(this);
        LightStatusBarUtils.setLightStatusBar(this, statusFont);
        if (camera) {
            if (savedInstanceState == null) {
                rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                if (aBoolean) {
                                    onTakePhoto();
                                } else {
                                    showToast(getString(R.string.picture_camera));
                                    closeActivity();
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                DebugUtil.e(TAG, throwable.getMessage());
                                showToast(throwable.getMessage());
                                closeActivity();
                            }
                        });
            }
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                    , WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.picture_empty);

        } else {
            setContentView(R.layout.picture_selector);
            initView(savedInstanceState);
        }
    }


    private void initTopBar() {
        rl_picture_title = (RelativeLayout) findViewById(R.id.rl_picture_title);
        ImageView picture_left_back = (ImageView) findViewById(R.id.picture_left_back);
        picture_title = (TextView) findViewById(R.id.picture_title);

        pictureConfirmContainer = (LinearLayout) findViewById(R.id.ll_picture_container);
        pictureOk = (TextView) findViewById(R.id.txt_picture_ok);

        String title = mimeType == PictureMimeType.ofVideo() ?
                getString(R.string.picture_all_video)
                : getString(R.string.picture_camera_roll);

        picture_title.setText(title);

        String titleText = picture_title.getText().toString().trim();
        if (isCamera) {
            isCamera = StringUtils.isCamera(titleText);
        }

        picture_left_back.setOnClickListener(this);

        picture_title.setOnClickListener(this);

        pictureConfirmContainer.setOnClickListener(this);
    }

    private void initBottomBar() {

        RelativeLayout rl_bottom = (RelativeLayout) findViewById(R.id.rl_bottom);
        picture_id_preview = (TextView) findViewById(R.id.picture_id_preview);
        checkBoxOriginalPicture = (TextView) findViewById(R.id.img_checkout_circle);
        tvOriginalPicture = (TextView) findViewById(R.id.txt_picture_original);

        pictureOriginalContainer = (LinearLayout) findViewById(R.id.picture_original_container);

        if (mimeType == PictureConfig.TYPE_IMAGE) {
            rl_bottom.setVisibility(View.VISIBLE);
            picture_id_preview.setOnClickListener(this);

            //原图选择是否
            pictureOriginalContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkBoxOriginalPicture.setSelected(!checkBoxOriginalPicture.isSelected());
                    isOriginal = checkBoxOriginalPicture.isSelected();
                  }
            });

        }
        //rl_bottom.setVisibility(selectionMode == PictureConfig.SINGLE ? View.GONE : View.VISIBLE);

    }


    private void initRecyclerView(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // 防止拍照内存不足时activity被回收，导致拍照后的图片未选中
            selectionMedias = PictureSelector.obtainSelectorList(savedInstanceState);
            preview_textColor = savedInstanceState.getInt("preview_textColor");
            complete_textColor = savedInstanceState.getInt("complete_textColor");
        } else {
            preview_textColor = AttrsUtils.getTypeValueColor(this, R.attr.picture_preview_textColor);
            complete_textColor = AttrsUtils.getTypeValueColor(this, R.attr.picture_complete_textColor);
        }

        RecyclerView picture_recycler = (RecyclerView) findViewById(R.id.picture_recycler);

        tv_empty = (TextView) findViewById(R.id.tv_empty);
        tv_empty.setText(getString(R.string.picture_empty));
        StringUtils.tempTextFont(tv_empty, mimeType);

        picture_recycler.setHasFixedSize(true);
        picture_recycler.addItemDecoration(new GridSpacingItemDecoration(spanCount,
                ScreenUtils.dip2px(this, 2), false));
        picture_recycler.setLayoutManager(new GridLayoutManager(this, spanCount));

        // 解决调用 notifyItemChanged 闪烁问题,取消默认动画
        ((SimpleItemAnimator) picture_recycler.getItemAnimator()).setSupportsChangeAnimations(false);

        adapter = new PictureImageGridAdapter(mContext, config);
        adapter.setOnPhotoSelectChangedListener(PictureSelectorActivity.this);
        adapter.bindSelectImages(selectionMedias);
        picture_recycler.setAdapter(adapter);
    }


    private void initPopWindow() {
        if (mimeType == PictureMimeType.ofAll()) {
            popupWindow = new PhotoPopupWindow(this);
            popupWindow.setOnItemClickListener(this);
        }

        if (mimeType == PictureMimeType.ofAudio()) {
            picture_id_preview.setVisibility(View.GONE);
            audioH = ScreenUtils.getScreenHeight(mContext)
                    + ScreenUtils.getStatusBarHeight(mContext);
        } else {
            picture_id_preview.setVisibility(mimeType == PictureConfig.TYPE_VIDEO
                    ? View.GONE : View.VISIBLE);
        }

        folderWindow = new FolderPopWindow(this, mimeType);
        folderWindow.setPictureTitleView(picture_title);
        folderWindow.setOnItemClickListener(this);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initRxPermissions() {
        mediaLoader = new LocalMediaLoader(this, mimeType, isGif, videoSecond);
        rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                               @Override
                               public void accept(Boolean aBoolean) throws Exception {
                                   showPleaseDialog("");
                                   if (aBoolean) {
                                       readLocalMedia();
                                   } else {
                                       showToast(getString(R.string.picture_jurisdiction));
                                       dismissDialog();
                                   }
                               }
                           },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                DebugUtil.e(TAG, throwable.getMessage());
                                closeActivity();
                            }
                        }
                );
    }


    private void initDrawable() {

//        checkNotNull(getResources(), "getResource cannot null!");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            previewDisableDrawable = getDrawable(R.drawable.ic_preview_disable);
            previewEnableDrawable = getDrawable(R.drawable.ic_preview_enable);
            topBarConfirmDisableDrawable = getDrawable(R.drawable.ic_confirm_disable);
            topBarConfirmEnableDrawable = getDrawable(R.drawable.ic_confirm_enable);

        } else {
            previewDisableDrawable = getResources().getDrawable(R.drawable.ic_preview_disable);
            previewEnableDrawable = getResources().getDrawable(R.drawable.ic_preview_enable);

            topBarConfirmDisableDrawable = getResources().getDrawable(R.drawable.ic_confirm_disable);
            topBarConfirmEnableDrawable = getResources().getDrawable(R.drawable.ic_confirm_enable);
        }

    }

    /**
     * init views
     */
    private void initView(Bundle savedInstanceState) {
        initDrawable();

        initTopBar();

        initBottomBar();

        initRecyclerView(savedInstanceState);

        isNumComplete(numComplete);

        initPopWindow();

        initRxPermissions();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (adapter != null) {
            outState.putInt("preview_textColor", preview_textColor);
            outState.putInt("complete_textColor", complete_textColor);
            List<LocalMedia> selectedImages = adapter.getSelectedImages();
            PictureSelector.saveSelectorList(outState, selectedImages);
        }
    }

    /**
     * none number style
     */
    private void isNumComplete(boolean numComplete) {
        pictureOk.setText(getString(R.string.action_ok));
    }

    /**
     * get LocalMedia s
     */
    protected void readLocalMedia() {
        mediaLoader.loadAllMedia(new LocalMediaLoader.LocalMediaLoadListener() {
            @Override
            public void loadComplete(List<LocalMediaFolder> folders) {
                DebugUtil.i("loadComplete:" + folders.size());
                if (folders.size() > 0) {
                    foldersList = folders;
                    LocalMediaFolder folder = folders.get(0);
                    folder.setChecked(true);
                    List<LocalMedia> localImg = folder.getImages();
                    // 这里解决有些机型会出现拍照完，相册列表不及时刷新问题
                    // 因为onActivityResult里手动添加拍照后的照片，
                    // 如果查询出来的图片大于或等于当前adapter集合的图片则取更新后的，否则就取本地的
                    if (localImg.size() >= images.size()) {
                        images = localImg;
                        folderWindow.bindFolder(folders);
                    }
                }
                if (adapter != null) {
                    if (images == null) {
                        images = new ArrayList<>();
                    }
                    adapter.bindImagesData(images);
                    tv_empty.setVisibility(images.size() > 0
                            ? View.INVISIBLE : View.VISIBLE);
                }
                dismissDialog();
            }
        });
    }

    /**
     * open camera
     */
//    public void startCamera() {
//        // 防止快速点击，但是单独拍照不管
//        if (!DoubleUtils.isFastDoubleClick() || camera) {
//            switch (mimeType) {
//                case PictureConfig.TYPE_ALL:
//                    // 如果是全部类型下，单独拍照就默认图片 (因为单独拍照不会new此PopupWindow对象)
//                    if (popupWindow != null) {
//                        if (popupWindow.isShowing()) {
//                            popupWindow.dismiss();
//                        }
//                        popupWindow.showAsDropDown(rl_picture_title);
//                    } else {
//                        startOpenCamera();
//                    }
//                    break;
//                case PictureConfig.TYPE_IMAGE:
//                    // 拍照
//                    startOpenCamera();
//                    break;
//            }
//        }
//    }

    /**
     * 开启CameraActivity.java 自定义Camera
     */
    private void startCustomCamera() {
        DebugUtil.i(TAG, "TANHQ===> startCustomCamera!!!!");

        // 防止快速点击
//        if (!DoubleUtils.isFastDoubleClick()) {
        switch (mimeType) {
            case PictureConfig.TYPE_IMAGE:
                // 拍照
                startOpenCustomCamera();
                break;

//            }
        }
    }

    /**
     * start to custom camera、preview、crop
     */
    public void startOpenCustomCamera() {
        //启动CameraActivity.java
        Intent cameraIntent = new Intent(PictureSelectorActivity.this, CameraActivity.class);

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
//            File cameraImageFile = PictureFileUtils.createCameraFileNew(this,
//                    mimeType == PictureConfig.TYPE_ALL ? PictureConfig.TYPE_IMAGE : mimeType,
//                    outputCameraPath);
//            cameraPath = cameraImageFile.getAbsolutePath();
//            Uri imageUri = parUri(cameraImageFile);
//            DebugUtil.i(TAG, "TANHQ===>  imageUri = " + imageUri + ", cameraPath = " + cameraPath);
//            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraIntent.putExtra(Constant.TYPE_CAMERA, PictureConfig.TYPE_IMAGE);
            startActivityForResult(cameraIntent, PictureConfig.REQUEST_CAMERA);
//            finish();
        }
    }

    /**
     * start to camera、preview、crop
     * not used yet
     */
    public void startOpenCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File cameraFile = PictureFileUtils.createCameraFile(this,
                    mimeType == PictureConfig.TYPE_ALL ? PictureConfig.TYPE_IMAGE : mimeType,
                    outputCameraPath);
            cameraPath = cameraFile.getAbsolutePath();
            Uri imageUri = parUri(cameraFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(cameraIntent, PictureConfig.REQUEST_CAMERA);
        }
    }


    /**
     * 生成uri
     *
     * @param cameraFile
     * @return
     */
    private Uri parUri(File cameraFile) {
        Uri imageUri;
        String authority = getPackageName() + ".provider";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //通过FileProvider创建一个content类型的Uri
            imageUri = FileProvider.getUriForFile(mContext, authority, cameraFile);
        } else {
            imageUri = Uri.fromFile(cameraFile);
        }
        return imageUri;
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.picture_left_back) {
            if (folderWindow.isShowing()) {
                folderWindow.dismiss();
            } else {
                closeActivity();
            }
        }
        if (id == R.id.picture_title) {
            if (folderWindow.isShowing()) {
                folderWindow.dismiss();
            } else {
                if (images != null && images.size() > 0) {
                    folderWindow.showAsDropDown(rl_picture_title);
                    List<LocalMedia> selectedImages = adapter.getSelectedImages();
                    folderWindow.notifyDataCheckedStatus(selectedImages);
                }
            }
        }

        if (id == R.id.picture_id_preview) {
            List<LocalMedia> selectedImages = adapter.getSelectedImages();

            List<LocalMedia> medias = new ArrayList<>();
            for (LocalMedia media : selectedImages) {
                medias.add(media);
            }
            Bundle bundle = new Bundle();
            bundle.putSerializable(PictureConfig.EXTRA_PREVIEW_SELECT_LIST, (Serializable) medias);
            bundle.putSerializable(PictureConfig.EXTRA_SELECT_LIST, (Serializable) selectedImages);
            bundle.putBoolean(PictureConfig.EXTRA_BOTTOM_PREVIEW, true);
            bundle.putInt(PictureConfig.EXTRA_MEDIA, PictureConfig.TYPE_IMAGE);
            startActivity(PicturePreviewActivity.class, bundle);
            overridePendingTransition(R.anim.a5, 0);
        }

        if (id == R.id.ll_picture_container) {
            List<LocalMedia> images = adapter.getSelectedImages();
            String pictureType = images.size() > 0 ? images.get(0).getPictureType() : "";
            // 如果设置了图片最小选择数量，则判断是否满足条件
            int size = images.size();
            boolean eqImg = pictureType.startsWith(PictureConfig.IMAGE);
            if (minSelectNum > 0 && selectionMode == PictureConfig.MULTIPLE) {
                if (size < minSelectNum) {
                    String str = getString(R.string.picture_min_img_num, minSelectNum);
                    showToast(str);
                    return;
                }
            }

            if (isCompress && eqImg && !isOriginal) {
                // 默认是 图片压缩，视频不管
                compressImage(images);
            } else if (isCompress && eqImg && isOriginal) {
                // 原图选中就不压缩
                originalImage(images);
            } else {
                onResult(images);
            }
        }
    }


    @Override
    public void onItemClick(String folderName, List<LocalMedia> images) {
        boolean camera = StringUtils.isCamera(folderName);
        camera = isCamera && camera;
        adapter.setShowCamera(camera);
        picture_title.setText(folderName);
        adapter.bindImagesData(images);
        folderWindow.dismiss();
    }

    @Override
    public void onTakePhoto() {

        //启动相机拍照,先判断手机是否有拍照、录音、写入权限
        rxPermissions.request(Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            startCustomCamera();
                        } else {
                            showToast(getString(R.string.picture_all_permission));
                            if (camera) {
                                closeActivity();
                            }
                        }
                    }
                });
    }

    @Override
    public void onChange(List<LocalMedia> selectImages) {
        changeImageNumber(selectImages);
    }

    @Override
    public void onPictureClick(LocalMedia media, int position) {
        List<LocalMedia> images = adapter.getImages();
        startPreview(images, position);
    }

    /**
     * preview image and video
     *
     * @param previewImages
     * @param position
     */
    public void startPreview(List<LocalMedia> previewImages, int position) {
        LocalMedia media = previewImages.get(position);
        String pictureType = media.getPictureType();
        Bundle bundle = new Bundle();
        List<LocalMedia> result = new ArrayList<>();
        int mediaType = PictureMimeType.isPictureType(pictureType);
        DebugUtil.i(TAG, "mediaType:" + mediaType);
        switch (mediaType) {
            case PictureConfig.TYPE_IMAGE:
                // image
                if (selectionMode == PictureConfig.SINGLE) {
                    result.add(media);
                    handlerResult(result);
                } else {
                    List<LocalMedia> selectedImages = adapter.getSelectedImages();
                    ImagesObservable.getInstance().saveLocalMedia(previewImages);
                    bundle.putSerializable(PictureConfig.EXTRA_SELECT_LIST, (Serializable) selectedImages);
                    bundle.putInt(PictureConfig.EXTRA_POSITION, position);
                    bundle.putInt(PictureConfig.EXTRA_MEDIA, PictureConfig.TYPE_IMAGE);
                    startActivity(PicturePreviewActivity.class, bundle);
                    overridePendingTransition(R.anim.a5, 0);
                }
                break;

        }
    }


    /**
     * change image selector state
     *
     * @param selectImages
     */
    public void changeImageNumber(List<LocalMedia> selectImages) {
        // 如果选择的视频没有预览功能
        String pictureType = selectImages.size() > 0 ? selectImages.get(0).getPictureType() : "";

        if (mimeType == PictureMimeType.ofAudio()) {
            picture_id_preview.setVisibility(View.GONE);
        } else {
            boolean isVideo = PictureMimeType.isVideo(pictureType);
            picture_id_preview.setVisibility(isVideo ? View.GONE : View.VISIBLE);
        }

        boolean enable = selectImages.size() != 0;
        if (enable) {
            picture_id_preview.setEnabled(true);
            picture_id_preview.setTextColor(preview_textColor);

            pictureConfirmContainer.setEnabled(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                picture_id_preview.setBackground(previewEnableDrawable);
                pictureConfirmContainer.setBackground(topBarConfirmEnableDrawable);
            } else {
                picture_id_preview.setBackgroundDrawable(previewEnableDrawable);
                pictureConfirmContainer.setBackgroundDrawable(topBarConfirmEnableDrawable);
            }

            pictureOk.setTextColor(complete_textColor);

            if (numComplete) {
                pictureOk.setText(getString(R.string.action_ok_format, selectImages.size()));
            } else {
                pictureOk.setText(getString(R.string.action_ok));
            }
        } else {
            picture_id_preview.setEnabled(false);
            pictureConfirmContainer.setEnabled(false);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                picture_id_preview.setBackground(previewDisableDrawable);
                pictureConfirmContainer.setBackground(topBarConfirmDisableDrawable);
            } else {
                picture_id_preview.setBackgroundDrawable(previewDisableDrawable);
                pictureConfirmContainer.setBackgroundDrawable(topBarConfirmDisableDrawable);
            }

            picture_id_preview.setTextColor
                    (ContextCompat.getColor(mContext, R.color.color_text_original));

            pictureOk.setText(getString(R.string.action_ok));
            pictureOk.setTextColor(ContextCompat.getColor(mContext, R.color.color_selector_top));
        }
    }

    /**
     * 加入CameraActivity.java拍照后的流程
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        DebugUtil.i("TANHQ===> requestCode = " + requestCode + ", resultCode = " + resultCode);
        if (resultCode == RESULT_OK && requestCode == PictureConfig.REQUEST_CAMERA) {
            List<LocalMedia> mediaList = PictureSelector.obtainMultipleResult(data);
            LocalMedia media = mediaList.get(0);

            File file = new File(media.getPath());
            String toType = PictureMimeType.fileToType(file);

            if (media.getMimeType() == PictureConfig.TYPE_IMAGE) { //图片返回

                boolean eqImg = toType.startsWith(PictureConfig.IMAGE);
                //DebugUtil.i("TANHQ===> isCompress = " + isCompress + ", eqImg = " + eqImg);
                if (isCompress && eqImg) {
                    //压缩
                    compressImage(mediaList);
                } else {
                    // 不裁剪 不压缩 直接返回结果
                    onResult(mediaList);
                }

            } else {//video 直接返回
                onResult(mediaList);
            }
        } else {
            closeActivity();
        }
    }

    /**
     * 手动添加拍照后的相片到图片列表，并设为选中
     *
     * @param media
     */
    private void manualSaveFolder(LocalMedia media) {
        try {
            createNewFolder(foldersList);
            LocalMediaFolder folder = getImageFolder(media.getPath(), foldersList);
            LocalMediaFolder cameraFolder = foldersList.size() > 0 ? foldersList.get(0) : null;
            if (cameraFolder != null && folder != null) {
                // 相机胶卷
                cameraFolder.setFirstImagePath(media.getPath());
                cameraFolder.setImages(images);
                cameraFolder.setImageNum(cameraFolder.getImageNum() + 1);
                // 拍照相册
                int num = folder.getImageNum() + 1;
                folder.setImageNum(num);
                folder.getImages().add(0, media);
                folder.setFirstImagePath(cameraPath);
                folderWindow.bindFolder(foldersList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
           if (RxBus.getDefault().isRegistered(this)) {
            RxBus.getDefault().unregister(this);
        }
        ImagesObservable.getInstance().clearLocalMedia();
        if (animation != null) {
            animation.cancel();
            animation = null;
        }

    }

    @Override
    public void onItemClick(int position) {
        switch (position) {
            case 0:
                // 拍照
                startOpenCamera();
                break;

        }
    }
}
