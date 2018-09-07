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

	public static final String ASS_CDM_NAMESPACE_ROOT = "http://www.asofterspace.com/";
	public static final String ASS_CDM_NAMESPACE = ASS_CDM_NAMESPACE_ROOT + "ConfigurationTracking/";

	private static List<CdmFile> fileList = new ArrayList<>();

	// has a CDM been loaded, like, at all?
	private static boolean cdmLoaded = false;
	
	private static Directory lastLoadedDirectory;


	public static void loadCdmDirectory(Directory cdmDir) throws AttemptingEmfException, CdmLoadingException {

		cdmLoaded = false;
		
		fileList = new ArrayList<>();

		List<File> cdmFiles = cdmDir.getAllFiles(true);

		for (File cdmFile : cdmFiles) {
			if (cdmFile.getFilename().endsWith(".cdm")) {
				loadCdmFile(cdmFile);
			}
		}
		
		if (fileList.size() <= 0) {
			throw new CdmLoadingException("The directory " + cdmDir + " does not seem to contain any .cdm files at all.");
		}
		
		lastLoadedDirectory = cdmDir;
		
		cdmLoaded = true;
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

		fileList.add(result);
		
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

		if (!cdmLoaded) {
			return results;
		}
		
		for (CdmFile cdmFile : fileList) {
			if ("configurationcontrol:ScriptCI".equals(cdmFile.getCiType())) {
				results.addAll(cdmFile.getScripts());
			}
		}

		return results;
	}
	
	public static List<CdmFile> getScriptToActivityMappers() {
	
		List<CdmFile> results = new ArrayList<>();

		if (!cdmLoaded) {
			return results;
		}
		
		for (CdmFile cdmFile : fileList) {
			if ("configurationcontrol:Script2ActivityMapperCI".equals(cdmFile.getCiType())) {
				results.add(cdmFile);
			}
		}

		return results;
	}
	
	public static boolean hasCdmBeenLoaded() {
	
		return cdmLoaded;
	}
	
	/**
	 * Save all currently opened files - the ones that have been deleted (so far just set an internal flag)
	 * will delete their contents from the disk
	 */
	public static void save() {
	
		for (CdmFile cdmFile : fileList) {
			cdmFile.save();
		}
	}

	/**
	 * Save all currently opened files to the new location
	 */
	public static void saveTo(Directory newLocation) {

		for (CdmFile cdmFile : fileList) {
			cdmFile.saveTo(lastLoadedDirectory.traverseFileTo(cdmFile, newLocation));
		}
		
		lastLoadedDirectory = newLocation;
	}
	
	public static Directory getLastLoadedDirectory() {
		return lastLoadedDirectory;
	}
	
	/**
	 * Get the CDM version of one CDM file at random - as they should all have the same version,
	 * we would like to receive the correct one no matter which one is being used ;)
	 */
	public static String getCdmVersion() {
		
		if (fileList.size() <= 0) {
			return "";
		}
		
		return fileList.get(0).getCdmVersion();
	}
	
	/**
	 * Get the list of CDM files that have been loaded
	 */
	public static List<CdmFile> getCdmFiles() {
		return new ArrayList<>(fileList);
	}
	
	/**
	 * Check if the CDM as a whole is valid;
	 * returns true if it is valid, and false if it is not;
	 * in the case of it not being valid, the StringBuilder
	 * that has been passed in will be filled with more detailed
	 * explanations about why it is not valid
	 */
	public static boolean isCdmValid(StringBuilder outProblemsFound) {

		// innocent unless proven otherwise
		boolean verdict = true;

		// validate that all CDM files are using the same CDM version
		List<CdmFile> cdmFiles = CdmCtrl.getCdmFiles();
		List<String> cdmVersionsFound = new ArrayList<>();

		for (CdmFile file : cdmFiles) {
			String curVersion = file.getCdmVersion();
			if (!cdmVersionsFound.contains(curVersion)) {
				cdmVersionsFound.add(curVersion);
			}
		}

		// oh no, we have different CDM versions!
		if (cdmVersionsFound.size() > 1) {
			verdict = false;
			outProblemsFound.append("CIs with multiple CDM versions have been mixed together!\n");
			outProblemsFound.append("Found CDM versions: ");
			String sep = "";
			for (String cdmVersionFound : cdmVersionsFound) {
				outProblemsFound.append(sep);
				sep = ", ";
				outProblemsFound.append(cdmVersionFound);
			}
		}

		// TODO :: check that all activity mappers are fully filled (e.g. no script or activity missing),
		// and that these mappings then also exist (e.g. not mapping to a CI that is not existing, etc.)

		return verdict;
	}

}
