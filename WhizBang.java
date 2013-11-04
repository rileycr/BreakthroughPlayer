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
	public static final int MAX_DEPTH = 6; // TODO
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
		initalizeStack();
		alphaBeta((BreakthroughState) state, 0, Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY);
		return moveStack[0];

	}

	/**
	 * Initailizes the move stack to the size of the max depth
	 */
	public void initalizeStack() {
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
		boolean isTerminal = terminalValue(board, moveStack, currentDepth);

		if (isTerminal) {
			;
		} else if (currentDepth == MAX_DEPTH - 1) {
			moveStack[currentDepth].score = evaluate(board);
		} else {
			double bestScore = (toMaximize ? Double.NEGATIVE_INFINITY
					: Double.POSITIVE_INFINITY);

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
		// TODO a preliminary implementation just to do some testing.
		return positionalEval(board) + forwardTriangleEval(board)
				+ detectBlock(board);
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
	public double forwardTriangleEval(BreakthroughState board) {
		double score = 0;
		for (int row = 0; row < BreakthroughState.N; row++) {
			for (int col = 0; col < BreakthroughState.N; col++) {
				if (board.board[row][col] != BreakthroughState.emptySym) {
					score += forwardTriAtPiece(board, row, col);
				}
			}
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

		if (board.board[row][col] == BreakthroughState.homeSym) {
			for (int i = row + 1; i < BreakthroughState.N; i++) {
				for (int j = row - i; j < i - row; j++) {
					if (col + j >= 0
							&& col + j < BreakthroughState.N
							&& board.board[i][col + j] == BreakthroughState.awaySym) {
						count--;
					}
				}
			}
		} else {
			for (int i = row - 1; i >= 0; i--) {
				for (int j = i - row; j < row - i; j++) {
					if (col + j >= 0
							&& col + j < BreakthroughState.N
							&& board.board[i][col + j] == BreakthroughState.homeSym) {
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
	public double positionalEval(BreakthroughState board) {
		double boardScore = 0;
		int pieceCount = 0;
		for (int row = 0; row < BreakthroughState.N; row++) {
			for (int col = 0; col < BreakthroughState.N; col++) {
				if (board.board[row][col] == BreakthroughState.homeSym) {
					pieceCount += 1;
					boardScore += homeValues[row][col];
				} else if (board.board[row][col] == BreakthroughState.awaySym) {
					pieceCount -= 1;
					boardScore -= awayValues[row][col];
				}
			}
			// If we have an uneven number of pieces on our board, update score
			boardScore += pieceCount * 1.5;
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
	public double detectBlock(BreakthroughState board) {
		double score = 0;
		for (int i = 0; i < BreakthroughState.N; i++) {
			for (int j = 0; j < BreakthroughState.N; j++) {
				if (board.board[i][j] == BreakthroughState.homeSym
						&& i + 2 <= BreakthroughState.N - 1) {
					if (board.board[i + 1][j] == BreakthroughState.awaySym
							&& board.board[i + 2][j] == BreakthroughState.awaySym) {
						score--;
					}
				} else if (board.board[i][j] == BreakthroughState.awaySym
						&& i - 2 >= 0) {
					if (board.board[i - 1][j] == BreakthroughState.homeSym
							&& board.board[i - 2][j] == BreakthroughState.homeSym) {
						score++;
					}
				}
			}
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
			ScoredBreakthroughMove[] mvStack, int depth) {
		GameState.Status status = board.getStatus();
		boolean isTerminal = true;

		if (status == GameState.Status.HOME_WIN) {
			mvStack[depth].score = MAX_SCORE;
		} else if (status == GameState.Status.AWAY_WIN) {
			mvStack[depth].score = -MAX_SCORE;
		} else if (status == GameState.Status.DRAW) {
			mvStack[depth].score = 0;
		} else {
			isTerminal = false;
		}

		return isTerminal;
	}

	public static void main(String[] args) {
		GamePlayer player = new WhizBang("The WhizBanger", false);

		 player.compete(args);

		// Used for testing
		//BreakthroughState st = new BreakthroughState();
		//player.init();
		//GameMove mv = player.getMove(st, "");
		//System.out.println(mv);

	}

	/**
	 * Extends BreakthroughMove to allow for scoring of moves
	 * 
	 * @author Cooper
	 * 
	 */
	class ScoredBreakthroughMove extends BreakthroughMove {
		public double score;

		public ScoredBreakthroughMove(int r1, int c1, int r2, int c2,
				double score) {
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
}
