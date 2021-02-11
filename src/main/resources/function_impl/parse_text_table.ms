string @data = @arguments[0];
array @options = array_get(@arguments, 1, associative_array());

array @ret = associative_array();
array @columns = array_get(@options, 'columns', array())[];
array @columnWidth = array_get(@options, 'columnWidth', array())[];
int @tabWidth = array_get(@options, 'tabWidth', 4);
boolean @skipEmptyLines = array_get(@options, 'skipEmptyLines', true);

array @lines = reg_split('\n|\r\n|\n\r', @data);

closure @normalizeLine = iclosure(@line, @tabWidth) {
	@output = "";
	for(@i = 0, @i < length(@line), @i++) {
		if(@line[@i] != '\t') {
			@output .= @line[@i];
		} else {
			@output .= string_multiply(" ", @tabWidth - (length(@output) % @tabWidth));
		}
	}
	return(@output);
};

closure @splitLine = iclosure(@line, @columnWidths) {
	array @sections = array();
	@lastX = 0;
	foreach(@width in @columnWidths) {
		try {
			if(@width == math_const('INFINITY') || @lastX + @width > length(@line)) {
				@sections[] = substr(@line, @lastX);
			} else {
				@sections[] = substr(@line, @lastX, @lastX + @width);
			}
		} catch (Exception @ex) {
			// The line ends before the last column, fill with empty cells.
			@sections[] = "";
		}
		@lastX += @width
	}
	return(@sections);
};

if(array_size(@columns) != 0) {
	// Validate input args
	if(array_size(@columnWidth) < array_size(@columns)) {
		@columnWidth[] = math_const('INFINITY');
	}
	if(array_size(@columnWidth) != array_size(@columns)) {
		throw(FormatException, "columnWidth must be the same size as, or one less than the size of the columns array.");
	}
} else {
	if(array_size(@lines) < 2) {
		throw(FormatException, "The input data must have at least 2 lines, the column names, and the header separator.");
	}
	// We need to calculate the columns and columnWidth ourselves.
	// The general approach here is to simply count the columns between
	// the beginning of the line and the start of the first character after
	// a space/tab character. This determines the width, then we go back and
	// use that data to calculate the values that go in @columns based on the first line.
	@columnNames = execute(@lines[0], @tabWidth, @normalizeLine);
	@columnSeparator = execute(@lines[1], @tabWidth, @normalizeLine);
	@inSpaces = false;
	@width = 0;
	for(@i = 0, @i < length(@columnSeparator), @i++) {
		@char = @columnSeparator[@i];
		if(@inSpaces && @char != ' ') {
			// New column here. Finalize this column and reset.
			@columnWidth[] =  @width;
			@width = 0;
			@inSpaces = false;
		} else if(@char == ' ') {
			@inSpaces = true;
		}
		@width++;
	}
	// Push infinity on the end, which is the last column width
	@columnWidth[] = math_const('INFINITY');
	// Now we know the column widths, parse the column names
	@columnNames = array_map(execute(@columnNames, @columnWidth, @splitLine), closure(@item) {return(trim(@item))});

	// remove the first two lines
	array_remove(@lines, 0);
	array_remove(@lines, 0);
}

foreach(@column in @columnNames) {
	@ret[@column] = array();
}

foreach(@line in @lines) {
	if(@skipEmptyLines) {
		if(trim(@line) == "") {
			continue();
		}
	}
	@columns = execute(execute(@line, @tabWidth, @normalizeLine), @columnWidth, @splitLine);
	for(@i = 0, @i < array_size(@columns), @i++) {
		@ret[@columnNames[@i]][] = trim(@columns[@i]);
	}
}

return(@ret);

// DONE??