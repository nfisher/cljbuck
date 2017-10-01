package ca.junctionbox.cljbuck.build;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class ClassPath {
    public Class<?> forName(final String name, final boolean initialize) throws ClassNotFoundException {
        final Class<?> clazz = ClassPath.class;
        return Class.forName(name, initialize, clazz.getClassLoader());
    }

    public void addClasspath(final String classPath) throws MalformedURLException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        final URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        final URL jarFile = new File(classPath).toURI().toURL();

        for (final URL url : Arrays.asList(classLoader.getURLs())) {
            if (url.equals(jarFile)) {
                return;
            }
        }

        final Method addUrl = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        try {
            addUrl.setAccessible(true);
            addUrl.invoke(classLoader, jarFile);
        } finally {
            addUrl.setAccessible(false);
        }
    }

    public void printPath() {
        final URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        for (URL url : classLoader.getURLs()) {
            System.out.println(url);
        }
    }
}
