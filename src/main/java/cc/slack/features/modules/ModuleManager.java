// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.impl.combat.*;
import cc.slack.features.modules.impl.exploit.*;
import cc.slack.features.modules.impl.ghost.*;
import cc.slack.features.modules.impl.movement.*;
import cc.slack.features.modules.impl.other.*;
import cc.slack.features.modules.impl.player.*;
import cc.slack.features.modules.impl.render.*;
import cc.slack.features.modules.impl.utilties.*;
import cc.slack.features.modules.impl.utilties.Float;
import cc.slack.features.modules.impl.world.*;

public class ModuleManager {
    public final Map<Class<? extends Module>, Module> modules = new LinkedHashMap<>();
    public final Map<Class<? extends Module>, Module> draggable = new LinkedHashMap<>();

    public void initialize() {
        try {
            addModules(
                    // Combat
                    new KillAura(),
                    new Velocity(),
                    new AntiFireball(),
                    new Criticals(),
                    new Hitbox(),

                    // Exploit
                    new Disabler(),
                    new PingSpoof(),
                    new ClientSpoofer(),
                    new MultiAction(),
                    new Phase(),
                    new Regen(),

                    // Ghost
                    new Reach(),
                    new Autoclicker(),
                    new AimAssist(),
                    new AutoTool(),
                    new Backtrack(),
                    new JumpReset(),
                    new KeepSprint(),
                    new LegitNofall(),
                    new LegitScaffold(),
                    new RealLag(),
                    new SilentHitbox(),
                    new SilentScaffold(),
                    new Wtap(),

                    // Movement
                    new Sprint(),
                    new Speed(),
                    new NoSlow(),
                    new Flight(),
                    new InvMove(),
                    new Jesus(),
                    new LongJump(),
                    new SafeWalk(),
                    new Spider(),
                    new Step(),
                    new Strafe(),
                    new TargetStrafe(),
                    new CustomSpeed(),

                    // Other
                    new AntiBot(),
                    new Killsults(),
                    new Performance(),
                    new RemoveEffect(),
                    new RichPresence(),
                    new Targets(),
                    new Test(), // dev test shit
                    new Tweaks(),

                    // Player
                    new AntiVoid(),
                    new Blink(),
                    new FastEat(),
                    new FreeCam(),
                    new FreeLook(),
                    new MCF(),
                    new NoFall(),
                    new TimerModule(),

                    // Render
                    new Hud(),
                    new Animations(),
                    new BasicESP(),
                    new TargetHUD(),
                    new BedESP(),
                    new Camera(),
                    new Cape(),
                    new Chams(),
                    new ClickGUI(),
                    new ChestESP(),
                    new FPSCounter(),
                    new ItemPhysics(),
                    new NameTags(),
                    new Projectiles(),
                    new ScoreboardModule(),
                    new SessionInfo(),
                    new Ambience(),
                    new Tracers(),
                    new XRay(),
                    new Zoom(),
                    new XYZCounter(),
                    new KeyStrokes(),
                    new BPSCounter(),

                    // Utilities
                    new AutoDisable(),
                    new AutoGapple(),
                    new AutoGG(),
                    new AutoLogin(),
                    new AutoPlay(),
                    new AutoPot(),
                    new AutoSword(),
                    new AntiHarm(),
                    new AntiStaff(),
                    new LagbackChecker(),
                    new NameProtect(),
                    new TNTHelper(),
                    new Float(),

                    // World
                    new Scaffold(),
                    new Breaker(),
                    new InvManager(),
                    new Stealer(),
                    new ChestAura(),
                    new FastPlace(),
                    new SpeedMine()
            );

            for(Module m : modules.values()) {
                draggable.put(m.getClass(), m);
            }

        } catch (Exception e) {
            // Shut Up Exception
        }
    }

    public List<Module> getModules() {
        return new ArrayList<>(modules.values());
    }

    public List<Module> getDraggable() {
        return new ArrayList<>(draggable.values());
    }

    public <T extends Module> T getInstance(Class<T> clazz) {
        return (T) modules.get(clazz);
    }

    public Module getModuleByName(String name) {
        for (Module mod : modules.values()) {
            if (mod.getName().equalsIgnoreCase(name))
                return mod;
        }


        throw new IllegalArgumentException("Module not found.");
    }

    private void addModules(Module... mod) {
        for (Module m : mod) {

            modules.put(m.getClass(), m);
        }
    }

    public Module[] getModulesByCategory(Category cat) {
        ArrayList<Module> category = new ArrayList<>();

        modules.forEach((aClass, mod) -> {
            if (mod.category == cat)
                category.add(mod);
        });

        return category.toArray(new Module[0]);
    }
}
