package com.sheca.umandroid.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.sheca.javasafeengine;
import com.sheca.umandroid.CertDeleteActivity;
import com.sheca.umandroid.R;
import com.sheca.umandroid.model.Cert;


import org.spongycastle.util.encoders.Base64;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommUtil {

    public static String getPWDHash(String strPWD) {
        String strPWDHash = "";

        javasafeengine oSE = new javasafeengine();
        byte[] bText = strPWD.getBytes();
        byte[] bDigest = oSE.digest(bText, "SHA-256", "SUN");   //做摘要
        strPWDHash = new String(Base64.encode(bDigest));

	/*	try {
			strPWDHash = URLEncoder.encode(strPWDHash,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        return strPWDHash;
    }

    //生成随机数字和字母,
    public static String getStringRandom(int length) {
        String val = "";
        Random random = new Random();
        //length为几位密码
        for(int i = 0; i < length; i++) {
            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            //输出字母还是数字
            if( "char".equalsIgnoreCase(charOrNum) ) {
                //输出是大写字母还是小写字母
                int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val += (char)(random.nextInt(26) + temp);
            } else if( "num".equalsIgnoreCase(charOrNum) ) {
                val += String.valueOf(random.nextInt(10));
            }
        }

        return val;
    }

    public static void setTitleColor(Activity activity,int titleColor,int titleTextColor){
        if (null == activity){
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window win = activity.getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            if (true) {
                winParams.flags |= bits;
            } else {
                winParams.flags &= ~bits;
            }
            win.setAttributes(winParams);

            SystemBarTintManager tintManager = new SystemBarTintManager(activity);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(titleColor);//通知栏所需颜色
        }

        View titlebar = activity.findViewById(R.id.titlebar);
        titlebar.setBackgroundColor(activity.getBaseContext().getResources().getColor(titleColor));

        TextView tv_title = (TextView) activity.findViewById(R.id.tv_title);
        tv_title.setTextColor(activity.getBaseContext().getResources().getColor(titleTextColor));
    }

    public static String formatString(android.content.Context context,int resId, String value){
        return String.format(context.getResources().getString(resId), value);
    }

    /**
     * 获取版本号名称
     *
     * @param context 上下文
     * @return
     */
    public static String getVerName(android.content.Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }

    public static int getVersionCode(Context context) {
        PackageManager manager = context.getPackageManager();
        int code = 0;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            code = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return code;
    }


    /*
       计算给定日期的长度，精确到天
       @date "yyyy-MM-dd"
     */
    public static long getLeftDay(String beginDate,String expireDate){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式

        Date bDate = null;
        try {
            bDate = df.parse(beginDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date exDate = null;
        try {
            exDate = df.parse(expireDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long l=exDate.getTime()-bDate.getTime();
        long day=l/(24*60*60*1000);

        return day;
    }

    /*
   计算给定日期距离今天的长度，精确到天
   @date "yyyy-MM-dd"
 */
    public static long getLeftDay(String YYYY_MM_DD){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
        Date date = null;
        try {
            date = df.parse(YYYY_MM_DD);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date now =new Date();
        long l=date.getTime()-now.getTime();
        long day=l/(24*60*60*1000);

        return day;
    }

    //判断手机是否root
    public static boolean isRoot() {
        String binPath = "/system/bin/su";
        String xBinPath = "/system/xbin/su";

        try {
            if (new File(binPath).exists() && isCanExecute(binPath)) {
                return true;
            }

            if (new File(xBinPath).exists() && isCanExecute(xBinPath)) {
                return true;
            }
        }catch (Exception ex){
            return false;
        }

        return false;
    }

    private static boolean isCanExecute(String filePath) {
        java.lang.Process process = null;

        try {
            process = Runtime.getRuntime().exec("ls -l " + filePath);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String str = in.readLine();

            if (str != null && str.length() >= 4) {
                char flag = str.charAt(3);
                if (flag == 's' || flag == 'x')
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

        return false;
    }

    public  static  void  exitByIsRoot(final Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("环境检测失败,检测到系统已root,移证通无法继续运行");
        builder.setIcon(R.drawable.alert);
        builder.setTitle("检测失败");
        builder.setCancelable(false);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    activity.finish();
                    System.exit(0);
                } catch (Exception e) {
                    System.exit(0);
                }

            }
        });

        builder.show();
    }

    public  static  void  exitByIsSupportAndroidVersion(final Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("环境检测失败,检测到android系统版本过低,移证通无法继续运行");
        builder.setIcon(R.drawable.alert);
        builder.setTitle("检测失败");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    activity.finish();
                    System.exit(0);
                } catch (Exception e) {
                    System.exit(0);
                }

            }
        });

        builder.show();
    }

    public  static  void  showByCheckAndroidVersion(final Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("环境检测通过,手机未root且android系统版本不低于4.4.4");
        builder.setIcon(R.drawable.alert);
        builder.setTitle("检测通过");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    //activity.finish();
                    //System.exit(0);
                } catch (Exception e) {
                   // System.exit(0);
                }

            }
        });

        builder.show();
    }


    public static boolean isSupportAndroidVersion(){
        //获取当前系统的版本号：
        String str =  "Product Model: " + android.os.Build.MODEL + "," + android.os.Build.VERSION.SDK + "," + android.os.Build.VERSION.RELEASE;

        /* 获取当前系统的android版本号 */
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        return (currentapiVersion>=21)?true:false;
    }



    /**
     * 密码8-16位。
     */
    public static boolean isPasswordValid(String password) {
        boolean isValid = false;
        if (password.length() > 7 && password.length() < 17) {
            isValid = true;
        }
        return isValid;
    }
//    public static boolean isPasswordValid(String strPwd){
//        boolean isValid = false;
//        //final String Match_Patton =  "/^[\\w_-]{8,16}$\\/"; //^[a-zA-Z0-9]{6,21}$
//        final String Match_Patton =  "/^(?=.*\\d)(?=.*[a-zA-Z])(?=.*[\\W])[\\da-zA-Z\\W]{8,16}$/"; //
//
//        if (strPwd.length() <= 0) {
//            return false;
//        } else {
//            //Pattern p = Pattern.compile(Match_Patton);
//           // Matcher m = p.matcher(strPwd);
//            //isValid = m.matches();
//            int level = com.wx.pwd.CheckStrength.checkPasswordStrength(strPwd);
//            if(level >= 3)
//                return true;
//        }
//
//        return isValid;
//    }


    public static  void  showErrPasswordMsg(Activity activity,int certid){
        SharedPreferences sharedPrefs = activity.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
        int pwdErrCount = sharedPrefs.getInt(CommonConst.SETTINGS_PWD_ERR_COUNT+certid+"", 0);
        if(pwdErrCount == CommonConst.MAX_ERR_PASSWORD_COUNT){
            Toast.makeText(activity,"该张证书密码输错已达6次，已锁定",Toast.LENGTH_SHORT).show();
            return;
        }

        pwdErrCount += 1;
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(CommonConst.SETTINGS_PWD_ERR_COUNT+certid+"", pwdErrCount);
        editor.commit();

        final int leftPwdErrCount = CommonConst.MAX_ERR_PASSWORD_COUNT - pwdErrCount;

        if(pwdErrCount < CommonConst.MAX_ERR_PASSWORD_COUNT)
            Toast.makeText(activity,"该张证书密码输错已"+pwdErrCount+"次，还剩余"+leftPwdErrCount+"次",Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(activity,"该张证书密码输错已达6次，已锁定",Toast.LENGTH_SHORT).show();
    }

    public static  boolean  isPasswordLocked(Activity activity,int certid){
        SharedPreferences sharedPrefs = activity.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
        int pwdErrCount = sharedPrefs.getInt(CommonConst.SETTINGS_PWD_ERR_COUNT+certid+"", 0);
        if(pwdErrCount == CommonConst.MAX_ERR_PASSWORD_COUNT){
            Toast.makeText(activity,"该张证书密码输错已达6次，已锁定",Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    public static  void  resetPasswordLocked(Activity activity,int certid){
        SharedPreferences sharedPrefs = activity.getSharedPreferences(CommonConst.PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(CommonConst.SETTINGS_PWD_ERR_COUNT+certid+"", 0);
        editor.commit();
    }

    //base64转图片
    public static Bitmap stringtoBitmap(String string) {
        //将字符串转换成Bitmap类型
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = android.util.Base64.decode(string, android.util.Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    //获取当前年月日时秒分
    public static String getNowTime() {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date=new Date();
        String time =  formatter.format(date);
        return time;

    }



    /**
     * 文件转base64字符串
     *
     * @param file
     * @return
     */
    public static String fileToBase64(File file) {
        String base64 = null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            byte[] bytes = new byte[in.available()];
            int length = in.read(bytes);
            base64 = android.util.Base64.encodeToString(bytes, 0, length, android.util.Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return base64;

    }

    /*
   解析证书内容展示
  */
    public static String getCertDetail(Cert cert, int itemNo) {
        javasafeengine jse = new javasafeengine();
        String result = "";
        byte[] bCert = Base64.decode(cert.getCertificate());
//        String commonName = jse.getCertDetail(17, bCert);//证书主题名
//        String organization = jse.getCertDetail(14, bCert);//组织机构名
//        String strNotBeforeTime = jse.getCertDetail(11, bCert);//证书开始时间
//        String strValidTime = jse.getCertDetail(12, bCert);//证书截止时间
//        Date fromDate = sdf.parse(strNotBeforeTime);
//        Date toDate = sdf.parse(strValidTime);

        try {
            result = jse.getCertDetail(itemNo, bCert);//证书主题名
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public static boolean isPersonNO(String temp) {//检测身份证号
        Pattern p = Pattern.compile("^(\\d{14}|\\d{17})(X|x|\\d)$");
        Matcher m = p.matcher(temp);
        return m.matches();
    }



    public static String getOrgStr(int orgType, boolean isRSA) {
        switch (orgType) {
            case 1:
                if (isRSA) {
                    return CommonConst.CERT_TYPE_RSA_QY;
                } else {
                    return CommonConst.CERT_TYPE_SM2_QY;
                }
            case 2:
                if (isRSA) {
                    return CommonConst.CERT_TYPE_RSA_SY;
                } else {
                    return CommonConst.CERT_TYPE_SM2_SY;
                }
            case 3:
                if (isRSA) {
                    return CommonConst.CERT_TYPE_RSA_ST;
                } else {
                    return CommonConst.CERT_TYPE_SM2_ST;
                }
            case 4:
                if (isRSA) {
                    return CommonConst.CERT_TYPE_RSA_JG;
                } else {
                    return CommonConst.CERT_TYPE_SM2_JG;
                }
            default:
                if (isRSA) {
                    return CommonConst.CERT_TYPE_RSA_QY;
                } else {
                    return CommonConst.CERT_TYPE_SM2_QY;
                }
        }
    }


    /**
     * 账号必须为手机号和电子邮箱。
     */
    public static boolean isAccountValid(String account) {
        boolean isValid = false;
        //手机号码
        //移动：134[0-8],135,136,137,138,139,150,151,157,158,159,182,187,188
        //联通：130,131,132,152,155,156,185,186
        //电信：133,1349,153,180,189
        //String MOBILE = "^1(3[0-9]|5[0-35-9]|8[025-9])\\d{8}$";
        String MOBILE = "^((1[3,5,8,9][0-9])|(14[5,7])|(17[0,6,7,8]))\\d{8}$";
        Pattern mobilepattern = Pattern.compile(MOBILE);
        Matcher mobileMatcher = mobilepattern.matcher(account);
        //邮箱
        //p{Alpha}：内容是必选的，和字母字符[\p{Lower}\p{Upper}]等价。
        //w{2,15}：2~15个[a-zA-Z_0-9]字符；w{}内容是必选的。
        //[a-z0-9]{3,}：至少三个[a-z0-9]字符,[]内的是必选的。
        //[.]：'.'号时必选的。
        //p{Lower}{2,}：小写字母，两个以上。
        String EMAIL = "\\p{Alpha}\\w{2,15}[@][a-z0-9]{3,}[.]\\p{Lower}{2,}";
        Pattern emailpattern = Pattern.compile(EMAIL);
        Matcher emailMatcher = emailpattern.matcher(account);
        //验证正则表达式
        if(mobileMatcher.matches() || emailMatcher.matches()) {
            isValid = true;
        }

        String regex = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[0-9]))\\d{8}$";
        if (account.length() != 11) {
            return false;
        } else {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(account);
            isValid = m.matches();
        }

        return isValid;
    }
    public static boolean isPhoneNumber(String input) {// 判断手机号码是否规则
        String regex = "(1[0-9][0-9]|15[0-9]|18[0-9])\\d{8}";
        Pattern p = Pattern.compile(regex);
        return p.matches(regex, input);//如果不是号码，则返回false，是号码则返回true

    }
}
