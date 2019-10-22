package org.eclipse.scout.migration.ecma6;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.model.old.JsFile;
import org.eclipse.scout.migration.ecma6.model.references.JsImport;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MigrationUtility {

  private static final Pattern REGEX_COMMENT_REMOVE_1 = Pattern.compile("//.*?\r\n");
  private static final Pattern REGEX_COMMENT_REMOVE_2 = Pattern.compile("//.*?\n");
  private static final Pattern REGEX_COMMENT_REMOVE_3 = Pattern.compile("(?s)/\\*.*?\\*/");

  private static final Logger LOG = LoggerFactory.getLogger(MigrationUtility.class);

  private MigrationUtility() {
  }

  public static void prependTodo(WorkingCopy workingCopy, String todoText) {
    String source = workingCopy.getSource();
    source = prependTodo(source, todoText, workingCopy.getLineDelimiter());
    workingCopy.setSource(source);
  }

  public static String prependTodo(String source, String todoText, String lineSeparator) {
    source = "// TODO MIG: " + todoText + lineSeparator + source;
    return source;
  }

  public static String parseMemberName(String fullyQualifiedName) {
    if (StringUtility.isNullOrEmpty(fullyQualifiedName)) {
      return null;
    }
    int lastDot = fullyQualifiedName.lastIndexOf('.');
    if (lastDot < 0) {
      return fullyQualifiedName;
    }
    return fullyQualifiedName.substring(lastDot + 1);
  }

  public static String removeFirstSegments(Path p, int numSegments) {
    int existingSegmentsCount = p.getNameCount();
    return p.subpath(Math.min(existingSegmentsCount - 1, numSegments), existingSegmentsCount).toString().replace('\\', '/');
  }

  public static String removeFirstSegments(String path, int numSegments) {
    return removeFirstSegments(Paths.get(path), numSegments);
  }

  public static String removeComments(CharSequence methodBody) {
    if (methodBody == null) {
      return null;
    }
    if (!StringUtility.hasText(methodBody)) {
      return methodBody.toString();
    }
    String retVal = REGEX_COMMENT_REMOVE_1.matcher(methodBody).replaceAll("");
    retVal = REGEX_COMMENT_REMOVE_2.matcher(retVal).replaceAll("");
    retVal = REGEX_COMMENT_REMOVE_3.matcher(retVal).replaceAll("");
    return retVal;
  }

  public static boolean waitForUserConfirmation() {
    //noinspection resource
    @SuppressWarnings("resource")
    Scanner inScanner = new Scanner(System.in); // Create a Scanner object
    System.out.println("Type 'yes' or 'y' to continue, 'no' or 'n' to abort.");
    String answer = inScanner.nextLine(); // Read user input
    return "yes".equalsIgnoreCase(answer) || "y".equalsIgnoreCase(answer);
  }

  public static void insertImports(WorkingCopy workingCopy, Context context) {
    JsFile jsFile = context.ensureJsFile(workingCopy);
    String lineDelimiter = workingCopy.getLineDelimiter();
    // create imports
    Collection<JsImport> imports = jsFile.getImports();
    if (imports.isEmpty()) {
      return;
    }
    String importsSource = imports.stream()
        .map(imp -> imp.toSource(context))
        .collect(Collectors.joining(lineDelimiter));

    StringBuilder sourceBuilder = new StringBuilder(workingCopy.getSource());
    if (jsFile.getCopyRight() == null) {
      sourceBuilder.insert(0, importsSource + lineDelimiter + lineDelimiter);
    }
    else {
      Matcher matcher = Pattern.compile(Pattern.quote(jsFile.getCopyRight().getSource())).matcher(sourceBuilder.toString());
      if (matcher.find()) {
        sourceBuilder.insert(matcher.end(), importsSource + lineDelimiter + lineDelimiter);
      }
      else {
        sourceBuilder.insert(0, MigrationUtility.prependTodo("", "insert 'var instance;' manual.", lineDelimiter));
        LOG.warn("Could not find end of copyright in file '" + jsFile.getPath() + "'");
      }
    }
    workingCopy.setSource(sourceBuilder.toString());
  }
}