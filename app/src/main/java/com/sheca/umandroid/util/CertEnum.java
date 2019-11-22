package com.sheca.umandroid.util;
/**
 * @author xuchangqing
 * @time 2019/4/19 13:27
 * @descript 证书操作类名
 */
public enum  CertEnum {
    GetCertList,            //获取证书枚举id
    GetSealList, //获取印章枚举id
    GetCertByID, //根据证书ID获取证书
    GetCertItem, //获取证书指定基本项内容。
    GetCertExt,  //获取证书扩展项
    VerifyCert,  //验证证书
    VerifySign,  //验证签名
    Encrypt,     //数据加密
    Decrypt      //数据解密
}
