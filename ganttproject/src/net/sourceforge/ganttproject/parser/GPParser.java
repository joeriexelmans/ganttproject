/*
 * Created on 12.03.2005
 */
package net.sourceforge.ganttproject.parser;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author bard
 */
public interface GPParser {
  void load(InputStream inStream) throws IOException;

  void addTagHandler(TagHandler handler);

  void addParsingListener(ParsingListener listener);
}
