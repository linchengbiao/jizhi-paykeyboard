

package com.geekmaker.paykeyboard;

public class USBDetector {
    private ICheckListener listener = null;

    public USBDetector() {
    }

    public void onAttach() {
        if (this.listener != null) {
            this.listener.onAttach();
        }

    }

    public void setListener(ICheckListener listener) {
        this.listener = listener;
    }

    public void release() {
    }
}
