package mods.flammpfeil.slashblade.capability.concentrationrank;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;

public class ConcentrationRank implements IConcentrationRank {

    long rankpoint;
    long lastupdate;
    long lastrankrise;

    static public long UnitCapacity = 300;

    /**
     * Codec for AttachmentType serialization.
     * Persists rawPoint and lastupdate; lastrankrise resets per session.
     */
    public static final Codec<ConcentrationRank> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.optionalFieldOf("rawPoint", 0L).forGetter(cr -> cr.rankpoint),
                    Codec.LONG.optionalFieldOf("lastupdate", 0L).forGetter(cr -> cr.lastupdate)
            ).apply(instance, (rawPoint, lastUpdate) -> {
                ConcentrationRank cr = new ConcentrationRank();
                cr.rankpoint = rawPoint;
                cr.lastupdate = lastUpdate;
                return cr;
            })
    );

    public ConcentrationRank() {
        rankpoint = 0;
        lastupdate = 0;
    }

    @Override
    public long getRawRankPoint() {
        return rankpoint;
    }

    @Override
    public void setRawRankPoint(long point) {
        this.rankpoint = point;
    }

    @Override
    public long getLastUpdate() {
        return lastupdate;
    }

    @Override
    public void setLastUpdte(long time) {
        this.lastupdate = time;
    }

    @Override
    public long getLastRankRise() {
        return this.lastrankrise;
    }

    @Override
    public void setLastRankRise(long time) {
        this.lastrankrise = time;
    }

    @Override
    public long getUnitCapacity() {
        return UnitCapacity;
    }

    @Override
    public float getRankPointModifier(DamageSource ds) {
        return 0.1f;
    }

    @Override
    public float getRankPointModifier(ResourceLocation combo) {
        return 0.1f;
    }

}
