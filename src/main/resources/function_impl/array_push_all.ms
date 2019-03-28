@destination = @arguments[0];
@input = @arguments[1];

foreach(@item in @input) {
	@destination[] = @item;
}
