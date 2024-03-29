package dev.yuafox.yuabot.utils;

import twitter4j.JSONArray;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;

public class Https {
    public static File download(String url, String path) throws IOException {
        new File(path).getParentFile().mkdirs();
        new FileOutputStream(path).getChannel().transferFrom(Channels.newChannel(new URL(url).openStream()), 0, Long.MAX_VALUE);
        return new File(path);
    }

    public static JSONArray getJsonArray(String url) throws IOException {
        StringBuilder content = new StringBuilder();
        URL myurl = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) myurl.openConnection();
        con.setRequestProperty ( "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0" );
        InputStream ins = con.getInputStream();
        InputStreamReader isr = new InputStreamReader(ins);
        try (BufferedReader in = new BufferedReader(isr)) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) content.append(inputLine);
        }
        return new JSONArray(content.toString());
    }
}
