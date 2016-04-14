using System;

namespace Mammoth.Couscous.org.zwobble.mammoth.@internal.util {
	internal static class Strings {
		internal static string trimLeft(string value, int character) {
			int index = 0;
			while (index < value.Length && value[index] == character) {
				index++;
			}
			return value.Substring(index);
		}
		
		internal static bool isNullOrEmpty(string value) {
			return string.IsNullOrEmpty(value);
		}
	}
}

