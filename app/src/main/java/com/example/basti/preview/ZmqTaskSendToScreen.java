package com.example.basti.preview;
import android.os.AsyncTask;
import android.graphics.BitmapFactory;
import org.zeromq.ZMQ;

/**
 * Created by basti on 10.03.2016.
 */
public class ZmqTaskSendToScreen extends AsyncTask<String, Void, String> {
    private final MainActivity m_a;
    private final String m_addr;
    public ZmqTaskSendToScreen(MainActivity a, String addr) {
        this.m_a = a;
        m_addr = addr;
    }

    @Override
    protected String doInBackground(String... params) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.REQ);
        System.out.println("connecting");

        String pic_name = params[0];
        socket.connect(m_addr);
        System.out.println("sending:send_to_screen");;
        socket.send("{\"cmd\":\"send_to_screen\",\"args\":[\"" + pic_name + "\"]}");
        System.out.println(socket.recvStr());

        socket.setLinger(0);
        socket.close();
        context.term();

        return pic_name;
    }

    @Override
    protected void onPostExecute(String pic_name) {
        if (m_a==null){
            return;
        }
        m_a.picturePrinted(pic_name);
    }
}