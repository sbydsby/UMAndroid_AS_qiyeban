package com.sheca.umee.model;

import net.sf.json.JSONObject;

public class APPResponse extends BaseResponse{

    JSONObject jb;

    public APPResponse(String json){
        jb = JSONObject.fromObject(json);
    }

    public int getReturnCode(){
        return retCode = jb.getInt(com.sheca.umplus.util.CommonConst.RETURN_CODE);
    }

    public String getReturnMsg(){
        return retMsg = jb.getString(com.sheca.umplus.util.CommonConst.RETURN_MSG);
    }

    public JSONObject getResult(){
        return jb.getJSONObject(com.sheca.umplus.util.CommonConst.RETURN_RESULT);
    }

    public String getResultStr(){
        return (String) jb.opt(com.sheca.umplus.util.CommonConst.RETURN_RESULT);
    }
}
