package io.dongtai.iast.core.handler.trace;

import com.alibaba.ttl.TransmittableThreadLocal;
import io.dongtai.iast.core.handler.hookpoint.controller.TrackerHelper;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Tracer {
    private static ThreadLocal<TraceContext> ttlContext = new TransmittableThreadLocal<TraceContext>();

    private static ThreadLocal<TraceContext> normalContext = new ThreadLocal<TraceContext>();

    /**
     * 开启追踪一次调用，非线程安全
     *
     * @return 调用上下文
     */
    public static void start(Map<String, Object> requestMeta) {
        TraceContext context = new TraceContext();
        context.setEntryState(1);
        context.setRequestMeta(requestMeta);
        context.setTraceMethodMap(new HashMap<Integer, MethodEvent>(1024));
        context.setMethodTaintPool(new HashSet<Object>());
        context.setMethodTaintSignature(new HashSet<Integer>());
        context.setTrackerHelper(new TrackerHelper());
        Tracer.getContextCarrie().set(context);
    }

    /**
     * 获取当前上下文
     *
     * @return TraceContext
     */
    public static TraceContext getContext() {
        return Tracer.getContextCarrie().get();
    }

    /**
     * 结束追踪一次调用，清理上下文
     */
    public static void end() {
        getContextCarrie().remove();
    }

    /**
     * 根据用户是否开启ttl选择合适的载体
     *
     * @return 上下文threadLocal载体
     */
    private static ThreadLocal<TraceContext> getContextCarrie() {
        return System.getProperty("dongtai.ttl", "false").equals("true") ? ttlContext : normalContext;
    }

}
