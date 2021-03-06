/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ymbl.smartgateway.transite;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.ymbl.smartgateway.transite.log.TransLogger;
import com.ymbl.smartgateway.transite.log.SystemLogger;

public abstract class AbstractActivator implements BundleActivator
{
    protected BundleContext context;
    private TransLogger logger;

    protected final BundleContext getBundleContext()
    {
        return this.context;
    }

    public final void start(BundleContext context) throws Exception
    {
        this.context = context;
        this.logger = new TransLogger(context);
        SystemLogger.setLogService(this.logger);
        doStart(context);
    }

    public final void stop(BundleContext context) throws Exception
    {
        doStop(context);
    }

    protected abstract void doStart(BundleContext context) throws Exception;
    protected abstract void doStop(BundleContext context) throws Exception;   
}
