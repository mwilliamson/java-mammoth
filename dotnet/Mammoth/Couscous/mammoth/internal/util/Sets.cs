using Mammoth.Couscous.java.lang;
using Mammoth.Couscous.java.util;
using Mammoth.Couscous.java.nio.file;
using System;

namespace Mammoth.Couscous.org.zwobble.mammoth.@internal.util
{
	internal static class Sets {
		internal static Set<T> set<T>(T[] values) {
			return new HashSet<T>(new System.Collections.Generic.HashSet<T>(values));
		}
		
		internal static Set<T> toSet<T>(Iterable<T> iterable) {
			return new HashSet<T>(new System.Collections.Generic.HashSet<T>(FromJava.IterableToEnumerable(iterable)));
		}
	}
}

