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


package com.cougaarsoftware.cougaar.ide.ui.wizards;


import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.cougaarsoftware.cougaar.ide.core.CougaarPlugin;
import com.cougaarsoftware.cougaar.ide.core.ICougaarInstall;
import com.cougaarsoftware.cougaar.ide.core.constants.ICougaarConstants;
import com.cougaarsoftware.cougaar.ide.ui.CougaarUI;
import com.cougaarsoftware.cougaar.ide.ui.CougaarUIMessages;
import com.cougaarsoftware.cougaar.ide.ui.IAddCougaarDialogRequestor;
import com.cougaarsoftware.cougaar.ide.ui.ICougaarInstallChangeListener;
import com.cougaarsoftware.cougaar.ide.ui.preferences.CougaarPreferencePage;
import com.cougaarsoftware.cougaar.ide.ui.preferences.CougaarPreferencesMessages;


/**
 * Capability page for cougaar projects TODO:  this should be almost completely
 * replaced with CougaarInstallSelectionWidget
 *
 * @author mabrams soster
 */
public class CougaarCapabilityConfigurationPage extends WizardPage
    implements IAddCougaarDialogRequestor, ICougaarInstallChangeListener {
    private static final String PAGE_NAME = "CougaarCapabilityConfigurationPage"; //$NON-NLS-1$
    private Combo fCougaarCombo;
    private NewCougaarProjectWizard cougaarProjectWizard;
    private Button fAddCougaarInstall;
    private Control control;

    // switch to control write of trace data
    private boolean traceEnabled = false;
    private String projectCougaarVersion;

    /**
     * constructor
     *
     * @param ncpw the <code>NewCougaarProjectWizard</code> that created this
     *        page
     */
    public CougaarCapabilityConfigurationPage(NewCougaarProjectWizard ncpw) {
        super(PAGE_NAME);
        cougaarProjectWizard = ncpw;
        setTitle(CougaarUIMessages.getString("CougaarCapabilityPageTitle"));
        setDescription(CougaarUIMessages.getString(
                "CougaarCapabilityPage.description"));

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite topComp = new Composite(parent, SWT.NONE);
        GridLayout topLayout = new GridLayout();
        topLayout.numColumns = 3;
        topLayout.marginWidth = 0;
        topLayout.marginHeight = 0;
        topComp.setLayout(topLayout);
        String[] cougaarNames = getCougaarVersions();

        Label cougaarSelectionLabel = new Label(topComp, SWT.NONE);
        cougaarSelectionLabel.setText(CougaarPreferencesMessages.getString(
                "CougaarConfigurationBlock.cougaarVersion"));
        cougaarSelectionLabel.setLayoutData(new GridData());

        fCougaarCombo = new Combo(topComp, SWT.READ_ONLY);
      
        if (cougaarNames.length > 0) {
            fCougaarCombo.setItems(cougaarNames);
            fCougaarCombo.select(0);
            handleCougaarComboBoxModified();
        }
    


        fCougaarCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fCougaarCombo.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent evt) {
                    handleCougaarComboBoxModified();
                }
            });

        fAddCougaarInstall = new Button(topComp, SWT.NONE);
        fAddCougaarInstall.setText(CougaarUIMessages.getString(
                "CougaarCapabilityConfigurationPage.newInstallButton"));
        fAddCougaarInstall.setLayoutData(new GridData());
        fAddCougaarInstall.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent evt) {
                    handleAddButtonSelected();
                }
            });

        DialogField.createEmptySpace(topComp, 2);

        control = topComp;
        setControl(topComp);
        setValues("");
    }


    private void handleAddButtonSelected() {
        String id = "com.cougaarsoftware.cougaar.ide.ui.preferences.CougaarPreferencePage";

        CougaarPreferencePage page = new CougaarPreferencePage(this);
        showPreferencePage(id, page);
        fCougaarCombo.update();
    }


    private boolean showPreferencePage(String id, IPreferencePage page) {
        final IPreferenceNode targetNode = new PreferenceNode(id, page);

        PreferenceManager manager = new PreferenceManager();
        manager.addToRoot(targetNode);

        final PreferenceDialog dialog = new PreferenceDialog(control.getShell(),
                manager);
        final boolean[] result = new boolean[] { false };
        BusyIndicator.showWhile(control.getDisplay(),
            new Runnable() {
                public void run() {
                    dialog.create();
                    dialog.setMessage(targetNode.getLabelText());
                    result[0] = (dialog.open() == Window.OK);
                }
            });
        return result[0];
    }


    /**
     * get the cougaar versions
     *
     * @return an array of currently configured cougaar versions
     */
    private String[] getCougaarVersions() {
        Map versions = CougaarUI.getCougaarLocations();
        String[] ret = new String[versions.size()];
        Iterator iter = versions.keySet().iterator();
        int count = 0;
        while (iter.hasNext()) {
            String version = (String) iter.next();
            ret[count++] = version;
        }

        return ret;
    }


    /**
     * called when the user makes a selection from the combo box
     */
    protected void handleCougaarComboBoxModified() {
        String version = fCougaarCombo.getText();
        if ((version != null) && !version.trim().equals("")) {
            projectCougaarVersion = version;
            this.getContainer().updateButtons();
        }
    }


    /* (non-Javadoc)
     * @see com.cougaarsoftware.cougaar.ide.ui.IAddCougaarDialogRequestor#isDuplicateName(java.lang.String)
     */
    public boolean isDuplicateName(String name) {
        return false;
    }


    /* (non-Javadoc)
     * @see com.cougaarsoftware.cougaar.ide.ui.IAddCougaarDialogRequestor#cougaarAdded(com.cougaarsoftware.cougaar.ide.core.ICougaarInstall)
     */
    public void cougaarAdded(ICougaarInstall cougaar) {
        String[] versions = getCougaarVersions();
        fCougaarCombo.setItems(versions);
        setValues(cougaar.getId());
    }


    /**
     * DOCUMENT ME!
     *
     * @param selectedVersion DOCUMENT ME!
     */
    protected void setValues(String selectedVersion) {
        int count = fCougaarCombo.getItemCount();

        for (int i = 0; i < count; i++) {
            String item = fCougaarCombo.getItem(i);
            if (item.equals(selectedVersion)) {
                fCougaarCombo.select(i);
                break;
            }
        }
    }


    /**
     *
     */
    public boolean isPageComplete() {
        return super.isPageComplete() && (projectCougaarVersion != null)
        && !projectCougaarVersion.trim().equals("");
    }


    /**
     *
     */
    protected void finishPage(IProgressMonitor monitor)
        throws InterruptedException, CoreException {
        JavaCapabilityConfigurationPage jcp = (JavaCapabilityConfigurationPage) cougaarProjectWizard
            .getPage("JavaCapabilityConfigurationPage");

        //only continue if we have a cougaar version
        if (projectCougaarVersion == null) {
            resultError("Create Cougaar Project",
                "You must set a valid Cougaar Installation for the project.");
            throw new CoreException(null);
        }

        IJavaProject javaProject = jcp.getJavaProject();

        SubProgressMonitor subMonitor = null;
        if (monitor != null) {
            subMonitor = new SubProgressMonitor(monitor, 3);
        }

        CougaarPlugin.addCougaarNature(javaProject.getProject(), subMonitor);

        //add the cougaar class path
        try {
            CougaarPlugin.addCougaarClasspathContainer(jcp.getJavaProject(),
                subMonitor);
        } catch (CoreException e) {
            e.printStackTrace();
        }

        CougaarPlugin.savePreference(ICougaarConstants.COUGAAR_VERSION,
            projectCougaarVersion, jcp.getJavaProject().getProject());

        try {
            CougaarPlugin.updateClasspathContainer(jcp.getJavaProject(),
                subMonitor);
        } catch (CoreException e) {
            resultError("Create Cougaar Project",
                "Failed to set and save classpath");
            e.printStackTrace();

        }

        CougaarPlugin.getDefault().savePluginSettings();
    }


    /**
     * Used to show action results.
     *
     * @see org.eclipse.jface.dialogs.MessageDialog
     */
    protected void resultError(String title, String msg) {
        // Indicate Error
        if (traceEnabled) {
            // trace only to console
            System.out.println(title + msg);
        } else {
            // user interaction response
            MessageDialog.openError(getShell(), title, msg);
        }
    }


    /**
     * Used to show action results.
     *
     * @see org.eclipse.jface.dialogs.MessageDialog
     */
    protected void resultInformation(String title, String msg) {
        // Confirm Result
        if (traceEnabled) {
            // trace only to console
            System.out.println(title + msg);
        } else {
            // user interaction response
            MessageDialog.openInformation(getShell(), title, msg);
        }
    }


    /* (non-Javadoc)
     * @see com.cougaarsoftware.cougaar.ide.ui.ICougaarInstallChangeListener#cougaarRemoved(com.cougaarsoftware.cougaar.ide.core.ICougaarInstall)
     */
    public void cougaarRemoved(ICougaarInstall removed) {
        String[] versions = getCougaarVersions();
        fCougaarCombo.setItems(versions);
        this.getContainer().updateButtons();
    }
}
