package cn.dabaotv.movie.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 奈蜇 on 2018-09-19.
 * X
 */
public class StringUtils {
    public static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }
}
