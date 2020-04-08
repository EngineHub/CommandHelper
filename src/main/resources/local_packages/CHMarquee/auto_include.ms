proc(_init_marquee, @name,
    if(!has_value('chmarquee.list')){
        store_value('chmarquee.list', array())
    }
    assign(@list, get_value('chmarquee.list'))
    if(!array_contains(@list, @name)){
        array_push(@list, @name)
        store_value('chmarquee.list', @list)
    }
)

proc(_check_marquee, @name,
    assign(@value, get_value('chmarquee.list'))
    if(!is_array(@value) || !array_contains(@value, @name)){
        return(false)
    } else {
        return(true)
    }
)

proc(_start_marquee, @name,
    #Stop the marquee if it's already started
    marquee_stop(@name)
    msg('Starting \''.@name.'\' marquee.')
    assign(@text, get_value('chmarquee.' . @name . '.text'))
    assign(@signs, get_values('chmarquee.' . @name . '.signloc'))
    assign(@signWidth, 15)
    assign(@count, 0)
    assign(@locs, array())
    while(true,
        if(has_value('chmarquee.' . @name . '.signloc.' . (@count + 1))){
            # Grab the sign locations, so we don't incur an io hit each time we want to rotate the text
            array_push(@locs, get_value('chmarquee.' . @name . '.signloc.' . (@count + 1)))
            @count++
        } else {
            break()
        }
    )
    
    if(@text == null){
        assign(@text, 'Use /marquee set' @name '<text> to set the text in this marquee')
    }
    assign(@text, @text . '    ')
    marquee(@name, @text, @count * @signWidth, 100, closure(@text,
        try(
            foreach(range(0, @count), @sign_index,            
                assign(@start, @sign_index * @signWidth)
                assign(@end, ((@sign_index + 1) * @signWidth) - 1)
                assign(@myText, @text[cslice(@start, @end)])
                #console('@myText' @myText)
                #If this is not a sign here right now, we're gonna make it one now
                set_sign_text(@locs[@sign_index], '               ', @myText, '===============')
            )
        , @ex, #catch
            # If something breaks, we just want to stop the queue, instead of spamming console with messages
            console('Something went wrong! Now stopping the \''.@name.'\' marquee.' @ex)
            _stop_marquee(@name)
        )
    ))
)

proc(_stop_marquee, @name,
    marquee_stop(@name)
)

proc(_remove_marquee, @name,
    _stop_marquee(@name)
    assign(@list, get_value('chmarquee.list'))
    array_remove_values(@list, @name)
    store_value('chmarquee.list', @list)
    foreach(array_keys(get_values('chmarquee.' . @name)), @key,
        clear_value(@key)
    )
)