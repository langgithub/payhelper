package com.lang.payhelper.ui.record;


import com.lang.payhelper.common.mvp.BasePresenter;
import com.lang.payhelper.common.mvp.BaseView;
import com.lang.payhelper.data.db.entity.SmsMsg;

import java.util.List;

interface CodeRecordContract {

    interface View extends BaseView {

        void showRefreshing();

        void stopRefresh();

        void displayData(List<SmsMsg> smsMsgList);

    }

    interface Presenter extends BasePresenter<View> {

        void loadData();

        void removeSmsMsg(List<SmsMsg> smsMsgList);
    }

}
