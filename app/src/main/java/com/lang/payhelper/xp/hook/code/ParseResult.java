package com.lang.payhelper.xp.hook.code;

class ParseResult {

    private boolean blockSms;

    ParseResult() {

    }

    boolean isBlockSms() {
        return blockSms;
    }

    void setBlockSms(boolean blockSms) {
        this.blockSms = blockSms;
    }
}
