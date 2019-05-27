package org.zwobble.mammoth.internal.documents;

public class Math implements DocumentElement {
	 private final String mathXMLFragment;

   public Math(String mathXMLFragment) {
       this.mathXMLFragment = mathXMLFragment;
   }

   public String getMathXMLFragment() {
       return mathXMLFragment;
   }
	
	@Override
	public <T, U> T accept(DocumentElementVisitor<T, U> visitor, U context) {
	  return visitor.visit(this, context);
	}

}
