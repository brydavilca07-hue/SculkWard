# Sculk Ward

Plugin de armadura y herramientas inspiradas en el Warden para **Paper 1.20.1** (Java 17).

## Qué incluye

| Pieza | ID | Base |
|---|---|---|
| Casco Sculk Ward | `helmet` | Netherite |
| Peto Sculk Ward | `chestplate` | Netherite |
| Pantalones Sculk Ward | `leggings` | Netherite |
| Botas Sculk Ward | `boots` | Netherite |
| Espada Sculk Ward | `sword` | Netherite |
| Pico Sculk Ward | `pickaxe` | Netherite |
| Hacha Sculk Ward | `axe` | Netherite |
| Pala Sculk Ward | `shovel` | Netherite |

**Bonus del set completo** (las 4 piezas puestas): Resistencia I, Visión nocturna, inmunidad al efecto Oscuridad y los Warden no te eligen como objetivo (mejor esfuerzo).

**Sonic Boom**: con la espada, agáchate + clic derecho. Necesita el set completo, hace 10 de daño (5 corazones), alcanza 16 bloques y tiene 15 s de enfriamiento.

Las piezas tienen la durabilidad y el encantamiento de netherite, y mantienen su resistencia al fuego.

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

**Armadura puesta**: en 1.20.1 Minecraft no permite texturas personalizadas de armadura sin un mod en el cliente. El pack incluye las texturas para **OptiFine** o **CIT Resewn** (Fabric). Sin ninguno de los dos, el jugador verá la armadura de netherite normal puesta, pero con el ícono Sculk Ward en el inventario.

## Configuración

Todo se ajusta en `plugins/SculkWard/config.yml`: puntos de armadura, dureza, bonus del set, daño, alcance y enfriamiento del Sonic Boom, y los mensajes. Usa `/sculkward reload` para aplicar los cambios.

Las piezas ya entregadas conservan los puntos de armadura con los que se crearon. Para aplicar valores nuevos hay que volver a darlas.
