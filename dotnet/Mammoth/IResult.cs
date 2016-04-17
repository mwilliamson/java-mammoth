using System.Collections.Generic;

namespace Mammoth {
    public interface IResult<T> {
        T Value { get; }
        ISet<string> Warnings { get; }
    }
}
