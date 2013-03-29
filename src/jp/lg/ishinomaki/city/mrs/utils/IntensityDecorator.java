package jp.lg.ishinomaki.city.mrs.utils;

public class IntensityDecorator implements StringDecorator {

    @Override
    public String decorate(String str) {

        if (str == null) {
            return null;
        }

        if (str.length() != 2) {
            return str;
        }

        if (str.endsWith("+")) {
            return "震度" + str.substring(0, 1) + "強";
        } else if (str.endsWith("-")) {
            return "震度" + str.substring(0, 1) + "弱";
        }

        return str;
    }

}
