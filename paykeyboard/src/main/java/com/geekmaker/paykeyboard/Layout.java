
package com.geekmaker.paykeyboard;

import java.util.HashMap;
import java.util.Map;

public class Layout {
    private static String[] layoutSettings = new String[]{"1:5,2:4,3:9,4:8,5:7,6:-,7:/,8:*,9:0,12:REFUND,13:=,14:3,15:2,16:6,19:.,20:+,31:1,21:ESC,23:PAY,28:Backspace,29:OPT,30:LIST", "1:5,2:4,3:9,4:8,5:7,6:-,7:/,8:*,9:0,12:REFUND,13:=,14:3,15:2,16:6,19:.,20:Backspace,31:1,21:+,23:PAY,28:LIST,29:OPT,30:ESC"};

    public Layout() {
    }

    public static Map<Integer, String> parse(String layoutString) {
        Map<Integer, String> result = new HashMap();
        String[] temp = layoutString.split(",");

        for(int i = 0; i < temp.length; ++i) {
            String[] temp1 = temp[i].split(":");
            result.put(Integer.parseInt(temp1[0].trim()), temp1[1].trim());
        }

        return result;
    }

    public static Map<Integer, String> getLayout(int index) {
        return parse(layoutSettings[index]);
    }
}
