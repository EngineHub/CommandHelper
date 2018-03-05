package com.laytonsmith.PureUtilities.Common;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class for building templates using a standard template format. The format is limited, but very simple to
 * use. Templates start with two percent signs, and end with two percent signs. Arguments may also be passed in. First,
 * the template name is given, followed by the pipe character, with the first argument, followed by pipe characters
 * separating each argument, finally closing out with two percent signs.
 *
 * This is a simple, no argument template: %%template%% This is a one argument template: %%template|arg1%% This is a two
 * argument template: %%template|arg1|arg2%%
 *
 * On the implementation side, the template name and generator are provided. Once the template is encountered in the
 * text, the generator is triggered, and the text to replace the template is returned.
 */
public class TemplateBuilder {

	private final Map<String, Generator> templates;
	private static final Pattern PATTERN = Pattern.compile("%%([^\\|%]+)([^%]*?)%%");
	private boolean silentFail = true;

	/**
	 * Creates a new TemplateBuilder with no templates in it.
	 */
	public TemplateBuilder() {
		templates = new HashMap<>();
	}

	/**
	 * Creates a new TemplateBuilder with the given templates in it.
	 *
	 * @param templates
	 */
	public TemplateBuilder(Map<String, Generator> templates) {
		this.templates = new HashMap<>(templates);
	}

	/**
	 * Adds a new template to this builder.
	 *
	 * @param name
	 * @param replacement
	 */
	public void addTemplate(String name, Generator replacement) {
		templates.put(name, replacement);
	}

	/**
	 * Removes the specified template from this builder.
	 *
	 * @param name
	 */
	public void removeTemplate(String name) {
		templates.remove(name);
	}

	/**
	 * Sets the silent fail flag. This flag determines whether or not the build method will throw an exception if a
	 * template is asked for which does not exist. If the state is false, (the default) then the template engine will
	 * silently fail, and replace the template tag with an empty string.
	 *
	 * @param silentFail
	 */
	public void setSilentFail(boolean silentFail) {
		this.silentFail = silentFail;
	}

	/**
	 * Given the input with possible template tags, parses and replaces any templates, returning the rendered string.
	 *
	 * @param template
	 * @throws IllegalArgumentException If the silent fail flag is false, and the input text contains a template which
	 * does not exist.
	 * @return
	 */
	public String build(String template) throws IllegalArgumentException {
		Matcher m = PATTERN.matcher(template);
		StringBuilder templateBuilder = new StringBuilder();
		int lastMatch = 0;
		boolean appended = false;
		while(m.find()) {
			if(!appended) {
				templateBuilder.append(template.substring(lastMatch, m.start()));
				appended = true;
			}
			String name = m.group(1);
//			for(String templateName : templates.keySet()){
//				if(templateName.equals(name)){
//					templateBuilder.append(templates.get(name));
//					lastMatch = m.end();
//					appended = false;
//					//template = template.replaceAll("%%" + Pattern.quote(name) + ".*?%%", customTemplates.get(name));
//				}
//			}
			String[] tmplArgs = ArrayUtils.EMPTY_STRING_ARRAY;
			if(m.group(2) != null && !m.group(2).isEmpty()) {
				//We have arguments
				//remove the initial |, then split
				tmplArgs = m.group(2).substring(1).split("\\|");
			}
			if(templates.containsKey(name)) {
				String templateValue = templates.get(name).generate(tmplArgs);
				templateBuilder.append(templateValue);
			} else {
				if(!silentFail) {
					throw new IllegalArgumentException("Template with name \"" + name + "\" was found in the input"
							+ " text, but no such template exists.");
				}
			}
			lastMatch = m.end();
			appended = false;
		}
		if(!appended) {
			templateBuilder.append(template.substring(lastMatch));
		}
		return templateBuilder.toString();
	}

	public static interface Generator {

		String generate(String... args);
	}

}
