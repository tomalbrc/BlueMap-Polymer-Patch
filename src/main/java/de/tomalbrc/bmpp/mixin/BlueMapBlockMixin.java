package de.tomalbrc.bmpp.mixin;

import com.mojang.logging.LogUtils;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.World;
import de.bluecolored.bluemap.core.world.block.Block;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Block.class)
public abstract class BlueMapBlockMixin<T extends Block<T>> {

    @Shadow private @Nullable BlockState blockState;

    @Inject(method = "getBlockState", at = @At("RETURN"), cancellable = true, remap = false)
    private void onGetBlockState(CallbackInfoReturnable<BlockState> cir) {
        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(blockState.getNamespace(), blockState.getValue());
        Map<String, String> propertyMap = blockState.getProperties();
        net.minecraft.world.level.block.Block block = BuiltInRegistries.BLOCK.get(resourceLocation);
        if (block instanceof PolymerBlock polymerBlock) {
            var mcState = block.defaultBlockState();
            for (Property prop: mcState.getProperties()) {
                if (propertyMap.containsKey(prop.getName())) {
                    mcState = mcState.setValue(prop, (Comparable) prop.getValue(propertyMap.get(prop.getName())).get());
                }
            }
            var realState = polymerBlock.getPolymerBlockState(mcState);
            Map<String, String> newPropertyMap = new Object2ObjectOpenHashMap<>();
            for (Property prop: realState.getProperties()) {
                newPropertyMap.put(prop.getName(), realState.getValue(prop).toString().toLowerCase());
            }

            this.blockState = new BlockState(BuiltInRegistries.BLOCK.getKey(realState.getBlock()).toString(), newPropertyMap);
            cir.setReturnValue(this.blockState);
        }
    }
}
