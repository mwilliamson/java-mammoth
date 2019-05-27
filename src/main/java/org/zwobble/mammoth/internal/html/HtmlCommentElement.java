package org.zwobble.mammoth.internal.html;

import org.zwobble.mammoth.internal.html.HtmlNode.Mapper;
import org.zwobble.mammoth.internal.html.HtmlNode.Visitor;

public class HtmlCommentElement implements HtmlNode{

	private String commentContent;

	public HtmlCommentElement(String commentContent) {
		this.commentContent = commentContent;
	}
	
	public String getCommentContent() {
		return commentContent;
	}
	
	 @Override
   public void accept(Visitor visitor) {
       visitor.visit(this);
   }

   @Override
   public <T> T accept(Mapper<T> visitor) {
       return visitor.visit(this);
   }
}
