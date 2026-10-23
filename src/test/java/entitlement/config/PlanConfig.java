package entitlement.config;

import java.util.List;
import java.util.Objects;

/**
 * Immutable expected entitlements for web validation, separate from account credentials.
 *
 * <p>The predefined catalog contains only the six documented paid plans. Free and
 * Guest accounts have known VOD thresholds, but their other entitlements must be
 * supplied explicitly once product requirements establish them.</p>
 *
 * @param planId stable identifier used to select a plan independently of its display name
 * @param planName human-readable plan name
 * @param accountType paid, free, or guest account classification
 * @param snAccess expected access to the SN catalog
 * @param snsAccess expected access to the SNS catalog
 * @param adEntitlement expected ad behavior
 * @param loginLimit permitted logged-in devices; distinct from simultaneous playback
 * @param concurrentPlaybackLimit permitted simultaneous playback sessions
 * @param liveTvAccess whether the plan includes Live TV
 * @param maxWebQuality maximum permitted web resolution in vertical pixels
 * @param vodTier selects the supplied VOD threshold without interpreting what it counts
 */
public record PlanConfig(
        String planId,
        String planName,
        AccountType accountType,
        ContentAccess snAccess,
        ContentAccess snsAccess,
        AdEntitlement adEntitlement,
        int loginLimit,
        int concurrentPlaybackLimit,
        boolean liveTvAccess,
        int maxWebQuality,
        VodTier vodTier) {

    public static final int WEB_MAX_QUALITY = 1080;

    /**
     * Exact supplied allowed_vods_threshold values. Their counting unit and
     * enforcement mechanism remain unspecified; no counting or enforcement logic is
     * implemented here.
     * BASIC is a threshold tier, not an additional predefined subscription plan.
     */
    public enum VodTier {
        FREE(30),
        GUEST(30),
        LITE(200),
        BASIC(200),
        PREMIUM(200);

        private final int allowedVodsThreshold;

        VodTier(int allowedVodsThreshold) {
            this.allowedVodsThreshold = allowedVodsThreshold;
        }

        public int allowedVodsThreshold() {
            return allowedVodsThreshold;
        }
    }

    private static final List<PlanConfig> PAID_PLANS = List.of(
            paid("combo_annual_platinum", "Combo Annual Platinum",
                    ContentAccess.FULL, ContentAccess.FULL,
                    AdEntitlement.AD_FREE, 4, 4, true, VodTier.PREMIUM),
            paid("combo_quarterly_platinum", "Combo Quarterly Platinum",
                    ContentAccess.FULL, ContentAccess.FULL,
                    AdEntitlement.WITH_ADS, 2, 1, true, VodTier.LITE),
            paid("sn_annual_premium", "SN Annual Premium",
                    ContentAccess.FULL, ContentAccess.FREE_CATALOG,
                    AdEntitlement.AD_FREE, 3, 2, true, VodTier.PREMIUM),
            paid("sn_quarterly_lite", "SN Quarterly Lite",
                    ContentAccess.FULL, ContentAccess.FREE_CATALOG,
                    AdEntitlement.WITH_ADS, 2, 1, true, VodTier.LITE),
            paid("sns_annual_premium", "SNS Annual Premium",
                    ContentAccess.FREE_CATALOG, ContentAccess.FULL,
                    AdEntitlement.AD_FREE, 3, 2, false, VodTier.PREMIUM),
            paid("sns_quarterly_lite", "SNS Quarterly Lite",
                    ContentAccess.FREE_CATALOG, ContentAccess.FULL,
                    AdEntitlement.WITH_ADS, 2, 1, false, VodTier.LITE));

    /** Rejects incomplete definitions instead of silently defaulting entitlements. */
    public PlanConfig {
        Objects.requireNonNull(planId, "planId");
        Objects.requireNonNull(planName, "planName");
        Objects.requireNonNull(accountType, "accountType");
        Objects.requireNonNull(snAccess, "snAccess");
        Objects.requireNonNull(snsAccess, "snsAccess");
        Objects.requireNonNull(adEntitlement, "adEntitlement");
        Objects.requireNonNull(vodTier, "vodTier");
        if (planId.isBlank() || planName.isBlank()) {
            throw new IllegalArgumentException("Plan ID and name must not be blank");
        }
        if (loginLimit < 0 || concurrentPlaybackLimit < 0) {
            throw new IllegalArgumentException("Login and concurrent playback limits must not be negative");
        }
        if (maxWebQuality != WEB_MAX_QUALITY) {
            throw new IllegalArgumentException("Maximum web quality must be 1080p");
        }
        boolean tierMatchesAccount = switch (accountType) {
            case FREE -> vodTier == VodTier.FREE;
            case GUEST -> vodTier == VodTier.GUEST;
            case PAID -> vodTier == VodTier.LITE
                    || vodTier == VodTier.BASIC || vodTier == VodTier.PREMIUM;
        };
        if (!tierMatchesAccount) {
            throw new IllegalArgumentException("VOD tier does not match account type");
        }
    }

    /** Returns the threshold value only; it does not determine or enforce its meaning. */
    public int allowedVodsThreshold() {
        return vodTier.allowedVodsThreshold();
    }

    /** Returns the immutable catalog of exactly six paid plans. */
    public static List<PlanConfig> paidPlans() {
        return PAID_PLANS;
    }

    /** Looks up an exact, case-sensitive paid-plan ID; unknown IDs fail explicitly. */
    public static PlanConfig forId(String planId) {
        Objects.requireNonNull(planId, "planId");
        return PAID_PLANS.stream()
                .filter(plan -> plan.planId().equals(planId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown paid plan ID: " + planId));
    }

    private static PlanConfig paid(String planId, String planName,
            ContentAccess snAccess, ContentAccess snsAccess,
            AdEntitlement adEntitlement, int loginLimit, int concurrentPlaybackLimit,
            boolean liveTvAccess, VodTier vodTier) {
        return new PlanConfig(planId, planName, AccountType.PAID, snAccess, snsAccess,
                adEntitlement, loginLimit, concurrentPlaybackLimit, liveTvAccess,
                WEB_MAX_QUALITY, vodTier);
    }
}
