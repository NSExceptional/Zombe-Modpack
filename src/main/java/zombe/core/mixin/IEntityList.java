package zombe.core.mixin;

import net.minecraft.entity.EntityList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(EntityList.class)
public interface IEntityList {

    /**
     * @deprecated This doesn't exist in forge
     */
    @Deprecated
    @SuppressWarnings("StaticMixinMember")
    @Accessor
    static List<String> getOLD_NAMES() {
        return null;
    }
}
