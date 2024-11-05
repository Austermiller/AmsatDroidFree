package uk.me.g4dpz.HamSatDroid;


public class AppConfig {

    // This class is used to store and retrieve user's Time Display preference (utc or local)
    // and the user's satellite sorting method preference.
    public static boolean isUtcDateTime() {
        return utcDateTime;
    }

    public static void setUtcDateTime(boolean utcDateTime) {
        AppConfig.utcDateTime = utcDateTime;
    }

    public static boolean utcDateTime;

    public static String getSatSortingMethod() {
        return satSortingMethod;
    }

    public static void setSatSortingMethod(String satSortingMethod) {
        AppConfig.satSortingMethod = satSortingMethod;
    }

    public static String satSortingMethod;

}
