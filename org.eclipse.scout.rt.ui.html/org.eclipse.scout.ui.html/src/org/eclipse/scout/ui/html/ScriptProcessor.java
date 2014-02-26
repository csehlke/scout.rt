package org.eclipse.scout.ui.html;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.phloc.css.ECSSVersion;
import com.phloc.css.decl.CSSStyleRule;
import com.phloc.css.decl.CascadingStyleSheet;
import com.phloc.css.reader.CSSReader;

/**
 * Process JS and CSS scripts such as <code>/WebContent/res/scout-4.0.0.css</code> and
 * <code>/WebContent/res/scout-4.0.0.js</code>
 */
public class ScriptProcessor {
  public static void main(String[] args) throws IOException {
    File outputFile = null;
    ScriptProcessor processor = new ScriptProcessor();
    for (int i = 0; i < args.length; i++) {
      if ("--i".equals(args[i])) {
        String path = args[i + 1];
        processor.setInput(path, TextFileUtil.readUTF8(new File(path)));
      }
      if ("--o".equals(args[i])) {
        outputFile = new File(args[i + 1]);
      }
      if ("--root".equals(args[i])) {
        final File rootDir = new File(args[i + 1]);
        processor.includeFileLoader = new ITextFileLoader() {
          @Override
          public String read(String path) throws IOException {
            return TextFileUtil.readUTF8(new File(rootDir, path));
          }
        };
      }
    }
    if (processor.m_input == null) {
      usage("missing --i");
      System.exit(-1);
    }
    if (outputFile == null) {
      usage("missing --o");
      System.exit(-1);
    }
    if (processor.includeFileLoader == null) {
      usage("missing --root");
      System.exit(-1);
    }
    TextFileUtil.writeUTF8(outputFile, processor.process());
  }

  public static void usage(String errorMessage) {
    if (errorMessage != null) {
      System.err.println("ERROR: " + errorMessage);
    }
    System.err.println("Usage: " + ScriptProcessor.class.getName() + " --i inputfilename --o outputfilename --root rootDirForIncludes");
  }

  private String m_input;
  private String m_inputType;
  private ITextFileLoader includeFileLoader;

  /**
   * @param input
   * @param inputType
   *          js or css
   */
  public void setInput(String path, String content) {
    int dot = path.lastIndexOf('.');
    m_inputType = path.substring(dot + 1);
    m_input = content;
  }

  public void setIncludeFileLoader(ITextFileLoader includeFileLoader) {
    this.includeFileLoader = includeFileLoader;
  }

  public String process() throws IOException {
    if (m_inputType.equalsIgnoreCase("js")) {
      return processJavascript();
    }
    if (m_inputType.equalsIgnoreCase("css")) {
      return processCss();
    }
    System.err.println("Unexpected inputType: " + m_inputType);
    return m_input;
  }

  protected String processJavascript() throws IOException {
    String content = m_input;
    content = replaceIncludeDirectives(content);
    content = replaceConstants(content);
    return content;
  }

  protected String processCss() throws IOException {
    String content = m_input;
    content = replaceIncludeDirectives(content);
    content = replaceConstants(content);
    //XXX cgu: when activated then tree icons are missing
    //content = makeCanonical(content);
    return content;
  }

  protected String replaceIncludeDirectives(String content) throws IOException {
    return TextFileUtil.processIncludeDirectives(content, includeFileLoader);
  }

  protected String replaceConstants(String content) throws IOException {
    //TODO imo
    return content;
  }

  /**
   * Merges all the selectors including its properties so every selector appears only once in the file.
   * <p>
   * The properties of the lower selectors have higher priority and therefore override existing properties when merged.
   */
  protected String makeCanonical(String content) throws IOException {
    CascadingStyleSheet styleSheet = CSSReader.readFromString(content, "utf-8", ECSSVersion.CSS30);
    if (styleSheet == null) {
      throw new IOException("Parsing file failed. Please check if the input file contains valid css.");
    }
    List<CSSStyleRule> styleRules = styleSheet.getAllStyleRules();
    CanonicalStyleMap styleMap = new CanonicalStyleMap();
    for (CSSStyleRule styleRule : styleRules) {
      styleMap.put(styleRule);
    }
    return styleMap.generateStyleSheetText();
  }
}
