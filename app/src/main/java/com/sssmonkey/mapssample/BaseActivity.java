package com.sssmonkey.mapssample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by Yu on 2016/04/06.
 */
public class BaseActivity extends FragmentActivity {
    private static final int PERMISSION_REQUEST_CODE = 0;
    protected void requestPermissions(@NonNull String[] permissions){
        Log.d(getPackageName(), "called requestPermissions() permissions" + Arrays.toString(permissions));
        if(isGrantedAllPermissions(this, permissions)){
            // 許可済なら終了
            requestPermissionsSuccess();
            return;
        }

        // Permission未許可
        if(canShowAgainPermissions(this, permissions)){
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }else{
            // 「再度表示しない」がONの場合
            requestPermissionsFailed(permissions, null);
        }
    }

    /**
     * Permission許可済判定
     *
     * @param context
     * @param permissions
     * @return 全て許可済；true
     */
    private static boolean isGrantedAllPermissions(@NonNull Context context, @NonNull String[] permissions){
        boolean result = false;
        Iterator<String> iter = Arrays.asList(permissions).iterator();
        while(iter.hasNext()){
            result = PackageManager.PERMISSION_GRANTED == PermissionChecker.checkSelfPermission(context, iter.next());
            // 許可されていないPermissionがあったら終了
            if(!result) break;
        }
        return result;
    }

    /**
     * Permission許可Dialog表示可能判定
     *
     * @param activity
     * @param permissions
     * @return 表示可能：true
     */
    protected static boolean canShowAgainPermissions(@NonNull Activity activity, @NonNull String[] permissions){
        boolean result = false;
        Iterator<String> iter = Arrays.asList(permissions).iterator();
        while(iter.hasNext()){
            result = ActivityCompat.shouldShowRequestPermissionRationale(activity, iter.next());
            // 「今後は確認しない」がONのPermissionがあったら終了
            if(!result) break;
        }
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults){
        Log.d(getPackageName(), "called onRequestPermissionsResult()");
        boolean granted = false;
        for(int i = 0 ; i < grantResults.length ; i++){
            granted = PackageManager.PERMISSION_GRANTED == grantResults[i];
            // 許可されていないPermissionがあったら終了
            if(!granted) break;
        }

        if(granted){
            requestPermissionsSuccess();
        }else{
            requestPermissionsFailed(permissions, grantResults);
        }
    }

    /* Permission確認成功. */
    protected void requestPermissionsSuccess(){
        Log.d(getPackageName(), "called requestPermissionsSuccess()");
        // Override Extended Class If Need.
    }
    /* Permission確認失敗. */
    protected void requestPermissionsFailed(String[] permissions, int[] grantResults){
        Log.d(getPackageName(), "called requestPermissionsFailed()");
        // Override Extended Class If Need.
    }
    /* 設定アプリで自アプリの画面を開く. */
    protected void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}
