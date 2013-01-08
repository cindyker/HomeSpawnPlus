/**
 * This package represents the Bukkit-specific classes of dynmap's HSP
 * integration. Since dynmap supports a server-agnostic API, it would
 * eventually be nice to slim this package down to a single call to
 * the Bukkit API to get the Dynmap plugin/API and then pass that along
 * to a core module.
 * 
 * In this fashion the entire HSP dynmap integration could be implemented in
 * core and be server agnostic; all the server implementation has to do
 * is locate the dynmap plugin in whatever way the server provides and
 * pass the reference along.
 */

package com.andune.minecraft.hsp.integration.dynmap;