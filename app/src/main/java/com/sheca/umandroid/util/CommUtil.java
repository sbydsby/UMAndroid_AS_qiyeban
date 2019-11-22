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

import org.spongycastle.util.encoders.Base64;

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

    public static boolean isPasswordValid(String strPwd){
        boolean isValid = false;
        //final String Match_Patton =  "/^[\\w_-]{8,16}$\\/"; //^[a-zA-Z0-9]{6,21}$
        final String Match_Patton =  "/^(?=.*\\d)(?=.*[a-zA-Z])(?=.*[\\W])[\\da-zA-Z\\W]{8,16}$/"; //

        if (strPwd.length() <= 0) {
            return false;
        } else {
            //Pattern p = Pattern.compile(Match_Patton);
           // Matcher m = p.matcher(strPwd);
            //isValid = m.matches();
            int level = com.wx.pwd.CheckStrength.checkPasswordStrength(strPwd);
            if(level >= 3)
                return true;
        }

        return isValid;
    }


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
}
