package io.dongtai.iast.core.bytecode.enhance.plugin.thread;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractAdviceAdapter;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import io.dongtai.iast.core.handler.hookpoint.controller.HookType;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class DispatchThreadPlugin implements DispatchPlugin {
    private String className;
    private final String classOfThreadPoolExecutor = " java.util.concurrent.ThreadPoolExecutor".substring(1);
    private final String classOfScheduledThreadPoolExecutor = " java.util.concurrent.ScheduledThreadPoolExecutor".substring(1);

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        this.className = context.getClassName();
        String matchClassName = isMatch();
        if (matchClassName != null) {
            context.setMatchClassName(matchClassName);
            classVisitor = new DispatchThreadPlugin.ClassVisit(classVisitor, context);
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        if (className.equals(classOfThreadPoolExecutor) || className.equals(classOfScheduledThreadPoolExecutor)) {
            return className;
        }
        return null;
    }

    class ClassVisit extends AbstractClassVisitor {

        ClassVisit(ClassVisitor classVisitor, IastContext context) {
            super(classVisitor, context);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            // todo:
            if (name.equals("execute") || name.equals("submit") || name.equals("schedule") || name.equals("scheduleAtFixedRate") || name.equals("scheduleWithFixedDelay")) {
                mv = new DispatchThreadPlugin.AdviceAdapter(mv, access, name, desc, context, "Thread", signature);
            }
            return mv;
        }

        @Override
        public boolean hasTransformed() {
            return transformed;
        }
    }

    class AdviceAdapter extends AbstractAdviceAdapter {

        public AdviceAdapter(MethodVisitor mv, int access, String name, String desc, IastContext context, String type, String signCode) {
            super(mv, access, name, desc, context, type, signCode);
        }

        @Override
        protected void before() {
            mark(tryLabel);
            mark(catchLabel);
        }

        @Override
        protected void after(int opcode) {
            if (!isThrow(opcode)) {
                Label endLabel = new Label();
                captureMethodState(opcode, HookType.SPRINGAPPLICATION.getValue(), true);
                mark(endLabel);
            }
        }
    }

}
