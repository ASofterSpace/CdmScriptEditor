package com.asofterspace.cdmScriptEditor;

import com.asofterspace.toolbox.io.File;

import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

public class CdmCtrl {

	public static void loadCdmFile(File cdmFile) {

		// TODO - load the CDM File using EMF: https://www.eclipse.org/modeling/emf/
		// you can get EMF from here: http://www.eclipse.org/modeling/emf/downloads/
		
		System.out.println(cdmFile);
		java.net.URI cdmURI = cdmFile.getURI();
		System.out.println(cdmURI);
	
		XMIResource resource = new XMIResourceImpl(URI.createURI(cdmURI.toString()));
		try {
			resource.load(null);
			System.out.println(resource.getContents().get(0));
		} catch (IOException e) {
			System.out.println(e);
		}
	}

}
