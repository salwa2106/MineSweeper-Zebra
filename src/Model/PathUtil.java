package Model;

import java.net.URLDecoder;

public class PathUtil {

    /**
     * Returns correct absolute path for ANY file:
     * - Works in Eclipse
     * - Works from inside the JAR
     */
    public static String getPath(String relativePath) {
        try {
            // The directory of the currently running JAR or project
            String base = PathUtil.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath();

            base = URLDecoder.decode(base, "UTF-8");

            if (base.endsWith(".jar")) {
                // running from JAR → go to directory
                base = base.substring(0, base.lastIndexOf("/"));
                return base + "/" + relativePath;  
            } else {
                // running from Eclipse
                base = base.substring(0, base.lastIndexOf("/"));
                return base + "/src/" + relativePath;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return relativePath; // fallback
        }
    }
}
