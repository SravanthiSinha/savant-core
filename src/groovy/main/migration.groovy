File fromDir = new File(args[0]);
File toDir = new File(args[1]);

AntBuilder ant = new AntBuilder();

ant.delete(dir: toDir)
ant.mkdir(dir: toDir)

// copy everything from the old savant repo to the new
ant.copy(toDir: toDir, preservelastmodified: true) {
  fileset(dir: fromDir);
}

// find amd files
toDir.eachDirRecurse { dir ->
  dir.eachFileRecurse { file ->
    migrateAmd(file);
  }
}

/**
 * Migrate the amd files.  Marks old ones as ".old" and also updates the md5
 *
 * @param amdFile the amd file
 * @return void
 */
def migrateAmd(File amdFile) {
  if (amdFile.isFile() && amdFile.getName().endsWith(".amd")) {

    File amdMd5File = new File(amdFile.absolutePath + ".md5");

    // save old
    AntBuilder ant = new AntBuilder();
    ant.copy(file: amdFile, toFile: new File(amdFile.absolutePath + ".old").absolutePath, preservelastmodified: true);
    ant.copy(file: amdMd5File, toFile: new File(amdMd5File.absolutePath + ".old").absolutePath, preservelastmodified: true);

    String oldXml = amdFile.getText();

    String newXml = migrateArtifactGroups(oldXml);

    println("\n");
    println(newXml);
    println("\n");
  }
}

/**
 * Parses the old xml, finds the artifacts, and modifies the groups to be the new format
 *
 * @param oldXml the xml from the old amd file
 * @return the new, properly formatted xml
 */
String migrateArtifactGroups(String oldXml) {
  Node root = new XmlParser().parseText(oldXml);
  def allNodes = root.depthFirst();
  allNodes.each {
    Node node = (Node) it;
    if (node.name() == 'artifact') {
      String oldGroup = node.attribute("group");
      def oldGroupTokens = oldGroup.tokenize(".");
      def newGroupTokens = [];
      String newGroup = "";
      oldGroupTokens.reverseEach { newGroupTokens << it }
      newGroupTokens.eachWithIndex { newGroupToken, i ->
        newGroup += newGroupToken;
        if (i < oldGroupTokens.size() - 1) {
          newGroup += ".";
        }
      }

      node.attributes()["group"] = newGroup;
    }
  }

  def writer = new StringWriter()
  new XmlNodePrinter(new PrintWriter(writer)).print(root)
  return writer.toString()
}