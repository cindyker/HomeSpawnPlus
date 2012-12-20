/**
 * 
 */
package org.morganm.homespawnplus.guice;

import javax.inject.Inject;

import org.morganm.homespawnplus.config.ConfigCore;
import org.morganm.homespawnplus.storage.Storage;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.StorageFactory;

import com.google.inject.Provider;

/**
 * @author morganm
 *
 */
public class StorageProvider implements Provider<Storage> {
    private final ConfigCore config;
    private final StorageFactory factory;
    private Storage storage;
    
    @Inject
    StorageProvider(ConfigCore config, StorageFactory factory) {
        this.config = config;
        this.factory = factory;
    }

    @Override
    public Storage get() {
        if( storage == null ) {
            try {
                storage = factory.getInstance(factory.getType(config.getStorageType()));
            } catch (StorageException e) {
                e.printStackTrace();
            }
        }

        return storage;
    }

}
