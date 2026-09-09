package dataProvider;

/**
 * PlanConfig - Data class representing a subscription plan's expected entitlements.
 * Maps directly from Excel rows in entitlement-testdata.xlsx (PlanConfig sheet).
 */
public class PlanConfig {

    private String planName;
    private String username;
    private String password;
    private String contentScope;      // "SN+SNS", "SN+FreeSNS", "SNS+FreeSN"
    private String adBehavior;        // "ad-free", "with-ads"
    private String snsAdTier;         // "free", "lite", "premium"
    private String maxVideoQuality;   // "4K UHD", "Full HD"
    private String audioQuality;      // "Dolby Atmos/DD+5.1", "DD+5.1", "Stereo 2.0"
    private boolean liveTvAccess;
    private int loginLimit;
    private int deviceLimit;
    private int contentLockThreshold; // 30 for free, 200 for paid
    private String snContentUrl;
    private String snsContentUrl;

    /**
     * Constructs PlanConfig from an Excel row (Object array).
     * Column order must match PlanConfig sheet layout.
     */
    public PlanConfig(Object[] row) {
        this.planName = row[0] != null ? row[0].toString().trim() : "";
        this.username = row[1] != null ? row[1].toString().trim() : "";
        this.password = row[2] != null ? row[2].toString().trim() : "";
        this.contentScope = row[3] != null ? row[3].toString().trim() : "";
        this.adBehavior = row[4] != null ? row[4].toString().trim() : "";
        this.snsAdTier = row[5] != null ? row[5].toString().trim() : "";
        this.maxVideoQuality = row[6] != null ? row[6].toString().trim() : "";
        this.audioQuality = row[7] != null ? row[7].toString().trim() : "";
        this.liveTvAccess = row[8] != null && row[8].toString().trim().equalsIgnoreCase("yes");
        this.loginLimit = parseIntSafe(row[9], 1);
        this.deviceLimit = parseIntSafe(row[10], 1);
        this.contentLockThreshold = parseIntSafe(row[11], 200);
        this.snContentUrl = row.length > 12 && row[12] != null ? row[12].toString().trim() : "";
        this.snsContentUrl = row.length > 13 && row[13] != null ? row[13].toString().trim() : "";
    }

    private int parseIntSafe(Object value, int defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ==================== GETTERS ====================

    public String getPlanName() { return planName; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getContentScope() { return contentScope; }
    public String getAdBehavior() { return adBehavior; }
    public String getSnsAdTier() { return snsAdTier; }
    public String getMaxVideoQuality() { return maxVideoQuality; }
    public String getAudioQuality() { return audioQuality; }
    public boolean isLiveTvAccess() { return liveTvAccess; }
    public int getLoginLimit() { return loginLimit; }
    public int getDeviceLimit() { return deviceLimit; }
    public int getContentLockThreshold() { return contentLockThreshold; }
    public String getSnContentUrl() { return snContentUrl; }
    public String getSnsContentUrl() { return snsContentUrl; }

    public boolean isAdFree() {
        return adBehavior.equalsIgnoreCase("ad-free");
    }

    public boolean hasSNAccess() {
        return contentScope.contains("SN");
    }

    public boolean hasSNSAccess() {
        return contentScope.contains("SNS");
    }

    @Override
    public String toString() {
        return "PlanConfig{" +
                "planName='" + planName + '\'' +
                ", contentScope='" + contentScope + '\'' +
                ", adBehavior='" + adBehavior + '\'' +
                ", snsAdTier='" + snsAdTier + '\'' +
                ", maxVideoQuality='" + maxVideoQuality + '\'' +
                ", audioQuality='" + audioQuality + '\'' +
                ", liveTvAccess=" + liveTvAccess +
                ", loginLimit=" + loginLimit +
                ", deviceLimit=" + deviceLimit +
                ", contentLockThreshold=" + contentLockThreshold +
                '}';
    }
}
