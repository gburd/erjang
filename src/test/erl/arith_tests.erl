-module(arith_tests).

-export([test/0]).

test() ->
    %% Test arithmetics() on small integers, large integers, bigints, and non-numbers.
    %% (TODO: Add other operators than '+'.)
    Operands = [0, 0.0,
		123, 65536, 123456789, 123456789123456789123456789, 1.0e299,
		-123, -65536, -123456789, -123456789123456789123456789, -1.0e299,
		atom, [], "abc", [a|b]],

    [catch(A+B) || A <- Operands,
		   B <- Operands].
