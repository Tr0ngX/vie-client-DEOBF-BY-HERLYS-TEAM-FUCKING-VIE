import jdk.internal.org.objectweb.asm.*;
import jdk.internal.org.objectweb.asm.tree.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.*;
import java.util.stream.Collectors;

/**
 * High-Precision ASM Bytecode-to-Structured-Java Decompiler.
 * Reconstructs full Java AST control flow (try/catch, while, if/else, guard clauses,
 * De Morgan boolean conditions, ternary expressions, array initializers, lambdas,
 * string concatenations, and exact scoped LocalVariableTable names/types).
 */
public class AsmJavaDecompiler {

    private final Map<String, ClassNode> allClasses = new LinkedHashMap<>();
    private final Map<String, List<ClassNode>> childrenMap = new HashMap<>();
    private final Set<String> innerClassNames = new HashSet<>();

    public void loadJar(Path jarPath) throws Exception {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) continue;
                byte[] raw;
                try (InputStream is = jar.getInputStream(entry)) {
                    raw = is.readAllBytes();
                }
                raw[6] = 0;
                raw[7] = 61;
                ClassReader cr = new ClassReader(raw);
                ClassNode cn = new ClassNode();
                cr.accept(cn, 0);
                allClasses.put(cn.name, cn);
            }
        }

        for (String name : allClasses.keySet()) {
            int dollar = name.lastIndexOf('$');
            if (dollar > 0) {
                String outer = name.substring(0, dollar);
                if (allClasses.containsKey(outer)) {
                    childrenMap.computeIfAbsent(outer, k -> new ArrayList<>()).add(allClasses.get(name));
                    innerClassNames.add(name);
                }
            }
        }
    }

    public int decompileAll(Path outputSrcDir) throws Exception {
        int fileCount = 0;
        for (Map.Entry<String, ClassNode> entry : allClasses.entrySet()) {
            String name = entry.getKey();
            if (innerClassNames.contains(name)) continue;
            ClassNode cn = entry.getValue();

            Set<String> imports = new TreeSet<>();
            String pkg = "";
            int lastSlash = cn.name.lastIndexOf('/');
            if (lastSlash > 0) {
                pkg = cn.name.substring(0, lastSlash).replace('/', '.');
            }

            StringBuilder body = new StringBuilder();
            decompileClass(cn, 0, pkg, imports, body);

            StringBuilder fileContent = new StringBuilder();
            if (!pkg.isEmpty()) {
                fileContent.append("package ").append(pkg).append(";\n\n");
            }
            for (String imp : imports) {
                if (imp.startsWith("java.lang.") && imp.indexOf('.', 10) == -1) continue;
                int impDot = imp.lastIndexOf('.');
                String impPkg = impDot > 0 ? imp.substring(0, impDot) : "";
                if (impPkg.equals(pkg)) continue;
                fileContent.append("import ").append(imp).append(";\n");
            }
            if (!imports.isEmpty()) {
                fileContent.append("\n");
            }
            fileContent.append(body);

            Path outFile = outputSrcDir.resolve(cn.name + ".java");
            Files.createDirectories(outFile.getParent());
            Files.writeString(outFile, fileContent.toString(), StandardCharsets.UTF_8);
            fileCount++;
        }
        return fileCount;
    }

    private String formatType(Type t, String currentPkg, Set<String> imports) {
        switch (t.getSort()) {
            case Type.VOID: return "void";
            case Type.BOOLEAN: return "boolean";
            case Type.CHAR: return "char";
            case Type.BYTE: return "byte";
            case Type.SHORT: return "short";
            case Type.INT: return "int";
            case Type.FLOAT: return "float";
            case Type.LONG: return "long";
            case Type.DOUBLE: return "double";
            case Type.ARRAY:
                return formatType(t.getElementType(), currentPkg, imports) + "[]".repeat(t.getDimensions());
            case Type.OBJECT:
                return formatInternalName(t.getInternalName(), currentPkg, imports);
            default:
                return t.getClassName();
        }
    }

    private String formatInternalName(String internal, String currentPkg, Set<String> imports) {
        if (internal == null) return "Object";
        String dotName = internal.replace('/', '.');
        int dollar = dotName.indexOf('$');
        String topLevel = dollar > 0 ? dotName.substring(0, dollar) : dotName;
        int lastDot = topLevel.lastIndexOf('.');
        String pkg = lastDot > 0 ? topLevel.substring(0, lastDot) : "";
        String simpleTop = lastDot > 0 ? topLevel.substring(lastDot + 1) : topLevel;
        String suffix = dollar > 0 ? dotName.substring(dollar).replace('$', '.') : "";

        if (!pkg.isEmpty() && !pkg.equals("java.lang") && !pkg.equals(currentPkg)) {
            imports.add(topLevel);
        }
        return simpleTop + suffix;
    }

    private String formatAnnotation(AnnotationNode an, String currentPkg, Set<String> imports) {
        String typeName = formatType(Type.getType(an.desc), currentPkg, imports);
        if (an.values == null || an.values.isEmpty()) {
            return "@" + typeName;
        }
        List<String> pairs = new ArrayList<>();
        for (int i = 0; i < an.values.size(); i += 2) {
            String key = (String) an.values.get(i);
            Object val = an.values.get(i + 1);
            String valStr = formatAnnotationValue(val, currentPkg, imports);
            if (an.values.size() == 2 && "value".equals(key)) {
                pairs.add(valStr);
            } else {
                pairs.add(key + " = " + valStr);
            }
        }
        return "@" + typeName + "(" + String.join(", ", pairs) + ")";
    }

    private String formatAnnotationValue(Object val, String currentPkg, Set<String> imports) {
        if (val instanceof String s) return escapeString(s);
        if (val instanceof Type t) return formatType(t, currentPkg, imports) + ".class";
        if (val instanceof String[] enumVal) {
            return formatType(Type.getType(enumVal[0]), currentPkg, imports) + "." + enumVal[1];
        }
        if (val instanceof AnnotationNode an) {
            return formatAnnotation(an, currentPkg, imports);
        }
        if (val instanceof List<?> list) {
            if (list.size() == 1) {
                return formatAnnotationValue(list.get(0), currentPkg, imports);
            }
            List<String> items = new ArrayList<>();
            for (Object item : list) {
                items.add(formatAnnotationValue(item, currentPkg, imports));
            }
            return "{" + String.join(", ", items) + "}";
        }
        return String.valueOf(val);
    }

    private void emitAnnotations(List<AnnotationNode> vis, List<AnnotationNode> invis,
                                 String indent, String currentPkg, Set<String> imports, StringBuilder sb) {
        if (vis != null) {
            for (AnnotationNode an : vis) {
                sb.append(indent).append(formatAnnotation(an, currentPkg, imports)).append("\n");
            }
        }
        if (invis != null) {
            for (AnnotationNode an : invis) {
                if (an.desc.contains("MixinMerged") || an.desc.contains("DebugMetadata")) continue;
                sb.append(indent).append(formatAnnotation(an, currentPkg, imports)).append("\n");
            }
        }
    }

    private void decompileClass(ClassNode cn, int depth, String currentPkg, Set<String> imports, StringBuilder sb) {
        String indent = "    ".repeat(depth);
        emitAnnotations(cn.visibleAnnotations, cn.invisibleAnnotations, indent, currentPkg, imports, sb);

        boolean isInterface = (cn.access & Opcodes.ACC_INTERFACE) != 0;
        boolean isEnum = (cn.access & Opcodes.ACC_ENUM) != 0;
        boolean isRecord = (cn.access & Opcodes.ACC_RECORD) != 0;
        boolean isAnnotation = (cn.access & Opcodes.ACC_ANNOTATION) != 0;

        sb.append(indent);
        if ((cn.access & Opcodes.ACC_PUBLIC) != 0) sb.append("public ");
        else if ((cn.access & Opcodes.ACC_PROTECTED) != 0) sb.append("protected ");
        else if ((cn.access & Opcodes.ACC_PRIVATE) != 0) sb.append("private ");

        if (depth > 0 && (cn.access & Opcodes.ACC_STATIC) != 0 && !isInterface && !isEnum && !isRecord) {
            sb.append("static ");
        }
        if ((cn.access & Opcodes.ACC_ABSTRACT) != 0 && !isInterface && !isEnum && !isRecord) {
            sb.append("abstract ");
        }
        if ((cn.access & Opcodes.ACC_FINAL) != 0 && !isEnum && !isRecord && !isInterface) {
            sb.append("final ");
        }

        if (isAnnotation) sb.append("@interface ");
        else if (isInterface) sb.append("interface ");
        else if (isEnum) sb.append("enum ");
        else if (isRecord) sb.append("record ");
        else sb.append("class ");

        String simpleName = cn.name.substring(cn.name.lastIndexOf('/') + 1);
        if (simpleName.contains("$")) {
            simpleName = simpleName.substring(simpleName.lastIndexOf('$') + 1);
        }
        sb.append(simpleName);

        if (isRecord && cn.recordComponents != null) {
            List<String> comps = new ArrayList<>();
            for (RecordComponentNode rcn : cn.recordComponents) {
                comps.add(formatType(Type.getType(rcn.descriptor), currentPkg, imports) + " " + rcn.name);
            }
            sb.append("(").append(String.join(", ", comps)).append(")");
        }

        if (!isEnum && !isRecord && !isInterface && cn.superName != null && !cn.superName.equals("java/lang/Object")) {
            sb.append(" extends ").append(formatInternalName(cn.superName, currentPkg, imports));
        }

        if (cn.interfaces != null && !cn.interfaces.isEmpty()) {
            List<String> ifaces = new ArrayList<>();
            for (String iface : cn.interfaces) {
                if (isAnnotation && iface.equals("java/lang/annotation/Annotation")) continue;
                ifaces.add(formatInternalName(iface, currentPkg, imports));
            }
            if (!ifaces.isEmpty()) {
                sb.append(isInterface ? " extends " : " implements ").append(String.join(", ", ifaces));
            }
        }

        sb.append(" {\n");
        String memberIndent = "    ".repeat(depth + 1);

        Map<String, MethodNode> lambdaMethods = new HashMap<>();
        for (MethodNode mn : cn.methods) {
            if ((mn.access & Opcodes.ACC_SYNTHETIC) != 0 && mn.name.startsWith("lambda$")) {
                lambdaMethods.put(mn.name + mn.desc, mn);
            }
        }

        if (isEnum) {
            List<String> enumConsts = new ArrayList<>();
            for (FieldNode fn : cn.fields) {
                if ((fn.access & Opcodes.ACC_ENUM) != 0) {
                    enumConsts.add(fn.name);
                }
            }
            if (!enumConsts.isEmpty()) {
                sb.append(memberIndent).append(String.join(",\n" + memberIndent, enumConsts)).append(";\n\n");
            }
        }

        for (FieldNode fn : cn.fields) {
            if ((fn.access & Opcodes.ACC_SYNTHETIC) != 0) continue;
            if ((fn.access & Opcodes.ACC_ENUM) != 0) continue;
            if (isRecord && (fn.access & Opcodes.ACC_STATIC) == 0) continue;

            emitAnnotations(fn.visibleAnnotations, fn.invisibleAnnotations, memberIndent, currentPkg, imports, sb);
            sb.append(memberIndent);
            if ((fn.access & Opcodes.ACC_PUBLIC) != 0) sb.append("public ");
            else if ((fn.access & Opcodes.ACC_PROTECTED) != 0) sb.append("protected ");
            else if ((fn.access & Opcodes.ACC_PRIVATE) != 0) sb.append("private ");

            if ((fn.access & Opcodes.ACC_STATIC) != 0) sb.append("static ");
            if ((fn.access & Opcodes.ACC_FINAL) != 0) sb.append("final ");
            if ((fn.access & Opcodes.ACC_VOLATILE) != 0) sb.append("volatile ");
            if ((fn.access & Opcodes.ACC_TRANSIENT) != 0) sb.append("transient ");

            Type ft = Type.getType(fn.desc);
            sb.append(formatType(ft, currentPkg, imports)).append(" ").append(fn.name);
            if (fn.value != null) {
                sb.append(" = ").append(formatConstant(fn.value, ft, currentPkg, imports));
            }
            sb.append(";\n");
        }
        if (!cn.fields.isEmpty()) sb.append("\n");

        for (MethodNode mn : cn.methods) {
            if ((mn.access & Opcodes.ACC_SYNTHETIC) != 0 || (mn.access & Opcodes.ACC_BRIDGE) != 0) continue;
            if (isEnum && (mn.name.equals("values") || mn.name.equals("valueOf") || mn.name.equals("$values"))) continue;
            if (isRecord && (mn.name.equals("toString") || mn.name.equals("hashCode") || mn.name.equals("equals"))) {
                if (usesObjectMethodsBootstrap(mn)) continue;
            }
            if (mn.name.equals("<init>") && !isEnum && isTrivialDefaultConstructor(mn, cn.superName) && cn.methods.size() > 1) {
                continue;
            }

            decompileMethod(cn, mn, simpleName, isInterface, isEnum, depth + 1, currentPkg, imports, lambdaMethods, sb);
        }

        List<ClassNode> inners = childrenMap.get(cn.name);
        if (inners != null) {
            for (ClassNode inner : inners) {
                sb.append("\n");
                decompileClass(inner, depth + 1, currentPkg, imports, sb);
            }
        }

        sb.append(indent).append("}\n");
    }

    private boolean usesObjectMethodsBootstrap(MethodNode mn) {
        for (AbstractInsnNode insn : mn.instructions) {
            if (insn instanceof InvokeDynamicInsnNode indy
                    && indy.bsm.getOwner().equals("java/lang/runtime/ObjectMethods")) {
                return true;
            }
        }
        return false;
    }

    private boolean isTrivialDefaultConstructor(MethodNode mn, String superName) {
        if (!mn.desc.equals("()V")) return false;
        List<AbstractInsnNode> real = new ArrayList<>();
        for (AbstractInsnNode insn : mn.instructions) {
            if (insn.getOpcode() >= 0) real.add(insn);
        }
        return real.size() == 3
                && real.get(0).getOpcode() == Opcodes.ALOAD
                && real.get(1) instanceof MethodInsnNode min
                && min.name.equals("<init>") && min.owner.equals(superName) && min.desc.equals("()V")
                && real.get(2).getOpcode() == Opcodes.RETURN;
    }

    private static class ScopedVar {
        final int slot;
        final int startInsn;
        final int endInsn;
        final String name;
        final Type type;

        ScopedVar(int slot, int startInsn, int endInsn, String name, Type type) {
            this.slot = slot;
            this.startInsn = startInsn;
            this.endInsn = endInsn;
            this.name = name;
            this.type = type;
        }
    }

    private List<ScopedVar> buildScopedVars(MethodNode mn, boolean isStatic, Type[] argTypes) {
        List<ScopedVar> list = new ArrayList<>();
        int n = mn.instructions.size();
        if (!isStatic) {
            list.add(new ScopedVar(0, 0, n, "this", Type.getObjectType("java/lang/Object")));
        }
        if (mn.localVariables != null) {
            for (LocalVariableNode lvn : mn.localVariables) {
                if (lvn.name == null || lvn.name.equals("this")) continue;
                int s = mn.instructions.indexOf(lvn.start);
                int e = mn.instructions.indexOf(lvn.end);
                list.add(new ScopedVar(
                        lvn.index,
                        Math.max(0, s - 2),
                        e < 0 ? n : e + 1,
                        sanitizeIdentifier(lvn.name, lvn.index),
                        lvn.desc != null ? Type.getType(lvn.desc) : null
                ));
            }
        }
        int slot = isStatic ? 0 : 1;
        for (int i = 0; i < argTypes.length; i++) {
            final int curSlot = slot;
            boolean found = list.stream().anyMatch(v -> v.slot == curSlot && v.startInsn <= 2);
            if (!found) {
                String pName = (mn.parameters != null && i < mn.parameters.size() && mn.parameters.get(i).name != null)
                        ? sanitizeIdentifier(mn.parameters.get(i).name, curSlot)
                        : "arg" + i;
                list.add(new ScopedVar(curSlot, 0, n, pName, argTypes[i]));
            }
            slot += argTypes[i].getSize();
        }
        return list;
    }

    private ScopedVar resolveVar(List<ScopedVar> vars, int slot, int insnIdx) {
        for (ScopedVar v : vars) {
            if (v.slot == slot && insnIdx >= v.startInsn && insnIdx <= v.endInsn) {
                return v;
            }
        }
        for (ScopedVar v : vars) {
            if (v.slot == slot) return v;
        }
        return null;
    }

    private void decompileMethod(ClassNode cn, MethodNode mn, String simpleClassName,
                                 boolean isInterface, boolean isEnum, int depth,
                                 String currentPkg, Set<String> imports,
                                 Map<String, MethodNode> lambdaMethods, StringBuilder sb) {
        String indent = "    ".repeat(depth);
        String bodyIndent = "    ".repeat(depth + 1);

        if (mn.name.equals("<clinit>")) {
            List<String> raw = decompileInstructions(cn, mn, "", currentPkg, imports, lambdaMethods, true);
            List<String> structured = structureStatements(raw, bodyIndent);
            if (!structured.isEmpty()) {
                sb.append(indent).append("static {\n");
                for (String s : structured) sb.append(s).append("\n");
                sb.append(indent).append("}\n\n");
            }
            return;
        }

        emitAnnotations(mn.visibleAnnotations, mn.invisibleAnnotations, indent, currentPkg, imports, sb);
        sb.append(indent);

        if (!isInterface) {
            if ((mn.access & Opcodes.ACC_PUBLIC) != 0) sb.append("public ");
            else if ((mn.access & Opcodes.ACC_PROTECTED) != 0) sb.append("protected ");
            else if ((mn.access & Opcodes.ACC_PRIVATE) != 0) sb.append("private ");
        } else if ((mn.access & Opcodes.ACC_PRIVATE) != 0) {
            sb.append("private ");
        } else if ((mn.access & Opcodes.ACC_STATIC) == 0 && mn.instructions.size() > 0) {
            sb.append("default ");
        }

        if ((mn.access & Opcodes.ACC_STATIC) != 0) sb.append("static ");
        if ((mn.access & Opcodes.ACC_FINAL) != 0 && !isInterface) sb.append("final ");
        if ((mn.access & Opcodes.ACC_SYNCHRONIZED) != 0) sb.append("synchronized ");
        if ((mn.access & Opcodes.ACC_NATIVE) != 0) sb.append("native ");
        if ((mn.access & Opcodes.ACC_ABSTRACT) != 0 && !isInterface) sb.append("abstract ");

        boolean isStatic = (mn.access & Opcodes.ACC_STATIC) != 0;
        Type methodType = Type.getMethodType(mn.desc);
        Type[] argTypes = methodType.getArgumentTypes();
        Type retType = methodType.getReturnType();

        boolean isCtor = mn.name.equals("<init>");
        if (isCtor) {
            sb.append(simpleClassName);
        } else {
            sb.append(formatType(retType, currentPkg, imports)).append(" ").append(mn.name);
        }

        List<ScopedVar> scopedVars = buildScopedVars(mn, isStatic, argTypes);
        int startArgIdx = (isCtor && isEnum && argTypes.length >= 2) ? 2 : 0;
        int slot = isStatic ? 0 : 1;
        for (int i = 0; i < startArgIdx; i++) {
            slot += argTypes[i].getSize();
        }

        List<String> paramDecls = new ArrayList<>();
        for (int i = startArgIdx; i < argTypes.length; i++) {
            Type at = argTypes[i];
            ScopedVar sv = resolveVar(scopedVars, slot, 0);
            String pName = sv != null ? sv.name : "arg" + i;
            String typeStr = formatType(at, currentPkg, imports);
            if (i == argTypes.length - 1 && (mn.access & Opcodes.ACC_VARARGS) != 0 && typeStr.endsWith("[]")) {
                typeStr = typeStr.substring(0, typeStr.length() - 2) + "...";
            }
            paramDecls.add(typeStr + " " + pName);
            slot += at.getSize();
        }
        sb.append("(").append(String.join(", ", paramDecls)).append(")");

        if (mn.exceptions != null && !mn.exceptions.isEmpty()) {
            List<String> exs = new ArrayList<>();
            for (String ex : mn.exceptions) {
                exs.add(formatInternalName(ex, currentPkg, imports));
            }
            sb.append(" throws ").append(String.join(", ", exs));
        }

        if ((mn.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0 || mn.instructions.size() == 0) {
            sb.append(";\n\n");
            return;
        }

        sb.append(" {\n");
        List<String> raw = decompileInstructions(cn, mn, "", currentPkg, imports, lambdaMethods, false);
        List<String> structured = structureStatements(raw, bodyIndent);
        for (String s : structured) {
            sb.append(s).append("\n");
        }
        sb.append(indent).append("}\n\n");
    }

    private String sanitizeIdentifier(String name, int idx) {
        if (name == null || name.isEmpty() || name.equals("_")) return "var" + idx;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i == 0 ? Character.isJavaIdentifierStart(c) : Character.isJavaIdentifierPart(c)) {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }
        return sb.toString();
    }

    private List<String> decompileInstructions(ClassNode cn, MethodNode mn, String indent,
                                               String currentPkg, Set<String> imports,
                                               Map<String, MethodNode> lambdaMethods,
                                               boolean isClinit) {
        List<String> out = new ArrayList<>();
        boolean isStatic = (mn.access & Opcodes.ACC_STATIC) != 0;
        boolean isEnum = (cn.access & Opcodes.ACC_ENUM) != 0;
        Type[] argTypes = Type.getArgumentTypes(mn.desc);
        Type retType = Type.getReturnType(mn.desc);

        List<ScopedVar> scopedVars = buildScopedVars(mn, isStatic, argTypes);
        Set<String> declaredVarNames = new HashSet<>();
        if (!isStatic) declaredVarNames.add("this");
        int pSlot = isStatic ? 0 : 1;
        for (Type at : argTypes) {
            ScopedVar sv = resolveVar(scopedVars, pSlot, 0);
            if (sv != null) declaredVarNames.add(sv.name);
            pSlot += at.getSize();
        }

        Map<LabelNode, Integer> labelIds = new HashMap<>();
        Set<LabelNode> jumpTargets = new HashSet<>();
        Map<LabelNode, List<TryCatchBlockNode>> catchStarts = new HashMap<>();
        Map<LabelNode, List<TryCatchBlockNode>> tryStarts = new HashMap<>();
        Map<LabelNode, List<TryCatchBlockNode>> tryEnds = new HashMap<>();

        if (mn.tryCatchBlocks != null) {
            for (TryCatchBlockNode tcb : mn.tryCatchBlocks) {
                if (tcb.type != null) {
                    catchStarts.computeIfAbsent(tcb.handler, k -> new ArrayList<>()).add(tcb);
                    tryStarts.computeIfAbsent(tcb.start, k -> new ArrayList<>()).add(tcb);
                    tryEnds.computeIfAbsent(tcb.end, k -> new ArrayList<>()).add(tcb);
                    jumpTargets.add(tcb.handler);
                }
            }
        }

        int lblCounter = 0;
        for (AbstractInsnNode insn : mn.instructions) {
            if (insn instanceof LabelNode ln) {
                labelIds.put(ln, lblCounter++);
            } else if (insn instanceof JumpInsnNode jin) {
                jumpTargets.add(jin.label);
            } else if (insn instanceof TableSwitchInsnNode tsin) {
                jumpTargets.add(tsin.dflt);
                jumpTargets.addAll(tsin.labels);
            } else if (insn instanceof LookupSwitchInsnNode lsin) {
                jumpTargets.add(lsin.dflt);
                jumpTargets.addAll(lsin.labels);
            }
        }

        Deque<String> stack = new ArrayDeque<>();
        int tempCounter = 0;

        AbstractInsnNode[] insns = mn.instructions.toArray();
        for (int i = 0; i < insns.length; i++) {
            AbstractInsnNode insn = insns[i];

            if (insn instanceof LabelNode ln) {
                if (tryStarts.containsKey(ln)) {
                    out.add("TRY_START:");
                }
                if (catchStarts.containsKey(ln)) {
                    List<TryCatchBlockNode> tcbs = catchStarts.get(ln);
                    String exType = tcbs.stream()
                            .map(t -> formatInternalName(t.type, currentPkg, imports))
                            .distinct()
                            .collect(Collectors.joining(" | "));
                    int lid = labelIds.getOrDefault(ln, 0);
                    String exVar = "ex" + lid;
                    stack.clear();
                    stack.push(exVar);
                    out.add("LABEL_" + lid + ":");
                    out.add("// catch (" + exType + " " + exVar + ")");
                } else if (jumpTargets.contains(ln)) {
                    out.add("LABEL_" + labelIds.get(ln) + ":");
                }
                continue;
            }

            int op = insn.getOpcode();
            if (op < 0) continue;

            // Detect inline ternary / boolean stack branches:
            // If a conditional jump jumps to L_else, and before L_else there is a single value push + GOTO L_merge,
            // and at L_else there is a single value push followed by L_merge:
            if ((op >= Opcodes.IFEQ && op <= Opcodes.IF_ACMPNE) || op == Opcodes.IFNULL || op == Opcodes.IFNONNULL) {
                JumpInsnNode jin = (JumpInsnNode) insn;
                String cond = extractJumpCondition(op, stack, scopedVars, i);
                int elseIdx = mn.instructions.indexOf(jin.label);
                TernaryMatch tm = tryMatchTernary(insns, i, elseIdx, currentPkg, imports, scopedVars);
                if (tm != null) {
                    String negCond = negateCondition(cond);
                    stack.push("(" + negCond + " ? " + tm.trueExpr + " : " + tm.falseExpr + ")");
                    i = tm.endInsnIdx;
                    continue;
                }
                int tgt = labelIds.getOrDefault(jin.label, 0);
                out.add("IF (" + cond + ") GOTO LABEL_" + tgt + ";");
                continue;
            }

            switch (op) {
                case Opcodes.NOP -> {}
                case Opcodes.ACONST_NULL -> stack.push("null");
                case Opcodes.ICONST_M1 -> stack.push("-1");
                case Opcodes.ICONST_0 -> stack.push("0");
                case Opcodes.ICONST_1 -> stack.push("1");
                case Opcodes.ICONST_2 -> stack.push("2");
                case Opcodes.ICONST_3 -> stack.push("3");
                case Opcodes.ICONST_4 -> stack.push("4");
                case Opcodes.ICONST_5 -> stack.push("5");
                case Opcodes.LCONST_0 -> stack.push("0L");
                case Opcodes.LCONST_1 -> stack.push("1L");
                case Opcodes.FCONST_0 -> stack.push("0.0f");
                case Opcodes.FCONST_1 -> stack.push("1.0f");
                case Opcodes.FCONST_2 -> stack.push("2.0f");
                case Opcodes.DCONST_0 -> stack.push("0.0");
                case Opcodes.DCONST_1 -> stack.push("1.0");
                case Opcodes.BIPUSH, Opcodes.SIPUSH -> stack.push(String.valueOf(((IntInsnNode) insn).operand));
                case Opcodes.LDC -> {
                    Object cst = ((LdcInsnNode) insn).cst;
                    stack.push(formatConstant(cst, null, currentPkg, imports));
                }
                case Opcodes.ILOAD, Opcodes.LLOAD, Opcodes.FLOAD, Opcodes.DLOAD, Opcodes.ALOAD -> {
                    int v = ((VarInsnNode) insn).var;
                    ScopedVar sv = resolveVar(scopedVars, v, i);
                    stack.push(sv != null ? sv.name : "var" + v);
                }
                case Opcodes.IALOAD, Opcodes.LALOAD, Opcodes.FALOAD, Opcodes.DALOAD,
                     Opcodes.AALOAD, Opcodes.BALOAD, Opcodes.CALOAD, Opcodes.SALOAD -> {
                    String idx = popSafe(stack);
                    String arr = popSafe(stack);
                    stack.push(arr + "[" + idx + "]");
                }
                case Opcodes.ISTORE, Opcodes.LSTORE, Opcodes.FSTORE, Opcodes.DSTORE, Opcodes.ASTORE -> {
                    int v = ((VarInsnNode) insn).var;
                    String val = popSafe(stack);
                    ScopedVar sv = resolveVar(scopedVars, v, i);
                    String vName = sv != null ? sv.name : "var" + v;
                    Type vt = sv != null ? sv.type : null;
                    if (vt != null && vt.getSort() == Type.BOOLEAN) {
                        val = toBooleanExpr(val);
                    }
                    if (!declaredVarNames.contains(vName)) {
                        declaredVarNames.add(vName);
                        String typeStr = vt != null ? formatType(vt, currentPkg, imports) : inferTypeFromOp(op);
                        out.add(typeStr + " " + vName + " = " + val + ";");
                    } else {
                        out.add(vName + " = " + val + ";");
                    }
                }
                case Opcodes.IASTORE, Opcodes.LASTORE, Opcodes.FASTORE, Opcodes.DASTORE,
                     Opcodes.AASTORE, Opcodes.BASTORE, Opcodes.CASTORE, Opcodes.SASTORE -> {
                    String val = popSafe(stack);
                    String idx = popSafe(stack);
                    String arr = popSafe(stack);
                    out.add(arr + "[" + idx + "] = " + val + ";");
                }
                case Opcodes.POP, Opcodes.POP2 -> {
                    String val = popSafe(stack);
                    if (isStatementExpr(val)) {
                        out.add(val + ";");
                    }
                }
                case Opcodes.DUP -> {
                    String top = popSafe(stack);
                    if (top.startsWith("new ") && !top.contains("(") && !top.contains("[")) {
                        stack.push(top);
                        stack.push(top);
                    } else if (isSimpleAtom(top)) {
                        stack.push(top);
                        stack.push(top);
                    } else {
                        String tmp = "tmp" + (tempCounter++);
                        declaredVarNames.add(tmp);
                        out.add("var " + tmp + " = " + top + ";");
                        stack.push(tmp);
                        stack.push(tmp);
                    }
                }
                case Opcodes.DUP_X1, Opcodes.DUP_X2 -> {
                    String v1 = popSafe(stack);
                    String v2 = popSafe(stack);
                    stack.push(v1);
                    stack.push(v2);
                    stack.push(v1);
                }
                case Opcodes.DUP2 -> {
                    String v1 = popSafe(stack);
                    stack.push(v1);
                    stack.push(v1);
                }
                case Opcodes.SWAP -> {
                    String v1 = popSafe(stack);
                    String v2 = popSafe(stack);
                    stack.push(v1);
                    stack.push(v2);
                }
                case Opcodes.IADD, Opcodes.LADD, Opcodes.FADD, Opcodes.DADD -> binaryOp(stack, "+");
                case Opcodes.ISUB, Opcodes.LSUB, Opcodes.FSUB, Opcodes.DSUB -> binaryOp(stack, "-");
                case Opcodes.IMUL, Opcodes.LMUL, Opcodes.FMUL, Opcodes.DMUL -> binaryOp(stack, "*");
                case Opcodes.IDIV, Opcodes.LDIV, Opcodes.FDIV, Opcodes.DDIV -> binaryOp(stack, "/");
                case Opcodes.IREM, Opcodes.LREM, Opcodes.FREM, Opcodes.DREM -> binaryOp(stack, "%");
                case Opcodes.INEG, Opcodes.LNEG, Opcodes.FNEG, Opcodes.DNEG -> {
                    String a = popSafe(stack);
                    stack.push("-(" + a + ")");
                }
                case Opcodes.ISHL, Opcodes.LSHL -> binaryOp(stack, "<<");
                case Opcodes.ISHR, Opcodes.LSHR -> binaryOp(stack, ">>");
                case Opcodes.IUSHR, Opcodes.LUSHR -> binaryOp(stack, ">>>");
                case Opcodes.IAND, Opcodes.LAND -> binaryOp(stack, "&");
                case Opcodes.IOR, Opcodes.LOR -> binaryOp(stack, "|");
                case Opcodes.IXOR, Opcodes.LXOR -> binaryOp(stack, "^");
                case Opcodes.IINC -> {
                    IincInsnNode iinc = (IincInsnNode) insn;
                    ScopedVar sv = resolveVar(scopedVars, iinc.var, i);
                    String vName = sv != null ? sv.name : "var" + iinc.var;
                    if (iinc.incr == 1) out.add(vName + "++;");
                    else if (iinc.incr == -1) out.add(vName + "--;");
                    else if (iinc.incr >= 0) out.add(vName + " += " + iinc.incr + ";");
                    else out.add(vName + " -= " + (-iinc.incr) + ";");
                }
                case Opcodes.I2L, Opcodes.F2L, Opcodes.D2L -> castOp(stack, "long");
                case Opcodes.I2F, Opcodes.L2F, Opcodes.D2F -> castOp(stack, "float");
                case Opcodes.I2D, Opcodes.L2D, Opcodes.F2D -> castOp(stack, "double");
                case Opcodes.L2I, Opcodes.F2I, Opcodes.D2I -> castOp(stack, "int");
                case Opcodes.I2B -> castOp(stack, "byte");
                case Opcodes.I2C -> castOp(stack, "char");
                case Opcodes.I2S -> castOp(stack, "short");
                case Opcodes.LCMP, Opcodes.FCMPL, Opcodes.FCMPG, Opcodes.DCMPL, Opcodes.DCMPG -> {
                    String b = popSafe(stack);
                    String a = popSafe(stack);
                    if (i + 1 < insns.length && insns[i + 1] instanceof JumpInsnNode jin) {
                        int jop = jin.getOpcode();
                        String cmpOp = switch (jop) {
                            case Opcodes.IFEQ -> "==";
                            case Opcodes.IFNE -> "!=";
                            case Opcodes.IFLT -> "<";
                            case Opcodes.IFGE -> ">=";
                            case Opcodes.IFGT -> ">";
                            case Opcodes.IFLE -> "<=";
                            default -> null;
                        };
                        if (cmpOp != null) {
                            String cond = a + " " + cmpOp + " " + b;
                            int elseIdx = mn.instructions.indexOf(jin.label);
                            TernaryMatch tm = tryMatchTernary(insns, i + 1, elseIdx, currentPkg, imports, scopedVars);
                            if (tm != null) {
                                String negCond = negateCondition(cond);
                                stack.push("(" + negCond + " ? " + tm.trueExpr + " : " + tm.falseExpr + ")");
                                i = tm.endInsnIdx;
                                break;
                            }
                            int tgt = labelIds.getOrDefault(jin.label, 0);
                            out.add("IF (" + cond + ") GOTO LABEL_" + tgt + ";");
                            i++;
                            break;
                        }
                    }
                    stack.push("Double.compare(" + a + ", " + b + ")");
                }
                case Opcodes.GOTO -> {
                    JumpInsnNode jin = (JumpInsnNode) insn;
                    int tgt = labelIds.getOrDefault(jin.label, 0);
                    out.add("GOTO LABEL_" + tgt + ";");
                }
                case Opcodes.TABLESWITCH -> {
                    TableSwitchInsnNode tsin = (TableSwitchInsnNode) insn;
                    String key = popSafe(stack);
                    out.add("switch (" + key + ") {");
                    for (int k = 0; k < tsin.labels.size(); k++) {
                        int val = tsin.min + k;
                        int tgt = labelIds.getOrDefault(tsin.labels.get(k), 0);
                        out.add("    case " + val + " -> { GOTO LABEL_" + tgt + "; }");
                    }
                    out.add("    default -> { GOTO LABEL_" + labelIds.getOrDefault(tsin.dflt, 0) + "; }");
                    out.add("}");
                }
                case Opcodes.LOOKUPSWITCH -> {
                    LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) insn;
                    String key = popSafe(stack);
                    out.add("switch (" + key + ") {");
                    for (int k = 0; k < lsin.keys.size(); k++) {
                        int val = lsin.keys.get(k);
                        int tgt = labelIds.getOrDefault(lsin.labels.get(k), 0);
                        out.add("    case " + val + " -> { GOTO LABEL_" + tgt + "; }");
                    }
                    out.add("    default -> { GOTO LABEL_" + labelIds.getOrDefault(lsin.dflt, 0) + "; }");
                    out.add("}");
                }
                case Opcodes.IRETURN, Opcodes.LRETURN, Opcodes.FRETURN, Opcodes.DRETURN, Opcodes.ARETURN -> {
                    String val = popSafe(stack);
                    if (retType.getSort() == Type.BOOLEAN) {
                        val = toBooleanExpr(val);
                    }
                    out.add("return " + val + ";");
                }
                case Opcodes.RETURN -> {
                    if (i < insns.length - 2) {
                        out.add("return;");
                    }
                }
                case Opcodes.GETSTATIC -> {
                    FieldInsnNode fin = (FieldInsnNode) insn;
                    String owner = fin.owner.equals(cn.name) ? "" : formatInternalName(fin.owner, currentPkg, imports) + ".";
                    stack.push(owner + fin.name);
                }
                case Opcodes.PUTSTATIC -> {
                    FieldInsnNode fin = (FieldInsnNode) insn;
                    String val = popSafe(stack);
                    if (isEnum && isClinit && fin.owner.equals(cn.name) && (fin.name.equals("$VALUES") || isEnumField(cn, fin.name))) {
                        break;
                    }
                    if (Type.getType(fin.desc).getSort() == Type.BOOLEAN) val = toBooleanExpr(val);
                    String owner = fin.owner.equals(cn.name) ? "" : formatInternalName(fin.owner, currentPkg, imports) + ".";
                    out.add(owner + fin.name + " = " + val + ";");
                }
                case Opcodes.GETFIELD -> {
                    FieldInsnNode fin = (FieldInsnNode) insn;
                    String obj = popSafe(stack);
                    stack.push((obj.equals("this") ? "this." : obj + ".") + fin.name);
                }
                case Opcodes.PUTFIELD -> {
                    FieldInsnNode fin = (FieldInsnNode) insn;
                    String val = popSafe(stack);
                    String obj = popSafe(stack);
                    if (Type.getType(fin.desc).getSort() == Type.BOOLEAN) val = toBooleanExpr(val);
                    out.add((obj.equals("this") ? "this." : obj + ".") + fin.name + " = " + val + ";");
                }
                case Opcodes.INVOKEVIRTUAL, Opcodes.INVOKESPECIAL, Opcodes.INVOKESTATIC, Opcodes.INVOKEINTERFACE -> {
                    MethodInsnNode min = (MethodInsnNode) insn;
                    Type mt = Type.getMethodType(min.desc);
                    Type[] mArgs = mt.getArgumentTypes();
                    List<String> args = new ArrayList<>();
                    for (int a = 0; a < mArgs.length; a++) {
                        String argExpr = popSafe(stack);
                        if (mArgs[mArgs.length - 1 - a].getSort() == Type.BOOLEAN) {
                            argExpr = toBooleanExpr(argExpr);
                        }
                        args.add(0, argExpr);
                    }
                    if (op == Opcodes.INVOKESTATIC) {
                        // Unwrap auto-boxing calls
                        if (min.name.equals("valueOf") && args.size() == 1 && (
                                min.owner.equals("java/lang/Integer") ||
                                min.owner.equals("java/lang/Long") ||
                                min.owner.equals("java/lang/Float") ||
                                min.owner.equals("java/lang/Double") ||
                                min.owner.equals("java/lang/Boolean") ||
                                min.owner.equals("java/lang/Byte") ||
                                min.owner.equals("java/lang/Short") ||
                                min.owner.equals("java/lang/Character"))) {
                            stack.push(min.owner.equals("java/lang/Boolean") ? toBooleanExpr(args.get(0)) : args.get(0));
                            break;
                        }
                        String owner = min.owner.equals(cn.name) ? "" : formatInternalName(min.owner, currentPkg, imports) + ".";
                        String call = owner + min.name + "(" + String.join(", ", args) + ")";
                        if (mt.getReturnType().getSort() == Type.VOID) {
                            out.add(call + ";");
                        } else {
                            stack.push(call);
                        }
                    } else {
                        String recv = popSafe(stack);
                        if (min.name.equals("<init>")) {
                            if (recv.equals("this")) {
                                if (min.owner.equals(cn.superName)) {
                                    if (!args.isEmpty() && !isEnum) {
                                        out.add("super(" + String.join(", ", args) + ");");
                                    }
                                } else if (min.owner.equals(cn.name)) {
                                    out.add("this(" + String.join(", ", args) + ");");
                                }
                            } else if (recv.startsWith("new ")) {
                                String typeName = formatInternalName(min.owner, currentPkg, imports);
                                String constructed = "new " + typeName + "(" + String.join(", ", args) + ")";
                                if (!stack.isEmpty() && stack.peek().equals(recv)) {
                                    stack.pop();
                                    stack.push(constructed);
                                } else {
                                    out.add(constructed + ";");
                                }
                            }
                        } else {
                            String prefix;
                            if (op == Opcodes.INVOKESPECIAL && recv.equals("this") && !min.owner.equals(cn.name)) {
                                prefix = "super.";
                            } else {
                                prefix = recv.equals("this") ? "" : recv + ".";
                            }
                            String call = prefix + min.name + "(" + String.join(", ", args) + ")";
                            if (mt.getReturnType().getSort() == Type.VOID) {
                                out.add(call + ";");
                            } else {
                                stack.push(call);
                            }
                        }
                    }
                }
                case Opcodes.INVOKEDYNAMIC -> {
                    InvokeDynamicInsnNode indy = (InvokeDynamicInsnNode) insn;
                    Type mt = Type.getMethodType(indy.desc);
                    Type[] mArgs = mt.getArgumentTypes();
                    List<String> args = new ArrayList<>();
                    for (int a = 0; a < mArgs.length; a++) {
                        args.add(0, popSafe(stack));
                    }
                    String bsmOwner = indy.bsm.getOwner();
                    if (bsmOwner.equals("java/lang/invoke/StringConcatFactory")) {
                        String recipe = (indy.bsmArgs != null && indy.bsmArgs.length > 0) ? String.valueOf(indy.bsmArgs[0]) : "";
                        stack.push(buildStringConcat(recipe, args, indy.bsmArgs));
                    } else if (bsmOwner.equals("java/lang/invoke/LambdaMetafactory") && indy.bsmArgs.length >= 2 && indy.bsmArgs[1] instanceof Handle h) {
                        if (h.getOwner().equals(cn.name) && lambdaMethods.containsKey(h.getName() + h.getDesc())) {
                            MethodNode targetLambda = lambdaMethods.get(h.getName() + h.getDesc());
                            stack.push(formatInlineLambda(cn, targetLambda, args.size(), indent, currentPkg, imports, lambdaMethods));
                        } else {
                            String ownerName = h.getOwner().equals(cn.name) ? "this" : formatInternalName(h.getOwner(), currentPkg, imports);
                            String refName = h.getName().equals("<init>") ? "new" : h.getName();
                            if (!args.isEmpty() && h.getTag() != Opcodes.H_INVOKESTATIC) {
                                stack.push(args.get(0) + "::" + refName);
                            } else {
                                stack.push(ownerName + "::" + refName);
                            }
                        }
                    } else {
                        stack.push(indy.name + "(" + String.join(", ", args) + ")");
                    }
                }
                case Opcodes.NEW -> {
                    TypeInsnNode tin = (TypeInsnNode) insn;
                    stack.push("new " + formatInternalName(tin.desc, currentPkg, imports));
                }
                case Opcodes.NEWARRAY -> {
                    IntInsnNode iin = (IntInsnNode) insn;
                    String sz = popSafe(stack);
                    String elem = switch (iin.operand) {
                        case Opcodes.T_BOOLEAN -> "boolean";
                        case Opcodes.T_CHAR -> "char";
                        case Opcodes.T_FLOAT -> "float";
                        case Opcodes.T_DOUBLE -> "double";
                        case Opcodes.T_BYTE -> "byte";
                        case Opcodes.T_SHORT -> "short";
                        case Opcodes.T_INT -> "int";
                        case Opcodes.T_LONG -> "long";
                        default -> "Object";
                    };
                    stack.push("new " + elem + "[" + sz + "]");
                }
                case Opcodes.ANEWARRAY -> {
                    TypeInsnNode tin = (TypeInsnNode) insn;
                    String sz = popSafe(stack);
                    stack.push("new " + formatType(Type.getObjectType(tin.desc), currentPkg, imports) + "[" + sz + "]");
                }
                case Opcodes.ARRAYLENGTH -> {
                    String arr = popSafe(stack);
                    stack.push(arr + ".length");
                }
                case Opcodes.ATHROW -> {
                    String ex = popSafe(stack);
                    out.add("throw " + ex + ";");
                }
                case Opcodes.CHECKCAST -> {
                    TypeInsnNode tin = (TypeInsnNode) insn;
                    String obj = popSafe(stack);
                    String castType = formatType(Type.getObjectType(tin.desc), currentPkg, imports);
                    stack.push("((" + castType + ") " + obj + ")");
                }
                case Opcodes.INSTANCEOF -> {
                    TypeInsnNode tin = (TypeInsnNode) insn;
                    String obj = popSafe(stack);
                    String checkType = formatType(Type.getObjectType(tin.desc), currentPkg, imports);
                    stack.push("(" + obj + " instanceof " + checkType + ")");
                }
                case Opcodes.MONITORENTER -> {
                    String mon = popSafe(stack);
                    out.add("// synchronized (" + mon + ")");
                }
                case Opcodes.MONITOREXIT -> popSafe(stack);
                case Opcodes.MULTIANEWARRAY -> {
                    MultiANewArrayInsnNode man = (MultiANewArrayInsnNode) insn;
                    List<String> dims = new ArrayList<>();
                    for (int d = 0; d < man.dims; d++) dims.add(0, popSafe(stack));
                    Type elem = Type.getType(man.desc).getElementType();
                    stack.push("new " + formatType(elem, currentPkg, imports) + "[" + String.join("][", dims) + "]");
                }
            }
        }
        return out;
    }

    private boolean isIntExpr(String a, List<ScopedVar> scopedVars, int insnIdx) {
        if (a == null) return false;
        String u = unwrapParens(a);
        if (u.endsWith(".length") || u.endsWith(".size()") || u.contains(".indexOf(") || u.contains(".lastIndexOf(")
                || u.contains(".compareTo(") || u.contains(".hashCode()") || u.startsWith("Double.compare(")) {
            return true;
        }
        for (String op : new String[]{" % ", " & ", " | ", " ^ ", " + ", " - ", " * ", " / ", " << ", " >> ", " >>> "}) {
            if (findTopLevelOp(u, op) > 0) return true;
        }
        for (ScopedVar sv : scopedVars) {
            if (sv.name.equals(u) && sv.type != null) {
                int sort = sv.type.getSort();
                return sort == Type.INT || sort == Type.SHORT || sort == Type.BYTE || sort == Type.CHAR || sort == Type.LONG;
            }
        }
        return false;
    }

    private String extractJumpCondition(int op, Deque<String> stack, List<ScopedVar> scopedVars, int insnIdx) {
        if (op >= Opcodes.IF_ICMPEQ && op <= Opcodes.IF_ACMPNE) {
            String b = popSafe(stack);
            String a = popSafe(stack);
            String cmpOp = switch (op) {
                case Opcodes.IF_ICMPEQ, Opcodes.IF_ACMPEQ -> "==";
                case Opcodes.IF_ICMPNE, Opcodes.IF_ACMPNE -> "!=";
                case Opcodes.IF_ICMPLT -> "<";
                case Opcodes.IF_ICMPGE -> ">=";
                case Opcodes.IF_ICMPGT -> ">";
                case Opcodes.IF_ICMPLE -> "<=";
                default -> "==";
            };
            return a + " " + cmpOp + " " + b;
        }
        String a = popSafe(stack);
        boolean intExpr = isIntExpr(a, scopedVars, insnIdx);
        return switch (op) {
            case Opcodes.IFEQ -> a.equals("0") ? "true" : (intExpr ? a + " == 0" : negateCondition(toBooleanExpr(a)));
            case Opcodes.IFNE -> intExpr ? a + " != 0" : toBooleanExpr(a);
            case Opcodes.IFLT -> a + " < 0";
            case Opcodes.IFGE -> a + " >= 0";
            case Opcodes.IFGT -> a + " > 0";
            case Opcodes.IFLE -> a + " <= 0";
            case Opcodes.IFNULL -> a + " == null";
            case Opcodes.IFNONNULL -> a + " != null";
            default -> a;
        };
    }

    private static class TernaryMatch {
        final String trueExpr;
        final String falseExpr;
        final int endInsnIdx;

        TernaryMatch(String trueExpr, String falseExpr, int endInsnIdx) {
            this.trueExpr = trueExpr;
            this.falseExpr = falseExpr;
            this.endInsnIdx = endInsnIdx;
        }
    }

    private TernaryMatch tryMatchTernary(AbstractInsnNode[] insns, int jumpIdx, int elseIdx,
                                         String currentPkg, Set<String> imports, List<ScopedVar> scopedVars) {
        if (elseIdx <= jumpIdx || elseIdx >= insns.length) return null;
        // Find GOTO just before elseIdx
        int gotoIdx = elseIdx - 1;
        while (gotoIdx > jumpIdx && insns[gotoIdx].getOpcode() < 0) gotoIdx--;
        if (gotoIdx <= jumpIdx || !(insns[gotoIdx] instanceof JumpInsnNode gotoInsn) || gotoInsn.getOpcode() != Opcodes.GOTO) {
            return null;
        }
        int mergeIdx = -1;
        for (int k = elseIdx + 1; k < Math.min(insns.length, elseIdx + 10); k++) {
            if (insns[k] == gotoInsn.label) {
                mergeIdx = k;
                break;
            }
        }
        if (mergeIdx < 0) return null;

        String trueExpr = evalSinglePushSlice(insns, jumpIdx + 1, gotoIdx - 1, currentPkg, imports, scopedVars);
        String falseExpr = evalSinglePushSlice(insns, elseIdx + 1, mergeIdx - 1, currentPkg, imports, scopedVars);
        if (trueExpr != null && falseExpr != null) {
            return new TernaryMatch(trueExpr, falseExpr, mergeIdx);
        }
        return null;
    }

    private String evalSinglePushSlice(AbstractInsnNode[] insns, int start, int end,
                                       String currentPkg, Set<String> imports, List<ScopedVar> scopedVars) {
        List<AbstractInsnNode> real = new ArrayList<>();
        int lastIdx = start;
        for (int k = start; k <= end; k++) {
            if (insns[k] instanceof LabelNode) return null;
            if (insns[k].getOpcode() >= 0) {
                real.add(insns[k]);
                lastIdx = k;
            }
        }
        if (real.size() == 1) {
            AbstractInsnNode n = real.get(0);
            int op = n.getOpcode();
            return switch (op) {
                case Opcodes.ACONST_NULL -> "null";
                case Opcodes.ICONST_M1 -> "-1";
                case Opcodes.ICONST_0 -> "0";
                case Opcodes.ICONST_1 -> "1";
                case Opcodes.ICONST_2 -> "2";
                case Opcodes.ICONST_3 -> "3";
                case Opcodes.ICONST_4 -> "4";
                case Opcodes.ICONST_5 -> "5";
                case Opcodes.LCONST_0 -> "0L";
                case Opcodes.LCONST_1 -> "1L";
                case Opcodes.FCONST_0 -> "0.0f";
                case Opcodes.FCONST_1 -> "1.0f";
                case Opcodes.FCONST_2 -> "2.0f";
                case Opcodes.DCONST_0 -> "0.0";
                case Opcodes.DCONST_1 -> "1.0";
                case Opcodes.BIPUSH, Opcodes.SIPUSH -> String.valueOf(((IntInsnNode) n).operand);
                case Opcodes.LDC -> formatConstant(((LdcInsnNode) n).cst, null, currentPkg, imports);
                case Opcodes.ILOAD, Opcodes.LLOAD, Opcodes.FLOAD, Opcodes.DLOAD, Opcodes.ALOAD -> {
                    int v = ((VarInsnNode) n).var;
                    ScopedVar sv = resolveVar(scopedVars, v, lastIdx);
                    yield sv != null ? sv.name : "var" + v;
                }
                case Opcodes.GETSTATIC -> {
                    FieldInsnNode fin = (FieldInsnNode) n;
                    yield formatInternalName(fin.owner, currentPkg, imports) + "." + fin.name;
                }
                default -> null;
            };
        }
        if (real.size() == 2 && real.get(0).getOpcode() == Opcodes.ALOAD && real.get(1) instanceof FieldInsnNode fin && fin.getOpcode() == Opcodes.GETFIELD) {
            int v = ((VarInsnNode) real.get(0)).var;
            ScopedVar sv = resolveVar(scopedVars, v, lastIdx);
            String obj = sv != null ? sv.name : (v == 0 ? "this" : "var" + v);
            return obj + "." + fin.name;
        }
        return null;
    }

    /**
     * AST Control-Flow Structuring Pass:
     * Transforms raw IF (...) GOTO LABEL_X / GOTO LABEL_Y / LABEL_X: sequences into
     * structured Java `if (...) { ... } else { ... }`, `while (...) { ... }`, and guard clauses.
     */
    private List<String> structureStatements(List<String> raw, String baseIndent) {
        List<String> stmts = new ArrayList<>(raw);

        // Pass 1: Combine consecutive IFs jumping to the exact same label:
        //   IF (c1) GOTO LABEL_X;
        //   IF (c2) GOTO LABEL_X;
        // -> IF ((c1) || (c2)) GOTO LABEL_X;
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i + 1 < stmts.size(); i++) {
                Matcher m1 = IF_GOTO_PAT.matcher(stmts.get(i));
                Matcher m2 = IF_GOTO_PAT.matcher(stmts.get(i + 1));
                if (m1.matches() && m2.matches() && m1.group(2).equals(m2.group(2))) {
                    stmts.set(i, "IF ((" + m1.group(1) + ") || (" + m2.group(1) + ")) GOTO LABEL_" + m1.group(2) + ";");
                    stmts.remove(i + 1);
                    changed = true;
                    break;
                }
                // Also detect short-circuit OR guard pattern:
                //   IF (c1) GOTO LABEL_A;
                //   IF (c2) GOTO LABEL_B;
                //   LABEL_A:
                //   return ...;
                //   LABEL_B:
                if (i + 4 < stmts.size() && m1.matches() && m2.matches()) {
                    String lblA = "LABEL_" + m1.group(2) + ":";
                    String lblB = "LABEL_" + m2.group(2) + ":";
                    if (stmts.get(i + 2).equals(lblA)
                            && (stmts.get(i + 3).startsWith("return") || stmts.get(i + 3).startsWith("throw "))
                            && stmts.get(i + 4).equals(lblB)
                            && countLabelRefs(stmts, m1.group(2)) == 1) {
                        String combinedCond = "(" + m1.group(1) + ") || (" + negateCondition(m2.group(1)) + ")";
                        stmts.set(i, "if (" + combinedCond + ") {");
                        stmts.set(i + 1, "    " + stmts.get(i + 3));
                        stmts.set(i + 2, "}");
                        stmts.remove(i + 3);
                        changed = true;
                        break;
                    }
                }
            }
        }

        // Pass 2: Structure guard clauses:
        //   IF (cond) GOTO LABEL_X;
        //   return ...; (or throw ...;)
        //   LABEL_X:
        changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i + 2 < stmts.size(); i++) {
                Matcher m = IF_GOTO_PAT.matcher(stmts.get(i));
                if (m.matches()) {
                    String s1 = stmts.get(i + 1);
                    String s2 = stmts.get(i + 2);
                    String targetLbl = "LABEL_" + m.group(2) + ":";
                    if ((s1.startsWith("return") || s1.startsWith("throw ")) && s2.equals(targetLbl)) {
                        String neg = negateCondition(m.group(1));
                        stmts.set(i, "if (" + neg + ") " + s1);
                        stmts.remove(i + 1);
                        if (countLabelRefs(stmts, m.group(2)) == 0) {
                            stmts.remove(i + 1);
                        }
                        changed = true;
                        break;
                    }
                }
            }
        }

        // Pass 3: Render into indented lines, transforming forward IF..LABEL blocks into structured `if (...) { ... } else { ... }`
        // and removing unused labels.
        Set<String> usedLabels = new HashSet<>();
        for (String s : stmts) {
            Matcher gm = GOTO_REF_PAT.matcher(s);
            while (gm.find()) usedLabels.add(gm.group(1));
        }

        // Pass 2.5: Detect while loops:
        //   LABEL_head:
        //   IF (exitCond) GOTO LABEL_exit;
        //   ...
        //   GOTO LABEL_head;
        //   LABEL_exit:
        Set<String> whileHeadLabels = new HashSet<>();
        Map<String, String> whileExitToHead = new HashMap<>();
        for (int i = 0; i + 2 < stmts.size(); i++) {
            String s0 = stmts.get(i);
            if (s0.startsWith("LABEL_") && s0.endsWith(":")) {
                String headLid = s0.substring(6, s0.length() - 1);
                Matcher im = IF_GOTO_PAT.matcher(stmts.get(i + 1));
                if (im.matches()) {
                    String exitLid = im.group(2);
                    int exitIdx = stmts.indexOf("LABEL_" + exitLid + ":");
                    if (exitIdx > i + 2 && stmts.get(exitIdx - 1).equals("GOTO LABEL_" + headLid + ";")) {
                        whileHeadLabels.add(headLid);
                        whileExitToHead.put(exitLid, headLid);
                    }
                }
            }
        }

        List<String> formatted = new ArrayList<>();
        int indentLevel = 0;
        Map<String, Integer> closeBracesAtLabel = new HashMap<>();
        Map<String, String> elseAtLabel = new HashMap<>();

        boolean inCatchWithoutEndLabel = false;
        for (int i = 0; i < stmts.size(); i++) {
            String s = stmts.get(i);
            if (s.equals("TRY_START:")) {
                formatted.add(baseIndent + "    ".repeat(indentLevel) + "try {");
                indentLevel++;
                continue;
            }
            if (s.startsWith("// catch (") && s.endsWith(")")) {
                String catchSig = s.substring(3);
                // If previous emitted line was break label_X, that label X is the end of the catch block!
                if (!formatted.isEmpty() && formatted.get(formatted.size() - 1).trim().startsWith("break label_")) {
                    String lastLine = formatted.remove(formatted.size() - 1).trim();
                    String afterLid = lastLine.substring("break label_".length(), lastLine.length() - 1);
                    if (countLabelRefs(stmts, afterLid) <= 1) usedLabels.remove(afterLid);
                    indentLevel = Math.max(0, indentLevel - 1);
                    formatted.add(baseIndent + "    ".repeat(indentLevel) + "} " + catchSig + " {");
                    indentLevel++;
                    closeBracesAtLabel.merge(afterLid, 1, Integer::sum);
                } else {
                    indentLevel = Math.max(0, indentLevel - 1);
                    formatted.add(baseIndent + "    ".repeat(indentLevel) + "} " + catchSig + " {");
                    indentLevel++;
                    inCatchWithoutEndLabel = true;
                }
                continue;
            }

            if (s.startsWith("LABEL_") && s.endsWith(":")) {
                String lid = s.substring(6, s.length() - 1);
                if (whileHeadLabels.contains(lid) && i + 1 < stmts.size()) {
                    Matcher im = IF_GOTO_PAT.matcher(stmts.get(i + 1));
                    if (im.matches()) {
                        String exitCond = im.group(1);
                        String exitLid = im.group(2);
                        int exitIdx = stmts.indexOf("LABEL_" + exitLid + ":");
                        if (exitIdx > i + 1 && stmts.get(exitIdx - 1).equals("GOTO LABEL_" + lid + ";")) {
                            stmts.remove(exitIdx - 1); // remove trailing back-edge GOTO LABEL_head
                            if (countLabelRefs(stmts, exitLid) <= 1) usedLabels.remove(exitLid);
                            if (countLabelRefs(stmts, lid) == 0) usedLabels.remove(lid);
                            formatted.add(baseIndent + "    ".repeat(indentLevel) + "while (" + negateCondition(exitCond) + ") {");
                            indentLevel++;
                            closeBracesAtLabel.merge(exitLid, 1, Integer::sum);
                            i++; // skip the IF_GOTO
                            continue;
                        }
                    }
                }
                if (elseAtLabel.containsKey(lid)) {
                    indentLevel = Math.max(0, indentLevel - 1);
                    formatted.add(baseIndent + "    ".repeat(indentLevel) + "} else {");
                    indentLevel++;
                    elseAtLabel.remove(lid);
                    continue;
                }
                if (closeBracesAtLabel.containsKey(lid)) {
                    int count = closeBracesAtLabel.remove(lid);
                    for (int c = 0; c < count; c++) {
                        indentLevel = Math.max(0, indentLevel - 1);
                        formatted.add(baseIndent + "    ".repeat(indentLevel) + "}");
                    }
                    if (!usedLabels.contains(lid)) continue;
                }
                if (usedLabels.contains(lid)) {
                    formatted.add(baseIndent + "    ".repeat(indentLevel) + "label_" + lid + ":");
                }
                continue;
            }

            Matcher im = IF_GOTO_PAT.matcher(s);
            if (im.matches()) {
                String cond = im.group(1);
                String lid = im.group(2);
                int targetIdx = stmts.indexOf("LABEL_" + lid + ":");
                if (targetIdx > i && countLabelRefs(stmts, lid) == 1 && !hasCrossJumps(stmts, i + 1, targetIdx)) {
                    // Check if immediately before LABEL_lid there is a GOTO LABEL_end (if-else)
                    if (targetIdx - 1 > i) {
                        Matcher gm = ONLY_GOTO_PAT.matcher(stmts.get(targetIdx - 1));
                        if (gm.matches()) {
                            String endLid = gm.group(1);
                            int endIdx = stmts.indexOf("LABEL_" + endLid + ":");
                            if (endIdx > targetIdx && countLabelRefs(stmts, endLid) == 1) {
                                usedLabels.remove(lid);
                                usedLabels.remove(endLid);
                                stmts.remove(targetIdx - 1); // remove the GOTO LABEL_end
                                formatted.add(baseIndent + "    ".repeat(indentLevel) + "if (" + negateCondition(cond) + ") {");
                                indentLevel++;
                                elseAtLabel.put(lid, endLid);
                                closeBracesAtLabel.merge(endLid, 1, Integer::sum);
                                continue;
                            }
                        }
                    }
                    usedLabels.remove(lid);
                    formatted.add(baseIndent + "    ".repeat(indentLevel) + "if (" + negateCondition(cond) + ") {");
                    indentLevel++;
                    closeBracesAtLabel.merge(lid, 1, Integer::sum);
                    continue;
                }
                formatted.add(baseIndent + "    ".repeat(indentLevel) + "if (" + cond + ") break label_" + lid + ";");
                continue;
            }

            Matcher gm = ONLY_GOTO_PAT.matcher(s);
            if (gm.matches()) {
                String lid = gm.group(1);
                formatted.add(baseIndent + "    ".repeat(indentLevel) + "break label_" + lid + ";");
                continue;
            }

            formatted.add(baseIndent + "    ".repeat(indentLevel) + s.replace("GOTO LABEL_", "break label_"));
            if (inCatchWithoutEndLabel && (s.startsWith("return") || s.startsWith("throw "))) {
                indentLevel = Math.max(0, indentLevel - 1);
                formatted.add(baseIndent + "    ".repeat(indentLevel) + "}");
                inCatchWithoutEndLabel = false;
            }
        }

        while (indentLevel > 0) {
            indentLevel--;
            formatted.add(baseIndent + "    ".repeat(indentLevel) + "}");
        }
        return formatted;
    }

    private static final Pattern IF_GOTO_PAT = Pattern.compile("^IF \\((.*)\\) GOTO LABEL_(\\d+);$");
    private static final Pattern ONLY_GOTO_PAT = Pattern.compile("^GOTO LABEL_(\\d+);$");
    private static final Pattern GOTO_REF_PAT = Pattern.compile("GOTO LABEL_(\\d+)");

    private int countLabelRefs(List<String> stmts, String lid) {
        int count = 0;
        String token = "GOTO LABEL_" + lid;
        for (String s : stmts) {
            if (s.contains(token)) count++;
        }
        return count;
    }

    private boolean hasCrossJumps(List<String> stmts, int start, int end) {
        for (int k = start; k < end; k++) {
            String s = stmts.get(k);
            if (s.startsWith("// catch ")) return true;
        }
        return false;
    }

    private String negateCondition(String cond) {
        cond = cond.trim();
        if (cond.startsWith("!(") && cond.endsWith(")")) {
            String inner = cond.substring(2, cond.length() - 1);
            if (isBalancedParens(inner)) return inner;
        }
        // Check top-level || or && (De Morgan's Law)
        int topOr = findTopLevelOp(cond, " || ");
        if (topOr > 0) {
            String left = cond.substring(0, topOr).trim();
            String right = cond.substring(topOr + 4).trim();
            return negateCondition(unwrapParens(left)) + " && " + negateCondition(unwrapParens(right));
        }
        int topAnd = findTopLevelOp(cond, " && ");
        if (topAnd > 0) {
            String left = cond.substring(0, topAnd).trim();
            String right = cond.substring(topAnd + 4).trim();
            return negateCondition(unwrapParens(left)) + " || " + negateCondition(unwrapParens(right));
        }
        // Simple binary comparison
        String[][] ops = {
            {" == ", " != "},
            {" != ", " == "},
            {" <= ", " > "},
            {" >= ", " < "},
            {" < ", " >= "},
            {" > ", " <= "}
        };
        for (String[] pair : ops) {
            int pos = findTopLevelOp(cond, pair[0]);
            if (pos > 0) {
                return cond.substring(0, pos) + pair[1] + cond.substring(pos + pair[0].length());
            }
        }
        if (cond.equals("true")) return "false";
        if (cond.equals("false")) return "true";
        return "!" + (isSimpleAtom(cond) ? cond : "(" + cond + ")");
    }

    private String unwrapParens(String s) {
        s = s.trim();
        while (s.startsWith("(") && s.endsWith(")") && isBalancedParens(s.substring(1, s.length() - 1))) {
            s = s.substring(1, s.length() - 1).trim();
        }
        return s;
    }

    private boolean isBalancedParens(String s) {
        int depth = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') {
                depth--;
                if (depth < 0) return false;
            }
        }
        return depth == 0;
    }

    private int findTopLevelOp(String s, String op) {
        int depth = 0;
        boolean inStr = false;
        for (int i = 0; i <= s.length() - op.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) inStr = !inStr;
            if (inStr) continue;
            if (c == '(') depth++;
            else if (c == ')') depth--;
            else if (depth == 0 && s.startsWith(op, i)) {
                return i;
            }
        }
        return -1;
    }

    private String formatInlineLambda(ClassNode cn, MethodNode lambdaMn, int capturedCount,
                                      String indent, String currentPkg, Set<String> imports,
                                      Map<String, MethodNode> lambdaMethods) {
        Type[] lArgs = Type.getArgumentTypes(lambdaMn.desc);
        boolean isStatic = (lambdaMn.access & Opcodes.ACC_STATIC) != 0;
        List<ScopedVar> scopedVars = buildScopedVars(lambdaMn, isStatic, lArgs);
        int slot = isStatic ? 0 : 1;
        for (int i = 0; i < Math.min(capturedCount, lArgs.length); i++) {
            slot += lArgs[i].getSize();
        }
        List<String> pNames = new ArrayList<>();
        for (int i = capturedCount; i < lArgs.length; i++) {
            ScopedVar sv = resolveVar(scopedVars, slot, 0);
            pNames.add(sv != null ? sv.name : "p" + i);
            slot += lArgs[i].getSize();
        }
        List<String> raw = decompileInstructions(cn, lambdaMn, "", currentPkg, imports, lambdaMethods, false);
        List<String> body = structureStatements(raw, indent + "    ");
        if (body.size() == 1) {
            String single = body.get(0).trim();
            if (single.startsWith("return ") && single.endsWith(";")) {
                return "(" + String.join(", ", pNames) + ") -> " + single.substring(7, single.length() - 1);
            } else if (!single.startsWith("if ") && !single.startsWith("switch ") && single.endsWith(";")) {
                return "(" + String.join(", ", pNames) + ") -> " + single.substring(0, single.length() - 1);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(String.join(", ", pNames)).append(") -> {\n");
        for (String line : body) sb.append(line).append("\n");
        sb.append(indent).append("}");
        return sb.toString();
    }

    private String buildStringConcat(String recipe, List<String> args, Object[] bsmArgs) {
        List<String> parts = new ArrayList<>();
        StringBuilder literal = new StringBuilder();
        int argIdx = 0;
        int constIdx = 1;
        for (int i = 0; i < recipe.length(); i++) {
            char c = recipe.charAt(i);
            if (c == '\u0001') {
                if (literal.length() > 0) {
                    parts.add(escapeString(literal.toString()));
                    literal.setLength(0);
                }
                parts.add(argIdx < args.size() ? args.get(argIdx++) : "\"\"");
            } else if (c == '\u0002') {
                if (literal.length() > 0) {
                    parts.add(escapeString(literal.toString()));
                    literal.setLength(0);
                }
                Object cVal = (bsmArgs != null && constIdx < bsmArgs.length) ? bsmArgs[constIdx++] : "";
                parts.add(escapeString(String.valueOf(cVal)));
            } else {
                literal.append(c);
            }
        }
        if (literal.length() > 0) {
            parts.add(escapeString(literal.toString()));
        }
        if (parts.isEmpty()) return "\"\"";
        return "(" + String.join(" + ", parts) + ")";
    }

    private boolean isEnumField(ClassNode cn, String name) {
        for (FieldNode fn : cn.fields) {
            if (fn.name.equals(name) && (fn.access & Opcodes.ACC_ENUM) != 0) return true;
        }
        return false;
    }

    private void binaryOp(Deque<String> stack, String op) {
        String b = popSafe(stack);
        String a = popSafe(stack);
        stack.push("(" + a + " " + op + " " + b + ")");
    }

    private void castOp(Deque<String> stack, String type) {
        String a = popSafe(stack);
        stack.push("((" + type + ") " + a + ")");
    }

    private String popSafe(Deque<String> stack) {
        return stack.isEmpty() ? "null" : stack.pop();
    }

    private boolean isSimpleAtom(String expr) {
        if (expr == null) return false;
        return expr.matches("[a-zA-Z0-9_.$]+") || expr.startsWith("\"");
    }

    private boolean isStatementExpr(String expr) {
        if (expr == null) return false;
        return expr.contains("(") || expr.startsWith("new ");
    }

    private String toBooleanExpr(String expr) {
        if ("1".equals(expr)) return "true";
        if ("0".equals(expr)) return "false";
        if (expr != null && expr.startsWith("(") && expr.endsWith(" ? 1 : 0)")) {
            return expr.substring(1, expr.length() - 9);
        }
        if (expr != null && expr.startsWith("(") && expr.endsWith(" ? 0 : 1)")) {
            return negateCondition(expr.substring(1, expr.length() - 9));
        }
        return expr;
    }

    private String inferTypeFromOp(int op) {
        return switch (op) {
            case Opcodes.ISTORE -> "int";
            case Opcodes.LSTORE -> "long";
            case Opcodes.FSTORE -> "float";
            case Opcodes.DSTORE -> "double";
            default -> "Object";
        };
    }

    private String formatConstant(Object cst, Type fieldType, String currentPkg, Set<String> imports) {
        if (cst instanceof String s) return escapeString(s);
        if (cst instanceof Type t) return formatType(t, currentPkg, imports) + ".class";
        if (cst instanceof Integer iv) {
            if (fieldType != null && fieldType.getSort() == Type.BOOLEAN) {
                return iv != 0 ? "true" : "false";
            }
            return String.valueOf(iv);
        }
        if (cst instanceof Long lv) return lv + "L";
        if (cst instanceof Float fv) {
            if (Float.isNaN(fv)) return "Float.NaN";
            if (Float.isInfinite(fv)) return fv > 0 ? "Float.POSITIVE_INFINITY" : "Float.NEGATIVE_INFINITY";
            return fv + "f";
        }
        if (cst instanceof Double dv) {
            if (Double.isNaN(dv)) return "Double.NaN";
            if (Double.isInfinite(dv)) return dv > 0 ? "Double.POSITIVE_INFINITY" : "Double.NEGATIVE_INFINITY";
            return String.valueOf(dv);
        }
        return String.valueOf(cst);
    }

    private String escapeString(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 32 || c > 126) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: AsmJavaDecompiler <input.jar> <outputSrcDir>");
            return;
        }
        AsmJavaDecompiler decompiler = new AsmJavaDecompiler();
        decompiler.loadJar(Paths.get(args[0]));
        int count = decompiler.decompileAll(Paths.get(args[1]));
        System.out.println("Decompiled " + decompiler.allClasses.size() + " classes into " + count + " .java source files in " + args[1]);
    }
}
