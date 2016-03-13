package com.example.basti.preview;
import android.os.AsyncTask;
import android.graphics.BitmapFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zeromq.ZMQ;

import java.util.ArrayList;

/**
 * Created by basti on 10.03.2016.
 */
public class ZmqTaskLoadImages extends AsyncTask<String, Void, ArrayList<String> > {
    private final MainActivity m_a;
    private final String m_addr;
    public ZmqTaskLoadImages(MainActivity a, String addr) {
        this.m_a = a;
        m_addr = addr;
    }

    @Override
    protected ArrayList<String> doInBackground(String... params) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.REQ);
        System.out.println("connecting");

        String pic_name = params[0];
        socket.connect(m_addr);
        System.out.println("sending:print_pic");;
        socket.send("{\"cmd\":\"get_all_images\",\"args\":[]}");
        ArrayList<String> image_names = new ArrayList<String>();
        try {
            JSONObject responseJSON = new JSONObject(socket.recvStr(0));
            JSONArray jsonArray = responseJSON.getJSONArray("get_all_imagesResult");
            if (jsonArray != null) {
                int len = jsonArray.length();
                for (int i=0;i<len;i++){
                    image_names.add(jsonArray.get(i).toString());
                }
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
        socket.setLinger(0);
        socket.close();
        context.term();
        return image_names;
    }

    @Override
    protected void onPostExecute(ArrayList<String>  image_names) {
        if (m_a==null){
            return;
        }
        m_a.init_images(image_names);
    }
}