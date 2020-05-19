package net.sourceforge.ganttproject.parser;

import com.google.common.collect.ImmutableSet;
import net.sourceforge.ganttproject.document.Document;
import net.sourceforge.ganttproject.document.DocumentManager;
import net.sourceforge.ganttproject.document.ProxyDocument;
import org.xml.sax.Attributes;

import java.util.Set;

public class PortfolioTagHandler extends AbstractTagHandler {
  private static final String PORTFOLIO_TAG = "portfolio";
  private static final String PROJECT_TAG = "project";
  private static final Set<String> TAGS = ImmutableSet.of(PORTFOLIO_TAG, PROJECT_TAG);
  private static final String LOCATION_ATTR = "location";

  private final DocumentManager documentManager;

  private boolean isReadingPortfolio = false;
  private Document defaultDocument = null;

  public PortfolioTagHandler(DocumentManager documentManager) {
    super(null, false);
    this.documentManager = documentManager;
  }

  @Override
  public void startElement(String namespaceURI, String sName, String qName, Attributes attrs)
      throws FileFormatException {
    if (!TAGS.contains(qName)) {
      return;
    }
    setTagStarted(true);
    if (PORTFOLIO_TAG.equals(qName)) {
      isReadingPortfolio = true;
      return;
    }
    if (PROJECT_TAG.equals(qName) && isReadingPortfolio) {
      String locationAsString = attrs.getValue(LOCATION_ATTR);
      if (locationAsString != null) {
        if (defaultDocument != null) {
          throw new IllegalStateException("Don't set default document twice");
        }
        Document doc = documentManager.getDocument(locationAsString);
        defaultDocument = doc;
      }
    }
  }

  @Override
  public void endElement(String namespaceURI, String sName, String qName) {
    if (!TAGS.contains(qName)) {
      return;
    }
    if (PORTFOLIO_TAG.equals(qName)) {
      isReadingPortfolio = false;
    }
    setTagStarted(false);
  }

  public Document getDefaultDocument() {
    return defaultDocument;
  }
}
