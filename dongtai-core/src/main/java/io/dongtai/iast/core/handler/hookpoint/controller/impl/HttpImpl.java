package io.dongtai.iast.core.handler.hookpoint.controller.impl;

import io.dongtai.iast.core.handler.hookpoint.IastClassLoader;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.trace.Tracer;
import io.dongtai.iast.core.service.ErrorLogReport;
import io.dongtai.iast.core.utils.HttpClientUtils;
import io.dongtai.log.DongTaiLog;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Http方法处理入口
 *
 * @author dongzhiyong@huoxian.cn
 */
public class HttpImpl {

    private static Method cloneRequestMethod;
    private static Method cloneResponseMethod;
    private static Class<?> CLASS_OF_SERVLET_PROXY;
    private static IastClassLoader iastClassLoader;
    public static File IAST_REQUEST_JAR_PACKAGE;

    static {
        IAST_REQUEST_JAR_PACKAGE = new File(System.getProperty("java.io.tmpdir") + File.separator + "iast" + File.separator + "dongtai-api.jar");
        if (!IAST_REQUEST_JAR_PACKAGE.exists()) {
            HttpClientUtils.downloadRemoteJar("/api/v1/engine/download?engineName=dongtai-api", IAST_REQUEST_JAR_PACKAGE.getAbsolutePath());
        }
    }


    private static void createClassLoader(Object req) {
        try {
            if (iastClassLoader != null) {
                return;
            }
            if (IAST_REQUEST_JAR_PACKAGE.exists()) {
                iastClassLoader = new IastClassLoader(
                        req.getClass().getClassLoader(),
                        new URL[]{IAST_REQUEST_JAR_PACKAGE.toURI().toURL()}
                );
                CLASS_OF_SERVLET_PROXY = iastClassLoader.loadClass("io.dongtai.api.ServletProxy");
                cloneRequestMethod = CLASS_OF_SERVLET_PROXY
                        .getDeclaredMethod("cloneRequest", Object.class, boolean.class);
                cloneResponseMethod = CLASS_OF_SERVLET_PROXY
                        .getDeclaredMethod("cloneResponse", Object.class, boolean.class);
            }
        } catch (MalformedURLException e) {
            DongTaiLog.error("create classLoader failure [MalformedURLException], reason: {}", e.getMessage());
            ErrorLogReport.sendErrorLog(e);
        } catch (NoSuchMethodException e) {
            DongTaiLog.error("create classLoader failure [NoSuchMethodException], reason: {}", e.getMessage());
            ErrorLogReport.sendErrorLog(e);
        } catch (Exception e) {
            DongTaiLog.error("create classLoader failure, reason: {}", e.getMessage());
            ErrorLogReport.sendErrorLog(e);
        }
    }

    private static void loadCloneResponseMethod() {
        if (cloneResponseMethod == null) {
            try {
                cloneResponseMethod = CLASS_OF_SERVLET_PROXY
                        .getDeclaredMethod("cloneResponse", Object.class, boolean.class);
            } catch (Exception e) {
                DongTaiLog.error("load response method failure, reason: {}", e.getMessage());
                ErrorLogReport.sendErrorLog(e);
            }
        }
    }

    /**
     * @param req       request object
     * @param isJakarta Is it a jakarta api request object
     * @return
     */
    public static Object cloneRequest(Object req, boolean isJakarta) {
        if (req == null) {
            return null;
        }
        try {
            createClassLoader(req);
            if (cloneRequestMethod != null) {
                return cloneRequestMethod.invoke(null, req, isJakarta);
            }
        } catch (Exception e) {
            DongTaiLog.info("clone request failure, reason: cloneRequestMethod is null");
            ErrorLogReport.sendErrorLog(e);
        }
        return req;
    }

    /**
     * Clone the response object, get the response header and response body
     *
     * @param response original response object
     * @return dongtai response object
     */
    public static Object cloneResponse(Object response, boolean isJakarta) {
        if (response == null) {
            return null;
        }
        try {
            loadCloneResponseMethod();
            if (cloneResponseMethod != null) {
                return cloneResponseMethod.invoke(null, response, isJakarta);
            }
        } catch (Exception e) {
            DongTaiLog.info("clone request failure, reason: cloneRequestMethod is null");
            ErrorLogReport.sendErrorLog(e);
        }
        return response;
    }

    public static Map<String, Object> getRequestMeta(Object request)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (cloneRequestMethod != null) {
            Method methodOfRequestMeta = request.getClass().getDeclaredMethod("getRequestMeta");
            return (Map<String, Object>) methodOfRequestMeta.invoke(request);
        }
        return new HashMap<String, Object>();
    }

    public static String getPostBody(Object request) {
        try {
            if (cloneRequestMethod != null) {
                Method methodOfRequestMeta = request.getClass().getDeclaredMethod("getPostBody");
                return (String) methodOfRequestMeta.invoke(request);
            }
        } catch (NoSuchMethodException ignored) {
        } catch (IllegalAccessException ignored) {
        } catch (InvocationTargetException ignored) {
        }
        return null;
    }

    public static Map<String, Object> getResponseMeta(Object response) {
        Method methodOfRequestMeta = null;
        try {
            if (cloneResponseMethod != null) {
                methodOfRequestMeta = response.getClass().getDeclaredMethod("getResponseMeta");
                return (Map<String, Object>) methodOfRequestMeta.invoke(response);
            }
        } catch (NoSuchMethodException ignored) {
        } catch (IllegalAccessException ignored) {
        } catch (InvocationTargetException ignored) {
        }
        return null;
    }

    /**
     * solve http request
     *
     * @param event method call event
     */
    public static void solveHttp(MethodEvent event) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Map<String, Object> requestMeta = getRequestMeta(event.argumentArray[0]);
        Tracer.startHook(requestMeta);

        if (DongTaiLog.isDebugEnabled()) {
            DongTaiLog.debug("HTTP Request:{} {} from: {}", requestMeta.get("method"), requestMeta.get("requestURI"),
                    event.getMethodDesc());
        }
    }

}
