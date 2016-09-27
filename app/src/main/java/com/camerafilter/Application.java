package com.camerafilter;

/**
 * Created by Le Minh An on 9/26/2016.
 */
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ImgLySdk.init(this);
    }


}
