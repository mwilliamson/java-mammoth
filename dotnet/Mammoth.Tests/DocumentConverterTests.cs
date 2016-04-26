using Xunit;
using System.IO;

namespace Mammoth.Tests {
	public class DocumentConverterTests {
		[Fact]
		public void DocxContainingOneParagraphIsConvertedToSingleParagraphElement() {
			assertSuccessfulConversion(
				convertToHtml("single-paragraph.docx"),
				"<p>Walking on imported air</p>");
		}

		private void assertSuccessfulConversion(IResult<string> result, string expectedValue) {
			Assert.Empty(result.Warnings);
			Assert.Equal(expectedValue, result.Value);
		}

		private IResult<string> convertToHtml(string name) {
			return new DocumentConverter().ConvertToHtml(TestFilePath(name));
		}

		private string TestFilePath(string name) {
			return Path.Combine("../../TestData", name);
		}
	}
}

