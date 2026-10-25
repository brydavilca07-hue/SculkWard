package com.sculkward;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Bonus del set completo + habilidad Sonic Boom de la espada.
 */
public class SetBonusListener implements Listener {

    private final SculkWardPlugin plugin;
    private final ItemManager items;

    private final Map<UUID, Long> sonicCooldowns = new HashMap<>();
    private final Set<UUID> wearing = new HashSet<>();
    private BukkitTask task;

    public SetBonusListener(SculkWardPlugin plugin, ItemManager items) {
        this.plugin = plugin;
        this.items = items;
    }

    // ------------------------------------------------------------------
    // Efectos del set completo (se revisan cada segundo)
    // ------------------------------------------------------------------

    public void startTask() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tick() {
        FileConfiguration cfg = plugin.getConfig();
        boolean enabled = cfg.getBoolean("set-bonus.enabled", true);
        int resistance = cfg.getInt("set-bonus.resistance-level", 1);
        boolean nightVision = cfg.getBoolean("set-bonus.night-vision", true);
        boolean darkness = cfg.getBoolean("set-bonus.darkness-immunity", true);

        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean full = enabled && items.hasFullSet(player);

            if (full) {
                wearing.add(player.getUniqueId());
                if (resistance > 0) {
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.DAMAGE_RESISTANCE, 60, resistance - 1, true, false, true));
                }
                if (nightVision) {
                    // Más de 200 ticks para que no parpadee al final.
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.NIGHT_VISION, 260, 0, true, false, true));
                }
                if (darkness) {
                    player.removePotionEffect(PotionEffectType.DARKNESS);
                }
            } else if (wearing.remove(player.getUniqueId())) {
                // Se quitó una pieza: limpiar la visión nocturna que dio el set.
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
        }
    }

    @EventHandler
    public void onPotion(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!plugin.getConfig().getBoolean("set-bonus.darkness-immunity", true)) {
            return;
        }
        PotionEffect newEffect = event.getNewEffect();
        if (newEffect == null || !newEffect.getType().equals(PotionEffectType.DARKNESS)) {
            return;
        }
        if (plugin.getConfig().getBoolean("set-bonus.enabled", true) && items.hasFullSet(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWardenTarget(EntityTargetLivingEntityEvent event) {
        if (!plugin.getConfig().getBoolean("set-bonus.warden-ignores-wearer", true)) {
            return;
        }
        if (!(event.getEntity() instanceof Warden)) {
            return;
        }
        if (event.getTarget() instanceof Player player
                && plugin.getConfig().getBoolean("set-bonus.enabled", true)
                && items.hasFullSet(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Que las recetas aparezcan en el libro de recetas.
        event.getPlayer().discoverRecipes(items.getRecipeKeys());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        wearing.remove(event.getPlayer().getUniqueId());
    }

    // ------------------------------------------------------------------
    // Sonic Boom
    // ------------------------------------------------------------------

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (items.identify(player.getInventory().getItemInMainHand()) != SculkItem.SWORD) {
            return;
        }

        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.getBoolean("sonic-boom.enabled", true)) {
            return;
        }
        if (cfg.getBoolean("sonic-boom.require-sneak", true) && !player.isSneaking()) {
            return;
        }

        event.setCancelled(true);

        if (cfg.getBoolean("sonic-boom.require-full-set", true) && !items.hasFullSet(player)) {
            player.sendActionBar(plugin.bar("needs-set"));
            return;
        }

        long now = System.currentTimeMillis();
        long cooldownMs = (long) (cfg.getDouble("sonic-boom.cooldown-seconds", 15) * 1000);
        long readyAt = sonicCooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (now < readyAt) {
            long seconds = (readyAt - now + 999) / 1000;
            player.sendActionBar(plugin.bar("cooldown", "seconds", String.valueOf(seconds)));
            return;
        }

        sonicCooldowns.put(player.getUniqueId(), now + cooldownMs);
        fireSonicBoom(player);
    }

    private void fireSonicBoom(Player player) {
        FileConfiguration cfg = plugin.getConfig();
        double range = cfg.getDouble("sonic-boom.range", 16);
        double damage = cfg.getDouble("sonic-boom.damage", 10.0);
        double knockback = cfg.getDouble("sonic-boom.knockback", 1.5);

        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        World world = player.getWorld();

        // El rayo se detiene en el primer bloque sólido.
        double maxDistance = range;
        RayTraceResult blockHit = world.rayTraceBlocks(eye, direction, range, FluidCollisionMode.NEVER, true);
        if (blockHit != null) {
            maxDistance = blockHit.getHitPosition().distance(eye.toVector());
        }

        RayTraceResult entityHit = world.rayTraceEntities(eye, direction, maxDistance, 0.6,
                entity -> entity instanceof LivingEntity
                        && !(entity instanceof ArmorStand)
                        && !entity.getUniqueId().equals(player.getUniqueId()));

        world.playSound(eye, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 1.0f);

        double drawUntil = maxDistance;
        if (entityHit != null && entityHit.getHitEntity() != null) {
            drawUntil = Math.min(maxDistance,
                    entityHit.getHitPosition().distance(eye.toVector()));
        }
        for (double d = 1.0; d <= drawUntil; d += 1.0) {
            Location point = eye.clone().add(direction.clone().multiply(d));
            world.spawnParticle(Particle.SONIC_BOOM, point, 1, 0, 0, 0, 0);
        }

        if (entityHit != null && entityHit.getHitEntity() instanceof LivingEntity target) {
            target.damage(damage, player);
            Vector push = direction.clone().multiply(knockback);
            push.setY(0.4);
            target.setVelocity(push);
            world.playSound(target.getLocation(), Sound.ENTITY_WARDEN_ATTACK_IMPACT, 1.0f, 1.0f);
        }
    }
}
