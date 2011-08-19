/*
 * Copyright (c) 2001-2006, Inversoft, All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.savantbuild.dep.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.savantbuild.BuildException;
import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.ArtifactGroup;
import org.savantbuild.domain.ArtifactMetaData;
import org.savantbuild.domain.Dependencies;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>
 * This class is a toolkit for handling artifact operations.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class ArtifactTools {
  /**
   * Parses the MetaData from the given Savant .amd file.
   *
   * @param file The File to read the XML MetaData information from.
   * @return The MetaData parsed.
   * @throws BuildException If the parsing failed.
   */
  public static ArtifactMetaData parseArtifactMetaData(final File file) {
    try {
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
      ArtifactMetaDataHandler handler = new ArtifactMetaDataHandler();
      parser.parse(file, handler);
      return new ArtifactMetaData(handler.dependencies, handler.compatibility);
    } catch (Exception e) {
      throw new BuildException(e);
    }
  }

  /**
   * Generates a temporary file that contains ArtifactMetaData XML which includes all
   * of the artifacts in the artifactMetaData given.
   *
   * @param artifactMetaData The MetaData object to serialize to XML.
   * @return The temp file and never null.
   * @throws BuildException If the temp could not be created, or the XML could not be written.
   */
  public static File generateXML(ArtifactMetaData artifactMetaData) throws BuildException {
    try {
      File tmp = File.createTempFile("savant", "amd");
      tmp.deleteOnExit();

      PrintWriter pw = new PrintWriter(new FileWriter(tmp));
      pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      pw.print("<artifact-meta-data");

      String compatibility = artifactMetaData.getCompatibility();
      if (compatibility != null) {
        pw.print(" compatibility=\"" + compatibility + "\"");
      }
      pw.println(">");

      Dependencies deps = artifactMetaData.getDependencies();
      if (deps != null) {
        pw.println("  <dependencies>");
        Map<String, ArtifactGroup> groups = deps.getArtifactGroups();
        Set<String> keys = groups.keySet();
        for (String key : keys) {
          ArtifactGroup group = groups.get(key);
          pw.println("    <artifact-group type=\"" + group.getType() + "\">");
          List<Artifact> artifacts = group.getArtifacts();
          for (Artifact artifact : artifacts) {
            pw.println("      <artifact " +
              "group=\"" + artifact.getGroup() + "\" " +
              "project=\"" + artifact.getProject() + "\" " +
              "name=\"" + artifact.getName() + "\" " +
              "version=\"" + artifact.getVersion() + "\" " +
              "type=\"" + artifact.getType() + "\"/>");

          }
          pw.println("    </artifact-group>");
        }
        pw.println("  </dependencies>");
      }
      pw.println("</artifact-meta-data>");

      pw.flush();
      pw.close();
      return tmp;
    } catch (IOException ioe) {
      throw new BuildException(ioe);
    }
  }

  private static class ArtifactMetaDataHandler extends DefaultHandler {
    private String compatibility;
    private Dependencies dependencies;
    private ArtifactGroup group;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if (qName.equals("artifact-meta-data")) {
        compatibility = attributes.getValue("compatType"); // 1.5 compatibility
        if (compatibility == null) {
          compatibility = attributes.getValue("compatibility");
        }
      } else if (qName.equals("dependencies")) {
        dependencies = new Dependencies();
      } else if (qName.equals("artifact-group")) {
        String type = attributes.getValue("type");
        group = new ArtifactGroup(type);
        dependencies.getArtifactGroups().put(type, group);
      } else if (qName.equals("artifact")) {
        Artifact artifact;
        try {
          artifact = new Artifact(attributes.getValue("group"), attributes.getValue("project"),
            attributes.getValue("name"), attributes.getValue("version"), attributes.getValue("type"));
        } catch (IllegalArgumentException e) {
          throw new BuildException(e);
        }
        group.getArtifacts().add(artifact);
      } else {
        throw new SAXException("Invalid element encountered in AMD file [" + qName + "]. You might need to upgrade " +
          "Savant to use this artifact because it might be using a new feature of Savant.");
      }
    }
  }
}
