/**
 * The essentials integration is primarily written to fix an issue where
 * essentials commandHandler consistently overrides HSP's commands because it
 * doesn't ask plugins what it's commands are, it depends on the plugin.yml
 * command definition implementation Bukkit uses. HSP doesn't use this since it
 * has a dynamic command system, so since Essentials doesn't detect commands
 * like "/home" and "/spawn", it goes out of it's way to usurp these commands
 * from HSP and gives Essentials/HSP admins a bad experience.
 */

package com.andune.minecraft.hsp.integration.essentials;