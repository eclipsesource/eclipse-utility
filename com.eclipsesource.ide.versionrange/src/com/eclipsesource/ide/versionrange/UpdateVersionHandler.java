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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;

public class UpdateVersionHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		InputDialog idRegex = new InputDialog(HandlerUtil.getActiveShell(event), "Plugin Regex",
				"Enter the RegEx to identify the plugins", ".*emf.ecp.*|.*emfforms.*", null);
		int regexResult = idRegex.open();
		if (regexResult == Window.CANCEL)
			return null;

		InputDialog idVersion = new InputDialog(HandlerUtil.getActiveShell(event), "Version Rang",
				"Enter the Version Range to set", "[1.10.0,1.11.0)", null);
		int versionResult = idVersion.open();
		if (versionResult == Window.CANCEL)
			return null;

		String regex = idRegex.getValue();
		String version = idVersion.getValue();
		Job job = Job.create("Update Versions",
				(ICoreRunnable) monitor -> VersionRangeUpdater.updateVersionRange(monitor, regex, version));
		job.schedule();
		return null;
	}

}
