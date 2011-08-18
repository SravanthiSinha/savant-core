import org.savantbuild.io.FileTools
import org.savantbuild.io.MD5

/**
 * Migrates a 1.0 savant repository to 2.0.  The primary migration
 * is to reverse the old artifact:group format to the new 2.0 format
 *
 * This script requires the savant-core jar
 */

validateArgs();

def validateArgs() {
  if (args.size() == 0) {
    println("""You must provide 2 arguments when executing this script.  The first argument
  is the directory you are migrating from, the second argument is the directory you are migrating to""");
    System.exit(-1);
  }
}

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

    File amdMd5File = new File("${amdFile.absolutePath}.md5");

    println("Migrating ${amdFile.getAbsolutePath()}")

    // save old
    AntBuilder ant = new AntBuilder();
    ant.copy(file: amdFile, toFile: new File("${amdFile.absolutePath}.old").absolutePath, preservelastmodified: true);
    ant.copy(file: amdMd5File, toFile: new File("${amdMd5File.absolutePath}.old").absolutePath, preservelastmodified: true);

    String oldXml = amdFile.getText();

    println("\nOld XML:");
    println(oldXml);

    String bugFixXml = fixCompatTypeBug(oldXml);

    println("\nBug Fixed XML");
    println(bugFixXml);

    String migratedXml = migrateArtifactGroups(bugFixXml);

    println("\nMigrated XML");
    println(migratedXml);

    amdFile.write(migratedXml);

    MD5 md5 = FileTools.md5(amdFile);
    amdMd5File.write(md5.sum);
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

/**
 * Some old files contained this:
 *
 * <artifact-meta-data> compatType="minor">
 *
 * This method fixes it
 *
 * @param node the root xml node
 * @return
 */
def fixCompatTypeBug(String xml) {
  return xml.
          replace("> compatType=\"minor\"", " compatType=\"minor\"").
          replace("> compatType=\"major\"", " compatType=\"major\"").
          replace("> compatType=\"patch\"", " compatType=\"patch\"").
          replace("> compatType=\"identical\"", " compatType=\"identical\"")
}