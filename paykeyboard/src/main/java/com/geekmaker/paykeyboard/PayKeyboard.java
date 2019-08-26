
package com.geekmaker.paykeyboard;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.hoho.android.usbserial.util.SerialInputOutputManager.Listener;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class PayKeyboard implements Listener {
    private static PayKeyboard instance = null;
    private static final String TAG = "KeyboardSDK";
    private final Context context;
    private final UsbSerialDriver driver;
    private final UsbDevice device;
    private BroadcastReceiver deattachReceiver;
    private Timer timer = null;
    private IKeyboardListener listener = null;
    private static AtomicInteger SEQ = new AtomicInteger(0);
    private SerialInputOutputManager serialManager;
    private static Object permissionLock = new Object();
    private final byte HEAD_ACK = 6;
    private final byte HEAD_PACK = 2;
    private final byte END_PACK = 3;
    private final int CMD_TYPE_KEY = 163;
    private static final int[] SignalLevels = new int[]{0, 8, 12, 14, 15};
    public static final byte SIGN_TYPE_G = 1;
    public static final byte SIGN_TYPE_W = 2;
    private byte signByte = 0;
    private static final String ACTION_USB_PERMISSION = "com.geekmaker.USB_PERMISSION";
    private static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    private static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private Map<Integer, IResponse> requests = new HashMap();
    private Map<Integer, String> keyNameMap = new HashMap();
    private String num1 = null;
    private String num2 = null;
    private boolean lock = false;
    private String op = "";
    public static final String KEY_0 = "0";
    public static final String KEY_1 = "1";
    public static final String KEY_2 = "2";
    public static final String KEY_3 = "3";
    public static final String KEY_4 = "4";
    public static final String KEY_5 = "5";
    public static final String KEY_6 = "6";
    public static final String KEY_7 = "7";
    public static final String KEY_8 = "8";
    public static final String KEY_9 = "9";
    public static final String KEY_ESC = "ESC";
    public static final String KEY_Backspace = "Backspace";
    public static final String KEY_EQUAL = "=";
    public static final String KEY_TIMES = "*";
    public static final String KEY_DIVIDE = "/";
    public static final String KEY_PLUS = "+";
    public static final String KEY_MINUS = "-";
    public static final String KEY_PAY = "PAY";
    public static final String KEY_REFUND = "REFUND";
    public static final String KEY_OPT = "OPT";
    public static final String KEY_LIST = "LIST";
    public static final String KEY_DOT = ".";
    public static final String KEY_FACE_PAY = "FACE";
    private boolean released = false;
    private PayKeyboard.RetryTask lastUpdateTask;
    private String lastTip = null;
    private UsbDeviceConnection connection;
    private UsbSerialPort port;
    public static final int DEFAULT_BAUD_RATE = 9600;
    private BroadcastReceiver usbReceiver;
    private int baudRate = 9600;
    private StringBuffer displayString;
    private String lastDisplay = null;
    private static USBDetector detector = null;

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        } else {
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(places, RoundingMode.HALF_UP);
            return bd.doubleValue();
        }
    }

    private PayKeyboard(UsbSerialDriver driver, Context context) {
        this.context = context;
        this.driver = driver;
        this.device = driver.getDevice();
        this.timer = new Timer();
        this.listener = new DefaultKeyboardListener();
        this.setLayout(0);
    }

    public void setLayout(int index) {
        this.setLayout(Layout.getLayout(index));
    }

    public void setLayout(String layoutString) {
        this.setLayout(Layout.parse(layoutString));
    }

    public void setLayout(Map<Integer, String> layout) {
        this.keyNameMap = layout;
    }

    public void setBaudRate(int rate) {
        this.baudRate = rate;
    }

    public Map<Integer, String> getLayout() {
        return this.keyNameMap;
    }

    /**
     * 打开键盘，所有与键盘的通信在此方法后发生
     */
    public void open() {
        Log.i("KeyboardSDK", "try open keyboard");
        UsbManager manager = (UsbManager)this.context.getSystemService(Context.USB_SERVICE);
        if (manager.hasPermission(this.device)) {
            this.initPort();
        } else {
            Log.i("KeyboardSDK", "try request usb permission");
            this.tryGetPermisson();
        }

    }

    private void tryGetPermisson() {
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this.context, 0, new Intent("com.geekmaker.USB_PERMISSION"), 0);
        IntentFilter filter = new IntentFilter("com.geekmaker.USB_PERMISSION");
        this.usbReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("com.geekmaker.USB_PERMISSION".equals(action)) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra("device");
                    if (intent.getBooleanExtra("permission", false)) {
                        if (device != null) {
                            PayKeyboard.this.initPort();
                        } else {
                            PayKeyboard.this.listener.onException(new PermissonDeniedException("no permisson"));
                        }
                    } else {
                        Log.d("KeyboardSDK", "permission denied for device " + device);
                        PayKeyboard.this.listener.onException(new PermissonDeniedException("no permisson"));
                    }
                }

            }
        };
        this.context.registerReceiver(this.usbReceiver, filter);
        UsbManager manager = (UsbManager)this.context.getSystemService(Context.USB_SERVICE);
        manager.requestPermission(this.device, permissionIntent);
    }

    private void initPort() {
        UsbManager manager = (UsbManager)this.context.getSystemService(Context.USB_SERVICE);
        this.connection = manager.openDevice(this.device);
        this.port = (UsbSerialPort)this.driver.getPorts().get(0);

        try {
            this.port.open(this.connection);
        } catch (IOException var4) {
            var4.printStackTrace();
            this.listener.onException(var4);
            return;
        }

        try {
            this.port.setParameters(this.baudRate, 0, 0, 0);
        } catch (IOException var3) {
            var3.printStackTrace();
        }

        this.serialManager = new SerialInputOutputManager(this.port, this);
        (new Thread(this.serialManager)).start();
        this.reset();
        this.deattachReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.hardware.usb.action.USB_DEVICE_DETACHED".equals(action)) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra("device");
                    if (device != null) {
                        Log.i("KeyboardSDK", "USB deattach!!!!!!");
                        PayKeyboard.this.release();
                        PayKeyboard.this.listener.onRelease();
                    }
                }

            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
        this.context.registerReceiver(this.deattachReceiver, filter);
        this.listener.onAvailable();
    }

    private void refresh() {
        if (this.lastTip != null) {
            this.showTip(this.lastTip);
        } else {
            this.updateDisplay(this.displayString.length() == 0 ? "0" : this.displayString.toString(), true);
        }
    }

    /**
     * 设置键盘显示屏的显示内容
     * @param tip
     */
    public void showTip(String tip) {
        Log.d("KeyboardSDK", String.format("show tip %s", tip));
        this.lastTip = tip;
        if (tip.length() > 8) {
            tip = tip.substring(0, 8);
        }

        if (tip.length() < 7) {
            StringBuffer tmp = new StringBuffer(tip);
            int padLen = (8 - tip.length()) / 2;

            for(int i = 0; i < padLen; ++i) {
                tmp.append(" ");
            }

            tip = tmp.toString();
        }

        this.sendRequest((new I2C(tip, false)).toBytes(), (byte)28);
    }

    private void updateDisplay(String string, boolean remember) {
        Log.d("KeyboardSDK", String.format("last update %s", string));
        if (string.length() > 8) {
            string = string.substring(0, 8);
        }

        if (this.lastUpdateTask != null) {
            this.lastUpdateTask.cancel();
            this.lastUpdateTask = null;
        }

        if (remember) {
            this.lastTip = null;
            this.lastDisplay = string;
        }

        this.sendRequest((new I2C(string)).toBytes(), (byte)28);
    }

    /**
     * 获取键盘的静态方法
     *
     * @param context activity context
     * @return PayKeyboard 如果键盘已经连接，则返回键盘实例，否则返回null
     */
    public static PayKeyboard get(Context context) {
        UsbManager manager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        UsbSerialDriver driver = null;

        for(int i = 0; i < availableDrivers.size(); ++i) {
            if (((UsbSerialDriver)availableDrivers.get(i)).getDevice().getVendorId() == 6790 && ((UsbSerialDriver)availableDrivers.get(i)).getDevice().getProductId() == 29987) {
                driver = (UsbSerialDriver)availableDrivers.get(i);
                break;
            }
        }

        if (driver == null) {
            Log.i("KeyboardSDK", "no  keyboard attached");
            return null;
        } else {
            Log.i("KeyboardSDK", "keyboard is attached");
            return new PayKeyboard(driver, context);
        }
    }

    /**
     *
     * @param listener 设置键盘事件侦听者
     */
    public void setListener(IKeyboardListener listener) {
        this.listener = listener;
    }

    public void onNewData(byte[] rawData) {
        Log.d("KeyboardSDK", "new data :" + HexDump.dumpHexString(rawData));
        if (rawData[0] == 6 || rawData[0] == 2) {
            ByteBuffer buffer = ByteBuffer.wrap(rawData);

            while(buffer.remaining() > 0) {
                byte head = buffer.get();
                boolean isResponse = head == 6;
                if (isResponse) {
                    head = buffer.get();
                }

                if (head != 2) {
                    return;
                }

                int len = buffer.getShort() & 255;
                int lrc = this.lrc(rawData, buffer.position() - 2, buffer.position() + len);
                int seq = buffer.get() & 255;
                int type = buffer.get() & 255;
                short errorCode = (short)(buffer.getShort() & 255);
                byte[] resp = new byte[len - 4];
                buffer.get(resp);
                if (buffer.get() != 3) {
                    return;
                }

                if ((buffer.get() & 255) != lrc) {
                    return;
                }

                if (isResponse) {
                    if (this.requests.containsKey(seq)) {
                        IResponse response = (IResponse)this.requests.remove(seq);
                        if (this.lastUpdateTask != null && this.lastUpdateTask.seq <= seq) {
                            this.lastUpdateTask.cancel();
                            this.lastUpdateTask = null;
                        }

                        if (errorCode != 0) {
                            Log.w("KeyboardSDK", String.format("response error!!!! %S", errorCode));
                            response.onError(errorCode, seq);
                        } else {
                            response.onResult(resp, seq);
                        }
                    }
                } else if (type == 163) {
                    int keyCode = resp[1] & 255;
                   if(! this.keyNameMap.containsKey(keyCode)) return;
                    String keyName = (String)this.keyNameMap.get(keyCode);
                    if (this.lock) {
                        this.listener.onKeyDown(keyCode,keyName);
                        return;
                    }


                    if (this.listener != null) {
                        if (resp[0] != 1) {
                            this.listener.onKeyUp(keyCode, keyName);
                        } else {
                            if (this.displayString.length() < 8 && (keyName.matches("^\\d$") || keyName.equals("."))) {
                                if (this.num1 != null && this.num2 != null) {
                                    this.num1 = null;
                                    this.num2 = null;
                                    this.op = "";
                                }

                                this.lastTip = null;
                                if (this.displayString.length() == 1 && this.displayString.toString().equals("0") && keyName.equals(".")) {
                                    Log.i("KeyboardSDK", "zero head");
                                    this.displayString.deleteCharAt(0);
                                }

                                if (keyName.equals(".")) {
                                    if (this.displayString.length() == 0) {
                                        this.displayString.append("0").append(".");
                                    } else if (this.displayString.indexOf(".") == -1) {
                                        this.displayString.append(".");
                                    }
                                } else {
                                    this.displayString.append(((String)this.keyNameMap.get(keyCode)).toString());
                                }

                                this.refresh();
                            }

                            if (keyName.equals("ESC")) {
                                this.displayString = new StringBuffer();
                                this.reset();
                            }

                            if (keyName.equals("Backspace")) {
                                if (this.displayString.length() != 0) {
                                    this.displayString.deleteCharAt(this.displayString.length() - 1);
                                }

                                this.refresh();
                            }

                            if (keyName.equals("=")) {
                                if (this.num1 != null && this.num2 != null) {
                                    this.calculate();
                                } else if (this.num1 != null && this.displayString.length() > 0) {
                                    this.num2 = this.displayString.toString();
                                    this.calculate();
                                    this.displayString = new StringBuffer();
                                }
                            }

                            if (keyName.equals("*") || keyName.equals("/") || keyName.equals("-") || keyName.equals("+")) {
                                if (this.num1 != null && this.num2 != null) {
                                    this.num2 = null;
                                }

                                this.op = keyName;
                                Log.i("KeyboardSDK", String.format(" op %s,num1 %s, num2 %s", keyCode, this.num1, this.displayString.toString()));
                                if (this.num1 != null && this.displayString.length() > 0) {
                                    this.num2 = this.displayString.toString();
                                    this.calculate();
                                    this.displayString = new StringBuffer();
                                    this.num2 = null;
                                } else if (this.displayString.length() > 0) {
                                    this.num1 = this.displayString.toString();
                                    this.displayString = new StringBuffer();
                                }
                            }

                            if (keyName.equals("PAY")) {
                                if (this.lastUpdateTask != null) {
                                    Log.w("KeyboardSDK", "pay while updating!!");
                                    return;
                                }

                                final double pay = round(Double.parseDouble(this.lastDisplay), 2);
                                if (this.num1 != null && this.op.length() > 0 && this.displayString.length() > 0) {
                                    this.num2 = this.displayString.toString();
                                    this.calculate();
                                    this.displayString = new StringBuffer();
                                    this.num2 = null;
                                    this.num1 = null;
                                    this.op = "";
                                    return;
                                }

                                if (pay > 0.0D) {
                                    this.lock = true;
                                    this.showTip("pay---");
                                    this.listener.onPay(new IPayRequest() {
                                        public double getMoney() {
                                            return pay;
                                        }

                                        public void setResult(boolean isOk) {
                                            this.setResult(isOk, false);
                                        }

                                        public void setResult(boolean isOk, boolean isCancel) {
                                            PayKeyboard.this.lock = false;
                                            PayKeyboard.this.lastDisplay = "0";
                                            PayKeyboard.this.displayString = new StringBuffer();
                                            if (isOk) {
                                                PayKeyboard.this.showTip("success");
                                            } else {
                                                PayKeyboard.this.showTip(isCancel ? "cancel" : "fail");
                                            }

                                        }
                                    });
                                }
                            }

                            this.listener.onKeyDown(keyCode, keyName);
                        }
                    }
                }
            }

        }
    }

    private void reset(boolean update) {
        this.num1 = null;
        this.num2 = null;
        this.op = "";
        this.lock = false;
        this.displayString = new StringBuffer();
        this.lastDisplay = "0";
        if (update) {
            this.updateDisplay("0", true);
        }

    }

    /**
     * 重置键盘状态
     */
    public void reset() {
        this.reset(true);
    }

    private void calculate() {
        if (this.num1 != null && this.num2 != null && this.op.length() > 0) {
//            double num1 = Double.parseDouble(this.num1);
//            double num2 = Double.parseDouble(this.num2);
            BigDecimal bigDecimal1 = new BigDecimal(this.num1);
            BigDecimal bigDecimal2 = new BigDecimal(this.num2);

            BigDecimal result = new BigDecimal(0);
            if (this.op.equals("*")) {
                result = bigDecimal1.multiply(bigDecimal2);
            }

            if (this.op.equals("/")) {
                result = bigDecimal1.divide(bigDecimal2);
            }

            if (this.op.equals("-")) {
                result = bigDecimal1.subtract(bigDecimal2);
            }

            if (this.op.equals("+")) {
                result = bigDecimal1.add(bigDecimal2);
            }

            if (result.doubleValue() > 9.9999999E7D) {
                this.reset(false);
                this.showTip("huge");
                return;
            }

            this.num1 = result.toPlainString();
            if (this.num1.indexOf(46) > 0) {
                Log.i("KeyboardSDK", "try trim subfix zero");
                this.num1 = this.num1.replaceAll("0+?$", "");
                this.num1 = this.num1.replaceAll("[.]$", "");
            }

            this.updateDisplay(this.num1, true);
        }

    }

    private int lrc(byte[] data, int start, int end) {
        Log.d("KeyboardSDK", "lrc start " + start + ",end " + end);
        int lrc = 0;

        for(int i = start; i <= end; ++i) {
            lrc = (lrc ^ data[i] & 255) & 255;
        }

        return lrc;
    }

    private void writeRaw(byte[] data) {
        this.serialManager.writeAsync(data);
        Log.i("KeyboardSDK", "write command1 " + HexDump.dumpHexString(data));
    }

    private int sendRequest(byte[] data, byte type) {
        return this.sendRequest(data, type, new IResponse() {
            public void onResult(byte[] data, int seq) {
            }

            public void onError(short code, int seq) {
            }
        });
    }

    private synchronized int sendRequest(byte[] data, byte type, IResponse response) {
        byte[] command = new byte[data.length + 7];
        command[0] = 2;
        int len = data.length + 2;
        command[1] = (byte)(len >> 8 & 255);
        command[2] = (byte)(len & 255);
        int seq = SEQ.getAndIncrement();
        command[3] = (byte)(seq & 255);
        command[4] = type;

        for(int i = 0; i < data.length; ++i) {
            command[5 + i] = data[i];
        }

        command[command.length - 2] = 3;
        command[command.length - 1] = (byte)this.lrc(command, 1, command.length - 2);
        Log.i("KeyboardSDK", "write command " + HexDump.dumpHexString(command));
        this.requests.put(seq, response);

        try {
            this.serialManager.writeAsync(command);
        } catch (Exception var8) {
            this.listener.onException(var8);
            Log.w("KeyboardSDK", "write to keyboard fail,please check connection!");
        }

        if (this.lastUpdateTask != null) {
            this.lastUpdateTask.cancel();
        }

        this.lastUpdateTask = new PayKeyboard.RetryTask(seq, data, type);
        this.timer.schedule(this.lastUpdateTask, 400L);
        return seq;
    }

    /**
     * 释放键盘资源
     */
    public void release() {
        this.released = true;
        if (this.serialManager != null) {
            this.serialManager.stop();
            this.serialManager = null;
        }

        if (this.connection != null) {
            this.connection.close();
            this.connection = null;
        }

        if (this.port != null) {
            try {
                this.port.close();
            } catch (IOException var2) {
                ;
            }

            this.port = null;
        }

        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }

        if (this.usbReceiver != null) {
            Log.i("KeyboardSDK", "unregister permission recevier");
            this.context.unregisterReceiver(this.usbReceiver);
            this.usbReceiver = null;
        }

        if (this.deattachReceiver != null) {
            Log.i("KeyboardSDK", "unregister deattatch recevier");
            this.context.unregisterReceiver(this.deattachReceiver);
            this.deattachReceiver = null;
        }

    }

    /**
     * 更新键盘信号值
     * @param wifi wifi信号
     * @param gprs gps信号
     */
    public void updateSign(int wifi, int gprs) {
        int sign = 0;
        if (wifi > 0) {
            if (wifi > 4) {
                wifi = 4;
            }

            sign = SignalLevels[wifi];
            sign |= 64;
        }

        if (gprs > 0) {
            sign |= 128;
        }

        I2C.signByte = (byte)sign;
        if (this.displayString != null) {
            this.refresh();
        }

    }

    public boolean isReleased() {
        return this.released;
    }

    public void onRunError(Exception e) {
        this.listener.onException(e);
    }

    /**
     * USB 设备连接检测器
     * @param context Activity Context
     * @return USBDetector 实例
     */
    public static USBDetector getDetector(final Context context) {
        if (detector == null) {
            Class var1 = USBDetector.class;
            synchronized(USBDetector.class) {
                if (detector == null) {
                    final BroadcastReceiver attachReceiver = new BroadcastReceiver() {
                        public void onReceive(Context context, Intent intent) {
                            String action = intent.getAction();
                            Log.i("KeyboardSDK", "usb event " + action);
                            if ("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(action)) {
                                Log.i("KeyboardSDK", "USB Attach!!!!!!");
                                PayKeyboard.detector.onAttach();
                            }

                        }
                    };
                    Log.i("KeyboardSDK", "set attach receiver");
                    IntentFilter filter = new IntentFilter();
                    filter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
                    context.registerReceiver(attachReceiver, filter);
                    detector = new USBDetector() {
                        public void release() {
                            super.release();
                            context.unregisterReceiver(attachReceiver);
                            PayKeyboard.detector = null;
                        }
                    };
                }
            }
        }

        return detector;
    }

    private class RetryTask extends TimerTask {
        public final int seq;
        private final byte[] data;
        private final byte type;

        public RetryTask(int seq, byte[] data, byte type) {
            this.seq = seq;
            this.data = data;
            this.type = type;
        }

        public void run() {
            Log.w("KeyboardSDK", "retry....");
            PayKeyboard.this.requests.clear();
            PayKeyboard.this.sendRequest(this.data, this.type);
        }
    }
}
