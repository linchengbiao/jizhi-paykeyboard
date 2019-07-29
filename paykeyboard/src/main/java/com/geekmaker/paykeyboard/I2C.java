
package com.geekmaker.paykeyboard;

import com.hoho.android.usbserial.util.HexDump;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * I2C相关数据编解码相关
 */
public class I2C {
    public static byte signByte = 0;
    private final byte[] DIGIS;
    private static final Map<String, Byte> letters = new HashMap();
    private static final String letterString = "-:20,a:77,b:6e,c:f,d:1c,e:2f,f:27,g:6f,h:76,i:50,j:59,l:e,n:57,o:5f,p:37,q:73,r:7,s:6b,t:2e,u:5e,y:7a";
    private byte[] data;

    public I2C(String string) {
        this(string, true);
    }

    public I2C(String dataString, boolean isNumber) {
        this.DIGIS = new byte[]{95, 80, 61, 121, 114, 107, 111, 83, 127, 123};
        if (!isNumber) {
            if (dataString.length() > 8) {
                dataString = dataString.substring(0, 8);
            }

            byte[] data = new byte[dataString.length()];

            for(int i = 0; i < dataString.length(); ++i) {
                String letter = dataString.substring(i, i + 1);
                data[i] = letters.containsKey(letter) ? (Byte)letters.get(letter) : 0;
            }

            this.data = data;
        } else {
            int dotIndex = dataString.indexOf(46);
            if (dotIndex != -1) {
                dataString = dataString.replace(".", "");
                --dotIndex;
            }

            byte[] data = new byte[dataString.length()];

            for(int i = 0; i < data.length; ++i) {
                String letter = dataString.substring(i, i + 1);
                byte digit = letter.equals("-") ? 32 : this.DIGIS[Integer.parseInt(letter)];
                if (dotIndex == i) {
                    digit = (byte)(digit | 128);
                }

                data[i] = digit;
            }

            this.data = data;
        }

    }

    public I2C(byte[] raw) {
        this.DIGIS = new byte[]{95, 80, 61, 121, 114, 107, 111, 83, 127, 123};
        this.data = raw;
    }

    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(47);
        buffer.put(new byte[]{-113, 45, 41, 1, -64, 39, 1, 1, 42, 1, 100, 41, 1, 64, 39, 1, 1, 42, 1, 100, 41, 13, -64});
        buffer.put(signByte);
        int pad = 10 - this.data.length;

        for(int i = 0; i < pad; ++i) {
            buffer.put((byte)0);
        }

        buffer.put(this.data);
        buffer.put(new byte[]{0, 39, 1, 1, 42, 1, 100, 41, 1, -105, 42, 1, 100});
        return buffer.array();
    }

    public static void main(String[] args) {
        System.out.println(HexDump.dumpHexString((new I2C("12")).toBytes()));
    }

    static {
        String[] tmp = "-:20,a:77,b:6e,c:f,d:1c,e:2f,f:27,g:6f,h:76,i:50,j:59,l:e,n:57,o:5f,p:37,q:73,r:7,s:6b,t:2e,u:5e,y:7a".split(",");

        for(int i = 0; i < tmp.length; ++i) {
            String[] tmp1 = tmp[i].split(":");
            letters.put(tmp1[0], (byte)Integer.parseInt(tmp1[1], 16));
        }

    }
}
