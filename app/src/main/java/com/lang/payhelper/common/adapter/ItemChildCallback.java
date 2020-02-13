package com.lang.payhelper.common.adapter;

import android.view.View;

public interface ItemChildCallback<E> {

    void onItemChildClicked(View childView, E item, int position);

}
