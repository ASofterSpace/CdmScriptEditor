package com.asofterspace.cdm;

import com.asofterspace.cdm.exceptions.AttemptingEmfException;
import com.asofterspace.cdm.exceptions.CdmLoadingException;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.XmlMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/* // TAKE OUT EMF DEPENDENCIES
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.resource.impl.BinaryResourceImpl;
import org.eclipse.emf.ecore.resource.Resource;
*/ // TAKE OUT EMF DEPENDENCIES

public class CdmCtrl {

	private static List<CdmFile> fileList = new ArrayList<>();


	public static void loadCdmDirectory(Directory cdmDir) throws AttemptingEmfException, CdmLoadingException {

		fileList = new ArrayList<>();

		List<File> cdmFiles = cdmDir.getAllFiles(true);

		for (File cdmFile : cdmFiles) {
			if (cdmFile.getFilename().endsWith(".cdm")) {
				fileList.add(loadCdmFile(cdmFile));
			}
		}
	}

	public static CdmFile loadCdmFile(File cdmFile) throws AttemptingEmfException, CdmLoadingException {

		CdmFile result = loadCdmFileViaXML(cdmFile);

		switch (result.getMode()) {

			case XML_LOADED:
				// all is good!;
				break;

			case EMF_LOADED:
				throw new AttemptingEmfException("The CDM file " + cdmFile.getLocalFilename() + " is an EMF binary file, which is not yet supported.\nPlease only use CDM files in XML format.");

			case NONE_LOADED:
			default:
				throw new CdmLoadingException("There was a problem while loading the CDM file " + cdmFile.getLocalFilename() + ".");
		}

		// TODO - also get the EMF stuff to work ;)
		// loadCdmFileViaEMF(cdmFile);

		return result;
	}

	private static CdmFile loadCdmFileViaXML(File cdmFile) {

		try {
			CdmFile cdm = new CdmFile(cdmFile);

			return cdm;

		} catch (Exception e) {
			System.err.println(e);
		}

		return null;
	}

	private static void loadCdmFileViaEMF(File cdmFile) {

/* // TAKE OUT EMF DEPENDENCIES
		// TODO - load the CDM File using EMF: https://www.eclipse.org/modeling/emf/
		// you can get EMF from here: http://www.eclipse.org/modeling/emf/downloads/
		// TODO - add CDM namespaces... we need some .ecore files or somesuch?
		// do this similar to: EPackage.Registry.INSTANCE.put("schemas.xmlsoap.org/wsdl/", "file:/C:/workspace/Trans/bin/metamodels/WSDL.ecore");
		// or similar to: Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new XMIResourceFactoryImpl());
		// also see: http://www.vogella.com/tutorials/EclipseEMF/article.html (so apparently we need .ecore and genmodel files, but genmodel can be created from ecore)
		// >> as all of this is rather cumbersome, maybe go for plain XML for now after all...

		System.out.println(cdmFile); // debug
		java.net.URI cdmURI = cdmFile.getURI();
		System.out.println(cdmURI); // debug

		// try to read an XML CDM...
		XMIResource xResource = new XMIResourceImpl(URI.createURI(cdmURI.toString()));
		try {
			xResource.load(null);
			System.out.println(xResource.getContents().get(0)); // debug
		} catch (IOException ex) {
			// ... there was an exception! Must be binary then...
			System.out.println(ex); // debug
			Resource bResource = new BinaryResourceImpl(URI.createURI(cdmURI.toString()));
			try {
				bResource.load(null);
				System.out.println(bResource.getContents().get(0)); // debug
			} catch (IOException eb) {
				// ... oh wow; not binary either. Is this a CDM encoded in Morse code?
				System.out.println(eb); // debug
			}
		}
*/ // TAKE OUT EMF DEPENDENCIES
	}

	public static List<CdmScript> getScripts() {

		List<CdmScript> results = new ArrayList<>();

		for (CdmFile cdmFile : fileList) {
			results.addAll(cdmFile.getScripts());
		}

		return results;
	}

}
