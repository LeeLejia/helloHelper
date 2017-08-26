package gzhu.cjwddz.hellovoicehelper;

/**
 * Created by cjwddz on 2017/5/21.
 */

public interface CallBack {
    void success(Object o);
    void update(Object... i);
    void fail(Object o);
}
