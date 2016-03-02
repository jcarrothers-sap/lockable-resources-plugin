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
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.jenkins.plugins.lockableresources.LockableResource;
import org.jenkins.plugins.lockableresources.LockableResourcesManager;
import org.jenkins.plugins.lockableresources.RequiredResourcesParameterValue;
import org.jenkins.plugins.lockableresources.RequiredResourcesProperty;

public class LockableResourcesStruct {

	public final Set<LockableResource> required;
	public final transient String requiredNames;
	public final String requiredVar;
	public final String requiredNumber;

	/**
	 * For caching reusable instances of this class.
	 */
	private static transient final Map<CacheKeyStruct,LockableResourcesStruct>
			cachedResourceStructs = new WeakHashMap<CacheKeyStruct,LockableResourcesStruct>();

	/**
	 * For caching reusable instances the key structure for this class's cache.
	 */
	private static transient final Map<Object,CacheKeyStruct>
			cachedKeyStructs = new WeakHashMap<Object,CacheKeyStruct>();

	public static synchronized LockableResourcesStruct get(RequiredResourcesProperty property)  {
		CacheKeyStruct key = cachedKeyStructs.get(property);
		if ( key == null ) {
			key = new CacheKeyStruct(
				property.getResourceNames(),
				property.getResourceNamesVar(),
				property.getResourceNumber()
			);
			cachedKeyStructs.put(property, key);
		}
		return buildStruct(key);
	}

	public static synchronized LockableResourcesStruct get( RequiredResourcesParameterValue param ) {
		CacheKeyStruct key = cachedKeyStructs.get(param);
		if ( key == null ) {
			key = new CacheKeyStruct(param.value, null, null);
			cachedKeyStructs.put(param, key);
		}
		return buildStruct(key);
	}

	private static synchronized LockableResourcesStruct buildStruct( CacheKeyStruct key ) {
		LockableResourcesStruct ret = cachedResourceStructs.get(key);
		if ( ret == null ) {
			ret = new LockableResourcesStruct(key.requiredNames, key.requiredVar, key.requiredNumber);
			cachedResourceStructs.put(key, ret);
		}
		return ret;
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

	private static class CacheKeyStruct {
		public final String requiredNames;
		public final String requiredVar;
		public final String requiredNumber;

		public CacheKeyStruct(String requiredNames, String requiredVar, String requiredNumber) {
			this.requiredNames = requiredNames;
			this.requiredVar = requiredVar;
			this.requiredNumber = requiredNumber;
		}

		@Override
		public int hashCode() {
			int hash = 3;
			hash = 61 * hash + (this.requiredNames != null ? this.requiredNames.hashCode() : 0);
			hash = 61 * hash + (this.requiredVar != null ? this.requiredVar.hashCode() : 0);
			hash = 61 * hash + (this.requiredNumber != null ? this.requiredNumber.hashCode() : 0);
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final CacheKeyStruct other = (CacheKeyStruct) obj;
			if ( !this.requiredNames.equals(other.requiredNames) ) {
				return false;
			}
			if ( !this.requiredVar.equals(other.requiredVar) ) {
				return false;
			}
			if ( !this.requiredNumber.equals(other.requiredNumber) ) {
				return false;
			}
			return true;
		}
	}
}
