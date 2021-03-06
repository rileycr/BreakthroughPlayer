package WhizBang;

import java.util.*;
import breakthrough.*;
import game.*;

/**
 * Whizbang is an AI breakthrough player for CSE 486.
 * 
 * @author Cooper Riley, Josh Ruebusch, Lauren Murray
 * 
 */
public class WhizBang extends GamePlayer {
	protected ScoredBreakthroughMove[] moveStack;
	public static final double MAX_SCORE = Double.MAX_VALUE;
	public static int MAX_DEPTH = 11; //Just used to initialize stack
	public static double totalTime;
	public static int currentMoveNum;
	
	// Board values for the home team
	private static final int[][] homeValues = { { 5, 15, 15, 5, 15, 15, 5 },
			{ 2, 3, 3, 3, 3, 3, 2 }, { 4, 5, 5, 5, 5, 5, 4 },
			{ 8, 10, 10, 10, 10, 10, 8 }, { 12, 15, 15, 15, 15, 15, 12 },
			{ 20, 25, 25, 25, 25, 25, 20 }, { 45, 45, 45, 45, 45, 45, 45 } };
	// Board values for the away team
	private static final int[][] awayValues = { { 45, 45, 45, 45, 45, 45, 45 },
			{ 20, 25, 25, 25, 25, 25, 20 }, { 12, 15, 15, 15, 15, 15, 12 },
			{ 8, 10, 10, 10, 10, 10, 8 }, { 4, 5, 5, 5, 5, 5, 4 },
			{ 2, 3, 3, 3, 3, 3, 2 }, { 5, 15, 15, 5, 15, 15, 5 } };

	public WhizBang(String nickname, boolean isDeterministic) {
		super(nickname, new BreakthroughState(), isDeterministic);
	}

	@Override
	public GameMove getMove(GameState state, String lastMv) {
		
		currentMoveNum = state.getNumMoves();
		initializeStack();
		alphaBeta((BreakthroughState) state, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		return moveStack[0];
	}
	
	/**
	 * Changes the depth of the search depending on how much time remains
	 * 
	 * @param board
	 * @param currentDepth
	 * @return
	 */
	public boolean takesTooLong(BreakthroughState board, int currentDepth) {
		if (currentMoveNum < 15) {
			return (currentDepth == 5);
		} else if ((360 - totalTime) > 120) {
			return currentDepth == 8;
		} else if ((360 - totalTime) > 5) {
			return currentDepth == 7;
		} else {
			return (currentDepth == 6);
		}
	}

	/**
	 * Overrides GamePlayers method to calculate time elapsed
	 */
	public void timeOfLastMove(double secs){
		totalTime += secs;
	}

	/**
	 * Initializes the move stack to the size of the max depth
	 */
	public void initializeStack() {
		moveStack = new ScoredBreakthroughMove[MAX_DEPTH];
		for (int i = 0; i < MAX_DEPTH; i++) {
			moveStack[i] = new ScoredBreakthroughMove();
		}
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
		
		boolean toMaximize = (board.getWho() == GameState.Who.HOME);
		boolean isTerminal = terminalValue(board, moveStack[currentDepth]);
		
		if (isTerminal) {
			return;
		} else if (takesTooLong(board, currentDepth)) {
			moveStack[currentDepth].score = evaluate(board);
		} else {
			double bestScore = (toMaximize ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);

			char whoseTurn;
			int dRow; // The change in row for a turn
			if (toMaximize) {
				whoseTurn = BreakthroughState.homeSym;
				dRow = 1;
			} else {
				whoseTurn = BreakthroughState.awaySym;
				dRow = -1;
			}

			ScoredBreakthroughMove tempMove = new ScoredBreakthroughMove();
			ScoredBreakthroughMove bestMove = moveStack[currentDepth];
			ScoredBreakthroughMove nextMove = moveStack[currentDepth + 1];
			bestMove.score = bestScore;

			ArrayList<ScoredBreakthroughMove> possibleMoves = new ArrayList<ScoredBreakthroughMove>();

			for (int i = 0; i < BreakthroughState.N; i++) {
				for (int j = 0; j < BreakthroughState.N; j++) {
					if (board.board[i][j] == whoseTurn) {
						tempMove.startRow = i;
						tempMove.startCol = j;
						tempMove.endingRow = i + dRow;

						for (int k = -1; k < 2; k++) {
							tempMove.endingCol = j + k;

							if (board.moveOK(tempMove)) {
								possibleMoves.add((ScoredBreakthroughMove) tempMove.clone());

							}
						}
					}
				}
			}
			Collections.shuffle(possibleMoves);

			doAB(board, possibleMoves, currentDepth, toMaximize, isTerminal,
					bestMove, nextMove, moveStack, alpha, beta);
		}
	}

	/**
	 * Does the Alpha Beta Search
	 * @param board
	 * @param possibleMoves
	 * @param currentDepth
	 * @param toMaximize
	 * @param isTerminal
	 * @param bestMove
	 * @param nextMove
	 * @param moveStack
	 * @param alpha
	 * @param beta
	 */
	public void doAB(BreakthroughState board,
			ArrayList<ScoredBreakthroughMove> possibleMoves, int currentDepth,
			boolean toMaximize, boolean isTerminal, ScoredBreakthroughMove bestMove, ScoredBreakthroughMove nextMove,
			ScoredBreakthroughMove[] moveStack, double alpha, double beta) {
		for (ScoredBreakthroughMove move : possibleMoves) {

			BreakthroughState tempBoard = (BreakthroughState) board.clone();
			tempBoard.makeMove(move);
			alphaBeta(tempBoard, currentDepth + 1, alpha, beta);

			// Updates the best score
			if (toMaximize && nextMove.score > bestMove.score) {
				bestMove.set(move);
				bestMove.score = nextMove.score;
			} else if (!toMaximize && nextMove.score < bestMove.score) {
				bestMove.set(move);
				bestMove.score = nextMove.score;
			}

			// Performing pruning
			if (toMaximize) {
				alpha = Math.max(bestMove.score, alpha);
				if (bestMove.score >= beta || bestMove.score == MAX_SCORE) {
					return;
				}
			} else {
				beta = Math.min(bestMove.score, beta);
				if (bestMove.score <= alpha || bestMove.score == -MAX_SCORE) {
					return;
				}
			}
		}
	}

	/**
	 * Evaluation function used by a alpha-beta search method. it calls all
	 * scoring functions to calculate the score.
	 * 
	 * @param board
	 *            the board to evaluate
	 * @return the score of the board (+ home winning, - away winning)
	 */
	public double evaluate(BreakthroughState board) {
		double score = 0;

		for (int row = 0; row < BreakthroughState.N; row++) {
			for (int col = 0; col < BreakthroughState.N; col++) {

				score += forwardTriangleEval(board, row, col)
						+ positionalEval(board, row, col)
						+ detectBlock(board, row, col)
						+ piecesInARow(board, row, col);
			}
		}
		return score;
	}

	/**
	 * Determines a score based on the number of opposing pieces in front and
	 * diagonal to the player's pieces. Will be run once.
	 * 
	 * Calculates the difference of home and away's scores internally.
	 * 
	 * @param board
	 *            the board to be evaluated
	 * @return the combined score of home and away
	 */
	public double forwardTriangleEval(BreakthroughState board, int row, int col) {
		double score = 0;
		if (board.board[row][col] != BreakthroughState.emptySym) {
			score += forwardTriAtPiece(board, row, col);
		}
		return score;
	}

	/**
	 * Used by forwardTriangleEval function, counts the opposing pieces in front
	 * of the piece.
	 * 
	 * @param board
	 *            to be examined
	 * @param row
	 *            of piece we are looking at
	 * @param col
	 *            of piece we are looking at
	 * @return number of opposing pieces in front of that piece
	 */
	public double forwardTriAtPiece(BreakthroughState board, int row, int col) {
		double count = 0;

		if (board.board[row][col] == BreakthroughState.homeSym
				&& row >= (BreakthroughState.N / 2)) {
			for (int i = row + 1; i < BreakthroughState.N; i++) {
				for (int j = row - i; j < i - row; j++) {
					if (col + j >= 0 && col + j < BreakthroughState.N && board.board[i][col + j] == BreakthroughState.awaySym) {
						count--;
					}
				}
			}
		} else if (row <= (BreakthroughState.N / 2)) {
			for (int i = row - 1; i >= 0; i--) {
				for (int j = i - row; j < row - i; j++) {
					if (col + j >= 0 && col + j < BreakthroughState.N && board.board[i][col + j] == BreakthroughState.homeSym) {
						count++;
					}
				}
			}
		}
		return count;
	}

	/**
	 * Part of the evaluation function used by the alpha-beta search method. It
	 * is called by evaluate(), and it returns the value of the board based on
	 * the position of the pieces.
	 * 
	 * @param board
	 *            the board to evaluate
	 * @return - value of the board
	 */
	public double positionalEval(BreakthroughState board, int row, int col) {
		double boardScore = 0;

		if (board.board[row][col] == BreakthroughState.homeSym) {
			boardScore += homeValues[row][col];
		} else if (board.board[row][col] == BreakthroughState.awaySym) {
			boardScore -= awayValues[row][col];
		}
		
		return boardScore;
	}

	/**
	 * Determines the score based on pieces that are blocked. Runs only once for
	 * the entire board
	 * 
	 * @param board
	 *            state is examined for scoring
	 * @return score positive if in home favor and negative if away favor
	 */
	public double detectBlock(BreakthroughState board, int row, int col) {
		double score = 0;

		if (board.board[row][col] == BreakthroughState.homeSym
				&& row + 2 <= BreakthroughState.N - 1) {
			if (board.board[row + 1][col] == BreakthroughState.awaySym
					&& board.board[row + 2][col] == BreakthroughState.awaySym) {
				score--;
			}
		} else if (board.board[row][col] == BreakthroughState.awaySym
				&& row - 2 >= 0) {
			if (board.board[row - 1][col] == BreakthroughState.homeSym
					&& board.board[row - 2][col] == BreakthroughState.homeSym) {
				score++;
			}
		}
		return score;
	}

	/**
	 * Counts the number of similar pieces in a row
	 * 
	 * @param board
	 *            to be evaluated
	 * @param row
	 *            of the piece we are checking
	 * @param col
	 *            of the piece we are checking
	 * @return score
	 */
	public double piecesInARow(BreakthroughState board, int row, int col) {
		double score = 0;

		if (col < BreakthroughState.N - 1 && board.board[row][col] == board.board[row][col + 1]) {
			score++;
		}

		return score;
	}

	/**
	 * Determines if the current board state is the end of a game.
	 * 
	 * @param board
	 * @param mvStack
	 *            the moves made
	 * @param depth
	 *            the last move, could be the ending move
	 * @return
	 */
	public boolean terminalValue(BreakthroughState board,
			ScoredBreakthroughMove move) {
		GameState.Status status = board.getStatus();
		boolean isTerminal = true;

		if (status == GameState.Status.HOME_WIN) {
			move.score = MAX_SCORE;
		} else if (status == GameState.Status.AWAY_WIN) {
			move.score = -MAX_SCORE;
		} else if (status == GameState.Status.DRAW) {
			move.score = 0;
		} else {
			isTerminal = false;
		}

		return isTerminal;
	}

	public static void main(String[] args) {
		totalTime = 0;
		
		GamePlayer player = new WhizBang("The WhizBanger", false);
		player.compete(args);

		/*
		// Used for testing
		BreakthroughState st = new BreakthroughState();
		player.init();
		GameMove mv = player.getMove(st, "");

		System.out.println(mv);
		*/
	}
}

/**
 * Extends BreakthroughMove to allow for scoring of moves
 * 
 * @author Cooper
 * 
 */
class ScoredBreakthroughMove extends BreakthroughMove {
	public double score;

	public ScoredBreakthroughMove(int r1, int c1, int r2, int c2, double score) {
		super(r1, c1, r2, c2);
		this.score = score;
	}

	public ScoredBreakthroughMove() {
		super();
		this.score = 0;
	}

	public Object clone() {
		return new ScoredBreakthroughMove(startRow, startCol, endingRow,
				endingCol, score);

	}

	public void set(ScoredBreakthroughMove mv) {
		this.startRow = mv.startRow;
		this.endingRow = mv.endingRow;
		this.startCol = mv.startCol;
		this.endingCol = mv.endingCol;
	}
}
