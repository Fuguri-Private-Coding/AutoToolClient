# AGENTS.md

## What this is
AutoTool — a Minecraft **1.8.9 ghost client** built on a **decompiled MCP workspace**. There is **no Gradle/Maven build**; the project compile/package flow lives in IntelliJ IDEA (`.iml` + `.idea/`), JDK 22 (corretto-22), Minecraft Dev plugin MCP facet (SRG).

All source is under `main/java`:
- `fuguriprivatecoding/autotoolrecode/**` — the client (modules, events, gui, settings, commands, utils).
- `net/minecraft/**` — decompiled vanilla Minecraft, **directly hand-patched** to fire client events. This is the hooking mechanism; there are no mixins/injection.
- `de/florianmichael/**` — bundled ViaMCP (ViaVersion/ViaBackwards/ViaRewind) protocol-translation stack for 1.8.9.

Dependencies are committed: `nat/1.8.9.jar` (deobf MC jar) + `nat/natives/*.dll`, and all `libs/*.jar`. `main/resources/` ships client assets (fonts, shaders, sounds, `msdf-gen.zip`, `smtc_bridge.dll`). Don't touch the `.iml` / `.idea/libraries` wiring or the artifact config.

## Build / run (no CLI build exists)
- Compile: IDEA build of module `AutoToolClient` (output `out/`). There is no lint/test tooling — the IDEA build is the only verification.
- Package: IDEA artifact `AutoToolClient:jar` → `out/artifacts/AutoToolClient_jar/AutoToolClient.jar` (fat jar = module output + all `libs/*.jar` + `nat/1.8.9.jar`, `Main-Class: Start`).
- Run: drop that jar into `jars/` (runtime dir with `assets/`, `natives/`, `ViaMCP/`, `AutoTool/`) and run `java -jar AutoToolClient.jar` from there. `Start` → `net.minecraft.client.main.Main` with `--assetsDir assets --assetIndex 1.8`.
- `jars/`, `out/`, `logs/`, `.idea/` are gitignored runtime/build dirs.

## Client bootstrap
`Client.init()` (`fuguriprivatecoding.autotoolrecode.Client`) wires everything: unpacks fonts/native dlls from resources to CWD-relative dirs, then `Modules.init()`, `Configs.init()`, the screen inits (`ConsoleScreen`, `ClickScreen`, `MainScreen`, `AltScreen`, ...), ViaMCP, etc. State persists to `AutoTool/configs/*.json` (module configs, saved on shutdown) and `AutoTool/binds/binds.json`. `Client.CLIENT_DIR = new File("AutoTool")` is relative to the run CWD.

## Adding a module (critical)
1. New class under `module/impl/<category>/`, annotated `@ModuleInfo(name=..., category=..., key=Keyboard.KEY_X, description=...)`, extends `Module`.
2. Settings are **public fields created in the constructor** — they self-register via `Setting`'s constructor. Patterns: `new FloatSetting("Name", this, min, max, def, step)`, `new IntegerSetting(...)`, `new CheckBox("Name", this)`, `new Mode("Name", this).addModes("A","B").setMode("A")`, `new ColorSetting(...)`. Optional `BooleanSupplier` overload gates visibility by other settings.
3. Register it in the **single vararg `register(...)` call in `Modules.init()`**. `Modules.register()` *replaces* the entire module list each call — never add a second call to it.
4. New chat commands go in `Commands.init()`.

## Event model
- Events fire from patched vanilla code via `new XEvent().call()` / `.call(onlyInWorld)` (default `true` = dispatch suppressed outside a loaded world). `Event.call()` resets `canceled`, then `Events.call(event, onlyInWorld)` invokes every subscriber method whose sole param is assignable from that event.
- In a module override `onEvent(Event event)` and `instanceof`-check. `Module.listen()` / `EventListener.listen()` is effectively dead code — the bus never consults it. Some events are singleton-mutable (`getInstance()` / `.INST`), others are `new`-constructed each fire (e.g. `PacketEvent`).
- `Module.toggle()` auto-registers/unregisters, so **modules only hear events while enabled**. Non-module always-on subscribers exist and must be constructed in `Client.init()`: `handle/` (`Clicks`, `Debl`, `Player`) and `PositionResolverComponent` call `Events.register(this)` in their ctors.
- Hooks are hand-edited into vanilla classes. Reference hook points: `Minecraft.run` (RunGameLoopEvent, TickEvent, KeyEvent), `NetworkManager.channelRead0`/`sendPacket` (PacketEvent; use `sendPacketNoEvent` to send without modules seeing it), `EntityPlayerSP.onUpdate`/`onUpdateWalkingPlayer` (UpdateEvent/MotionEvent), `EntityLivingBase.jump` (JumpEvent), `EntityRenderer` (ScreenEvent/Render2DEvent/Render3DEvent), `NetHandlerPlayClient` (ChatMessageEvent), `WorldClient` (WorldChangeEvent), `PlayerControllerMP`/`Entity` (attack/block damage events). To add a new hook, create the event under `event/events/<area>/` and fire it from the patched vanilla class.

## Conventions / gotchas
- Heavy Lombok (`@UtilityClass` for static-only classes — don't instantiate them, don't add ctors). `mc` (Minecraft) comes from the `Imports` interface.
- `newsetting/` (`Value`, `NumberValue`, `Configureable`, ...) is an **unwired experimental** settings framework — nothing outside that package references it. Always build on `setting/` (impls in `setting/impl/`).
- Russian comments and commit messages everywhere; the shipped `Test`/`Test2`/`TestRender` modules are intentional.
- KillAura ships an NN aim-assist (`utils/ai/NeuralNet`, `void-ai` lib); `Client.init()` auto-loads the model from `test123.json` in the run CWD if present.
- Config/binds serialization keys on `module.getName()` — rename a `@ModuleInfo` name and saved configs silently stop applying.