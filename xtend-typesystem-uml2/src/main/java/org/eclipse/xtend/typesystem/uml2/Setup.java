/*******************************************************************************
 * Copyright (c) 2005, 2006 committers of openArchitectureWare and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     committers of openArchitectureWare - initial API and implementation
 *******************************************************************************/
package org.eclipse.xtend.typesystem.uml2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.emf.common.EMFPlugin;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;
import org.eclipse.xtend.typesystem.emf.Mapping;

/**
 * Initialization of UML2 Support. This class must be integrated into workflows
 * using EMF UML2 like follows:
 * <p>
 *
 * <pre>
 *  	MWE1:
 *  	&lt;bean class=&quot;org.eclipse.internal.xtend.uml2.Setup&quot;/&gt;
 *
 *  	MWE2
 *  	bean = org.eclipse.internal.xtend.uml2.Setup {  }
 * </pre>
 *
 * @author Sven Efftinge - initial implementation and API
 * @author Karsten Thoms - docs, maintenance
 */
public class Setup extends org.eclipse.xtend.typesystem.emf.Setup {
	private static final Log LOG = LogFactory.getLog(Setup.class);
	/**
	 * Constructor. Registers the UMLPackage.
	 *
	 */
	public Setup() {
		doStandaloneRegistration();
	}

	/**
	 * @deprecated Registration already occurs with instantiation
	 */
	@Deprecated
	public void setStandardUML2Setup(boolean b) {
		LOG.warn("Setting 'standardUML2Setup' not required anymore. Please remove the setting from your workflow/standalone configuration.");
	}

	/**
	 * Registers all required UML resources, if not running within Eclipse.
	 * <p>
	 * Registration is done by UML2 itself: UML packages (incl. the namespaces of older UML2 versions, so that models of
	 * older versions can be read), resource factories and the URI mappings of UML libraries, profiles and metamodels.
	 */
	private void doStandaloneRegistration() {
		if (EMFPlugin.IS_ECLIPSE_RUNNING) {
			return;
		}
		UMLResourcesUtil.initGlobalRegistries();
		addExtensionMap(new Mapping("ecore", "org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl"));
		addExtensionMap(new Mapping("uml2", "org.eclipse.uml2.uml.resource.UML22UMLResource"));
	}
}
