package com.asofterspace.cdm.scriptEditor;

import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.web.JSON;

public class ScriptTab {

	JPanel parent;

	String title;
	
	String path;
	
	JPanel visualPanel;
	
	ConfigFile configuration;
	

	public ScriptTab(JPanel parentPanel, String page, String pathToPage) {

		parent = parentPanel;
		
		title = page;
		
		path = pathToPage;
		
		visualPanel = createVisualPanel();
		
		parent.add(visualPanel);
		
		configuration = new ConfigFile(path + "/webengine.json");
	}
	
	private JPanel createVisualPanel() {
		
		JPanel tab = new JPanel();
		tab.setLayout(new GridLayout(4, 1));

		JLabel titleLabel = new JLabel(title);
		tab.add(titleLabel);

		JLabel pathLabel = new JLabel("Path: " + path);
		tab.add(pathLabel);

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 2);
		buttonRowLayout.setHgap(10);
		buttonRow.setLayout(buttonRowLayout);
		tab.add(buttonRow);

	    JButton previewButton = new JButton("Preview");
	    previewButton.addActionListener(new ActionListener()
	    {
	      public void actionPerformed(ActionEvent e)
	      {
	        performPreview();
	      }
	    });
	    buttonRow.add(previewButton);

	    JButton compileButton = new JButton("Compile");
	    compileButton.addActionListener(new ActionListener()
	    {
	      public void actionPerformed(ActionEvent e)
	      {
	        performCompile();
	      }
	    });
	    buttonRow.add(compileButton);

		tab.setVisible(false);

	    return tab;
	}

	public boolean isItem(String item) {

		if (title == null) {
			return false;
		}
		
		return title.equals(item);
	}

	public void show() {

		visualPanel.setVisible(true);
	}

	public void hide() {

		visualPanel.setVisible(false);
	}
	
	private void performPreview() {
		
		File indexIn = new File(path + "/index.php");
		
		String content = indexIn.getContent();
		
		content = compilePhp(content);
		
		content = removePhp(content);
		
		String newFileName = path + "/index.htm";
		
		File indexOut = new File(newFileName);
		
		indexOut.saveContent(content);
		
		openPreviewInBrowser(newFileName);
	}
	
	private void performCompile() {

		File indexIn = new File(path + "/index.php");
		
		String content = indexIn.getContent();

		content = compilePhp(content);
		
		String compiledDirPath = path + "/compiled";
		
		Directory compiledDir = new Directory(compiledDirPath);
		
		compiledDir.clear();
		
		File indexOut = new File(compiledDirPath + "/index.php");
		
		indexOut.saveContent(content);
	}

	/**
	 * Perform the compilation / templating of a PHP file
	 * @param content  a string containing PHP source code
	 * @return the same string after templating
	 */
	private String compilePhp(String content) {

		content = removeTemplatingComments(content);
		
		content = removeWhitespaceAndEmptyLines(content);
		
		content = insertContentText(content);
		
		Integer oldVersion = configuration.getInteger("version");
		
		Integer newVersion = oldVersion + 1;
		
		content = insertNewVersion(content, newVersion);
		
		configuration.set("version", newVersion);
		
		return content;
	}
	
	/**
	 * Removes all PHP tags from a PHP file to convert it into
	 * regular HTML, e.g. for local previews
	 * @param content  a string containing PHP source code
	 * @return the same string without the PHP tags and their contents
	 */
	private String removePhp(String content) {
		
		// TODO
		
		return content;
	}

	/**
	 * Takes a string containing source code and removes
	 * {{-- comments like these --}}
	 * @param content  a string containing source code
	 * @return the same string, but without {{-- comments --}}
	 */
	private String removeTemplatingComments(String content) {

		while (content.contains("{{--")) {
			
			String contentBefore = content.substring(0, content.indexOf("{{--"));
			
			String contentAfter = content.substring(content.indexOf("{{--"));
			contentAfter = contentAfter.substring(contentAfter.indexOf("--}}") + 4);
			
			content = contentBefore + contentAfter;
		}
		
		return content;
	}
	
	/**
	 * Takes a string containing source code and removes starting and
	 * trailing spaces as well as excessive newlines
	 * @param content  a string containing source code
	 * @return the same string but without excessive whitespace
	 */
	private String removeWhitespaceAndEmptyLines(String content) {

		while (content.startsWith(" ")) {
			content = content.substring(1);
		}
		
		while (content.contains(" \n") || content.contains("\t\n")) {
			content = content.replaceAll(" \n", "\n");
			content = content.replaceAll("\t\n", "\n");
		}

		while (content.contains("\n ") || content.contains("\n\t")) {
			content = content.replaceAll("\n ", "\n");
			content = content.replaceAll("\n\t", "\n");
		}

		while (content.endsWith(" ")) {
			content = content.substring(0, content.length() - 1);
		}

		while (content.contains("\n\n")) {
			content = content.replaceAll("\n\n", "\n");
		}

		return content;
	}

	/**
	 * Takes a string containing source code and inserts text
	 * replacing @content(foobar) placeholders
	 * @param content  a string containing source code
	 * @return the same string, but with placeholders filled
	 */
	private String insertContentText(String content) {
		
		JSON contentConfig = configuration.getAllContents().get("content");
		
		while (content.contains("@content(")) {
			
			int atIndex = content.indexOf("@content(");
			
			String beforeContent = content.substring(0, atIndex);
			
			String contentKey = content.substring(atIndex + 9, content.length());
			
			atIndex = contentKey.indexOf(")");
			
			String afterContent = contentKey.substring(atIndex + 1);
			
			contentKey = contentKey.substring(0, atIndex);
			
			content = beforeContent + contentConfig.getString(contentKey) + afterContent;
		}
		
		return content;
	}

	/**
	 * Takes a string containing source code and inserts incrementing
	 * text for @version placeholders
	 * @param content  a string containing source code
	 * @return the same string, but with @version placeholders replaced
	 */
	private String insertNewVersion(String content, Integer version) {

		while (content.contains("@version")) {
			content = content.replaceAll("@version", ""+version);
		}
		
		return content;
	}

	private void openPreviewInBrowser(String previewFileName) {

		try {
			String absolutePath = new java.io.File(previewFileName).getCanonicalPath().toString().replace("\\", "/");

			if (Desktop.isDesktopSupported()) {
					Desktop.getDesktop().browse(new URI("file:///" + absolutePath));
			}
		} catch (IOException | URISyntaxException e) {
			System.err.println("[ERROR] trying to open the preview in a browser resulted in an I/O Exception - not quite inconceivable!");
		}
	}
	
}
