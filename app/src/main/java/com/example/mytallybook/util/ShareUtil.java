package com.example.mytallybook.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

/**
 * 分享工具类，提供分享到微信、QQ等平台的功能
 */
public class ShareUtil {
    
    // 微信包名
    private static final String WECHAT_PACKAGE_NAME = "com.tencent.mm";
    
    // QQ包名
    private static final String QQ_PACKAGE_NAME = "com.tencent.mobileqq";
    
    /**
     * 检查指定的应用是否已安装
     * @param context 上下文
     * @param packageName 包名
     * @return 是否已安装
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
    /**
     * 分享文本到微信
     * @param context 上下文
     * @param content 要分享的内容
     * @param title 分享的标题
     * @return 是否成功发起分享
     */
    public static boolean shareToWeChat(Context context, String content, String title) {
        if (!isAppInstalled(context, WECHAT_PACKAGE_NAME)) {
            Toast.makeText(context, "您尚未安装微信，无法完成分享", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, content);
            intent.setPackage(WECHAT_PACKAGE_NAME);
            context.startActivity(Intent.createChooser(intent, title));
            return true;
        } catch (Exception e) {
            Toast.makeText(context, "分享到微信失败，请稍后再试", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    
    /**
     * 分享文本到QQ
     * @param context 上下文
     * @param content 要分享的内容
     * @param title 分享的标题
     * @return 是否成功发起分享
     */
    public static boolean shareToQQ(Context context, String content, String title) {
        if (!isAppInstalled(context, QQ_PACKAGE_NAME)) {
            Toast.makeText(context, "您尚未安装QQ，无法完成分享", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, content);
            intent.setPackage(QQ_PACKAGE_NAME);
            context.startActivity(Intent.createChooser(intent, title));
            return true;
        } catch (Exception e) {
            Toast.makeText(context, "分享到QQ失败，请稍后再试", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    
    /**
     * 通过系统分享对话框分享文本
     * @param context 上下文
     * @param content 要分享的内容
     * @param title 分享的标题
     */
    public static void shareTextBySystem(Context context, String content, String title) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, content);
        context.startActivity(Intent.createChooser(intent, title));
    }
    
    /**
     * 获取应用名称
     * @param context 上下文
     * @return 应用名称
     */
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            return String.valueOf(packageManager.getApplicationLabel(
                    context.getApplicationInfo()));
        } catch (Exception e) {
            return "记账本";
        }
    }
}