/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Common.Annotations.ForceImplementation;

/**
 * Generally, implementations should extend this class, rather than {@link CommandLineTool}, so changes can be more
 * easily implemented without breaking external tools.
 */
public abstract class AbstractCommandLineTool implements CommandLineTool {

	@ForceImplementation
	public AbstractCommandLineTool() {
	}

}
