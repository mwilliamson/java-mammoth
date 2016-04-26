using Xunit;
using System.IO;

namespace Mammoth.Tests {
	public class DocumentConverterTests {
		[Fact]
		public void DocxContainingOneParagraphIsConvertedToSingleParagraphElement() {
			assertSuccessfulConversion(
				ConvertToHtml("single-paragraph.docx"),
				"<p>Walking on imported air</p>");
		}

		[Fact]
		public void CanReadFilesWithUtf8Bom() {
			assertSuccessfulConversion(
				ConvertToHtml("utf8-bom.docx"),
				"<p>This XML has a byte order mark.</p>");
		}

		private void assertSuccessfulConversion(IResult<string> result, string expectedValue) {
			Assert.Empty(result.Warnings);
			Assert.Equal(expectedValue, result.Value);
		}

		private IResult<string> ConvertToHtml(string name) {
			return new DocumentConverter().ConvertToHtml(TestFilePath(name));
		}

		private string TestFilePath(string name) {
			return Path.Combine("../../TestData", name);
		}
	}
}

