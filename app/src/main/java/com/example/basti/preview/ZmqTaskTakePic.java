package com.example.basti.preview;
import android.os.AsyncTask;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import org.zeromq.ZMQ;

/**
 * Created by basti on 10.03.2016.
 */
public class ZmqTaskTakePic extends AsyncTask<String, Void, Integer> {
    private final MainActivity m_a;
    private final String m_addr;
    public ZmqTaskTakePic(MainActivity a, String addr)
    {
        m_addr=addr;
        this.m_a = a;
    }

    @Override
    protected Integer doInBackground(String... params) {

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.REQ);
        System.out.println("connecting");
        socket.connect(m_addr);
        socket.send("{\"cmd\":\"take_pic\",\"args\":[]}");

        System.out.println("sending:");
        System.out.println(socket.recvStr());

//        byte[] bitmapdata = socket.recv();
//        System.out.println(bitmapdata.length);
//        PreviewPic preview_pic = new PreviewPic("test_image");
//        preview_pic.setBitmap( BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length) );

        socket.setLinger(0);
        socket.close();
        context.term();

        return 1;
    }

    @Override
    protected void onPostExecute(Integer sec) {
        if (m_a==null){
            return;
        }
        m_a.pictureTimerStarted(sec);
    }
}
