
/**
 * Formats raw milliseconds into a human readable format, and returns a string with the highest
 * two denominations.
 */
proc(_human_readable_time, @ms,
	#Trivial case
	if(@ms < 1000, return(@ms . ' ms'))
	
	@seconds = (floor(@ms / 1000) % 60)
    @minutes = (floor((@ms / 1000) / 60) % 60)
    @hours = (floor(((@ms / 1000) / 60) / 60) % 24)
	@ms = (@ms % 1000) / 1000 #Put the ms into seconds

	if(@seconds < 10){
		@sec = sprintf('%.3f', @seconds + @ms)
	} else {
		@sec = @seconds
	}
	
	#Grab the top two denominations
	if(@minutes == 0){
		return(@sec.' sec')
	}
	@seconds = round(@seconds)
	if(@seconds < 10, @seconds = '0'.@seconds)
	
	if(@hours == 0){
		return(@minutes.'.'.@seconds.' minutes')
	}
	
	if(@minutes < 10, @minutes = '0'.@minutes)
	return (@hours.':'.@minutes.'.'.@seconds.' hours')
)

/**
 * Formats raw bytes into a human readable format, using the
 * highest unit possible.
 * @param @bytes The number of bytes
 * @param @useSI Should this use SI units?
 */
proc(_human_readable_bytes, @bytes, @useSI = true,
	@unit = if(@useSI, 1000, 1024)
	if(@bytes < @unit){
		return(@bytes . ' B')
	}
	@exp = integer(logarithm(@bytes, @unit))
	@pre = if(@useSI, 'kMGTPE', 'KMGTPE')[@exp - 1] . if(!@useSI, 'i')
	return(sprintf('%.1f %sB', @bytes / pow(@unit, @exp), @pre))
)

/**
 * Formats a 2d table of data into a nice textual table output.
 * @param @data A 2d array of data to put in the table.
 */
proc(_format_table, @data,
	@data = @data[] #Make a copy, so we don't break the original
	proc(_string_multiply, @string, @count,
		@ret = ''
		foreach(range(@count), @i,
			@ret = @ret.@string
		)
		return(@ret)
	)
	# First calculate the column widths, which is based on the widest element
	@columnWidths = array()
	foreach(@data, @row,
		@row = array_normalize(@row) #Protect against associative arrays
		array_resize(@columnWidths, array_size(@row), 0)
		foreach(array_keys(@row), @columnIndex,
			@columnWidths[@columnIndex] = max(@columnWidths[@columnIndex], length(@row[@columnIndex]) + 2) #Pad with 2 spaces
		)
	)

	# Ok, we have the column widths now, so write out the data.
	@string = ''
	@divider = ''
	foreach(@columnWidths, @width,
		@divider = @divider . '+' . _string_multiply('-', @width)
	)
	@divider = @divider . '+\n'
	foreach(@data, @row,
		array_resize(@row, array_size(@columnWidths), '')
		@string = @string . @divider
		@row = array_normalize(@row)
		foreach(array_keys(@row), @column,
			@cell = @row[@column]
			@width = length(@cell)
			@paddingLeft = _string_multiply(' ', ceil((@columnWidths[@column] - @width) / 2))
			@paddingRight = _string_multiply(' ', floor((@columnWidths[@column] - @width) / 2))
			@string = @string . '|' . @paddingLeft . @cell . @paddingRight	
		)
		@string = @string . '|\n'
	)
	return('\n'.@string.@divider.'\n')
)
