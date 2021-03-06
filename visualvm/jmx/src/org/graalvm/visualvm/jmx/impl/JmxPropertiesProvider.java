/*
 * Copyright (c) 2007, 2018, Oracle and/or its affiliates. All rights reserved.
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

package org.graalvm.visualvm.jmx.impl;

import org.graalvm.visualvm.core.datasource.Storage;
import org.graalvm.visualvm.core.properties.PropertiesPanel;
import org.graalvm.visualvm.core.properties.PropertiesProvider;
import org.graalvm.visualvm.jmx.JmxConnectionCustomizer;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
public final class JmxPropertiesProvider extends PropertiesProvider<JmxApplication> {
    
    /**
     * Key for the "JMX Connection" properties category.
     */
    public static final int CATEGORY_JMX_CONNECTION = 200;

    private static final String PROP_CONN_CUSTOMIZER_ID = "prop_conn_customizer_id"; // NOI18N


    public JmxPropertiesProvider() {
        super(NbBundle.getMessage(JmxPropertiesProvider.class, "CAP_JmxConnection"), // NOI18N
              NbBundle.getMessage(JmxPropertiesProvider.class, "DESCR_JmxConnection"), // NOI18N
              CATEGORY_JMX_CONNECTION, 0);
    }


    public PropertiesPanel createPanel(JmxApplication application) {
        JmxConnectionCustomizer customizer = getCustomizer(application);
        return customizer == null ? null : customizer.createPanel(application);
    }

    public boolean supportsDataSource(JmxApplication application) {
        if (application == null) return false;
        JmxConnectionCustomizer customizer = getCustomizer(application);
        return customizer == null ? false : customizer.supportsDataSource(application);
    }

    public void propertiesDefined(PropertiesPanel panel, JmxApplication application) {
        JmxConnectionCustomizer customizer = getCustomizer(application);
        if (customizer != null) customizer.propertiesDefined(panel, application);
    }

    public void propertiesChanged(PropertiesPanel panel, JmxApplication application) {
        JmxConnectionCustomizer customizer = getCustomizer(application);
        if (customizer != null) customizer.propertiesChanged(panel, application);
    }

    public void propertiesCancelled(PropertiesPanel panel, JmxApplication application) {
        JmxConnectionCustomizer customizer = getCustomizer(application);
        if (customizer != null) customizer.propertiesCancelled(panel, application);
    }


    static void setCustomizer(JmxApplication application, JmxConnectionCustomizer customizer) {
        setCustomizer(application.getStorage(), customizer);
    }

    private static void setCustomizer(Storage storage, JmxConnectionCustomizer customizer) {
        storage.setCustomProperty(PROP_CONN_CUSTOMIZER_ID, customizer.getId());
    }

    private static JmxConnectionCustomizer getCustomizer(JmxApplication application) {
        String customizerId = application.getStorage().getCustomProperty(PROP_CONN_CUSTOMIZER_ID);
        return customizerId != null ? JmxConnectionSupportImpl.getCustomizer(customizerId) : null;
    }

}
