package me.drake.viecheats.modules.render.hud.elements;

import java.util.Optional;
import me.drake.viecheats.mixin.MixinMobSpawnerLogicAccessor;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.spawner.MobSpawnerEntry;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Purified SpawnersHUD helper demonstrating the fixed MixinMobSpawnerLogicAccessor cast.
 * In the original obfuscated JAR, readSpawnerType erroneously cast MobSpawnerLogic to
 * me/drake/viecheats/hook/n (MixinClientPlayNetworkHandler) instead of _n (MixinMobSpawnerLogicAccessor),
 * causing a ClassCastException and falling back to slow reflection.
 */
public class SpawnersHUD {
    public static String readSpawnerType(MobSpawnerBlockEntity spawner) {
        try {
            MobSpawnerLogic logic = spawner.getLogic();
            if (logic instanceof MixinMobSpawnerLogicAccessor accessor) {
                MobSpawnerEntry entry = accessor.getSpawnEntry();
                if (entry != null) {
                    NbtCompound entityNbt = entry.getNbt();
                    if (entityNbt != null && entityNbt.contains("id")) {
                        String id = entityNbt.getString("id");
                        Optional<EntityType<?>> typeOpt = Registries.ENTITY_TYPE.getOptionalValue(Identifier.tryParse(id));
                        if (typeOpt.isPresent()) {
                            return typeOpt.get().getName().getString();
                        }
                        return id.replace("minecraft:", "");
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "Empty";
    }
}
