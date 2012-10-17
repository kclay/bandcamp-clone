package utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Keyston
 * Date: 7/25/12
 * Time: 2:35 AM
 */
public class Context {
    public static ThreadLocal<Context> current = new ThreadLocal<Context>();
    /**
     * Free space to store your request specific data
     */
    /**
     * Free space to store your request specific data
     */
    public Map<String, Object> args = new HashMap<String, Object>(16);

    /**
     * Retrieves the current HTTP context, for the current thread.
     */
    public static Context current() {
        Context c = current.get();
        if (c == null) {
            c = new Context();
            current.set(c);
        }
        return c;
    }

    public static void remove() {
        current.remove();
    }

}
