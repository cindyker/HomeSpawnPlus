/**
 * 
 */
package org.morganm.homespawnplus.convert;

import org.morganm.homespawnplus.server.api.CommandSender;

/** Interface Converters are required to implement.
 * @author morganm
 *
 */
public interface Converter extends Runnable {
    /**
     * Run the conversion process.
     * 
     * @return the number of conversions completed
     * 
     * @throws Exception
     */
    public int convert() throws Exception;
    
    public void setInitiatingSender(CommandSender sender);
    
    /**
     * Return the name for this converter, eg. "CommandBook"
     * 
     * @return
     */
    public String getConverterName();
}
