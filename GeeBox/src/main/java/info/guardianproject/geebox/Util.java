package info.guardianproject.geebox;

/**
 * @author devrandom
 */
public class Util {
    public static String getLastPathSegment(String path) {
        if ("".equals(path))
            throw new IllegalArgumentException("empty path");
        String[] comps = path.split("/");
        return comps[comps.length - 1];
    }
}
