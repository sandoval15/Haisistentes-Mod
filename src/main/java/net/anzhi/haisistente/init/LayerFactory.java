
package net.mcreator.haisistente.init;

import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import net.mcreator.haisistente.entity.HaisistenteAbstract;

@FunctionalInterface
public interface LayerFactory<T extends HaisistenteAbstract> {
    GeoRenderLayer<T> create(GeoEntityRenderer<T> renderer);
}
