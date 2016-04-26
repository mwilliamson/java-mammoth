using Xunit;
using System.IO;
using Xunit.Sdk;
using System;

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

		[Fact]
		public void EmptyParagraphsAreIgnoredByDefault() {
			assertSuccessfulConversion(
				ConvertToHtml("empty.docx"),
				"");
		}

		[Fact]
		public void EmptyParagraphsArePreservedIfIgnoreEmptyParagraphsIsFalse() {
			assertSuccessfulConversion(
				ConvertToHtml("empty.docx", converter => converter.PreserveEmptyParagraphs()),
				"<p></p>");
		}

		// TODO: simple list

		[Fact]
		public void WordTablesAreConvertedToHtmlTables() {
			assertSuccessfulConversion(
				ConvertToHtml("tables.docx"),
				"<p>Above</p>" +
				"<table>" +
				"<tr><td><p>Top left</p></td><td><p>Top right</p></td></tr>" +
				"<tr><td><p>Bottom left</p></td><td><p>Bottom right</p></td></tr>" +
				"</table>" +
				"<p>Below</p>");
		}

		[Fact]
		public void InlineImagesAreIncludedInOutput() {
			assertSuccessfulConversion(
				ConvertToHtml("tiny-picture.docx"),
				"<p><img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAIAAAACUFjqAAAAAXNSR0IArs4c6QAAAAlwSFlzAAAOvgAADr4B6kKxwAAAABNJREFUKFNj/M+ADzDhlWUYqdIAQSwBE8U+X40AAAAASUVORK5CYII=\" /></p>");
		}

		private void assertSuccessfulConversion(IResult<string> result, string expectedValue) {
			if (result.Warnings.Count > 0) {
				throw new XunitException("Unexpected warnings: " + string.Join(", ", result.Warnings));
			}
			Assert.Equal(expectedValue, result.Value);
		}

		private IResult<string> ConvertToHtml(string name) {
			return ConvertToHtml(name, converter => converter);
		}

		private IResult<string> ConvertToHtml(string name, Func<DocumentConverter, DocumentConverter> configure) {
			return configure(new DocumentConverter()).ConvertToHtml(TestFilePath(name));
		}

		private string TestFilePath(string name) {
			return Path.Combine("../../TestData", name);
		}
	}
}

