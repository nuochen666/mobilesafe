package com.nuochen.b;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.nuochen.b.activity.HomeActivity;
import com.nuochen.b.utils.StreamTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SplashActivity extends AppCompatActivity {

    protected static final String TAG="SplashActivity";
    private static final int ENTER_HOME =1 ;
    private static final int SHOW_UPDATE_DIALOG =2;
    private static final int URL_ERROR =3 ;
    private static final int NETWORK_ERROR =4 ;
    private static final int JSON_ERROR =5 ;
    private TextView tv_splash_version;
    private TextView tv_splash_updateinfo;
    /*
    * 升级的描述信息 和 升级的下载地址
    */
    private String scription;
    private String apkurl;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case ENTER_HOME:  //没有更新进入主页面
                    enterHome();
                    break;
                case SHOW_UPDATE_DIALOG://弹出升级对话框
                    Toast.makeText(SplashActivity.this, "有新版本了是否更新", Toast.LENGTH_LONG).show();
                    showUpdateDialog();
                    break;
                case URL_ERROR://URL异常
                    Toast.makeText(getApplicationContext(),"URL异常",Toast.LENGTH_LONG).show();
                    enterHome();
                    break;
                case NETWORK_ERROR://网络异常

                    Toast.makeText(getApplicationContext(), "网络异常", Toast.LENGTH_LONG).show();
                    enterHome();

                    break;
                case JSON_ERROR:// JOSON解析异常
                    Toast.makeText(getApplicationContext(),"JOSON解析异常",Toast.LENGTH_LONG).show();
                    enterHome();
                    break;
                default:break;
            }
        }
    };
    /*
    * 显示升级对话框
    * */
    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage(scription);
        builder.setNegativeButton("下次再说", null);
        builder.setPositiveButton("立刻升级", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                下载apk 替换安装
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    HttpUtils http=new HttpUtils();
                    http.download(apkurl, Environment.getExternalStorageDirectory() +
                            "/H59A149A3_0715190313.apk", true,true,new RequestCallBack<File>() {
                        @Override
                        public void onLoading(long total, long current, boolean isUploading) {
                            super.onLoading(total, current, isUploading);
                            tv_splash_updateinfo.setVisibility(View.VISIBLE);
                            int progress= (int) (current*100/total);
                            tv_splash_updateinfo.setText("下载总进度为："+progress+"%");
                        }

                        @Override
                        public void onSuccess(ResponseInfo<File> responseInfo) {

                        }

                        @Override
                        public void onFailure(HttpException e, String s) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),"下载失败了",Toast.LENGTH_SHORT);

                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "sdcard不可用", Toast.LENGTH_SHORT);
                }
            }
        });
        builder.show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        tv_splash_version= (TextView) findViewById(R.id.tv_splash_version);
//        Toast.makeText(this,getVersion(),Toast.LENGTH_SHORT).show();
        tv_splash_version.setText("版本号为:" + getVersion());
        tv_splash_updateinfo= (TextView) findViewById(R.id.tv_splash_updateinfo);
//        软件的升级
        showUpdateDialog();
        checkVersion();

    }
    private String getVersion(){
//        包管理器
        PackageManager pm=getPackageManager();
        PackageInfo pki= null;
        try {
            pki = pm.getPackageInfo(getPackageName(),0);
            return pki.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }
    /*
    *进入主页面
    */
    protected void enterHome(){
        Intent intent=new Intent(this,HomeActivity.class);
        startActivity(intent);
//        关闭当前页面
        finish();

    }
    //    检查是否升级信息
    private void checkVersion(){

        new Thread(){
            @Override
            public void run() {
                super.run();
                Looper.prepare();
                Toast.makeText(SplashActivity.this, "haahha", Toast.LENGTH_SHORT);
                Looper.loop();
//                请求网络
                Message msg=Message.obtain();
                try {
                    URL url=new URL(getString(R.string.serverurl));
                    HttpURLConnection conn= (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(4000);
                    if(conn.getResponseCode()==200){
//                       请求成功把流转化成string
                        InputStream is= conn.getInputStream();
                        String result = StreamTools.readStreamString(is);
                        Log.e(TAG, "result=" + result);
                        JSONObject obj=new JSONObject(result);
                        String version= (String) obj.get("version");
                        scription= (String) obj.get("description");
                        apkurl= (String) obj.get("apkurl");
                        if(getVersion().equals(version)){
//                            没有新的版本显示主页面
                            msg.what=ENTER_HOME;
                        }else{
//                            弹出升级对话框
                            msg.what=SHOW_UPDATE_DIALOG;
                        }

                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    msg.what=URL_ERROR;
                } catch (IOException e) {
//                    URL异常 网络异常
                    msg.what=NETWORK_ERROR;
                    e.printStackTrace();
                } catch (JSONException e) {
//                    解析json异常
                    e.printStackTrace();
                    msg.what=JSON_ERROR;
                }finally {
                    handler.sendMessage(msg);
                }
            }
        }.start();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
