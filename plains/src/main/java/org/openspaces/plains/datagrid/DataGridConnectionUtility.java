package org.openspaces.plains.datagrid;

import com.j_spaces.core.IJSpace;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.space.SpaceDeployment;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.CannotFindSpaceException;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.core.space.cache.LocalCacheSpaceConfigurer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DataGridConnectionUtility {
    Map<String, UrlSpaceConfigurer> configurerMap = new ConcurrentHashMap<String, UrlSpaceConfigurer>();
    Map<String, GigaSpace> gigaSpaceMap = new ConcurrentHashMap<String, GigaSpace>();
    Logger log = Logger.getLogger(this.getClass().getName());

    static DataGridConnectionUtility instance = new DataGridConnectionUtility();

    private DataGridConnectionUtility() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (UrlSpaceConfigurer configurer : configurerMap.values()) {
                    try {
                        log.fine("Shutting down configurer " + configurer);
                        configurer.destroy();
                    } catch (Exception e) {
                        log.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }
        });
    }

    public static GigaSpace getSpace(String spaceName, int instances, int backups) {
        GigaSpace gigaspace = instance.gigaSpaceMap.get(spaceName);
        if (gigaspace == null) {
            UrlSpaceConfigurer configurer = new UrlSpaceConfigurer("jini:/*/*/" + spaceName);
            instance.configurerMap.put(spaceName, configurer);
            IJSpace space = null;
            try {
                space = configurer.space();
            } catch (CannotFindSpaceException cfse) {
                Admin admin = new AdminFactory().createAdmin();
                GridServiceManager esm = admin.getGridServiceManagers().waitForAtLeastOne();
                ProcessingUnit pu = esm.deploy(new SpaceDeployment(spaceName)
                        .partitioned(instances, backups));
                pu.waitForSpace();
                admin.close();
                space = configurer.space();
            }
            LocalCacheSpaceConfigurer cacheConfigurer = new LocalCacheSpaceConfigurer(space);
            gigaspace = new GigaSpaceConfigurer(cacheConfigurer.space()).gigaSpace();
            instance.gigaSpaceMap.put(spaceName, gigaspace);
        }
        return gigaspace;

    }

    public static GigaSpace getSpace(String spaceName) {
        return getSpace(spaceName, 2, 1);
    }
}
