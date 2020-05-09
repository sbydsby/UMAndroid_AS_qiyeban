package com.sheca.umandroid.companyCert.album.observable;



import com.sheca.umandroid.companyCert.album.entity.LocalMedia;
import com.sheca.umandroid.companyCert.album.entity.LocalMediaFolder;

import java.util.List;

/**
 * author：luck
 * project：PictureSelector
 * package：com.luck.picture.lib.observable
 * email：893855882@qq.com
 * data：17/1/16
 */
public interface ObserverListener {
    void observerUpFoldersData(List<LocalMediaFolder> folders);

    void observerUpSelectsData(List<LocalMedia> selectMedias);
}
