package com.android.landicorp.f8face.http;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;

/**
 * Created by admin on 2019/6/20.
 */


public class ReturnXMLParser {
    public static String parseGetAuthInfoXML(InputStream is) throws  Exception{
        String result=null;

        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(is,"UTF-8");

        int eventType = parser.getEventType();
        while(eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (parser.getName().equals("authinfo")) {
                        eventType = parser.next();
                        result=parser.getText();
                    }
            }
            eventType = parser.next();
        }

        return result;
    }
}