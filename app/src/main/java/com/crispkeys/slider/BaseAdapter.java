package com.crispkeys.slider;

import android.view.View;

/**
 * Created by Behzodbek Qodirov on 8/16/15.
 */
public abstract class BaseAdapter<V extends View> {
    abstract int getCount();
    abstract V getView(int position);
}
