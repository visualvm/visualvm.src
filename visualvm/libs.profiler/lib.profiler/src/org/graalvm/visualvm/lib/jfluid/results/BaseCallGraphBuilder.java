/*
 * Copyright (c) 1997, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.graalvm.visualvm.lib.jfluid.results;

import org.graalvm.visualvm.lib.jfluid.ProfilerClient;
import org.graalvm.visualvm.lib.jfluid.global.ProfilingSessionStatus;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Jaroslav Bachorik
 */
public abstract class BaseCallGraphBuilder implements ProfilingResultListener, CCTProvider {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    protected static final Logger LOGGER = Logger.getLogger(BaseCallGraphBuilder.class.getName());

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected List /*<Runnable>*/ afterBatchCommands = new ArrayList /*<Runnable>*/();
    protected ProfilingSessionStatus status;
    protected final Set cctListeners = new CopyOnWriteArraySet();
    protected WeakReference clientRef;
    protected boolean batchNotEmpty = false;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of BaseCallGraphBuilder */
    public BaseCallGraphBuilder() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void addListener(CCTProvider.Listener listener) {
        cctListeners.add(listener);
    }

    public void onBatchStart() {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Starting batch"); // NOI18N
        }

        afterBatchCommands.clear();
        batchNotEmpty = false;
        doBatchStart();
    }

    public void onBatchStop() {
        doBatchStop();

        if (batchNotEmpty) {
            fireCCTEstablished(false);
        } else {
            fireCCTEstablished(true);
        }

        if (!afterBatchCommands.isEmpty()) {
            for (Iterator iter = afterBatchCommands.iterator(); iter.hasNext();) {
                ((Runnable) iter.next()).run();
            }

            afterBatchCommands.clear();
        }

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Finishing batch"); // NOI18N
        }
    }

    public void removeAllListeners() {
        cctListeners.clear();
    }

    public void removeListener(CCTProvider.Listener listener) {
        cctListeners.remove(listener);
    }

    public void reset() {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Resetting CallGraphBuilder"); // NOI18N
        }

        try {
            doReset();
            fireCCTReset();
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public void shutdown() {
        status = null;
        afterBatchCommands.clear();
        doShutdown();
    }

    public void startup(ProfilerClient profilerClient) {
        status = profilerClient.getStatus();
        clientRef = new WeakReference(profilerClient);
        doStartup(profilerClient);
    }

    protected abstract RuntimeCCTNode getAppRootNode();

    protected abstract void doBatchStart();

    protected abstract void doBatchStop();

    protected abstract void doReset();

    protected abstract void doShutdown();

    protected abstract void doStartup(ProfilerClient profilerClient);

    protected ProfilerClient getClient() {
        if (clientRef == null) {
            return null;
        }

        return (ProfilerClient) clientRef.get();
    }

    private void fireCCTEstablished(boolean empty) {
        RuntimeCCTNode appNode = getAppRootNode();

        if (appNode == null) {
            return;
        }

        for (Iterator iter = cctListeners.iterator(); iter.hasNext();) {
            ((CCTProvider.Listener) iter.next()).cctEstablished(appNode, empty);
        }
    }

    private void fireCCTReset() {
        for (Iterator iter = cctListeners.iterator(); iter.hasNext();) {
            ((CCTProvider.Listener) iter.next()).cctReset();
        }
    }
}
