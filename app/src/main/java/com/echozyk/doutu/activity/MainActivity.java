package com.echozyk.doutu.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.echozyk.doutu.adapter.RecyclerAdapter;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.orhanobut.logger.Logger;
import com.wandoujia.ads.sdk.Ads;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressDialog progressDialog;
    private static ArrayList<String> urlArrayList;
    private static ArrayList<String> objectIdArrayList;

    private Handler handler;
    private String url;
    private String objectId;
    private static RecyclerAdapter adapter;
    private static int RESULT_LOAD_IMAGE = 0x01;

    //豌豆荚广告
    private static final String APP_ID = "100045358";
    private static final String SECRET_KEY = "1bb9e388f26aaa89ae998b5634773080";
    private static final String BANNER = "56e298e5c3b22627595427748c96ce24";
    private static final String INTERSTITIAL = "0fe20052113b235735daee5d7afdf302";

    private static final String LEAN_APP_ID = "6wPbscYngBI12ITri23g6Rc9-gzGzoHsz";
    private static final String LEAN_SECRET_KEY = "8n00oe7bwgBqhvCBmrFOaNSf";

    private GestureDetector mGestureDetector;
    private static final int FLING_MIN_DISTANCE = 20;   //最小距离
    private static final int FLING_MIN_VELOCITY = 0;  //最小速度

    @Override
    protected void onResume() {
        super.onResume();
//        getData();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);
        initLeanCloud();
        initUI();
        initAD();
        getData();
        getMessage();
        GestureDetect();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);

//                Snackbar.make(view, "正在为您刷新数据", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

    }

    private void GestureDetect() {
        GestureDetector.SimpleOnGestureListener myGestureListener = new GestureDetector.SimpleOnGestureListener(){
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                Log.e("<--滑动测试-->", "开始滑动");
                float x = e1.getX()-e2.getX();
                float x2 = e2.getX()-e1.getX();
                if(x>FLING_MIN_DISTANCE&&Math.abs(velocityX)>FLING_MIN_VELOCITY){
                    Toast.makeText(MainActivity.this, "向左手势", Toast.LENGTH_SHORT).show();
//                    Ads.showInterstitial(MainActivity.this, INTERSTITIAL);

                }else if(x2>FLING_MIN_DISTANCE&&Math.abs(velocityX)>FLING_MIN_VELOCITY){
                    Toast.makeText(MainActivity.this, "向右手势", Toast.LENGTH_SHORT).show();
                }

                return false;
            };
        };
        mGestureDetector = new GestureDetector(this, myGestureListener);
    }

    private void initAD() {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    Ads.init(MainActivity.this, APP_ID, SECRET_KEY);
                    return true;
                } catch (Exception e) {
                    Log.e("ads-sample", "error", e);
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                final ViewGroup container = (ViewGroup) findViewById(R.id.banner_container);

                if (success) {
                    /**
                     * pre load
                     */
                    Ads.preLoad(BANNER, Ads.AdFormat.banner);
                    Ads.preLoad(INTERSTITIAL, Ads.AdFormat.interstitial);
//                    Ads.preLoad(APP_WALL, Ads.AdFormat.appwall);

                    /**
                     * add ad views
                     */
                    View bannerView = Ads.createBannerView(MainActivity.this, BANNER);
                    container.addView(bannerView, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ));

//                    Button btI = new Button(MainActivity.this);
//                    btI.setText("interstitial");
//                    btI.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Ads.showInterstitial(MainActivity.this, INTERSTITIAL);
//                        }
//                    });
//                    container.addView(btI);
//
//                    Button btW = new Button(MainActivity.this);
//                    btW.setText("app wall");
//                    btW.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Ads.showAppWall(MainActivity.this, APP_WALL);
//                        }
//                    });
//                    container.addView(btW);
                } else {
                    TextView errorMsg = new TextView(MainActivity.this);
                    errorMsg.setText("init failed");
                    container.addView(errorMsg);
                }
            }
        }.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            progressDialog.show();

            Uri selectedImageUri = data.getData();

            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            String picPath = cursor.getString(columnIndex);
            String picPath = null;
            try {
                picPath = URLDecoder.decode(cursor.getString(columnIndex), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            cursor.close();

            Bitmap compressedBitmap = getimage(picPath);
            File compressedImageFile = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".png");
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(compressedImageFile));
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
//                AVFile imageFile = AVFile.withAbsoluteLocalPath(System.currentTimeMillis() + ".png", picPath);
                AVFile imageFile = AVFile.withFile(System.currentTimeMillis() + ".png", compressedImageFile);
                imageFile.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        progressDialog.dismiss();
//                        Toast.makeText(MainActivity.this, "upload successful", Toast.LENGTH_LONG).show();
                        Snackbar.make(swipeRefreshLayout, "upload successful", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        getData();
                    }
                });


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void getMessage() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                adapter = new RecyclerAdapter(swipeRefreshLayout, urlArrayList, objectIdArrayList, MainActivity.this);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);

            }
        };

    }

    private void getData() {
        urlArrayList = new ArrayList<>();
        objectIdArrayList = new ArrayList<>();

        AVQuery<AVObject> query = new AVQuery<>("_File");
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                urlArrayList.clear();
                for (AVObject file : list) {
                    url = file.getString("url");
                    objectId = file.getObjectId();
                    Log.e("objectId", objectId);
                    urlArrayList.add(url);
                    objectIdArrayList.add(objectId);
                    handler.sendEmptyMessage(0x01);
                }

            }
        });


    }

    private void initLeanCloud() {
        AVOSCloud.initialize(this, LEAN_APP_ID, LEAN_SECRET_KEY);
        AVAnalytics.trackAppOpened(getIntent());
    }

    private void initUI() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();

            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("uploading...");

        Logger.init("LOG");

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        switch (id) {
//
//            case R.id.action_settings:
//                return true;
//
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    private Bitmap getimage(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 260f;//这里设置高度为800f
        float ww = 320f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }

    private Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 10) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }
}
