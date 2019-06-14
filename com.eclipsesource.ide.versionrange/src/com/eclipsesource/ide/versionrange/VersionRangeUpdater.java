/**
 * Copyright (c) 2011-2019 EclipseSource Muenchen GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Eugen Neufeld - initial API and implementation
 */
package com.eclipsesource.ide.versionrange;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.core.plugin.IFragment;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.bundle.BundleFragmentModel;
import org.eclipse.pde.internal.core.bundle.BundlePluginModelBase;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundleModel;
import org.eclipse.pde.internal.core.plugin.WorkspaceExtensionsModel;
import org.eclipse.pde.internal.core.text.bundle.ImportPackageHeader;
import org.eclipse.pde.internal.core.text.bundle.ImportPackageObject;
import org.osgi.framework.Constants;

@SuppressWarnings("restriction")
public class VersionRangeUpdater {

	public static void updateVersionRange(IProgressMonitor monitor, String regex, String versionRange){
		IPluginModelBase[] workspaceModels = PluginRegistry.getWorkspaceModels();
		monitor.beginTask("Update Versions", workspaceModels.length);
		for(IPluginModelBase modelBase:workspaceModels){
			monitor.subTask("Checking "+modelBase.getBundleDescription().getSymbolicName());
			if(BundleFragmentModel.class.isInstance(modelBase)){
				IFragment fragment = BundleFragmentModel.class.cast(modelBase).getFragment();
				if(fragment.getPluginId().matches(regex)){
					try {
						fragment.setPluginVersion(versionRange);
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			}
			WorkspaceBundleModel bundleModel = WorkspaceBundleModel.class.cast(BundlePluginModelBase.class.cast(modelBase).getBundleModel());
			WorkspaceExtensionsModel extensionsModel = WorkspaceExtensionsModel.class.cast(BundlePluginModelBase.class.cast(modelBase).getExtensionsModel());
			
			bundleModel.setEditable(true);
			if(extensionsModel!=null)
				extensionsModel.setEditable(true);
			IPluginBase pluginBase = modelBase.getPluginBase();
			for(IPluginImport pluginImport: pluginBase.getImports()){
				if(pluginImport.getId().matches(regex)){
					try {
						pluginImport.setVersion(versionRange);
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			}
			ImportPackageHeader packageHeader = ImportPackageHeader.class.cast(bundleModel.getBundle().getManifestHeader(Constants.IMPORT_PACKAGE));
			if(packageHeader!=null){
				for(ImportPackageObject importPackage: packageHeader.getPackages()){
					if(importPackage.getName().matches(regex)){
						importPackage.setAttribute(Constants.VERSION_ATTRIBUTE, versionRange);
						packageHeader.update();
					}
				}
			}
			bundleModel.setEditable(false);
			if(extensionsModel!=null)
				extensionsModel.setEditable(false);
			BundlePluginModelBase.class.cast(modelBase).save();
			monitor.worked(1);
		}
		monitor.done();
	}
}
