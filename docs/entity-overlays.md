# Overlays externos sobre entidades GeckoLib

Sistema genérico para que efectos visuales de otros mods (baba de slime, escarcha,
auras, etc.) se rendericen sobre nuestras entidades GeckoLib, que normalmente
quedan excluidas de ese tipo de efectos.

## El problema

Los mods que dibujan un efecto sobre el modelo de una entidad lo hacen con un
`RenderLayer` vanilla añadido a `LivingEntityRenderer`, normalmente en el evento
`EntityRenderersEvent.AddLayers`. Nuestro `HaisistenteRenderer` extiende
`GeoEntityRenderer` de GeckoLib, que hereda de `EntityRenderer` (no de
`LivingEntityRenderer`), así que:

1. `AddLayers.getRenderer()` castea a `LivingEntityRenderer` → con un renderer Geo
   lanza `ClassCastException` y el mod externo nunca registra su capa (los mods
   suelen tragarse la excepción en un try/catch, por eso no crashea, solo no se ve).
2. Aunque la capa se registrara, no funcionaría: esas capas re-renderizan el
   `EntityModel` vanilla del renderer padre (`getParentModel().renderToBuffer(...)`),
   y un renderer Geo no tiene `EntityModel` — usa `BakedGeoModel`.

Esto afecta a **todos** los mods con GeckoLib, no solo al nuestro.

### Caso real investigado: efecto "Slimed" de Supplementaries

El efecto de baba al lanzar una bola de slime **no es de Quark** (verificado en las
ramas 1.20–1.21 de su repo): es el efecto *Slimed* de **Supplementaries**
([MehVahdJukaar/Supplementaries](https://github.com/MehVahdJukaar/Supplementaries),
rama `master` = 1.20.1). Cómo funciona:

| Pieza | Implementación en Supplementaries |
|---|---|
| Proyectil | `SlimeBallEntity.onHitEntity` llama `supp$setSlimedTicks(duración, true)` |
| Estado | Mixin sobre `LivingEntity` (interfaz `ISlimeable`): campo `supp$slimedTicks`, NBT `supplementaries:slimed_ticks`, sync con packet propio `ClientBoundSyncSlimedMessage` |
| Alpha | `ISlimeable.getAlpha`: opacidad 1 mientras ticks > 70, fade lineal en los últimos 70 ticks; se limpia bajo el agua o con jabón |
| Render | `SlimedLayer` (patrón del aura del creeper cargado): re-renderiza el modelo padre con `textures/entity/slime_overlay.png` translúcida, scroll vertical con período de 400 s, depth test `EQUAL` |
| Registro | `SupplementariesForgeClient.onAddLayers` itera todas las entidades vivientes → aquí falla con nuestros renderers (issue [#1216](https://github.com/MehVahdJukaar/Supplementaries/issues/1216)) |

## La solución implementada (genérica)

Equivalente Geo del patrón "re-renderizar el modelo con otra textura", en
`net.anzhi.haisistente.client.overlay`:

```
EntityOverlayProvider   → contrato de un efecto: getAlpha() + getRenderType()
EntityOverlayRegistry   → lista de proveedores activos (registro en client setup)
EntityOverlayGeoLayer   → GeoRenderLayer que itera los proveedores y re-dibuja el
                          BakedGeoModel ya poseado con GeoRenderer.reRender(...)
```

La capa está añadida al final del constructor de `HaisistenteRenderer`, de modo que
los overlays se dibujan encima de las capas de outfit/item y siguen exactamente la
animación del modelo. Con el registro vacío la capa es un no-op sin costo.

Ventajas frente a las alternativas evaluadas:

- Solo usa API pública y estable de GeckoLib (`GeoRenderLayer`, `reRender`) — no
  reimplementa internals que cambian entre versiones (a diferencia de un renderer
  híbrido que extienda `LivingEntityRenderer`).
- Cada efecto externo es un proveedor aislado de ~60 líneas que puede
  desactivarse solo si el mod externo cambia, sin tumbar el render.

## Cómo añadir un overlay nuevo (transformación paso a paso)

1. **Investigar el mod externo**: localizar dónde guarda el estado del efecto
   (mixin/capability/NBT/SynchedEntityData) y con qué `RenderType`/textura lo dibuja.
2. **Crear el proveedor** en `client/compat/`: implementar `EntityOverlayProvider`
   replicando la curva de alpha y el render type del mod original. Si el estado se
   expone vía mixin (lo más común), leerlo por reflexión con un `MethodHandle`
   cacheado → dependencia suave, sin tocar `build.gradle`.
3. **Registrarlo** en `FMLClientSetupEvent`, condicionado a `ModList.get().isLoaded(...)`.
4. Probar in-game con el mod instalado (aplicar el efecto y verificar overlay + fade).

## Ejemplo completo listo para implementar: Slimed (Supplementaries)

`src/main/java/net/anzhi/haisistente/client/compat/SlimedOverlayProvider.java`:

```java
package net.anzhi.haisistente.client.compat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;

import net.anzhi.haisistente.HaisistenteMod;
import net.anzhi.haisistente.client.overlay.EntityOverlayProvider;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;

/**
 * Replicates Supplementaries' "Slimed" goo overlay for our GeckoLib entities.
 * Soft dependency: reads the mixin-added supp$slimedTicks field on LivingEntity
 * through a cached MethodHandle; if the accessor is missing, the compat stays off.
 */
public final class SlimedOverlayProvider implements EntityOverlayProvider {
    private static final ResourceLocation SLIME_OVERLAY_TEXTURE =
            new ResourceLocation("supplementaries", "textures/entity/slime_overlay.png");
    /** Supplementaries fades the overlay out over the last 70 ticks. */
    private static final float FADE_TICKS = 70f;
    /** Supplementaries scrolls the goo texture downward over a 400-second period. */
    private static final float SCROLL_PERIOD_TICKS = 8000f;

    private final MethodHandle getSlimedTicks;

    private SlimedOverlayProvider(MethodHandle getSlimedTicks) {
        this.getSlimedTicks = getSlimedTicks;
    }

    /** Empty if Supplementaries is not loaded or its slimed-ticks accessor cannot be resolved. */
    public static Optional<SlimedOverlayProvider> create() {
        if (!ModList.get().isLoaded("supplementaries"))
            return Optional.empty();

        Method accessor = findSlimedTicksAccessor();
        if (accessor == null) {
            HaisistenteMod.LOGGER.warn(
                    "Supplementaries is loaded but its slimed-ticks accessor was not found; slime overlay compat disabled");
            return Optional.empty();
        }
        try {
            return Optional.of(new SlimedOverlayProvider(MethodHandles.publicLookup().unreflect(accessor)));
        } catch (IllegalAccessException e) {
            HaisistenteMod.LOGGER.warn(
                    "Could not access Supplementaries slimed-ticks accessor; slime overlay compat disabled", e);
            return Optional.empty();
        }
    }

    private static Method findSlimedTicksAccessor() {
        try {
            return LivingEntity.class.getMethod("supp$getSlimedTicks");
        } catch (NoSuchMethodException e) {
            // Fallback in case the mixin prefix changes between versions
            for (Method method : LivingEntity.class.getMethods()) {
                if (method.getParameterCount() == 0 && method.getReturnType() == int.class
                        && method.getName().toLowerCase(Locale.ROOT).contains("slimedticks")) {
                    return method;
                }
            }
            return null;
        }
    }

    @Override
    public float getAlpha(LivingEntity entity, float partialTick) {
        int ticks = slimedTicks(entity);
        if (ticks <= 0)
            return 0f;
        return Math.min(1f, (ticks - partialTick) / FADE_TICKS);
    }

    @Override
    public RenderType getRenderType(LivingEntity entity, float partialTick) {
        float scroll = ((entity.level().getGameTime() + partialTick) % SCROLL_PERIOD_TICKS) / SCROLL_PERIOD_TICKS;
        // energySwirl scrolls the texture via the texturing matrix, like Supplementaries' custom shader
        return RenderType.energySwirl(SLIME_OVERLAY_TEXTURE, 0f, scroll);
    }

    private int slimedTicks(LivingEntity entity) {
        try {
            return (int) getSlimedTicks.invoke(entity);
        } catch (Throwable t) {
            return 0;
        }
    }
}
```

Registro — `src/main/java/net/anzhi/haisistente/init/HaisistenteOverlays.java`:

```java
package net.anzhi.haisistente.init;

import net.anzhi.haisistente.HaisistenteMod;
import net.anzhi.haisistente.client.compat.SlimedOverlayProvider;
import net.anzhi.haisistente.client.overlay.EntityOverlayRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = HaisistenteMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HaisistenteOverlays {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                SlimedOverlayProvider.create().ifPresent(EntityOverlayRegistry::register));
    }
}
```

Notas para cuando se implemente:

- Verificar in-game el nombre exacto del accessor del mixin (`supp$getSlimedTicks`);
  el fallback por nombre cubre cambios de prefijo, pero conviene confirmarlo.
- Si el alpha no se respeta con `RenderType.energySwirl` (su shader podría ignorar
  el alpha del vértice según versión), copiar el render type de Supplementaries
  (`SlimedRenderTypes`: `NEW_ENTITY` + `TRANSLUCENT_TRANSPARENCY` + `EQUAL_DEPTH_TEST`).
- El overlay se aplica al modelo base; las capas de outfit son modelos Geo aparte y
  no lo reciben (mismo comportamiento que vanilla, donde la armadura tampoco).

## Extensión futura: cubrir todas las entidades GeckoLib del modpack

GeckoLib 4.4.2 dispara `GeoRenderEvent.Entity.CompileRenderLayers` (bus de Forge)
una vez por renderer. Para inyectar la capa también en mobs GeckoLib de otros mods:

```java
@SubscribeEvent
public static void onCompileGeoLayers(GeoRenderEvent.Entity.CompileRenderLayers event) {
    if (event.getRenderer().getAnimatable() instanceof LivingEntity)
        event.addLayer(new EntityOverlayGeoLayer<>(event.getRenderer()));
}
```

(En ese caso, quitar el `addRenderLayer` del constructor de `HaisistenteRenderer`
para no duplicar la capa.)
