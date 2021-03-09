@prototype = @arguments;

if(array_size(@prototype) == 0) {
	return(closure() { return(array()); });
}

// Fail fast, validate the basic stuff about each of the prototypes
foreach(@p in @prototype) {
	if(@p notinstanceof string && @p notinstanceof ClassType && !instanceof(@p, closure) && @p notinstanceof array) {
		throw(CastException, 'Unexpected argument type provided to get_tabcomplete_prototype.');
	}
}

return(closure(@alias, @sender, @args) {
	if(array_size(@args) > array_size(@prototype)) {
		return(false);
	}
	@p = @prototype[array_size(@args) - 1];
	// There are 5 types, string, enum ClassType, array, closure, and associative array.
	if(@p instanceof string) {
		// These can just be written as transforms
		switch_ic(@p) {
			case 'Player':
				@p = all_players();
			case 'OfflinePlayer':
				@p = get_offline_players();
			case 'Boolean':
				@p = array('true', 'false');
			case 'None':
				@p = array();
		}
	}

	if(is_array(@p) && is_associative(@p)) {
		// As above, these can just be rewritten as transforms, however, we have to first
		// figure out which key to use, based on previous arguments.
		@transform = null;
		foreach(@keys in array_keys(@p)) {
			if(string_contains(@keys, '|')) {
				@keys = split(@keys, '|');
			} else {
				@keys = array(@keys);
			}
			foreach(@key in @keys) {
				if(@key == "<" && @transform == null) {
					@transform = @p[@key];
				} else {
					// Count the <
					@prev = 0;
					@pKey = null;
					for(@i = 0, @i < length(@key), @i++) {
						@c = @key[@i];
						if(@c == '<') {
							@prev++
						} else {
							@pKey = @key[cslice(@i, -1)];
							break();
						}
					}
					if(@prev > length(@args)) {
						throw(FormatException, "Associative array passed to get_tabcomplete_prototype looks back beyond the start of the array.")
					}
					// Check if the args[current - prev] matches @key
					if(@args[length(@args) - 1 - @prev] == @pKey) {
						@transform = @p[@key];
					}
				}
			}
		}
		@p = @transform;
	}

	@set = null;

	if(@p instanceof ClassType) {
		@set = array_map(reflect_pull('enum', string(@p)), closure(@v) { return(string(@v)); });
	} else if(instanceof(@p, closure)) {
		@set = execute(@alias, @sender, @args, @p);
	} else if(!is_associative(@p)) {
		@set = @p;
	}

	// @set now contains the list of possible completions. Match this against @args, and see if there are any values that simply can't match.
	@ret = array();
	@currentArg = @args[-1];
	foreach(@value in @set) {
		@value = string(@value);
		if(string_starts_with(to_lower(@value), to_lower(@currentArg))) {
			@ret[] = @value;
		}
	}
	return(@ret);
});