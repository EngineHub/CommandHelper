double @value = double(@arguments[0]);
double @min = double(@arguments[1]);
double @max = double(@arguments[2]);
double @v = null;
if(@min == @max) {
	@v = @min;
} else if(@min < @max) {
	// Normal mode
	if(@value < @min) {
		@v = @min;
	} else if(@value > @max) {
		@v = @max;
	} else {
		@v = @value;
	}
} else if(@value < @max) {
	// Reverse mode, actually min
	@v = @value;
} else if(@value > @min) {
	// Reverse mode, actually max
	@v = @value;
} else if(@value - @max <= @min - @value) {
	// Special handling, find the closer value
	@v = @max;
} else {
	@v = @min;
}
return(@v);