package com.sheca.umandroid.model;

/**
 * @author xuchangqing
 * @time 2019/4/22 19:51
 * @descript
 */
public class PersonInfo {
    public String accountUID;	//	是	账户唯一标识。
    public String name;	//	是	姓名。
    public String mobile;	//	是	手机。
    public int type;	//	是	账户类别（1：个人；2：单位）。
    public int status;	//	是	账户实名认证状态（0：未实名认证；1：已实名认证；）。
    public int active; //账户是否激活状态（0：未激活；1：已激活；）
    public String identityCode; //身份证号码。


}
