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


package com.cougaarsoftware.cougaar.ide.core;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.cougaarsoftware.cougaar.ide.core.constants.ICougaarConstants;


/**
 * Initializes the cougaar classpath container
 *
 * @author soster
 */
public class CougaarClasspathContainerInitializer
    extends ClasspathContainerInitializer {
    /**
     * Creates a new CougaarClasspathContainerInitializer object.
     */
    public CougaarClasspathContainerInitializer() {
        super();
    }

    /**
     * Signal this is able to update itself.
     *
     * @param containerPath classpath container path
     * @param project cougaar project
     *
     * @return true
     */
    public boolean canUpdateClasspathContainer(IPath containerPath,
        IJavaProject project) {
        return true;
    }


    /**
     * Re-read the project properties and rebuild the cp based on the cougaar
     * install path
     *
     * @param containerPath classpath container path
     * @param project cougaar project
     * @param containerSuggestion NA
     *
     * @throws CoreException when JavaCore has problems
     */
    public void requestClasspathContainerUpdate(IPath containerPath,
        IJavaProject project, IClasspathContainer containerSuggestion)
        throws CoreException {
        populateCougaarContainer(project);
    }


    /**
     * Read the project properties and rebuild the cp based on the cougaar
     * install path
     *
     * @param containerPath classpath container path
     * @param javaProject cougaar project
     *
     * @throws CoreException when JavaCore has problems
     */
    public void initialize(IPath containerPath, IJavaProject javaProject)
        throws CoreException {
        populateCougaarContainer(javaProject);
    }


    /**
     * Get the project's cougaar version and create a cougaar classpath
     * container for it
     *
     * @param javaProject cougaar project
     *
     * @throws CoreException when JavaCore has problems
     */
    private void populateCougaarContainer(IJavaProject javaProject)
        throws CoreException {
        IProject project = javaProject.getProject();
        if (CougaarPlugin.isCougaarProject(project)) {
            String version = CougaarPlugin.getCougaarPreference(project,
                    ICougaarConstants.COUGAAR_VERSION);
            String installPrefix = CougaarPlugin.getCougaarBaseLocation(version);

            IPath path = new Path(IResourceIDs.CLASSPATH_CONTAINER_ID);
            CougaarClasspathContainer container = new CougaarClasspathContainer(installPrefix);

            IJavaProject[] javaProjects = new IJavaProject[] {
                    //is this really necessary? or could we use javaProject
                    JavaCore.create(project)
                };
            IClasspathContainer[] containers = new IClasspathContainer[] {
                    container
                };
            JavaCore.setClasspathContainer(path, javaProjects, containers, null);
        }
    }
}
