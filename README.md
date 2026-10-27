# Sculk Ward

Plugin de armadura y herramientas inspiradas en el Warden para **Paper 1.20.1** (Java 17).

## Qué incluye

| Pieza | ID | Base |
|---|---|---|
| Casco Sculk Ward | `helmet` | Cuero blanco |
| Peto Sculk Ward | `chestplate` | Cuero blanco |
| Pantalones Sculk Ward | `leggings` | Cuero blanco |
| Botas Sculk Ward | `boots` | Cuero blanco |
| Espada Sculk Ward | `sword` | Netherite |
| Pico Sculk Ward | `pickaxe` | Netherite |
| Hacha Sculk Ward | `axe` | Netherite |
| Pala Sculk Ward | `shovel` | Netherite |

**Bonus del set completo** (las 4 piezas puestas): Resistencia I, Visión nocturna, inmunidad al efecto Oscuridad y los Warden no te eligen como objetivo (mejor esfuerzo).

**Sonic Boom**: con la espada, agáchate + clic derecho. Necesita el set completo, hace 10 de daño (5 corazones), alcanza 16 bloques y tiene 15 s de enfriamiento.

Las herramientas son de netherite. La armadura da los mismos puntos que netherite (configurable).

## Comandos

Permiso: `sculkward.admin` (por defecto solo OP). Alias: `/sw`.

```
/sculkward give <jugador> <ítem|set|tools|all> [cantidad]
/sculkward list
/sculkward reload
```

Ejemplos: `/sw give Steve set`, `/sw give Steve sword`, `/sw give Steve all`.

## Recetas

`N` = lingote de netherite, `E` = fragmento de eco, `S` = palo.

| Pieza | Forma |
|---|---|
| Casco | `NEN` / `E E` |
| Peto | `E E` / `NEN` / `ENE` |
| Pantalones | `NEN` / `E E` / `N N` |
| Botas | `E E` / `N N` |
| Espada | ` E ` / ` N ` / ` S ` |
| Pico | `ENE` / ` S ` / ` S ` |
| Hacha | `EN` / `ES` / ` S` |
| Pala | `E` / `N` / `S` |

Se pueden desactivar en `config.yml` (`recipes.enabled: false`) y dar las piezas solo por comando.

## Compilar

Necesitas Java 17 y Maven:

```
mvn package
```

El plugin queda en `target/SculkWard-1.0.0.jar`. Cópialo a la carpeta `plugins/` de tu servidor y reinícialo.

**Sin instalar nada:** sube esta carpeta a un repositorio de GitHub. El workflow de `.github/workflows/build.yml` compila solo y deja el `.jar` en la pestaña *Actions → Artifacts*.

## Resource pack

El plugin usa `CustomModelData` (1001 a 1008). Instala `SculkWard-ResourcePack.zip`:

- **Servidor**: en `server.properties` pon `resource-pack=<enlace directo al zip>` y `resource-pack-sha1=<sha1 del zip>`.
- **Jugador**: copia el zip a `.minecraft/resourcepacks` y actívalo.

**Íconos** (inventario, mano y suelo) funcionan en Minecraft vanilla.

**Armadura puesta**: la armadura está hecha sobre **cuero teñido de blanco** y el pack cambia la textura de las capas de cuero. Así **todos los jugadores la ven con Minecraft normal**, sin OptiFine ni mods; solo tienen que aceptar el resource pack del servidor.

Detalles de este diseño:
- Toda armadura de cuero (también la normal) se verá con este diseño mientras el pack esté activo. Si tu servidor usa mucha armadura de cuero vanilla, avísame.
- El cuero dura poco, así que el plugin multiplica su durabilidad (`armor.durability-multiplier`, por defecto 7).
- No se puede teñir ni lavar en el caldero (se perdería el diseño).
- Si cambias el pack, cambia su nombre o define `resource-pack-sha1` en `server.properties`, porque si no los jugadores no lo vuelven a descargar.

## Configuración

Todo se ajusta en `plugins/SculkWard/config.yml`: puntos de armadura, dureza, bonus del set, daño, alcance y enfriamiento del Sonic Boom, y los mensajes. Usa `/sculkward reload` para aplicar los cambios.

Las piezas ya entregadas conservan los puntos de armadura con los que se crearon. Para aplicar valores nuevos hay que volver a darlas.
