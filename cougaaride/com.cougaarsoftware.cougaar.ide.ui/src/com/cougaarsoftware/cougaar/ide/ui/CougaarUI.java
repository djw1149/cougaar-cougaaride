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


package com.cougaarsoftware.cougaar.ide.ui;


import java.util.Map;

import com.cougaarsoftware.cougaar.ide.core.CougaarLocations;


/**
 * A utility class to call core functions
 *
 * @author Matt Abrams
 */
public class CougaarUI {
    private CougaarUI() {
        // prevent instantiation of CougaarUI.
    }

    /**
     * Set the cougaar install path for a cougaar version
     *
     * @param version the version to set the path for
     * @param cougaarInstallPath the cougaar install path
     */
    public static void setCougaarInstallPathLocation(String version,
        String cougaarInstallPath) {
        CougaarLocations.setCougaarLocation(version, cougaarInstallPath);
    }


    /**
     * get the cougaar install path for a specific version
     *
     * @param version
     *
     * @return
     */
    public static String getCougaarBaseLocation(String version) {
        return CougaarLocations.getCougaarBaseLocation(version);
    }


    /**
     * get all of the cougaar locations stored in the workspace
     *
     * @return map of cougaar installs
     */
    public static Map getCougaarLocations() {
        return CougaarLocations.getAllCougaarLocations();
    }
}
