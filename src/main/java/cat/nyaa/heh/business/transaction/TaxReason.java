package cat.nyaa.heh.business.transaction;

/**
 * reason for tax.
 * every transaction must have a reason.
 * suggested name is REASON_[YOUR_BUSINESS].
 * can be any kind of string.
 */
public class TaxReason {
    public static final String REASON_MARKET = "MARKET";
    public static final String REASON_SIGN_SHOP = "SIGN_SHOP";
    public static final String REASON_LOTTO = "LOTTO";
    public static final String REASON_AUC = "AUC";
    public static final String REASON_REQ = "REQ";
    public static final String REASON_STORAGE = "STORAGE";
    public static final String REASON_DIRECT = "DIRECT";
}
