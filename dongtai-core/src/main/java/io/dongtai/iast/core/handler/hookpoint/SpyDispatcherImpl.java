package io.dongtai.iast.core.handler.hookpoint;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.bytecode.enhance.plugin.spring.SpringApplicationImpl;
import io.dongtai.iast.core.handler.hookpoint.controller.HookType;
import io.dongtai.iast.core.handler.hookpoint.controller.impl.DubboImpl;
import io.dongtai.iast.core.handler.hookpoint.controller.impl.HttpImpl;
import io.dongtai.iast.core.handler.hookpoint.controller.impl.PropagatorImpl;
import io.dongtai.iast.core.handler.hookpoint.controller.impl.SinkImpl;
import io.dongtai.iast.core.handler.hookpoint.controller.impl.SourceImpl;
import io.dongtai.iast.core.handler.hookpoint.graphy.GraphBuilder;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.trace.TraceContext;
import io.dongtai.iast.core.handler.trace.Tracer;
import io.dongtai.iast.core.service.ErrorLogReport;

import java.lang.dongtai.SpyDispatcher;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @since 1.3.1
 */
public class SpyDispatcherImpl implements SpyDispatcher {

    public static final AtomicInteger INVOKE_ID_SEQUENCER = new AtomicInteger(1);

    /**
     * mark for enter Http Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void enterHttp() {
        try {
            EngineManager.SCOPE_TRACKER.enterHttp();
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
    }

    /**
     * mark for leave Http Entry Point
     *
     * @param response HttpResponse Object for collect http response body.
     * @since 1.3.1
     */
    @Override
    public void leaveHttp(Object request, Object response) {
        try {
            TraceContext context = Tracer.getContext();
            if (context.supportCollect()) {
                context.stopCollect();

                EngineManager.SCOPE_TRACKER.leaveHttp();
                if (Tracer.getContext().isInEntry() && EngineManager.SCOPE_TRACKER.isExitedHttp()) {
                    EngineManager.maintainRequestCount();
                    GraphBuilder.buildAndReport(request, response);
                    EngineManager.cleanThreadState();
                }

                context.restartCollect();
            }

        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
            EngineManager.cleanThreadState();
        }
    }

    /**
     * Determines whether it is a layer 1 HTTP entry
     *
     * @return
     * @since 1.3.1
     */
    @Override
    public boolean isFirstLevelHttp() {
        try {
            return EngineManager.isEngineRunning() && EngineManager.SCOPE_TRACKER
                    .isFirstLevelHttp();
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
        return false;
    }

    /**
     * clone request object for copy http post body.
     *
     * @param req       HttpRequest Object
     * @param isJakarta true if jakarta-servlet-api else false
     * @return
     * @since 1.3.1
     */
    @Override
    public Object cloneRequest(Object req, boolean isJakarta) {
        return HttpImpl.cloneRequest(req, isJakarta);
    }

    /**
     * clone response object for copy http response data.
     *
     * @param response
     * @param isJakarta
     * @return
     * @since 1.3.1
     */
    @Override
    public Object cloneResponse(Object response, boolean isJakarta) {
        return HttpImpl.cloneResponse(response, isJakarta);
    }

    /**
     * mark for enter Dubbo Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void enterDubbo() {
        try {
            EngineManager.SCOPE_TRACKER.enterDubbo();
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
    }

    /**
     * mark for leave Dubbo Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void leaveDubbo() {
        try {
            TraceContext context = Tracer.getContext();
            if (context.supportCollect()) {
                context.stopCollect();

                context.getTrackerHelper().leaveDubbo();
                if (context.getTrackerHelper().isExitedDubbo() && !context.isInEntry()) {
                    EngineManager.maintainRequestCount();
                    GraphBuilder.buildAndReport(null, null);
                    EngineManager.cleanThreadState();
                }

                context.restartCollect();
            }
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
            Tracer.end();
        }
    }

    /**
     * Determines whether it is a layer 1 Dubbo entry
     *
     * @return true if is a layer 1 Dubbo entry; else false
     * @since 1.3.1
     */
    @Override
    public boolean isFirstLevelDubbo() {
        try {
            TraceContext context = Tracer.getContext();
            return EngineManager.isEngineRunning() && context.getTrackerHelper().isFirstLevelDubbo();
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
        return false;
    }

    /**
     * mark for enter Source Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void enterSource() {
        try {
            TraceContext context = Tracer.getContext();
            if (context.supportCollect()) {
                EngineManager.SCOPE_TRACKER.enterSource();
            }
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
    }

    /**
     * mark for leave Source Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void leaveSource() {
        try {
            if (Tracer.getContext().supportCollect()) {
                EngineManager.SCOPE_TRACKER.leaveSource();
            }
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
    }

    /**
     * Determines whether it is a layer 1 Dubbo entry
     *
     * @return true if is a layer 1 Dubbo entry; else false
     * @since 1.3.1
     */
    @Override
    public boolean isFirstLevelSource() {
        try {
            return EngineManager.isEngineRunning() && Tracer.getContext().supportCollect() && EngineManager.SCOPE_TRACKER
                    .isFirstLevelSource();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * mark for enter Source Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void enterPropagator() {
        try {
            if (Tracer.getContext().supportCollect()) {
                EngineManager.SCOPE_TRACKER.enterPropagation();
            }
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
    }

    /**
     * mark for leave Source Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void leavePropagator() {
        try {
            if (Tracer.getContext().supportCollect()) {
                EngineManager.SCOPE_TRACKER.leavePropagation();
            }
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
    }

    /**
     * Determines whether it is a layer 1 Propagator entry
     *
     * @return true if is a layer 1 Propagator entry; else false
     * @since 1.3.1
     */
    @Override
    public boolean isFirstLevelPropagator() {
        try {
            return Tracer.getContext().supportCollect() && EngineManager.isEngineRunning() && EngineManager.SCOPE_TRACKER.isFirstLevelPropagator();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * mark for enter Sink Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void enterSink() {
        try {
            if (Tracer.getContext().supportCollect()) {
                EngineManager.SCOPE_TRACKER.enterSink();
            }
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
    }

    /**
     * mark for enter Sink Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void leaveSink() {
        try {
            if (Tracer.getContext().supportCollect()) {
                EngineManager.SCOPE_TRACKER.leaveSink();
            }
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
    }

    /**
     * Determines whether it is a layer 1 Sink entry
     *
     * @return
     * @since 1.3.1
     */
    @Override
    public boolean isFirstLevelSink() {
        try {
            TraceContext context = Tracer.getContext();
            return EngineManager.isEngineRunning() && context.supportCollect() && context.getTrackerHelper().isFirstLevelSink();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * mark for enter Source Entry Point
     *
     * @param instance       current class install object value, null if static class
     * @param argumentArray
     * @param retValue
     * @param framework
     * @param className
     * @param matchClassName
     * @param methodName
     * @param methodSign
     * @param isStatic
     * @param hookType
     * @return false if normal else throw a exception
     * @since 1.3.1
     */
    @Override
    public boolean collectMethodPool(Object instance, Object[] argumentArray, Object retValue, String framework,
                                     String className, String matchClassName, String methodName, String methodSign, boolean isStatic,
                                     int hookType) {
        TraceContext context = Tracer.getContext();
        boolean supportCollect = context.supportCollect();

        if (supportCollect || (HookType.HTTP.equals(hookType) || HookType.DUBBO.equals(hookType))) {
            try {
                if (supportCollect) {
                    context.stopCollect();
                }

                if (HookType.SPRINGAPPLICATION.equals(hookType)) {
                    if (!SpringApplicationImpl.isFinished()) {
                        MethodEvent event = new MethodEvent(0, -1, className, matchClassName, methodName,
                                methodSign, instance, argumentArray, retValue, framework, isStatic, null);
                        SpringApplicationImpl.getWebApplicationContext(event);
                    }
                } else {
                    boolean isEnterEntryPoint = context.isInEntry() || context.getTrackerHelper().isFirstLevelDubbo();
                    boolean isEntryPointMethod = HookType.HTTP.equals(hookType) || HookType.DUBBO.equals(hookType);
                    if (isEnterEntryPoint || isEntryPointMethod) {
                        MethodEvent event = new MethodEvent(0, -1, className, matchClassName, methodName,
                                methodSign, instance, argumentArray, retValue, framework, isStatic, null);
                        if (HookType.HTTP.equals(hookType)) {
                            HttpImpl.solveHttp(event);
                        } else if (HookType.DUBBO.equals(hookType)) {
                            DubboImpl.solveDubbo(event, INVOKE_ID_SEQUENCER);
                        } else if (HookType.PROPAGATOR.equals(hookType) && !context.getMethodTaintPool().isEmpty()) {
                            PropagatorImpl.solvePropagator(event, INVOKE_ID_SEQUENCER);
                        } else if (HookType.SOURCE.equals(hookType)) {
                            SourceImpl.solveSource(event, INVOKE_ID_SEQUENCER);
                        } else if (HookType.SINK.equals(hookType) && !context.getMethodTaintPool().isEmpty()) {
                            SinkImpl.solveSink(event);
                        }
                    }
                }
            } catch (Exception e) {
                ErrorLogReport.sendErrorLog(e);
            } finally {
                if (supportCollect) {
                    context.restartCollect();
                }
            }
        }
        return false;
    }
}
