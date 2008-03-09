/*******************************************************************************
 * Copyright (c) 2006 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.sudoku.factory.generator;

import org.eclipse.sudoku.core.factories.SudokuBoardFactory;
import org.eclipse.sudoku.core.models.SudokuBoard;

public class SudokuBoardGeneratorFactory implements SudokuBoardFactory {
	
	private SudokuBoardGenerator generator;
	public void setGenerator(SudokuBoardGenerator generator) {
		this.generator = generator;
	}
	public SudokuBoardGenerator getGenerator() {
		return generator;
	}
	public SudokuBoard createNewBoard() {
		return getGenerator().generate();
	}
	private String name;
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}

}
