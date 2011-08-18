/*
 * Copyright (c) 2001-2010, Inversoft, All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.savantbuild.dep.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.savantbuild.BuildException;
import org.savantbuild.domain.Artifact;
import org.savantbuild.domain.ArtifactGroup;
import org.savantbuild.domain.ArtifactMetaData;
import org.savantbuild.domain.Dependencies;
import org.savantbuild.io.DoesNotExistException;
import org.savantbuild.io.PermanentIOException;
import org.savantbuild.io.TemporaryIOException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * This is a toolkit for helping deal with Maven crap.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class MavenTools {
  /**
   * Builds the POM name based on the Artifact.
   *
   * @param artifact The artifact.
   * @return The POM name.
   */
  public static String pomName(Artifact artifact) {
    return artifact.getName() + "-" + artifact.getVersion() + ".pom";
  }

  /**
   * Parses a POM and extracts a ArtifactMetaData object.
   *
   * @param file The file to parse.
   * @return The Dependencies object.
   * @throws DoesNotExistException Never.
   * @throws PermanentIOException  If the parsing of the POM fails.
   * @throws TemporaryIOException  Never.
   */
  public static ArtifactMetaData parsePOM(final File file)
    throws TemporaryIOException, PermanentIOException, DoesNotExistException {
    try {
      DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      FileInputStream fis = new FileInputStream(file);
      Document doc = db.parse(fis);
      Element root = doc.getDocumentElement();
      NodeList list = root.getElementsByTagName("dependencies");
      if (list.getLength() == 0) {
        return new ArtifactMetaData(null, "major");
      }

      Dependencies deps = new Dependencies();
      for (int i = 0; i < list.getLength(); i++) {
        Element element = (Element) list.item(i);
        if (element.getParentNode() == root) {
          NodeList dependencies = element.getElementsByTagName("dependency");
          for (int j = 0; j < dependencies.getLength(); j++) {
            Element dependency = (Element) dependencies.item(j);
            Artifact artifact = new Artifact(childValue(dependency, "groupId", null),
              childValue(dependency, "artifactId", null), childValue(dependency, "artifactId", null),
              childValue(dependency, "version", "{latest}"), childValue(dependency, "type", "jar"));
            String groupType = childValue(dependency, "scope", "run");
            if (groupType.equals("test")) {
              groupType = "test-run";
            } else if (groupType.equals("provided")) {
              groupType = "compile-only";
            }

            ArtifactGroup group = deps.getArtifactGroups().get(groupType);
            if (group == null) {
              group = new ArtifactGroup(groupType);
              deps.getArtifactGroups().put(groupType, group);
            }

            group.getArtifacts().add(artifact);
          }
        }
      }

      return new ArtifactMetaData(deps, "major");
    } catch (SAXException e) {
      throw new BuildException(e);
    } catch (ParserConfigurationException e) {
      throw new BuildException(e);
    } catch (IOException e) {
      throw new BuildException(e);
    }
  }

  private static String childValue(Element element, String name, String def) {
    NodeList list = element.getElementsByTagName(name);
    if (list.getLength() == 0) {
      return def;
    }

    return list.item(0).getTextContent().trim();
  }
}
