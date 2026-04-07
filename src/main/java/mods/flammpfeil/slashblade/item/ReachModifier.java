package mods.flammpfeil.slashblade.item;

public final class ReachModifier {
    private static final double DEFAULT_REACH = 4.0D;
    private static final double BROKEN_REACH = 3.75D;
    private static final double BLADE_REACH = 5.0D;
    private static final double RESOLVED_REACH_OFFSET = 1.0D;
    private static final double DEFAULT_ENTITY_INTERACTION_RANGE = 3.0D;

    private ReachModifier() {
    }

    public static double brokenReach() {
        return BROKEN_REACH;
    }

    public static double bladeReach() {
        return BLADE_REACH;
    }

    public static double brokenRangeModifier() {
        return toEntityInteractionRangeModifier(BROKEN_REACH);
    }

    public static double bladeRangeModifier() {
        return toEntityInteractionRangeModifier(BLADE_REACH);
    }

    public static double toEntityInteractionRangeModifier(double resolvedReach) {
        return resolvedReach - RESOLVED_REACH_OFFSET - DEFAULT_ENTITY_INTERACTION_RANGE;
    }

    public static double defaultReach() {
        return DEFAULT_REACH;
    }

    @Deprecated(forRemoval = true)
    public static double BrokendReach() {
        return brokenReach();
    }

    @Deprecated(forRemoval = true)
    public static double BladeReach() {
        return bladeReach();
    }
}
