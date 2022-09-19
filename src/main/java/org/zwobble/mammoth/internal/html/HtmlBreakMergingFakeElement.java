package org.zwobble.mammoth.internal.html;

import static org.zwobble.mammoth.internal.util.Lists.list;

import org.zwobble.mammoth.internal.util.Maps;

/**
 * Fake element used for breaking the collapsing of the elements 
 */
public class HtmlBreakMergingFakeElement extends HtmlElement {

  /**
   * Constructor
   */
  public HtmlBreakMergingFakeElement() {
    super(new HtmlTag(list(""), Maps.map(), false, ""), list());
  }
  
  @Override
  public <T> T accept(Mapper<T> visitor) {
    return visitor.visit(this);
  }
  
  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
