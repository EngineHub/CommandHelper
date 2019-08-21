number @a = double(@arguments[0]);
number @b = double(@arguments[1]);

if(@a <= 0 || @b <= 0) {
    throw(RangeException, 'The arguments passed to hypot may not be negative or zero.');
}

return(
    sqrt(@a ** 2 + @b ** 2)
);