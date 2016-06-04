# Mammoth .docx to HTML converter for .NET

Mammoth is designed to convert .docx documents,
such as those created by Microsoft Word,
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

## Installation

Available on [NuGet](https://www.nuget.org/packages/Mammoth/).

```
Install-Package Mammoth
```

## Other supported platforms

* [JavaScript](https://github.com/mwilliamson/mammoth.js), both the browser and node.js.
  Available [on npm](https://www.npmjs.com/package/mammoth).

* [Python](https://github.com/mwilliamson/python-mammoth).
  Available [on PyPI](https://pypi.python.org/pypi/mammoth).

* [WordPress](https://wordpress.org/plugins/mammoth-docx-converter/).

* [Java/JVM](https://github.com/mwilliamson/java-mammoth).
  Available [on Maven Central](http://search.maven.org/#search|ga|1|g%3A%22org.zwobble.mammoth%22%20AND%20a%3A%22mammoth%22).

## Usage

### Library

#### Basic conversion

To convert an existing .docx file to HTML,
create an instance of `DocumentConverter` and
pass the path of the file to `ConvertToHtml`.
For instance:

```csharp
using Mammoth;

var converter = new DocumentConverter();
var result = converter.ConvertToHtml("document.docx");
var html = result.Value; // The generated HTML
var warnings = result.Warnings; // Any warnings during conversion
```

You can also extract the raw text of the document by using `ExtractRawText`.
This will ignore all formatting in the document.
Each paragraph is followed by two newlines.

```csharp
var converter = new DocumentConverter();
var result = converter.ExtractRawText("document.docx");
var html = result.Value; // The raw text
var warnings = result.Warnings; // Any warnings during conversion
```

#### Custom style map

By default,
Mammoth maps some common .docx styles to HTML elements.
For instance,
a paragraph with the style name `Heading 1` is converted to a `h1` element.
You can add custom style maps by calling `AddStyleMap(string)`.
A description of the syntax for style maps can be found in the section "Writing style maps".
For instance, if paragraphs with the style name `Section Title` should be converted to `h1` elements,
and paragraphs with the style name `Subsection Title` should be converted to `h2` elements:

```csharp
var converter = new DocumentConverter()
    .AddStyleMap("p[style-name='Section Title'] => h1:fresh")
    .AddStyleMap("p[style-name='Subsection Title'] => h2:fresh");
```

You can also pass in the entire style map as a single string,
which can be useful if style maps are stored in text files:

```csharp
var styleMap =
    "p[style-name='Section Title'] => h1:fresh\n" +
    "p[style-name='Subsection Title'] => h2:fresh";
var converter = new DocumentConverter()
    .AddStyleMap(styleMap);
```

The most recently-added styles have the greatest precedence.
User-defined style mappings are used in preference to the default style mappings.
To stop using the default style mappings altogether,
call `DisableDefaultStyleMap`:

```csharp
var converter = new DocumentConverter()
    .DisableDefaultStyleMap();
```

#### Bold

By default, bold text is wrapped in `<strong>` tags.
This behaviour can be changed by adding a style mapping for `b`.
For instance, to wrap bold text in `<em>` tags:

```csharp
var converter = new DocumentConverter()
    .AddStyleMap("b => em");
```

#### Italic

By default, italic text is wrapped in `<em>` tags.
This behaviour can be changed by adding a style mapping for `i`.
For instance, to wrap italic text in `<strong>` tags:

```csharp
var converter = new DocumentConverter()
    .AddStyleMap("i => strong");
```

#### Underline

By default, the underlining of any text is ignored since underlining can be confused with links in HTML documents.
This behaviour can be changed by adding a style mapping for `u`.
For instance, suppose that a source document uses underlining for emphasis.
The following will wrap any explicitly underlined source text in `<em>` tags:

```csharp
var converter = new DocumentConverter()
    .AddStyleMap("u => em");
```

#### Strikethrough

By default, strikethrough text is wrapped in `<s>` tags.
This behaviour can be changed by adding a style mapping for `strike`.
For instance, to wrap strikethrough text in `<del>` tags:

```csharp
var converter = new DocumentConverter()
    .AddStyleMap("strike => del");
```
### API

#### `DocumentConverter`

Methods:

* `IResult<string> ConvertToHtml(string path)`: converts the file at `path` into an HTML string.

* `IResult<string> ConvertToHtml(Stream stream)`: converts `stream` into an HTML string.
  Note that using this method instead of `convertToHtml(File file)` means that relative paths
  to other files, such as images, cannot be resolved.

* `IResult<string> ExtractRawText(string path)`:
  Extract the raw text of the document.
  This will ignore all formatting in the document.
  Each paragraph is followed by two newlines.

* `IResult<string> ExtractRawText(Stream stream)`:
  Extract the raw text of the document.
  This will ignore all formatting in the document.
  Each paragraph is followed by two newlines.

* `DocumentConverter AddStyleMap(string styleMap)`:
  add a style map to specify the mapping of Word styles to HTML.
  The most recently added style map has the greatest precedence.
  See the section "Writing style maps" for a description of the syntax.

* `DocumentConverter DisableDefaultStyleMap()`: by default,
  any added style maps are combined with the default style map.
  Call this to stop using the default style map altogether.

* `DocumentConverter PreserveEmptyParagraphs()`: by default, empty paragraphs are ignored.
  Call this to preserve empty paragraphs in the output.

* `DocumentConverter IdPrefix(string idPrefix)`:
  a string to prepend to any generated IDs,
  such as those used by bookmarks, footnotes and endnotes.
  Defaults to the empty string.

#### `IResult<T>`

Represents the result of a conversion. Properties:

* `T Value`: the generated text.

* `ISet<string> Warnings`: any warnings generated during the conversion.

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

The two consective `Heading 1` .docx paragraphs will then be converted to two separate `h1` elements.

Reusing elements is useful in generating more complicated HTML structures.
For instance, suppose your .docx contains asides.
Each aside might have a heading and some body text,
which should be contained within a single `div.aside` element.
In this case, style mappings similar to `p[style-name='Aside Heading'] => div.aside > h2:fresh` and
`p[style-name='Aside Text'] => div.aside > p:fresh` might be helpful.

### Document element matchers

#### Paragraphs and runs

Match any paragraph:

```
p
```

Match any run:

```
r
```

To match a paragraph or run with a specific style,
you can reference the style by name.
This is the style name that is displayed in Microsoft Word or LibreOffice.
For instance, to match a paragraph with the style name `Heading 1`:

```
p[style-name='Heading 1']
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

To require that an element is fresh, use `:fresh`:

```
h1:fresh
```

Modifiers must be used in the correct order:

```
h1.section-title:fresh
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

* Custom image handlers
* CLI
* Embedded style map support
* Markdown support
* Document transforms

