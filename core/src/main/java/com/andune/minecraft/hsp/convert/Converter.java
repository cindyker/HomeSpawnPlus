/**
 * 
 */
package com.andune.minecraft.hsp.convert;

import com.andune.minecraft.hsp.server.api.CommandSender;

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
