import jdk.internal.org.objectweb.asm.*;
import jdk.internal.org.objectweb.asm.tree.*;
import jdk.internal.org.objectweb.asm.commons.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

/**
 * Custom Automated ASM Deobfuscation, Constant Purification, Decoy Eradication,
 * and Semantic Mixin Remapping Tool for VieCheats (VieCrackByLowzii.jar).
 *
 * Execute via:
 *   java --add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED \
 *        --add-exports java.base/jdk.internal.org.objectweb.asm.tree=ALL-UNNAMED \
 *        --add-exports java.base/jdk.internal.org.objectweb.asm.commons=ALL-UNNAMED \
 *        VieCheatsDeobfuscator <input.jar> <output.jar>
 */
public class VieCheatsDeobfuscator {

    // Complete semantic class remapping table for obfuscated Mixin hooks (me/drake/viecheats/hook/* -> me/drake/viecheats/mixin/*)
    public static final Map<String, String> CLASS_MAP = new LinkedHashMap<>();

    // Decoy, dead stub, and unused preLaunch loader classes to completely eradicate
    public static final Set<String> ERADICATED_CLASSES = new HashSet<>(Arrays.asList(
        "me/drake/viecheats/entry/EntryPoint.class",
        "me/drake/viecheats/entry/EntryPoint$1ClassEntry.class",
        "me/drake/viecheats/entry/EntryPoint$ClassDefiner.class",
        "me/drake/viecheats/entry/SecurityWiper.class",
        "me/drake/viecheats/security/CoreTransformer.class",
        "me/drake/viecheats/hook/c.class",
        "me/drake/viecheats/hook/y.class",
        // Pre-existing duplicates in mixin/ that are superseded by remapped hook/ classes
        "me/drake/viecheats/mixin/MixinChunkOcclusionDataBuilder.class",
        "me/drake/viecheats/mixin/MixinFrustum.class",
        "me/drake/viecheats/mixin/MixinWorldHooks.class",
        "me/drake/viecheats/mixin/MixinWorldRenderer.class"
    ));

    // Obfuscated/native binary resources to strip
    public static final Set<String> ERADICATED_RESOURCES = new HashSet<>(Arrays.asList(
        "resources/data.bin",
        "resources/data-arm32.bin",
        "resources/data-arm64.bin",
        "vie_client.json"
    ));

    static {
        CLASS_MAP.put("me/drake/viecheats/hook/_m", "me/drake/viecheats/mixin/MixinBlockEntityTickHooks");
        CLASS_MAP.put("me/drake/viecheats/hook/_n", "me/drake/viecheats/mixin/MixinMobSpawnerLogicAccessor");
        CLASS_MAP.put("me/drake/viecheats/hook/_o", "me/drake/viecheats/mixin/MixinMouse");
        CLASS_MAP.put("me/drake/viecheats/hook/_p", "me/drake/viecheats/mixin/MixinPlayerEntity");
        CLASS_MAP.put("me/drake/viecheats/hook/_q", "me/drake/viecheats/mixin/MixinPlayerListHudFakeStats");
        CLASS_MAP.put("me/drake/viecheats/hook/_r", "me/drake/viecheats/mixin/MixinSystemDetails");
        CLASS_MAP.put("me/drake/viecheats/hook/_s", "me/drake/viecheats/mixin/MixinTitleScreen");
        CLASS_MAP.put("me/drake/viecheats/hook/_t", "me/drake/viecheats/mixin/MixinWorldHooks");
        CLASS_MAP.put("me/drake/viecheats/hook/_u", "me/drake/viecheats/mixin/MixinWorldRenderer");
        CLASS_MAP.put("me/drake/viecheats/hook/a", "me/drake/viecheats/mixin/MixinAbstractClientPlayerEntity");
        CLASS_MAP.put("me/drake/viecheats/hook/ac", "me/drake/viecheats/mixin/MixinClientPlayerInteractionManagerAccessor");
        CLASS_MAP.put("me/drake/viecheats/hook/b", "me/drake/viecheats/mixin/MixinBlockEntity");
        CLASS_MAP.put("me/drake/viecheats/hook/d", "me/drake/viecheats/mixin/MixinCamera");
        CLASS_MAP.put("me/drake/viecheats/hook/e", "me/drake/viecheats/mixin/MixinChatHud");
        CLASS_MAP.put("me/drake/viecheats/hook/f", "me/drake/viecheats/mixin/MixinChunkLightProvider");
        CLASS_MAP.put("me/drake/viecheats/hook/g", "me/drake/viecheats/mixin/MixinChunkOcclusionDataBuilder");
        CLASS_MAP.put("me/drake/viecheats/hook/h", "me/drake/viecheats/mixin/MixinWorldChunk");
        CLASS_MAP.put("me/drake/viecheats/hook/i", "me/drake/viecheats/mixin/MixinChunkBuilder");
        CLASS_MAP.put("me/drake/viecheats/hook/j", "me/drake/viecheats/mixin/MixinClientChunkManager");
        CLASS_MAP.put("me/drake/viecheats/hook/k", "me/drake/viecheats/mixin/MixinClientConnection");
        CLASS_MAP.put("me/drake/viecheats/hook/l", "me/drake/viecheats/mixin/MixinClientPlayerEntity");
        CLASS_MAP.put("me/drake/viecheats/hook/m", "me/drake/viecheats/mixin/MixinMinecraftClient");
        CLASS_MAP.put("me/drake/viecheats/hook/n", "me/drake/viecheats/mixin/MixinClientPlayNetworkHandler");
        CLASS_MAP.put("me/drake/viecheats/hook/o", "me/drake/viecheats/mixin/MixinClientWorld");
        CLASS_MAP.put("me/drake/viecheats/hook/p", "me/drake/viecheats/mixin/MixinEntity");
        CLASS_MAP.put("me/drake/viecheats/hook/q", "me/drake/viecheats/mixin/MixinFrustum");
        CLASS_MAP.put("me/drake/viecheats/hook/r", "me/drake/viecheats/mixin/MixinGameOptions");
        CLASS_MAP.put("me/drake/viecheats/hook/s", "me/drake/viecheats/mixin/MixinGameRenderer");
        CLASS_MAP.put("me/drake/viecheats/hook/t", "me/drake/viecheats/mixin/MixinClientPlayNetworkHandlerChat");
        CLASS_MAP.put("me/drake/viecheats/hook/u", "me/drake/viecheats/mixin/MixinInGameHud");
        CLASS_MAP.put("me/drake/viecheats/hook/v", "me/drake/viecheats/mixin/MixinInGameHudFakeStats");
        CLASS_MAP.put("me/drake/viecheats/hook/w", "me/drake/viecheats/mixin/MixinKeyboard");
        CLASS_MAP.put("me/drake/viecheats/hook/x", "me/drake/viecheats/mixin/MixinLightingProvider");
        CLASS_MAP.put("me/drake/viecheats/hook/z", "me/drake/viecheats/mixin/MixinLivingEntity");
    }

    /**
     * Purifies `ChimeraEngine` by removing the dead `OBFUSCATED_FALLBACK_KEY` byte array
     * and `XOR_KEY` (`0x7F`) field initialization from `<clinit>` while preserving the
     * plain inlined fallback key literal (`"Chimera_Protect_Discord_xduyhoangg_2026"`).
     */
    public static String purifyChimeraEngine(ClassNode cn) {
        String inlinedKey = null;
        for (MethodNode mn : cn.methods) {
            if (mn.name.equals("decodeFallbackKey")) {
                for (AbstractInsnNode insn : mn.instructions) {
                    if (insn instanceof LdcInsnNode ldc && ldc.cst instanceof String s) {
                        inlinedKey = s;
                    }
                }
            }
        }

        // Remove dead OBFUSCATED_FALLBACK_KEY and XOR_KEY fields
        cn.fields.removeIf(fn -> fn.name.equals("OBFUSCATED_FALLBACK_KEY") || fn.name.equals("XOR_KEY"));

        // Strip OBFUSCATED_FALLBACK_KEY array allocation from <clinit>
        for (MethodNode mn : cn.methods) {
            if (mn.name.equals("<clinit>")) {
                AbstractInsnNode start = null;
                AbstractInsnNode end = null;
                for (AbstractInsnNode insn : mn.instructions) {
                    if (start == null && insn instanceof IntInsnNode iin && iin.getOpcode() == Opcodes.BIPUSH && iin.operand == 36
                            && insn.getNext() instanceof IntInsnNode arr && arr.getOpcode() == Opcodes.NEWARRAY) {
                        start = insn;
                    }
                    if (start != null && insn instanceof FieldInsnNode fin
                            && fin.getOpcode() == Opcodes.PUTSTATIC
                            && fin.name.equals("OBFUSCATED_FALLBACK_KEY")) {
                        end = insn;
                        break;
                    }
                }
                if (start != null && end != null) {
                    AbstractInsnNode cur = start;
                    while (cur != null) {
                        AbstractInsnNode next = cur.getNext();
                        mn.instructions.remove(cur);
                        if (cur == end) break;
                        cur = next;
                    }
                }
            }
        }
        return inlinedKey;
    }

    /**
     * Fixes the broken obfuscated reference in SpawnersHUD.readSpawnerType where
     * `me/drake/viecheats/hook/n` was referenced instead of `me/drake/viecheats/hook/_n`
     * (`MixinMobSpawnerLogicAccessor`).
     */
    public static void fixSpawnersHudAccessorCast(ClassNode cn) {
        if (!cn.name.equals("me/drake/viecheats/modules/render/SpawnersHUD")) {
            return;
        }
        for (MethodNode mn : cn.methods) {
            for (AbstractInsnNode insn : mn.instructions) {
                if (insn instanceof TypeInsnNode tin && tin.getOpcode() == Opcodes.CHECKCAST
                        && tin.desc.equals("me/drake/viecheats/hook/n")) {
                    tin.desc = "me/drake/viecheats/hook/_n";
                } else if (insn instanceof MethodInsnNode min
                        && min.owner.equals("me/drake/viecheats/hook/n")
                        && min.name.equals("getSpawnEntry")) {
                    min.owner = "me/drake/viecheats/hook/_n";
                }
            }
        }
    }

    public static void deobfuscateJar(Path inputJar, Path outputJar) throws Exception {
        Remapper remapper = new SimpleRemapper(CLASS_MAP);
        String inlinedChimeraKey = null;

        try (JarFile jar = new JarFile(inputJar.toFile());
             JarOutputStream jos = new JarOutputStream(Files.newOutputStream(outputJar))) {

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (ERADICATED_CLASSES.contains(name) || ERADICATED_RESOURCES.contains(name)) {
                    continue;
                }

                byte[] rawBytes;
                try (InputStream is = jar.getInputStream(entry)) {
                    rawBytes = is.readAllBytes();
                }

                if (name.endsWith(".class")) {
                    byte origMajor = rawBytes[7];
                    if (origMajor > 61) {
                        rawBytes[7] = 61;
                    }
                    ClassReader cr = new ClassReader(rawBytes);
                    ClassNode cn = new ClassNode();
                    cr.accept(cn, 0);

                    if (cn.name.equals("me/drake/viecheats/security/ChimeraEngine")) {
                        inlinedChimeraKey = purifyChimeraEngine(cn);
                    }
                    fixSpawnersHudAccessorCast(cn);

                    ClassNode remappedNode = new ClassNode();
                    ClassRemapper classRemapper = new ClassRemapper(remappedNode, remapper);
                    cn.accept(classRemapper);

                    // Update SourceFile debug attribute if class was remapped from hook/*
                    if (CLASS_MAP.containsKey(cn.name)) {
                        String simpleName = remappedNode.name.substring(remappedNode.name.lastIndexOf('/') + 1);
                        remappedNode.sourceFile = simpleName + ".java";
                    }

                    ClassWriter cw = new ClassWriter(0);
                    remappedNode.accept(cw);
                    byte[] outBytes = cw.toByteArray();
                    outBytes[7] = origMajor;

                    String newEntryName = remappedNode.name + ".class";
                    JarEntry outEntry = new JarEntry(newEntryName);
                    jos.putNextEntry(outEntry);
                    jos.write(outBytes);
                    jos.closeEntry();
                } else if (name.equals("fabric.mod.json")) {
                    String cleanedFabricJson = """
                        {
                          "schemaVersion": 1,
                          "id": "vie_cheats",
                          "version": "1.0.0",
                          "name": "Vie Cheats",
                          "description": "Vie Cheats — Ultimate DonutSMP Utility Client by Drake",
                          "authors": ["Drake"],
                          "contact": {},
                          "license": "MIT",
                          "icon": "assets/vie_cheats/icon.png",
                          "environment": "client",
                          "entrypoints": {
                            "client": ["me.drake.viecheats.VieCheatsMod"]
                          },
                          "mixins": ["mixins.vie_cheats.json"],
                          "depends": {
                            "fabricloader": ">=0.16.0",
                            "minecraft": ">=1.21.4",
                            "java": ">=21",
                            "fabric-api": "*"
                          }
                        }
                        """;
                    JarEntry outEntry = new JarEntry(name);
                    jos.putNextEntry(outEntry);
                    jos.write(cleanedFabricJson.getBytes(StandardCharsets.UTF_8));
                    jos.closeEntry();
                } else if (name.equals("mixins.vie_cheats.json")) {
                    StringBuilder mixinArray = new StringBuilder();
                    List<String> mixinSimpleNames = new ArrayList<>();
                    for (String targetInternal : CLASS_MAP.values()) {
                        mixinSimpleNames.add(targetInternal.substring(targetInternal.lastIndexOf('/') + 1));
                    }
                    Collections.sort(mixinSimpleNames);
                    for (int i = 0; i < mixinSimpleNames.size(); i++) {
                        mixinArray.append("    \"").append(mixinSimpleNames.get(i)).append("\"");
                        if (i + 1 < mixinSimpleNames.size()) mixinArray.append(",");
                        mixinArray.append("\n");
                    }
                    String cleanedMixinsJson = "{\n"
                        + "  \"required\": true,\n"
                        + "  \"package\": \"me.drake.viecheats.mixin\",\n"
                        + "  \"compatibilityLevel\": \"JAVA_21\",\n"
                        + "  \"client\": [\n"
                        + mixinArray
                        + "  ],\n"
                        + "  \"injectors\": {\n"
                        + "    \"defaultRequire\": 1\n"
                        + "  }\n"
                        + "}\n";
                    JarEntry outEntry = new JarEntry(name);
                    jos.putNextEntry(outEntry);
                    jos.write(cleanedMixinsJson.getBytes(StandardCharsets.UTF_8));
                    jos.closeEntry();
                } else {
                    JarEntry outEntry = new JarEntry(name);
                    jos.putNextEntry(outEntry);
                    jos.write(rawBytes);
                    jos.closeEntry();
                }
            }
        }
        System.out.println("Verified inlined ChimeraEngine fallback key: \"" + inlinedChimeraKey + "\"");
        System.out.println("Remapped " + CLASS_MAP.size() + " obfuscated hook classes into me.drake.viecheats.mixin.*");
        System.out.println("Eradicated " + ERADICATED_CLASSES.size() + " decoy/dead classes and "
            + ERADICATED_RESOURCES.size() + " native binary payload blobs.");
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: VieCheatsDeobfuscator <input.jar> <output.jar>");
            return;
        }
        Path in = Paths.get(args[0]);
        Path out = Paths.get(args[1]);
        deobfuscateJar(in, out);
        System.out.println("Successfully deobfuscated and purified " + in + " -> " + out);
    }
}
