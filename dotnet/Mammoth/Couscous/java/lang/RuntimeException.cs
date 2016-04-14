using System;

namespace Mammoth.Couscous.java.lang
{
	internal class RuntimeException : Exception {
		internal RuntimeException() : base() {
		}
		
		internal RuntimeException(string message) : base(message) {
		}
	}
}

