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

package com.cougaarsoftware.cougaar.ide.core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Persists and reads cougaar locations associated with the workspace
 * 
 * @author Matt Abrams
 */
public class CougaarLocations {
	private static String STORE_FILE = "cougaarlocations.xml";
	private static Map fCougaarLocations = null;
	private static final String NODE_ROOT = "cougaarlocation";
	private static final String NODE_PATH = "path";
	private static final String NODE_VERSION = "version";
	private static final String NODE_DEFAULT = "default";
	private static final String NODE_ENTRY = "location_01";
	private static String defaultVersion = "";

	private static Map getCougaarLocations() {
		if (fCougaarLocations == null) {
			fCougaarLocations = new HashMap();
			try {
				initCougaarLocations();
			} catch (CoreException e) {
				CougaarPlugin.log(e);
			}
		}

		return fCougaarLocations;
	}

	/**
	 * Returns all cougaar locations
	 * 
	 * @return a <code>Map</code> of all cougaar locations
	 */
	public static Map getAllCougaarLocations() {
		Map map = new HashMap();
		if (fCougaarLocations == null) {
			map = getCougaarLocations();
		} else {
			map = new HashMap(fCougaarLocations);
		}

		return map;
	}

	private static synchronized void initCougaarLocations()
			throws CoreException {
		loadFromFile();

	}

	private static boolean loadFromFile() throws CoreException {
		File file = getStoreFile();
		if (file.exists()) {
			Reader reader = null;
			try {
				reader = new FileReader(file);
				loadFromStream(reader);
				return true;
			} catch (IOException e) {
				CougaarPlugin.log(e); //$NON-NLS-1$
			} finally {
				try {
					if (reader != null) {
						reader.close();
					}
				} catch (IOException e) {
				}
			}
		}

		return false;
	}

	private static void loadFromStream(Reader reader) throws CoreException {
		Element cpElement;
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			cpElement = parser.parse(new InputSource(reader))
					.getDocumentElement();
		} catch (SAXException e) {
			CougaarPlugin.log(e);
			return;
		} catch (ParserConfigurationException e) {
			CougaarPlugin.log(e);
			return;
		} catch (IOException e) {
			CougaarPlugin.log(e);
			return;
		}

		if (cpElement == null) {
			return;
		}

		if (!cpElement.getNodeName().equalsIgnoreCase(NODE_ROOT)) {
			return;
		}

		NodeList list = cpElement.getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				if (element.getNodeName().equalsIgnoreCase(NODE_ENTRY)) {
					String varPath = element.getAttribute(NODE_PATH);
					String varVersion = element.getAttribute(NODE_VERSION);
					boolean isDefault = element.getAttribute(NODE_DEFAULT)
							.equalsIgnoreCase("true");

					setCougaarBaseLocation(varVersion, varPath, false);
					if (isDefault) {
						setDefaultVersion(varVersion);
					}
				}
			}
		}
	}

	/**
	 * Get the cougaar base location for the specified version
	 * 
	 * @param version
	 *            the version of the cougaar base location to retreive
	 * 
	 * @return the cougaar base location for the specified version
	 */
	public static String getCougaarBaseLocation(String version) {
		return (String) getCougaarLocations().get(version);
	}

	/**
	 * Sets the cougaar base location for the specified version
	 * 
	 * @param version
	 *            the version for the specified location
	 * @param path
	 *            the path to the cougaar base location
	 */
	public static void setCougaarLocation(String version, String path) {
		setCougaarBaseLocation(version, path.replace('\\', '/'), true);
	}

	private static void setCougaarBaseLocation(String version, String path,
			boolean save) {
		boolean needsSave;
		if (path.equals("")) {
			Object old = getCougaarLocations().remove(version);
			needsSave = save && (old != null);
		} else {
			Object old = getCougaarLocations().put(version, path);
			needsSave = save && (!path.equals(old));
		}

		if (needsSave) {
			try {
				storeLocations();
			} catch (CoreException e) {
				CougaarPlugin.log(e);
			}
		}
	}

	private static File getStoreFile() {
		IPath path = CougaarPlugin.getDefault().getStateLocation();
		path = path.append(STORE_FILE);
		return path.toFile();
	}

	private static synchronized void storeLocations() throws CoreException {
		File file = null;
		Result result = null;

		file = getStoreFile();
		result = new StreamResult(file);
		saveToStream(fCougaarLocations, result);

	}

	private static void saveToStream(Map locations, Result result)
			throws CoreException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			Element root = (Element) document.createElement(NODE_ROOT);
			document.appendChild(root);

			Iterator iter = locations.keySet().iterator();

			while (iter.hasNext()) {
				String version = (String) iter.next();
				String path = getCougaarBaseLocation(version);

				Element varElement = document.createElement(NODE_ENTRY);
				varElement.setAttribute(NODE_PATH, path);
				varElement.setAttribute(NODE_VERSION, version);
				String def = "false";
				if (isDefaultVersion(version)) {
					def = "true";
				}

				varElement.setAttribute(NODE_DEFAULT, def);
				root.appendChild(varElement);
			}
			Transformer xformer = TransformerFactory.newInstance()
					.newTransformer();
			Source src = new DOMSource(document);
			xformer.transform(src, result);

		} catch (ParserConfigurationException e) {
			CougaarPlugin.log(e);
		} catch (TransformerConfigurationException e) {
			CougaarPlugin.log(e);
		} catch (TransformerFactoryConfigurationError e) {
			CougaarPlugin.log(e);
		} catch (TransformerException e) {
			CougaarPlugin.log(e);
		}

		//        Document document = new DocumentImpl();
		//        Element rootElement = document.createElement(NODE_ROOT);
		//        document.appendChild(rootElement);
		//
		//        Iterator iter = locations.keySet().iterator();
		//
		//        while (iter.hasNext()) {
		//            String version = (String) iter.next();
		//            String path = getCougaarBaseLocation(version);
		//
		//            Element varElement = document.createElement(NODE_ENTRY);
		//            varElement.setAttribute(NODE_PATH, path);
		//            varElement.setAttribute(NODE_VERSION, version);
		//            String def = "false";
		//            if (isDefaultVersion(version)) {
		//                def = "true";
		//            }
		//
		//            varElement.setAttribute(NODE_DEFAULT, def);
		//            rootElement.appendChild(varElement);
		//        }
		//
		//        try {
		//            OutputFormat format = new OutputFormat();
		//            format.setIndenting(true);
		//            Serializer serializer = SerializerFactory.getSerializer(P)
		//// .getSerializerFactory(Method.XML)
		//// .makeSerializer(writer,
		//// format);
		//            serializer.asDOMSerializer().serialize(document);
		//        } catch (IOException e) {
		//            CougaarPlugin.log(e);
		//        }
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param version
	 *            DOCUMENT ME!
	 */
	public static void setDefaultVersion(String version) {
		//TODO: add error checking
		String prevDef = defaultVersion;
		defaultVersion = version;
		if (!defaultVersion.equals(prevDef)) {
			try {
				storeLocations();
			} catch (CoreException e) {
				CougaarPlugin.log(e);
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public static String getDefaultVersion() {
		return defaultVersion;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param version
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public static boolean isDefaultVersion(String version) {
		return defaultVersion.equals(version);
	}
}