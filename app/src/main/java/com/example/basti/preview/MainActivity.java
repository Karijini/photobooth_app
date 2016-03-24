package com.example.basti.preview;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.ArrayList;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {
    private Integer m_current_image_index;
    private ArrayList<String> m_image_names;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        m_current_image_index = -1;
        m_image_names = new ArrayList<String>();
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnTouchListener(new OnSwipeTouchListener(this) {
            public void onSwipeTop() {
                sendToScreen();
            }

            public void onSwipeRight() {
                //Toast.makeText(getApplicationContext(), "right", Toast.LENGTH_SHORT).show();
                if (m_current_image_index-1>=0 ) {
                    m_current_image_index -= 1;
                    loadBitmap();
                }
            }

            public void onSwipeLeft() {
                //Toast.makeText(getApplicationContext(), "left", Toast.LENGTH_SHORT).show();
                if (m_current_image_index+1<m_image_names.size() ) {
                    m_current_image_index += 1;
                    loadBitmap();
                }
            }

            public void onSwipeBottom() {
                Toast.makeText(getApplicationContext(), "bottom", Toast.LENGTH_SHORT).show();
                loadBitmap();
            }

        });

        Thread thread = new Thread(new ZmqEventWantcher(this,"tcp://192.168.0.1:5557")
        {
            @Override
            public void process_event(JSONObject event) {
                try{
                    String event_name = event.getString("event");
                    switch (event_name) {
                        case "new_image": {
                            System.out.println(event.getString("image_name"));
                            threadMsg(Constants.EVENT_TYPE_NEW_IMAGE,
                                    event.getString("image_name"));
                        }
                        case "count_down_changed":{
                            System.out.println(event.getInt("count_down"));
                            Integer count_down = event.getInt("count_down");
                            String data = Integer.toString(count_down);
                            threadMsg(Constants.EVENT_TYPE_COUNT_DOWN,
                                      data);
                        }
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            private void threadMsg(Integer eventType, String msg) {
                Message msgObj = handler.obtainMessage();
                Bundle b = new Bundle();
                b.putInt("eventType", eventType);
                b.putString("data", msg);
                msgObj.setData(b);
                handler.sendMessage(msgObj);
            }
            private final Handler handler = new Handler() {
                public void handleMessage(Message msg) {
                    int eventType = msg.getData().getInt("eventType");
                    switch (eventType) {
                        case Constants.EVENT_TYPE_NEW_IMAGE:{
                            m_a.new_image(msg.getData().getString("data"));
                            m_a.set_info_text(msg.getData().getString("data"));
                            break;
                        }
                        case Constants.EVENT_TYPE_COUNT_DOWN:{
                            m_a.set_info_text(msg.getData().getString("data"));
                            break;
                        }
                    }
                }
            };
        }
        );
        thread.start();

        new ZmqTaskLoadImages(this,"tcp://192.168.0.1:5556").execute("");
    }
    public void sendToScreen()
    {
        if (m_current_image_index>=m_image_names.size() || m_current_image_index<0)
        {
            return;
        }
        Toast.makeText(getApplicationContext(),
                "send to screen: "+m_image_names.get(m_current_image_index),
                Toast.LENGTH_SHORT).show();
        new ZmqTaskSendToScreen(this,"tcp://192.168.0.1:5556").execute(m_image_names.get(m_current_image_index));
    }
    public void loadBitmap()
    {
        set_info_text("__");
        System.out.println(m_current_image_index);
        if (m_current_image_index>=m_image_names.size() || m_current_image_index<0)
        {
            return;
        }
        PreviewPic pic = new PreviewPic(m_image_names.get(m_current_image_index));
        new ZmqTaskLoadPreview(this,"tcp://192.168.0.1:5556").execute(pic);
    }

    public void takePictureOnClick(View v)
    {
        Button b = (Button)v;
        //b.setText("clicked");
        System.out.println("takePictureOnClick");
        new ZmqTaskTakePic(this,"tcp://192.168.0.1:5556").execute("take_pic");
    }

    public void previewBitmapLoaded(PreviewPic preview_pic)
    {
        ImageView v = (ImageView) findViewById(R.id.imageView);
        v.setImageBitmap(preview_pic.getBitmap());
        set_info_text(preview_pic.picName());
        Toast.makeText(
                getBaseContext(),
                "loaded: "+preview_pic.picName(),
                Toast.LENGTH_SHORT).show();

    }

    public void printPictureOnClick(View v)
    {
        if (m_current_image_index>=m_image_names.size())
        {
            return;
        }
        Button b = (Button)v;
        //b.setText("clicked");
        System.out.println("printPictureOnClick");
        new ZmqTaskPrintPic(this,"tcp://192.168.0.1:5556").execute(m_image_names.get(m_current_image_index));
    }

    public void pictureTimerStarted(Integer sec)
    {

    }

    public void picturePrinted(String pic_name)
    {

    }

    public void new_image(String image_name)
    {
        m_image_names.add(image_name);
        m_current_image_index = m_image_names.size()-1;

        loadBitmap();
    }

    public void init_images(ArrayList<String> image_names)
    {
        int len = image_names.size();
        for (int i=0;i<len;i++){
            System.out.println(image_names.get(i));
        }
        m_image_names = image_names;
        m_current_image_index = m_image_names.size()-1;
        loadBitmap();
    }

    public void set_info_text(String count_down)
    {
        TextView v = (TextView) findViewById(R.id.textView);
        v.setText(count_down);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
