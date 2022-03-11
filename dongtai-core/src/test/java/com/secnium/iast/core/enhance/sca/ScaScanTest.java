package com.secnium.iast.core.enhance.sca;

import io.dongtai.iast.core.bytecode.sca.ScaScanner;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class ScaScanTest {

    @Test
    public void scan() throws MalformedURLException {
        String[] packagePaths = new String[]{
        };

        for (String packagePath : packagePaths) {
            ScaScanner.scanForSCA(new URL(packagePath).getFile(), "");
        }
    }
}