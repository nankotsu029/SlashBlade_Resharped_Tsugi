package mods.flammpfeil.slashblade.event.client;

public class AdvancementsRecipeRenderer {
    private static final class SingletonHolder {
        private static final AdvancementsRecipeRenderer INSTANCE = new AdvancementsRecipeRenderer();
    }

    public static AdvancementsRecipeRenderer getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private AdvancementsRecipeRenderer() {
    }

    public void register() {
        // TODO(neoforge-1.21.1): Re-implement the advancements recipe overlay against the 1.21.1 screen API.
    }
}
