using System;
using Mammoth;

namespace Mammoth.Cli {
	internal class Program {
		public static void Main(string[] args) {
			var result = new DocumentConverter().ConvertToHtml(args[0]);
			Console.WriteLine(result.Value);
		}
	}
}
