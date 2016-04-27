namespace Mammoth.Couscous.java.math {
    internal class BigInteger {
        internal static readonly BigInteger _ONE = new BigInteger(1);
        
        // TOOD: use proper BigInteger
        private readonly long _value;
        
        internal BigInteger(string value) {
            _value = long.Parse(value);
        }
        
        internal BigInteger(long value) {
            _value = value;
        }
        
        internal BigInteger subtract(BigInteger other) {
            return new BigInteger(_value - other._value);
        }
        
        internal string toString() {
            return _value.ToString();
        }
    }
}
