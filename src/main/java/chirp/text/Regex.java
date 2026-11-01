package chirp.text;

import chirp.internal.Logger;

/**
 * Ready-to-use regex patterns — organised by domain.
 */
public final class Regex {

    private Regex() {}

    // ------------------------------------------------------------ basics
    public static String alpha() { return "[A-Za-z]+"; }
    public static String digits() { return "\\d+"; }
    public static String numeric() { return "-?\\d+(\\.\\d+)?"; }
    public static String alphanumeric() { return "[A-Za-z0-9]+"; }
    public static String slug() { return "^[a-z0-9]+(-[a-z0-9]+)*$"; }
    public static String username() { return "^[A-Za-z0-9_]{3,20}$"; }

    // ------------------------------------------------------------ contact
    /** Same pattern as {@link Check#isEmail(String)} (see its rules there). */
    public static String email() {
        return Check.emailPattern();
    }
    public static String phone() { return "^\\+?[0-9]{7,15}$"; }

    // ------------------------------------------------------------ network
    public static String url() { return "^(https?|ftp)://[\\w.-]+(:\\d+)?(/.*)?$"; }
    public static String ipAddress() {
        return "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";
    }
    public static String ipv4() { return ipAddress(); }
    public static String ipv6() {
        return "^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}"
                + "|([0-9a-fA-F]{1,4}:){1,7}:"
                + "|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}"
                + "|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}"
                + "|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}"
                + "|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}"
                + "|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}"
                + "|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})"
                + "|:((:[0-9a-fA-F]{1,4}){1,7}|:))$";
    }
    public static String macAddress() { return "^([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}$"; }
    public static String uuid() {
        return "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    }

    // ------------------------------------------------------------ time
    public static String dateISO() { return "^\\d{4}-\\d{2}-\\d{2}$"; }
    public static String dateDMY() { return "^(0?[1-9]|[12]\\d|3[01])[/-](0?[1-9]|1[0-2])[/-]\\d{4}$"; }
    public static String dateMDY() { return "^(0?[1-9]|1[0-2])[/-](0?[1-9]|[12]\\d|3[01])[/-]\\d{4}$"; }
    public static String time24Hour() { return "^([01]?\\d|2[0-3]):[0-5]\\d(:[0-5]\\d)?$"; }
    public static String time12Hour() { return "^(0?[1-9]|1[0-2]):[0-5]\\d(:[0-5]\\d)?\\s?(AM|PM|am|pm)$"; }
    public static String isoDateTime() { return "^\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}(:\\d{2})?$"; }

    // ------------------------------------------------------------ financial
    public static String creditCardVisa() { return "^4\\d{12}(\\d{3})?$"; }
    public static String creditCardMastercard() { return "^(5[1-5]\\d{14}|2[2-7]\\d{14})$"; }
    public static String creditCardAmex() { return "^3[47]\\d{13}$"; }
    public static String iban() { return "^[A-Z]{2}\\d{2}[A-Z0-9]{11,30}$"; }

    // ------------------------------------------------------------ publishing
    public static String isbn10() { return "^\\d{9}[\\dX]$"; }
    public static String isbn13() { return "^97[89]\\d{10}$"; }

    // ------------------------------------------------------------ Pakistan
    public static String pakistaniMobile() { return "^(?:\\+92|0092|92|0)?3[0-6]\\d{8}$"; }
    public static String pakistaniCnic() { return "^[1-8]\\d{12}$"; }
    public static String pakistaniLandline() { return "^0[2-9]\\d{8,9}$"; }
    public static String pakistaniPostalCode() { return "^\\d{5}$"; }
    public static String pakistaniPassport() { return "^[A-Z]{2}\\d{7}$"; }
    public static String pakistaniNtn() { return "^\\d{7}(-\\d)?$"; }
    public static String pakistaniIban() { return "^PK\\d{2}[A-Z]{4}[A-Z0-9]{16}$"; }
    public static String pakistaniVehiclePlate() { return "^[A-Z]{2,4}[- ]?\\d{3,5}$"; }

    // ------------------------------------------------------------ misc
    public static String hexColor() { return "#[0-9A-Fa-f]{6}"; }
    public static String hexColorShort() { return "#[0-9A-Fa-f]{3}"; }
    public static String base64() { return "^[A-Za-z0-9+/]+={0,2}$"; }
    public static String htmlTag() { return "<[^>]+>"; }
    public static String latitude() { return "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?)$"; }
    public static String longitude() { return "^[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)$"; }

    // ------------------------------------------------------------ cannot-express
    public static String strongPassword() {
        Logger.warn("Strong-password regex cannot be generated safely. "
                + "Use Check.isStrongPassword(...) or Rules.strongPassword() instead.");
        return null;
    }
    public static String veryStrongPassword() {
        Logger.warn("Very-strong-password regex cannot be generated safely. "
                + "Use Check.isVeryStrongPassword(...) instead.");
        return null;
    }
}
