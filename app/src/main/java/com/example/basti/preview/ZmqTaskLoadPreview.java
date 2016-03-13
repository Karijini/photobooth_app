package com.example.basti.preview;
import android.os.AsyncTask;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import org.zeromq.ZMQ;

/**
 * Created by basti on 10.03.2016.
 */
public class ZmqTaskLoadPreview extends AsyncTask<PreviewPic, Void, PreviewPic> {
    private final MainActivity m_a;
    private final String m_addr;
    public ZmqTaskLoadPreview(MainActivity a, String addr)
    {
        this.m_a = a;
        m_addr = addr;
    }

    @Override
    protected PreviewPic doInBackground(PreviewPic... params) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.REQ);
        System.out.println("connecting");

        PreviewPic preview_pic = params[0];
        socket.connect(m_addr);
        System.out.println("sending:load_preview_pic");
        socket.send("{\"cmd\":\"load_preview_pic\",\"args\":[\"" + preview_pic.picName() + "\"]}");

        byte[] bitmapdata = socket.recv();
        System.out.println(bitmapdata.length);
        preview_pic.setBitmap( BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length) );

        socket.setLinger(0);
        socket.close();
        context.term();

        return preview_pic;
    }

    @Override
    protected void onPostExecute(PreviewPic preview_pic) {
        if (m_a==null){
            return;
        }
        m_a.previewBitmapLoaded(preview_pic);
    }
}