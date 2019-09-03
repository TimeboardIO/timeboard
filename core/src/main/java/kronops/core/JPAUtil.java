package kronops.core;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JPAUtil {
    private static final String PERSISTENCE_UNIT_NAME = "kronops-pu";
    private static EntityManagerFactory factory;

    public static EntityManagerFactory getEntityManagerFactory() {
        Thread.currentThread().setContextClassLoader(JPAUtil.class.getClassLoader());
        if (factory == null) {
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        }
        return factory;
    }

    public static void shutdown() {
        if (factory != null) {
            factory.close();
        }
    }
}