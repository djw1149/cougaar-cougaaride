/*
 * Cougaar IDE
 *
 * Copyright (C) 2003, Cougaar Software, Inc. <tcarrico@cougaarsoftware.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */


package com.cougaarsoftware.cougaar.ide.launcher.core;


import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;


/**
 * Cougaar Launcher Plugin
 *
 * @author mabrams
 */
public class LauncherPlugin extends AbstractUIPlugin {
    /** The shared instance */
    private static LauncherPlugin plugin;

    /**
     * DOCUMENT ME!
     *
     * @param descriptor
     */
    public LauncherPlugin(IPluginDescriptor descriptor) {
        super(descriptor);
        plugin = this;
    }

    /**
     * Returns the shared instance.
     *
     * @return The default value
     */
    public static LauncherPlugin getDefault() {
        return plugin;
    }


    /**
     * Logs the specified status with this plug-in's log.
     *
     * @param status status to log
     */
    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }


    /**
     * Logs an internal error with the specified throwable
     *
     * @param e the exception to be logged
     */
    public static void log(Throwable e) {
        log(new Status(IStatus.ERROR, getUniqueIdentifier(), 0,
                "Internal Error", e)); //$NON-NLS-1$
    }


    /**
     * Logs an internal error with the specified message.
     *
     * @param message the error message to log
     */
    public static void logError(String message) {
        logError(message, null);
    }


    /**
     * Logs a throwable with the specified message.
     *
     * @param message Message to log
     * @param throwable Throwable to log
     */
    public static void logError(String message, Throwable throwable) {
        log(new Status(IStatus.ERROR, getUniqueIdentifier(), 0, message,
                throwable));
    }


    /**
     * Convenience method which returns the unique identifier of this plugin.
     *
     * @return The unique indentifier value
     */
    public static String getUniqueIdentifier() {
        if (getDefault() == null) {
            return "com.cougaarsoftware.cougaar.ide.launcher.core.LauncherPlugin";
        }

        return getDefault().getDescriptor().getUniqueIdentifier();
    }


    /**
     * Shows a message dialog with the given message
     *
     * @param message Message to display
     */
    public void showErrorMessage(String message) {
        IWorkbenchWindow window = getDefault().getWorkbench()
                                      .getActiveWorkbenchWindow();
        if (window != null) {
            MessageDialog.openError(window.getShell().getShell(), "Error",
                message);
        }
    }


    /**
     * Shows a message dialog with the given throwable
     *
     * @param throwable Throwable to display
     */
    public void showErrorMessage(Throwable throwable) {
        showErrorMessage(throwable.getClass().getName() + " "
            + throwable.getMessage());
    }


    /**
     * Gets the base installation dir of the plugin
     *
     * @return The directory
     */
    public String getBaseDir() {
        return this.find(new Path(".")).getFile().toString();
    }
}
