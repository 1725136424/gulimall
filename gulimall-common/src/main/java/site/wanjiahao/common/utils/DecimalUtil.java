package site.wanjiahao.common.utils;

import java.util.regex.Pattern;

public class DecimalUtil {

    //两位小数金额校验
    public static boolean judgeTwoDecimal(Object obj) {
        boolean flag = false;
        try {
            if (obj != null) {
                String source = obj.toString();
                // 判断是否是整数或者是携带一位或者两位的小数
                Pattern pattern = Pattern.compile("^[+]?([0-9]+(.[0-9]{1,2})?)$");
                if (pattern.matcher(source).matches()) {
                    flag = true;
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }
        return flag;
    }

    //两位小数金额校验
    public static boolean judgeThreeDecimal(Object obj) {
        boolean flag = false;
        try {
            if (obj != null) {
                String source = obj.toString();
                // 判断是否是整数或者是携带一位或者两位的小数
                Pattern pattern = Pattern.compile("^[+]?([0-9]+(.[0-9]{1,3})?)$");
                if (pattern.matcher(source).matches()) {
                    flag = true;
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }
        return flag;
    }

}
