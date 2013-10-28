package WhizBang;

import breakthrough.*;
import game.*;

/**
 * Whizbang is an AI breakthrough player for CSE 486.
 * 
 * @author Cooper Riley, Josh Ruebusch, Lauren Murray
 * 
 */
public class WhizBang extends GamePlayer {

	public WhizBang(String nickname, boolean isDeterministic) {
		super(nickname, new BreakthroughState(), isDeterministic);
	}

	@Override
	public GameMove getMove(GameState state, String lastMv) {
		BreakthroughState board = (BreakthroughState) state;

		// TODO Scoring and move decision

		return null;
	}

	/**
	 * The method that searches through the possible moves.
	 * 
	 * @param board
	 *            the board being analyzed
	 * @param currentDepth
	 *            the current depth of the search
	 * @param alpha
	 *            best Max value
	 * @param beta
	 *            best Min value
	 */
	public void alphaBeta(BreakthroughState board, int currentDepth,
			double alpha, double beta) {
		// TODO the method for doing the a-b search
	}

	/**
	 * Evaluation function used by a alpha-beta search method. it calls all
	 * scoring functions to calculate the score.
	 * 
	 * @param board
	 *            the board to evaluate
	 * @return the score of the board (+ home winning, - away winning)
	 */
	public int evaluate(BreakthroughState board) {
		// TODO We can figure out this once we write all our scoring methods.
		return 0;
	}

	/**
	 * Determines a score based on the number of opposing pieces in front and
	 * diagonal to the player's pieces. Will be run once for each player.
	 * 
	 * @param board
	 *            the board which is being examined
	 * @param player
	 *            which player are we evaluating for
	 * @return the score for player
	 */
	public int forwardTriangleEval(BreakthroughState board, char player) {
		int score = 0;

		// TODO Adjust score to allow for weighted scoring
		for (int row = 0; row < BreakthroughState.N; row++) {
			for (int col = 0; col < BreakthroughState.N; col++) {
				if (board.board[row][col] == player) {
					score += forwardTriAtPiece(board, row, col);
				}
			}
		}
		return score;
	}

	/**
	 * Used by forwardTriangleEval function, counts the opposing pieces in front
	 * of a specific player piece.
	 * 
	 * @param board
	 *            to be examined
	 * @param row
	 *            of piece we are looking at
	 * @param col
	 *            of piece we are looking at
	 * @return number of opposing pieces in front of that piece
	 */
	public int forwardTriAtPiece(BreakthroughState board, int row, int col) {
		int count = 0;

		if (board.who == GameState.Who.HOME) {
			for (int i = row + 1; i < BreakthroughState.N; i++) {
				for (int j = row - i; j <= i - row; j++) {
					if (col + j >= 0 && col + j < BreakthroughState.N) {
						if (board.board[i][col + j] == BreakthroughState.awaySym) {
							count++;
						}
					}
				}
			}
		} else {
			for (int i = row - 1; i >= 0; i--) {
				for (int j = i - row; j <= row - i; j++) {
					if (col + j >= 0 && col + j < BreakthroughState.N) {
						if (board.board[i][col + j] == BreakthroughState.homeSym)
							count++;
					}
				}
			}
		}
		return count;
	}
	
	/**
	 * Determines the score based on pieces that are blocked. Runs only once for the entire board
	 * @param board
	 * 			state is examined for scoring
	 * @return score
	 * 			positive if in home favor and negative if away favor
	 */
	public int detectBlock(BreakthroughState board){
		int score = 0;
		for(int i = 0; i < BreakthroughState.N; i++){
			for(int j = 0; j < BreakthroughState.N; j++){
				if(board.board[i][j] == BreakthroughState.homeSym && 
						j+2 <= BreakthroughState.N-1){
						if(board.board[i][j+1] == BreakthroughState.awaySym &&
								board.board[i][j+2] == BreakthroughState.awaySym){
							score--;
						}
				}
				else if(board.board[i][j] == BreakthroughState.awaySym &&
						j-2 >= 0){
					if(board.board[i][j-1] == BreakthroughState.homeSym &&
							board.board[i][j-2] == BreakthroughState.homeSym){
						score++;
					}
				}
			}
		}
		return score;
	}

	public static void main(String[] args) {
		GamePlayer player = new WhizBang("The WhizBanger", false);
		player.compete(args);
	}
}
