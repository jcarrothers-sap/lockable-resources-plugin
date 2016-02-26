/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2013-2015, Aki Asikainen                              *
 *                          SAP SE                                     *
 *                                                                     *
 * This file is part of the Jenkins Lockable Resources Plugin and is   *
 * published under the MIT license.                                    *
 *                                                                     *
 * See the "LICENSE.txt" file for more information.                    *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.jenkins.plugins.lockableresources.queue;

import hudson.Util;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jenkins.plugins.lockableresources.LockableResource;
import org.jenkins.plugins.lockableresources.LockableResourcesManager;
import org.jenkins.plugins.lockableresources.RequiredResourcesParameterValue;
import org.jenkins.plugins.lockableresources.RequiredResourcesProperty;

// TODO: re-creating this struct for every canRun analysis is wasteful and inefficient
//       the result should be cached instead
public class LockableResourcesStruct {

	public final Set<LockableResource> required;
	public final transient String requiredNames;
	public final String requiredVar;
	public final String requiredNumber;

	public LockableResourcesStruct(RequiredResourcesProperty property) {
		this(
				property.getResourceNames(),
				property.getResourceNamesVar(),
				property.getResourceNumber()
		);
	}

	public LockableResourcesStruct( RequiredResourcesParameterValue param ) {
		this(param.value, null, null);
	}

	private LockableResourcesStruct( String requiredNames, String requiredVar, String requiredNumber ) {
		Set<LockableResource> required = new LinkedHashSet<LockableResource>();
		requiredNames = Util.fixEmptyAndTrim(requiredNames);
		if ( requiredNames != null ) {
			for ( String name : requiredNames.split("\\s+") ) {
				LockableResource r = LockableResourcesManager.get().fromName(
					name);
				if (r != null) {
					required.add(r);
				}
				else {
					required.addAll(LockableResourcesManager.get().getResourcesWithLabel(name));
				}
			}
		}
		this.requiredNames = requiredNames;
		this.required = Collections.unmodifiableSet(required);

		this.requiredVar = Util.fixEmptyAndTrim(requiredVar);

		requiredNumber = Util.fixEmptyAndTrim(requiredNumber);
		if ( requiredNumber != null && requiredNumber.equals("0") ) requiredNumber = null;
		this.requiredNumber = requiredNumber;
	}

	public String toString() {
		return "Required resources: " + this.required +
			", Variable name: " + this.requiredVar +
			", Number of resources: " + this.requiredNumber;
	}
}
