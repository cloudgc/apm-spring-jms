package org.apache.skywalking.apm.plugin.spring.jms;

/**
 * @author szw
 * @date 2024/8/1 15:02
 * @desc
 */
public class UrlUtil {

    private static String URL = null;

    public static void setUrl(String brokerUrl) {
        if (brokerUrl == null || !brokerUrl.contains("//")) {
            UrlUtil.URL = "empty";
            return;
        }
        UrlUtil.URL = brokerUrl.split("//")[1];
        if (URL != null) {
            UrlUtil.URL = UrlUtil.URL.replaceAll("\\)", "");
        }
    }

    public static String getUrl() {
        return URL;
    }
}
