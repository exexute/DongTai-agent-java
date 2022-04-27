package io.dongtai.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
    public static boolean getResourceToFile(String resourceName, String fileName) throws IOException {
        File targetFile = new File(fileName);

        if (!targetFile.exists()) {
            if (!targetFile.getParentFile().exists()) {
                if (!targetFile.getParentFile().mkdirs()) {
                    throw new NullPointerException("文件创建失败");
                }
            }
            if (!targetFile.createNewFile()) {
                throw new NullPointerException("文件创建失败");
            }
        }
        // todo: add agent model check, attach mode couldn't reload stream with classloader
        //if (AgentLauncher.LAUNCH_MODE_AGENT.equals("agent")) {
        InputStream is = FileUtil.class.getClassLoader().getResourceAsStream(resourceName);
        if (is == null) return false;
        FileOutputStream fos = new FileOutputStream(targetFile);
        int length = 0;
        byte[] data = new byte[1024];
        while ((length = is.read(data)) != -1) {
            fos.write(data, 0, length);
        }

        is.close();
        fos.close();
        return true;
        //}
        //return false;
    }
}
