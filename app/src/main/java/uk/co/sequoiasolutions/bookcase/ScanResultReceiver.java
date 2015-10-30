package uk.co.sequoiasolutions.bookcase;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Receives the result of the ebook scan
 */
public class ScanResultReceiver extends ResultReceiver {

    private Receiver mReceiver;

    private Creator CREATOR;

    public ScanResultReceiver(Handler handler) {
        super(handler);
        // TODO Auto-generated constructor stub
    }

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);

    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }

}
