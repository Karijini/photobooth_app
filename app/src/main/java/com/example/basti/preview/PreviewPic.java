package com.example.basti.preview;

import android.graphics.Bitmap;

/**
 * Created by basti on 12.03.2016.
 */
public class PreviewPic {
    private Bitmap m_bitmap;
    private String m_picName;
    public PreviewPic(String name)
    {
        m_picName = name;
        m_bitmap = null;
    }
    public void setBitmap(Bitmap bitmap)
    {
        m_bitmap = bitmap;
    }
    public Bitmap getBitmap()
    {
        return m_bitmap;
    }
    public String picName()
    {
        return m_picName;
    }
}
