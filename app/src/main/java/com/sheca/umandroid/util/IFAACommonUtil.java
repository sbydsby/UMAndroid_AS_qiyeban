package com.sheca.umandroid.util;


import com.esandinfo.core.data.GsonUtil;
import com.esandinfo.core.utils.MyLog;
import com.esandinfo.core.utils.StringUtil;
import com.sheca.umandroid.model.IFAAGatewayResponse;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

/**
 * @author wmding
 * @date 2019/3/27
 * @describe
 */
public class IFAACommonUtil {


    /**
     * 构建初始化带签名的报文，接入方可直接使用该方法，无需修改
     *
     * @param appId      接入方 APPID
     * @param transId    业务流水号
     * @param payload    业务拓展字段，可为空
     * @param privateKey 接入方私钥
     * @return
     */
    public static String getInitInfo(String appId, String transId, String payload, String privateKey) {

        if (StringUtil.isBlank(appId)) {
            MyLog.error("appId 为空");
            return null;
        }
        if (StringUtil.isBlank(privateKey)) {
            MyLog.error("privateKey 为空");
            return null;
        }

        if (StringUtil.isBlank(transId)) {
            MyLog.error("transId 为空");
            return null;
        }

        StringBuilder builder = new StringBuilder();
        StringBuilder signDataBuilder = new StringBuilder();
        builder.append(appId);
        signDataBuilder.append(appId);
        signDataBuilder.append("&");
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        df.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String timestamp = df.format(new Date(System.currentTimeMillis()));
        builder.append(timestamp);
        signDataBuilder.append(timestamp);
        signDataBuilder.append("&");
        signDataBuilder.append(transId);

        Map<String, String> initInfo = new HashMap();
        initInfo.put("appId", appId);
        initInfo.put("timestamp", timestamp);
        initInfo.put("transId", transId);
        if (StringUtil.isBlank(payload)) {
            initInfo.put("transPayload", "");
        } else {
            initInfo.put("transPayload", payload);
            signDataBuilder.append("&");
            signDataBuilder.append(payload);
        }

        String sign;
        try {
            sign = IFAARSAUtil.sign(signDataBuilder.toString().getBytes(), privateKey);
            initInfo.put("sign", sign);
        } catch (Exception e) {
            MyLog.error("IFAARSAUtil.sign error: " + e.getMessage());
            e.printStackTrace();
        }

        return GsonUtil.getAllJson().toJson(initInfo);
    }

    /**
     * 在认证和更新指位后对结果进行可信验证，接入发可直接使用，无需修改
     *
     * @param gatewayResp SDK返回的msg字段
     * @param esandPubKey 一砂服务公钥
     * @return 验证结果
     */
    public static boolean getBusinessResult(String gatewayResp, String esandPubKey) {
        IFAAGatewayResponse gateway = GsonUtil.getAllJson().fromJson(gatewayResp, IFAAGatewayResponse.class);
        String timestamp = gateway.getTimestamp();
        String bizContent = gateway.getBizContent();
        String signature = gateway.getSign();
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        df.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        try {
            Date signTime = df.parse(timestamp);
            Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
            Date currentTime = cal.getTime();
            long diff = currentTime.getTime() - signTime.getTime();
            if (diff > 900000L) {
                MyLog.error("时间戳超期");
                return false;
            }
        } catch (ParseException var15) {
            MyLog.error("时间戳转换时发生错误：" + var15.getMessage());
            return false;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(timestamp);
        if (!StringUtil.isBlank(bizContent)) {
            builder.append("&");
            builder.append(bizContent);
        }

        boolean verify = false;
        try {
            verify = IFAARSAUtil.verify(builder.toString().getBytes(), esandPubKey, signature);
        } catch (Exception e) {
            MyLog.error("IFAARSAUtil.verify error: " + e.getMessage());
            e.printStackTrace();
        }

        return verify;
    }

    /**
     * 根据APPID生成 transId（业务流水号，方便之后查日志使用）
     *
     * @param appId
     * @return
     */
    public static String getTransId(String appId) {
        StringBuilder builder = new StringBuilder();
        builder.append(appId);

        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        df.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String timestamp = df.format(new Date(System.currentTimeMillis()));
        builder.append(timestamp);
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        builder.append(uuid, 10, 16);
        String transId = builder.toString();
        return transId;
    }
}
