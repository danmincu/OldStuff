package com.example.myapp.Renderer;

import com.example.myapp.IDisplayImage;
import com.example.myapp.IHeartbeat;

import java.util.List;


public interface ISimpleRenderer {

    void renderAsync(List<Integer> data);
    // Bitmap render();
    void setRenderer(IDisplayImage displayImage);
    void setHeartbeat(IHeartbeat heartbeat);
    Integer getHeartbeat();

}
