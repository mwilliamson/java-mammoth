# Mammoth .docx to HTML converter for Java/JVM

Mammoth is designed to convert .docx documents,
such as those created by Microsoft Word, Google Docs and LibreOffice,
and convert them to HTML.
Mammoth aims to produce simple and clean HTML by using semantic information in the document,
and ignoring other details.
For instance,
Mammoth converts any paragraph with the style `Heading 1` to `h1` elements,
rather than attempting to exactly copy the styling (font, text size, colour, etc.) of the heading.

There's a large mismatch between the structure used by .docx and the structure of HTML,
meaning that the conversion is unlikely to be perfect for more complicated documents.
Mammoth works best if you only use styles to semantically mark up your document.

The following features are currently supported:

* Headings.

* Lists.

* Customisable mapping from your own docx styles to HTML.
  For instance, you could convert `WarningHeading` to `h1.warning` by providing an appropriate style mapping.

* Tables.
  The formatting of the table itself, such as borders, is currently ignored,
  but the formatting of the text is treated the same as in the rest of the document.

* Footnotes and endnotes.

* Images.

* Bold, italics, underlines, strikethrough, superscript and subscript.

* Links.

* Line breaks.

* Text boxes. The contents of the text box are treated as a separate paragraph
  that appears after the paragraph containing the text box.

* Comments.

## Installation

Available on [Maven Central](http://search.maven.org/#artifactdetails|org.zwobble.mammoth|mammoth|1.9.0|jar).

```xml
<dependency>
  <groupId>org.zwobble.mammoth</groupId>
  <artifactId>mammoth</artifactId>
  <version>1.9.0</version>
</dependency>
```

## Other supported platforms

* [JavaScript](https://github.com/mwilliamson/mammoth.js), both the browser and node.js.
  Available [on npm](https://www.npmjs.com/package/mammoth).

* [Python](https://github.com/mwilliamson/python-mammoth).
  Available [on PyPI](https://pypi.python.org/pypi/mammoth).

* [WordPress](https://wordpress.org/plugins/mammoth-docx-converter/).

* [.NET](https://github.com/mwilliamson/dotnet-mammoth).
  Available [on NuGet](https://www.nuget.org/packages/Mammoth/).

## Usage

### Library

#### Basic conversion

To convert an existing .docx file to HTML,
create an instance of `DocumentConverter` and
pass an instance of `File` to `convertToHtml`.
For instance:

```java
import org.zwobble.mammoth.DocumentConverter;
import org.zwobble.mammoth.Result;

DocumentConverter converter = new DocumentConverter();
Result<String> result = converter.convertToHtml(new File("document.docx"));
String html = result.getValue(); // The generated HTML
Set<String> warnings = result.getWarnings(); // Any warnings during conversion
```

You can also extract the raw text of the document by using `extractRawText`.
This will ignore all formatting in the document.
Each paragraph is followed by two newlines.

```java
DocumentConverter converter = new DocumentConverter();
Result<String> result = converter.extractRawText(new File("document.docx"));
String html = result.getValue(); // The raw text
Set<String> warnings = result.getWarnings(); // Any warnings during conversion
```

#### Custom style map

By default,
Mammoth maps some common .docx styles to HTML elements.
For instance,
a paragraph with the style name `Heading 1` is converted to a `h1` element.
You can add custom style maps by calling `addStyleMap(String)`.
A description of the syntax for style maps can be found in the section ["Writing style maps"](#writing-style-maps).
For instance, if paragraphs with the style name `Section Title` should be converted to `h1` elements,
and paragraphs with the style name `Subsection Title` should be converted to `h2` elements:

```java
DocumentConverter converter = new DocumentConverter()
    .addStyleMap("p[style-name='Section Title'] => h1:fresh")
    .addStyleMap("p[style-name='Subsection Title'] => h2:fresh");
```

You can also pass in the entire style map as a single string,
which can be useful if style maps are stored in text files:

```java
String styleMap =
    "p[style-name='Section Title'] => h1:fresh\n" +
    "p[style-name='Subsection Title'] => h2:fresh";
DocumentConverter converter = new DocumentConverter()
    .addStyleMap(styleMap);
```

The most recently-added styles have the greatest precedence.
User-defined style mappings are used in preference to the default style mappings.
To stop using the default style mappings altogether,
call `disableDefaultStyleMap`:

```java
DocumentConverter converter = new DocumentConverter()
    .disableDefaultStyleMap();
```

#### Custom image handlers

By default, images are converted to `<img>` elements with the source included inline in the `src` attribute.
This behaviour can be changed by calling `imageConverter()` with an [image converter](#image-converters) .

For instance, the following would replicate the default behaviour:

```java
DocumentConverter converter = new DocumentConverter()
    .imageConverter(image -> {
        String base64 = streamToBase64(image::getInputStream);
        String src = "data:" + image.getContentType() + ";base64," + base64;
        Map<String, String> attributes = new HashMap<>();
        attributes.put("src", src);
        return attributes;
    });
```

where `streamToBase64` is a function that reads an input stream and encodes it as a Base64 string.

#### Bold

By default, bold text is wrapped in `<strong>` tags.
This behaviour can be changed by adding a style mapping for `b`.
For instance, to wrap bold text in `<em>` tags:

```java
DocumentConverter converter = new DocumentConverter()
    .addStyleMap("b => em");
```

#### Italic

By default, italic text is wrapped in `<em>` tags.
This behaviour can be changed by adding a style mapping for `i`.
For instance, to wrap italic text in `<strong>` tags:

```java
DocumentConverter converter = new DocumentConverter()
    .addStyleMap("i => strong");
```

#### Underline

By default, the underlining of any text is ignored since underlining can be confused with links in HTML documents.
This behaviour can be changed by adding a style mapping for `u`.
For instance, suppose that a source document uses underlining for emphasis.
The following will wrap any explicitly underlined source text in `<em>` tags:

```java
DocumentConverter converter = new DocumentConverter()
    .addStyleMap("u => em");
```

#### Strikethrough

By default, strikethrough text is wrapped in `<s>` tags.
This behaviour can be changed by adding a style mapping for `strike`.
For instance, to wrap strikethrough text in `<del>` tags:

```java
DocumentConverter converter = new DocumentConverter()
    .addStyleMap("strike => del");
```

#### Comments

By default, comments are ignored.
To include comments in the generated HTML,
add a style mapping for `comment-reference`.
For instance:

```java
DocumentConverter converter = new DocumentConverter()
    .addStyleMap("comment-reference => sup");
```

Comments will be appended to the end of the document,
with links to the comments wrapped using the specified style mapping.

### API

#### `DocumentConverter`

Methods:

* `Result<String> convertToHtml(File file)`: converts `file` into an HTML string.

* `Result<String> convertToHtml(InputStream stream)`: converts `stream` into an HTML string.
  Note that using this method instead of `convertToHtml(File file)` means that relative paths
  to other files, such as images, cannot be resolved.

* `Result<String> extractRawText(File file)`:
  extract the raw text of the document.
  This will ignore all formatting in the document.
  Each paragraph is followed by two newlines.

* `Result<String> extractRawText(InputStream stream)`:
  extract the raw text of the document.
  This will ignore all formatting in the document.
  Each paragraph is followed by two newlines.

* `DocumentConverter addStyleMap(String styleMap)`:
  add a style map to specify the mapping of Word styles to HTML.
  The most recently added style map has the greatest precedence.
  See the section ["Writing style maps"](#writing-style-maps) for a description of the syntax.

* `DocumentConverter disableDefaultStyleMap()`: by default,
  any added style maps are combined with the default style map.
  Call this to stop using the default style map altogether.

* `DocumentConverter disableEmbeddedStyleMap()`: by default,
  if the document contains an embedded style map, then it is combined with the default style map.
  Call this to ignore any embedded style maps.

* `DocumentConverter preserveEmptyParagraphs()`: by default, empty paragraphs are ignored.
  Call this to preserve empty paragraphs in the output.

* `DocumentConverter idPrefix(String idPrefix)`:
  a string to prepend to any generated IDs,
  such as those used by bookmarks, footnotes and endnotes.
  Defaults to the empty string.

* `DocumentConverter imageConverter(ImageConverter.ImgElement imageConverter)`:
  by default, images are converted to `<img>` elements with the source included inline in the `src` attribute.
  Call this to change how images are converted.

#### `Result<T>`

Represents the result of a conversion. Methods:

* `T getValue()`: the generated text.

* `Set<String> getWarnings()`: any warnings generated during the conversion.

#### Image converters

An image converter can be created by implementing `ImageConverter.ImgElement`.
This creates an `<img>` element for each image in the original docx.
The interface has a single method, `Map<String, String> convert(Image image)`.
The `image` argument is the image element being converted,
and has the following methods:

* `InputStream getInputStream()`: open the image file.

* `String getContentType()`: the content type of the image, such as `image/png`.

* `Optional<String> getAltText()`: the alt text of the image, if any.

`convert()` should return a `Map` of attributes for the `<img>` element.
At a minimum, this should include the `src` attribute.
If any alt text is found for the image,
this will be automatically added to the element's attributes.

For instance, the following replicates the default image conversion:

```java
DocumentConverter converter = new DocumentConverter()
    .imageConverter(image -> {
        String base64 = streamToBase64(image::getInputStream);
        String src = "data:" + image.getContentType() + ";base64," + base64;
        Map<String, String> attributes = new HashMap<>();
        attributes.put("src", src);
        return attributes;
    });
```

where `streamToBase64` is a function that reads an input stream and encodes it as a Base64 string.

## Writing style maps

A style map is made up of a number of style mappings separated by new lines.
Blank lines and lines starting with `#` are ignored.

A style mapping has two parts:

* On the left, before the arrow, is the document element matcher.
* On the right, after the arrow, is the HTML path.

When converting each paragraph,
Mammoth finds the first style mapping where the document element matcher matches the current paragraph.
Mammoth then ensures the HTML path is satisfied.

### Freshness

When writing style mappings, it's helpful to understand Mammoth's notion of freshness.
When generating, Mammoth will only close an HTML element when necessary.
Otherwise, elements are reused.

For instance, suppose one of the specified style mappings is `p[style-name='Heading 1'] => h1`.
If Mammoth encounters a .docx paragraph with the style name `Heading 1`,
the .docx paragraph is converted to a `h1` element with the same text.
If the next .docx paragraph also has the style name `Heading 1`,
then the text of that paragraph will be appended to the *existing* `h1` element,
rather than creating a new `h1` element.

In most cases, you'll probably want to generate a new `h1` element instead.
You can specify this by using the `:fresh` modifier:

`p[style-name='Heading 1'] => h1:fresh`

The two consecutive `Heading 1` .docx paragraphs will then be converted to two separate `h1` elements.

Reusing elements is useful in generating more complicated HTML structures.
For instance, suppose your .docx contains asides.
Each aside might have a heading and some body text,
which should be contained within a single `div.aside` element.
In this case, style mappings similar to `p[style-name='Aside Heading'] => div.aside > h2:fresh` and
`p[style-name='Aside Text'] => div.aside > p:fresh` might be helpful.

### Document element matchers

#### Paragraphs, runs and tables

Match any paragraph:

```
p
```

Match any run:

```
r
```

Match any table:

```
table
```

To match a paragraph, run or table with a specific style,
you can reference the style by name.
This is the style name that is displayed in Microsoft Word or LibreOffice.
For instance, to match a paragraph with the style name `Heading 1`:

```
p[style-name='Heading 1']
```

You can also match a style name by prefix.
For instance, to match a paragraph where the style name starts with `Heading`:

```
p[style-name^='Heading']
```

Styles can also be referenced by style ID.
This is the ID used internally in the .docx file.
To match a paragraph or run with a specific style ID,
append a dot followed by the style ID.
For instance, to match a paragraph with the style ID `Heading1`:

```
p.Heading1
```

#### Bold

Match explicitly bold text:

```
b
```

Note that this matches text that has had bold explicitly applied to it.
It will not match any text that is bold because of its paragraph or run style.

#### Italic

Match explicitly italic text:

```
i
```

Note that this matches text that has had italic explicitly applied to it.
It will not match any text that is italic because of its paragraph or run style.

#### Underline

Match explicitly underlined text:

```
u
```

Note that this matches text that has had underline explicitly applied to it.
It will not match any text that is underlined because of its paragraph or run style.

#### Strikethough

Match explicitly struckthrough text:

```
strike
```

Note that this matches text that has had strikethrough explicitly applied to it.
It will not match any text that is struckthrough because of its paragraph or run style.

#### All caps

Match explicitly all caps text:

```
all-caps
```

Note that this matches text that has had all caps explicitly applied to it.
It will not match any text that is all caps because of its paragraph or run style.

#### Small caps

Match explicitly small caps text:

```
small-caps
```

Note that this matches text that has had small caps explicitly applied to it.
It will not match any text that is small caps because of its paragraph or run style.

#### Highlight

Match explicitly highlighted text:

```
highlight
```

Note that this matches text that has had a highlight explicitly applied to it.
It will not match any text that is highlighted because of its paragraph or run style.

It's also possible to match specific colours.
For instance, to match yellow highlights:

```
highlight[color='yellow']
```

The set of colours typically used are:

* `black`
* `blue`
* `cyan`
* `green`
* `magenta`
* `red`
* `yellow`
* `white`
* `darkBlue`
* `darkCyan`
* `darkGreen`
* `darkMagenta`
* `darkRed`
* `darkYellow`
* `darkGray`
* `lightGray`

#### Ignoring document elements

Use `!` to ignore a document element.
For instance, to ignore any paragraph with the style `Comment`:

```
p[style-name='Comment'] => !
```

### HTML paths

#### Single elements

The simplest HTML path is to specify a single element.
For instance, to specify an `h1` element:

```
h1
```

To give an element a CSS class,
append a dot followed by the name of the class:

```
h1.section-title
```

To add an attribute, use square brackets similarly to a CSS attribute selector:

```
p[lang='fr']
```

To require that an element is fresh, use `:fresh`:

```
h1:fresh
```

Modifiers must be used in the correct order:

```
h1.section-title:fresh
```

#### Text Alignment

To convert text alignment, it is possible to match paragraphs with text-align attribute.

```
p[text-align='center'] => p.center:fresh
p[text-align='right'] => p.right:fresh
p[text-align='justify'] => p.justify:fresh
p[style-name='Heading 1', text-align='center'] => h1.center:fresh
p[style-name='Heading 1', text-align='right'] => h1.right:fresh
```

Note: Order is important. Last selector wins!

#### Separators

To specify a separator to place between the contents of paragraphs that are collapsed together,
use `:separator('SEPARATOR STRING')`.

For instance, suppose a document contains a block of code where each line of code is a paragraph with the style `Code Block`.
We can write a style mapping to map such paragraphs to `<pre>` elements:

```
p[style-name='Code Block'] => pre
```

Since `pre` isn't marked as `:fresh`,
consecutive `pre` elements will be collapsed together.
However, this results in the code all being on one line.
We can use `:separator` to insert a newline between each line of code:

```
p[style-name='Code Block'] => pre:separator('\n')
```

#### Nested elements

Use `>` to specify nested elements.
For instance, to specify `h2` within `div.aside`:

```
div.aside > h2
```

You can nest elements to any depth.

## Missing features

Compared to the JavaScript and Python implementations, the following features
are currently missing:

* CLI
* Writing embedded style maps
* Markdown support
* Document transforms

## Donations

If you'd like to say thanks, feel free to [make a donation through Ko-fi](https://ko-fi.com/S6S01MG20).

If you use Mammoth as part of your business,
please consider supporting the ongoing maintenance of Mammoth by [making a weekly donation through Liberapay](https://liberapay.com/mwilliamson/donate).
