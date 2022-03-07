package io.dongtai.iast.core.bytecode.enhance.plugin.spring;

import io.dongtai.iast.core.handler.hookpoint.IastClassLoader;
import io.dongtai.iast.core.handler.hookpoint.api.GetApiThread;
import io.dongtai.iast.core.handler.hookpoint.controller.impl.HttpImpl;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * niuerzhuang@huoxian.cn
 */
public class SpringApplicationImpl {

    private static IastClassLoader iastClassLoader;
    public static Method getAPI;
    public static boolean finished;

    public static boolean isFinished() {
        return finished;
    }

    public static void setFinished() {
        SpringApplicationImpl.finished = true;
    }

    public static void getWebApplicationContext(MethodEvent event) {
        if (!isFinished()) {
            Object applicationContext = event.returnValue;
            createClassLoader(applicationContext);
            loadApplicationContext();
            GetApiThread getApiThread = new GetApiThread(applicationContext);
            getApiThread.start();
        }
        setFinished();
    }

    private static void createClassLoader(Object applicationContext) {
        try {
            if (iastClassLoader == null) {
                if (HttpImpl.IAST_REQUEST_JAR_PACKAGE.exists()) {
                    Class<?> applicationContextClass = applicationContext.getClass();
                    URL[] adapterJar = new URL[]{HttpImpl.IAST_REQUEST_JAR_PACKAGE.toURI().toURL()};
                    iastClassLoader = new IastClassLoader(applicationContextClass.getClassLoader(), adapterJar);
                }
            }
        } catch (MalformedURLException e) {
            DongTaiLog.error(e.getMessage());
        }
    }

    private static void loadApplicationContext() {
        if (getAPI == null) {
            try {
                Class<?> proxyClass;
                proxyClass = iastClassLoader.loadClass("cn.huoxian.iast.spring.SpringApplicationContext");
                getAPI = proxyClass.getDeclaredMethod("getAPI", Object.class);
            } catch (NoSuchMethodException e) {
                DongTaiLog.error(e.getMessage());
            }
        }
    }

}
