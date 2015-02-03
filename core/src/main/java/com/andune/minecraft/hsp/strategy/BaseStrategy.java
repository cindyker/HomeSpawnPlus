/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2015 Andune (andune.alleria@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
/**
 *
 */
package com.andune.minecraft.hsp.strategy;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.hsp.config.ConfigCore;

import javax.inject.Inject;

/**
 * Basic routines common/useful to most all strategies.
 *
 * @author andune
 */
public abstract class BaseStrategy implements Strategy {
    protected Logger log = LoggerFactory.getLogger(BaseStrategy.class);
    protected ConfigCore configCore;
    protected StrategyResultFactory resultFactory;

    @Inject
    public void setConfigCore(ConfigCore configCore) {
        this.configCore = configCore;
    }

    @Inject
    public void setStrategyResultFactory(StrategyResultFactory resultFactory) {
        this.resultFactory = resultFactory;
    }

    @Override
    public String getStrategyConfigName() {
        return this.getClass().getSimpleName();
    }

    protected boolean isVerbose() {
        return configCore.isVerboseStrategyLogging();
    }

    protected void logVerbose(final Object... args) {
        if (isVerbose()) {
            final StringBuilder sb = new StringBuilder();

            sb.append("(strategy ");
            sb.append(this.getStrategyConfigName());
            sb.append(") ");

            for (int i = 0; i < args.length; i++) {
                sb.append(args[i]);
            }

            log.info(sb.toString());
        }
    }

    protected void logInfo(String msg) {
        log.info(msg);
    }

    /**
     * By default, strategy is assumed valid. Subclass can override to do
     * it's own checks.
     */
    public void validate() throws StrategyException {
    }
}
