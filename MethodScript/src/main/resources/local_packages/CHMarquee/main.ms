console('Starting up the marquees')
foreach(get_value('chmarquee.list'), @marquee,
    _start_marquee(@marquee)
)