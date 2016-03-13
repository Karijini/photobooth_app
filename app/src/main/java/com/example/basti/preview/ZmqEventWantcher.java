package com.example.basti.preview;

import org.zeromq.ZMQ;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * Created by basti on 12.03.2016.
 */
public class ZmqEventWantcher implements Runnable {
    public final MainActivity m_a;
    private String m_addr;
    private Boolean m_running;
    ZmqEventWantcher(MainActivity a,String addr)
    {
        this.m_a = a;
        m_addr = addr;
        m_running = true;
    }

    public void process_event(JSONObject event)
    {

    }

    @Override
    public void run()
    {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.SUB);
        socket.subscribe("".getBytes());
        System.out.println("connecting");

        socket.connect(m_addr);

        ZMQ.Poller items = new ZMQ.Poller (1);
        items.register(socket, ZMQ.Poller.POLLIN);

        while(!Thread.currentThread().isInterrupted()) {
            items.poll(100);
            if (items.pollin(0)) {
                JSONObject responseJSON = null;
                try {
                    responseJSON = new JSONObject(socket.recvStr(0));
                    process_event(responseJSON);
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
