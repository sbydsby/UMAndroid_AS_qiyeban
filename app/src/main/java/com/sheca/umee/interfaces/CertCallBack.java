package com.sheca.umee.interfaces;


import com.sheca.umplus.model.Cert;
import com.sheca.umplus.model.SealInfo;

import java.util.List;

/**
 * @author xuchangqing
 * @time 2019/4/19 10:54
 * @descript
 */
public interface CertCallBack {
//    void certCallBack(String strVal);
    void certCallBackforList(List<Cert> mList);
    void sealCallBackfoirList(List<SealInfo> mList);
    void certCallBackForCert(Cert mCert);

}
