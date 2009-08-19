/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene;

/**
 * An application specific data object to be set on a spatial's setData-method.
 * This object holds information about chess pieces placement on the board and
 * the color of the piece.
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class PieceData {
	/**
	 * 0 to 7 corresponds to a to h on a chess board
	 */
	public int col_index = -1;
	/**
	 * 0 to 7 corresponds to 1 to 8 on a chess board
	 */
	public int row_index = -1;
	public boolean isDark;
	
	/**
	 * Creates a new instance that is placed outside the board
	 */
	public PieceData() {
		col_index = row_index = -1;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PieceData))
			return false;
		PieceData other = (PieceData) obj;
		if (col_index != other.col_index)
			return false;
		if (row_index != other.row_index)
			return false;
		return true;
	}

	/**
	 * Creates a new instance with the given indices
	 * @param col_index Column index (0 to 7)
	 * @param row_index Row index (0 to 7)
	 */
	public PieceData(int col_index, int row_index) {
		this.col_index = col_index;
		this.row_index = row_index;
	}
	
	/**
	 * @param col Chess column name ('a' to 'h')
	 * @param row Chess row name (1 to 8)
	 */
	public PieceData(char col, int row) {
		this.col_index = getColIndex(col);
		this.row_index = getRowIndex(row);
	}
	
	/**
	 * @return true if the piece is placed within the limits of the board.
	 */
	public boolean placedInBoard() {
		return (col_index >= 0 && col_index <= 7 && row_index >= 0 && row_index <= 7);
	}
	
	/**
	 * Set whether the piece is dark or light
	 * @param b true if dark, else light
	 */
	public void setDark(boolean b) {
		isDark = b;
	}
	
	/**
	 * @return true if the piece is dark, otherwise it's light
	 */
	public boolean isDark() {
		return isDark;
	}
	
	/**
	 * @param col a chess board column letter (from 'a' to 'h')
	 * @return the column index corresponding to the given input
	 */
	public static int getColIndex(char col) {
		return (col - 'a');
	}
	
	/**
	 * @param row a chess board row number (from 1 to 8)
	 * @return the row index corresponding to the given input
	 */
	public static int getRowIndex(int row) {
		return row-1;
	}
	
	/**
	 * @param col a chess board column letter (from 'a' to 'h')
	 * @return the x coordinate corresponding to the given input
	 */
	public static float getColPos(char col) {
		return (col - 'a') - 3.5f;
	}
	
	/**
	 * @param row a chess board row number (from 1 to 8)
	 * @return the y coordinate corresponding to the given input
	 */
	public static float getRowPos(int row) {
		return row - 4.5f;
	}
}
