package org.eclipse.sudoku.core.internal;

import org.eclipse.sudoku.core.exceptions.CannotCreateSudokuBoardException;
import org.eclipse.sudoku.core.exceptions.SudokuBoardFactoryUnavailableException;
import org.eclipse.sudoku.core.factories.SudokuBoardFactory;
import org.eclipse.sudoku.core.models.SudokuBoard;

public class Factory {

	private final SudokuBoardFactory factory;

	public Factory(SudokuBoardFactory factory) {
		this.factory = factory;
	}

	public String getName() {
		return factory.getName();
	}

	public SudokuBoard createNewBoard() throws SudokuBoardFactoryUnavailableException, CannotCreateSudokuBoardException {
		return getFactory().createNewBoard();
	}
	
	protected SudokuBoardFactory getFactory() throws SudokuBoardFactoryUnavailableException {
		return factory;
	}
}
