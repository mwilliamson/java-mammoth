using System;

namespace Mammoth.Couscous.java.io
{
	internal class IOException : Exception {
		internal IOException() : base() {
		}
		
		internal IOException(string message) : base(message) {
		}
		
		internal string getMessage() {
			return Message;
		}
	}
}

