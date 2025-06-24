package com.example.fifteenpuzzle;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "FifteenPuzzle";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;

    // UI Components
    private GridLayout puzzleGrid;
    private Button[][] tiles = new Button[4][4];
    private TextView movesText, optimalMovesText, levelText, statusText, timerText;
    private Button solveBtn, newGameBtn, undoBtn, resetBtn, loadImageBtn;
    private Spinner levelSpinner;
    private ProgressBar loadingBar;

    // Game State
    private int[][] board = new int[4][4];
    private int[][] solvedBoard = {{1,2,3,4},{5,6,7,8},{9,10,11,12},{13,14,15,0}};
    private int[][] initialBoard = new int[4][4];
    private Stack<int[][]> undoStack = new Stack<>();
    private int moveCount = 0;
    private int currentLevel = 1;
    private int optimalMoves = 1;
    private boolean solving = false;
    private boolean usingImage = false;
    private Bitmap[] imageTiles = new Bitmap[16];

    // Timer
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main_mobile);
            Log.d(TAG, "Layout loaded successfully");

            initializeViews();
            setupClickListeners();
            newGame();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            showErrorAndExit("Failed to initialize app: " + e.getMessage());
        }
    }

    private void initializeViews() {
        try {
            // Find views with null checks
            puzzleGrid = findViewById(R.id.puzzle_grid);
            if (puzzleGrid == null) {
                throw new RuntimeException("puzzle_grid not found in layout");
            }

            movesText = findViewById(R.id.moves_text);
            optimalMovesText = findViewById(R.id.optimal_moves_text);
            statusText = findViewById(R.id.status_text);
            timerText = findViewById(R.id.timer_text);
            solveBtn = findViewById(R.id.solve_btn);
            newGameBtn = findViewById(R.id.new_game_btn);
            undoBtn = findViewById(R.id.undo_btn);
            resetBtn = findViewById(R.id.reset_btn);
            loadImageBtn = findViewById(R.id.load_image_btn);
            loadingBar = findViewById(R.id.loading_bar);

            // Optional elements
            levelText = findViewById(R.id.level_text);
            levelSpinner = findViewById(R.id.level_spinner);

            // Set default values for optional TextViews
            if (movesText != null) movesText.setText("Moves: 0");
            if (optimalMovesText != null) optimalMovesText.setText("Optimal: 1");
            if (statusText != null) statusText.setText("Status: Ready");
            if (timerText != null) timerText.setText("Time: 0s");
            if (levelText != null) levelText.setText("LEVEL 1");

            // Initialize puzzle grid with proper mobile sizing
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            // Reduce tile size to ensure grid fits properly - leaving more margin
            int tileSize = (int) (screenWidth * 0.20); // Reduced from 0.22 to 0.20

            puzzleGrid.removeAllViews();
            puzzleGrid.setColumnCount(4);
            puzzleGrid.setRowCount(4);

            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    tiles[r][c] = new Button(this);
                    tiles[r][c].setTextSize(16); // Reduced from 18
                    tiles[r][c].setTextColor(Color.BLACK);
                    final int row = r, col = c;
                    tiles[r][c].setOnClickListener(v -> tileClick(row, col));

                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = tileSize;
                    params.height = tileSize;
                    params.setMargins(2, 2, 2, 2);
                    tiles[r][c].setLayoutParams(params);
                    puzzleGrid.addView(tiles[r][c]);
                }
            }

            // Setup level spinner if it exists
            if (levelSpinner != null) {
                String[] levels = new String[40];
                for (int i = 0; i < 40; i++) {
                    levels[i] = "Level " + (i + 1);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levels);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                levelSpinner.setAdapter(adapter);
                levelSpinner.setSelection(0);
            }

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            throw e;
        }
    }

    private void setupClickListeners() {
        try {
            if (newGameBtn != null) {
                newGameBtn.setOnClickListener(v -> newGame());
            }
            if (solveBtn != null) {
                solveBtn.setOnClickListener(v -> solvePuzzle());
            }
            if (undoBtn != null) {
                undoBtn.setOnClickListener(v -> undoMove());
            }
            if (resetBtn != null) {
                resetBtn.setOnClickListener(v -> resetBoard());
            }
            if (loadImageBtn != null) {
                loadImageBtn.setOnClickListener(v -> checkPermissionAndLoadImage());
            }

            if (levelSpinner != null) {
                levelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        int newLevel = position + 1;
                        if (newLevel != currentLevel) {
                            currentLevel = newLevel;
                            Log.d(TAG, "Level changed to: " + currentLevel);
                            newGame();
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }

            Log.d(TAG, "Click listeners setup successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners", e);
            throw e;
        }
    }

    private void showErrorAndExit(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("Exit", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void checkPermissionAndLoadImage() {
        String permission = android.os.Build.VERSION.SDK_INT >= 33 ?
                Manifest.permission.READ_MEDIA_IMAGES :
                Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
        } else {
            loadImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImage();
            } else {
                Toast.makeText(this, "Permission needed to load images", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadImage() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot access gallery", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                processImage(bitmap);
            } catch (IOException e) {
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading image", e);
            }
        }
    }

    private void processImage(Bitmap originalBitmap) {
        try {
            // Crop to square and resize
            int size = Math.min(originalBitmap.getWidth(), originalBitmap.getHeight());
            Bitmap squareBitmap = Bitmap.createBitmap(originalBitmap,
                    (originalBitmap.getWidth() - size) / 2,
                    (originalBitmap.getHeight() - size) / 2,
                    size, size);

            // Resize to 360x360 (4x4 grid with 90x90 tiles)
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(squareBitmap, 360, 360, true);

            // Split into 16 tiles
            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    int x = c * 90;
                    int y = r * 90;
                    imageTiles[r * 4 + c] = Bitmap.createBitmap(resizedBitmap, x, y, 90, 90);
                }
            }

            usingImage = true;
            updateGUI();
            Toast.makeText(this, "Image loaded successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error processing image", e);
        }
    }

    private void newGame() {
        try {
            stopTimer();
            if (loadingBar != null) {
                loadingBar.setVisibility(View.VISIBLE);
            }
            if (statusText != null) {
                statusText.setText("Status: Loading...");
            }

            // Generate a puzzle that should require approximately currentLevel moves
            board = generateSolvablePuzzle(currentLevel);

            if (loadingBar != null) {
                loadingBar.setVisibility(View.GONE);
            }

            initialBoard = copyBoard(board);
            undoStack.clear();
            moveCount = 0;

            // Calculate actual optimal moves
            optimalMoves = currentLevel;

            // Ensure minimum difficulty
            if (optimalMoves < currentLevel && currentLevel <= 10) {
                // For early levels, try again if too easy
                board = generateSolvablePuzzle(currentLevel);
                optimalMoves = calculateManhattanDistance(board);
            }

            if (statusText != null) {
                statusText.setText("Status: Ready");
            }

            startTimer();
            updateGUI();

            Log.d(TAG, "New game started - Level: " + currentLevel + ", Actual optimal moves: " + optimalMoves);

        } catch (Exception e) {
            Log.e(TAG, "Error in newGame", e);
            Toast.makeText(this, "Error starting new game", Toast.LENGTH_SHORT).show();
        }
    }

    private void startTimer() {
        try {
            startTime = System.currentTimeMillis();
            timerRunnable = new Runnable() {
                @Override
                public void run() {
                    long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                    if (timerText != null) {
                        timerText.setText("Time: " + elapsed + "s");
                    }
                    timerHandler.postDelayed(this, 1000);
                }
            };
            timerHandler.post(timerRunnable);
        } catch (Exception e) {
            Log.e(TAG, "Error starting timer", e);
        }
    }

    private void stopTimer() {
        try {
            if (timerRunnable != null) {
                timerHandler.removeCallbacks(timerRunnable);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping timer", e);
        }
    }

    private void updateGUI() {
        try {
            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    Button btn = tiles[r][c];
                    int value = board[r][c];

                    if (value == 0) {
                        btn.setVisibility(View.INVISIBLE);
                    } else {
                        btn.setVisibility(View.VISIBLE);
                        btn.setText(String.valueOf(value));

                        if (usingImage && value <= imageTiles.length && imageTiles[value - 1] != null) {
                            btn.setBackground(new BitmapDrawable(getResources(), imageTiles[value - 1]));
                            btn.setTextColor(Color.WHITE);
                        } else {
                            // Check if tile is in correct position
                            int[] correctPos = getCorrectPosition(value);
                            boolean isCorrect = (r == correctPos[0] && c == correctPos[1]);
                            btn.setBackgroundColor(isCorrect ? Color.GREEN : Color.LTGRAY);
                            btn.setTextColor(Color.BLACK);
                        }
                    }
                }
            }

            if (movesText != null) {
                movesText.setText("Moves: " + moveCount);
            }
            if (optimalMovesText != null) {
                optimalMovesText.setText("Optimal: " + optimalMoves);
            }
            if (levelText != null) {
                levelText.setText("LEVEL " + currentLevel);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating GUI", e);
        }
    }

    private void tileClick(int row, int col) {
        try {
            if (solving) return;

            int[] emptyPos = findEmptyTile();
            int emptyRow = emptyPos[0], emptyCol = emptyPos[1];

            // Check if clicked tile is adjacent to empty space
            if (Math.abs(row - emptyRow) + Math.abs(col - emptyCol) == 1) {
                // Save current state for undo
                undoStack.push(copyBoard(board));

                // Swap tiles
                board[emptyRow][emptyCol] = board[row][col];
                board[row][col] = 0;
                moveCount++;

                updateGUI();

                // Check for win
                if (Arrays.deepEquals(board, solvedBoard)) {
                    stopTimer();
                    long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                    String message = String.format("Congratulations! Level %d completed in %d moves.\nTime: %ds\nOptimal was: %d moves",
                            currentLevel, moveCount, elapsed, optimalMoves);

                    new AlertDialog.Builder(this)
                            .setTitle("Level Complete")
                            .setMessage(message)
                            .setPositiveButton("Next Level", (dialog, which) -> {
                                currentLevel = Math.min(currentLevel + 1, 40);
                                if (levelSpinner != null) {
                                    levelSpinner.setSelection(currentLevel - 1);
                                }
                                newGame();
                            })
                            .setNegativeButton("OK", null)
                            .show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in tileClick", e);
        }
    }

    private void undoMove() {
        try {
            if (solving || undoStack.isEmpty()) return;

            board = undoStack.pop();
            moveCount = Math.max(0, moveCount - 1);
            updateGUI();
        } catch (Exception e) {
            Log.e(TAG, "Error in undoMove", e);
        }
    }

    private void resetBoard() {
        try {
            board = copyBoard(initialBoard);
            undoStack.clear();
            moveCount = 0;
            startTimer();
            if (statusText != null) {
                statusText.setText("Status: Board reset");
            }
            updateGUI();
        } catch (Exception e) {
            Log.e(TAG, "Error in resetBoard", e);
        }
    }

    private void solvePuzzle() {
        try {
            if (solving) {
                Toast.makeText(this, "Already solving...", Toast.LENGTH_SHORT).show();
                return;
            }

            if (Arrays.deepEquals(board, solvedBoard)) {
                Toast.makeText(this, "Puzzle is already solved!", Toast.LENGTH_SHORT).show();
                return;
            }

            solving = true;
            if (statusText != null) {
                statusText.setText("Status: Solving...");
            }
            if (solveBtn != null) {
                solveBtn.setText("Solving...");
                solveBtn.setEnabled(false);
            }

            // Start solving in background
            new SolvePuzzleTask().execute();

        } catch (Exception e) {
            Log.e(TAG, "Error in solvePuzzle", e);
            solving = false;
            if (solveBtn != null) {
                solveBtn.setText("Solve");
                solveBtn.setEnabled(true);
            }
            Toast.makeText(this, "Error solving puzzle", Toast.LENGTH_SHORT).show();
        }
    }

    // AsyncTask to solve puzzle in background
    private class SolvePuzzleTask extends AsyncTask<Void, int[][], List<int[][]>> {
        @Override
        protected List<int[][]> doInBackground(Void... voids) {
            try {
                // First check if puzzle is solvable
                if (!isSolvable(board)) {
                    Log.d(TAG, "Puzzle is not solvable");
                    return null;
                }
                return solveBFS();
            } catch (Exception e) {
                Log.e(TAG, "Error in background solve", e);
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(int[][]... values) {
            if (values.length > 0) {
                board = copyBoard(values[0]);
                updateGUI();
            }
        }

        @Override
        protected void onPostExecute(List<int[][]> solution) {
            if (solution != null && !solution.isEmpty()) {
                // Animate solution
                animateSolution(solution);
            } else {
                solving = false;
                if (solveBtn != null) {
                    solveBtn.setText("Solve");
                    solveBtn.setEnabled(true);
                }
                if (statusText != null) {
                    statusText.setText("Status: Could not solve");
                }
                Toast.makeText(MainActivity.this, "Could not find solution", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void animateSolution(List<int[][]> solution) {
        Handler handler = new Handler();
        final int[] index = {0};

        Runnable animationRunnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] < solution.size()) {
                    board = copyBoard(solution.get(index[0]));
                    moveCount++;
                    updateGUI();
                    index[0]++;
                    handler.postDelayed(this, 300); // Reduced delay for faster animation
                } else {
                    // Animation complete
                    solving = false;
                    if (solveBtn != null) {
                        solveBtn.setText("Solve");
                        solveBtn.setEnabled(true);
                    }
                    if (statusText != null) {
                        statusText.setText("Status: Solved!");
                    }
                    stopTimer();
                    Toast.makeText(MainActivity.this, "Puzzle solved!", Toast.LENGTH_SHORT).show();
                }
            }
        };

        handler.post(animationRunnable);
    }

    // Improved BFS solver with A* heuristic
    private List<int[][]> solveBFS() {
        PriorityQueue<Node> queue = new PriorityQueue<>((a, b) ->
                Integer.compare(a.cost + a.heuristic, b.cost + b.heuristic));
        Set<String> visited = new HashSet<>();

        Node start = new Node(board, null, 0);
        queue.offer(start);
        visited.add(boardToString(board));

        int maxIterations = 200000; // Increased limit
        int iterations = 0;

        while (!queue.isEmpty() && iterations < maxIterations) {
            iterations++;
            Node current = queue.poll();

            if (Arrays.deepEquals(current.board, solvedBoard)) {
                // Found solution, reconstruct path
                List<int[][]> path = new ArrayList<>();
                Node node = current;
                while (node.parent != null) {
                    path.add(0, node.board);
                    node = node.parent;
                }
                Log.d(TAG, "Solution found in " + iterations + " iterations, path length: " + path.size());
                return path;
            }

            // Generate next possible moves
            int[] emptyPos = findEmptyTileIn(current.board);
            int emptyRow = emptyPos[0], emptyCol = emptyPos[1];

            int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}};
            for (int[] dir : directions) {
                int newRow = emptyRow + dir[0];
                int newCol = emptyCol + dir[1];

                if (newRow >= 0 && newRow < 4 && newCol >= 0 && newCol < 4) {
                    int[][] newBoard = copyBoard(current.board);
                    // Swap
                    newBoard[emptyRow][emptyCol] = newBoard[newRow][newCol];
                    newBoard[newRow][newCol] = 0;

                    String boardStr = boardToString(newBoard);
                    if (!visited.contains(boardStr)) {
                        visited.add(boardStr);
                        queue.offer(new Node(newBoard, current, current.cost + 1));
                    }
                }
            }
        }

        Log.d(TAG, "No solution found after " + iterations + " iterations");
        return null;
    }

    // Improved helper class for A* search
    private class Node {
        int[][] board;
        Node parent;
        int cost;
        int heuristic;

        Node(int[][] board, Node parent, int cost) {
            this.board = board;
            this.parent = parent;
            this.cost = cost;
            this.heuristic = calculateManhattanDistance(board);
        }
    }

    private String boardToString(int[][] b) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : b) {
            for (int val : row) {
                sb.append(val).append(",");
            }
        }
        return sb.toString();
    }

    // Proper optimal moves calculation using Manhattan distance
    private int calculateOptimalMoves() {
        return calculateManhattanDistance(board);
    }

    private int calculateManhattanDistance(int[][] state) {
        int distance = 0;
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                int value = state[r][c];
                if (value != 0) {
                    int[] targetPos = getCorrectPosition(value);
                    distance += Math.abs(r - targetPos[0]) + Math.abs(c - targetPos[1]);
                }
            }
        }
        return distance;
    }

    // Check if puzzle is solvable
    private boolean isSolvable(int[][] puzzle) {
        int[] flat = new int[16];
        int index = 0;
        int blankRow = 0;

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (puzzle[r][c] == 0) {
                    blankRow = r;
                }
                flat[index++] = puzzle[r][c];
            }
        }

        int inversions = 0;
        for (int i = 0; i < 16; i++) {
            if (flat[i] == 0) continue;
            for (int j = i + 1; j < 16; j++) {
                if (flat[j] != 0 && flat[i] > flat[j]) {
                    inversions++;
                }
            }
        }

        // For 4x4 grid: puzzle is solvable if:
        // - blank on even row counting from bottom (odd row counting from top) and inversions odd
        // - blank on odd row counting from bottom (even row counting from top) and inversions even
        int blankRowFromBottom = 4 - blankRow;
        return (blankRowFromBottom % 2 == 0) == (inversions % 2 == 1);
    }

    // Generate puzzle that requires exactly N moves for level N
    private int[][] generateSolvablePuzzle(int targetMoves) {
        // Start from solved state and make exactly targetMoves optimal moves backwards
        int[][] puzzle = copyBoard(solvedBoard);
        Set<String> visited = new HashSet<>();
        visited.add(boardToString(puzzle));

        Random random = new Random();

        // Make exactly targetMoves moves from solved state
        for (int move = 0; move < targetMoves; move++) {
            int[] emptyPos = findEmptyTileIn(puzzle);
            int emptyRow = emptyPos[0], emptyCol = emptyPos[1];

            // Get all valid moves
            List<int[][]> validMoves = new ArrayList<>();
            int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}};

            for (int[] dir : directions) {
                int newRow = emptyRow + dir[0];
                int newCol = emptyCol + dir[1];

                if (newRow >= 0 && newRow < 4 && newCol >= 0 && newCol < 4) {
                    int[][] newPuzzle = copyBoard(puzzle);
                    newPuzzle[emptyRow][emptyCol] = newPuzzle[newRow][newCol];
                    newPuzzle[newRow][newCol] = 0;

                    // Only add moves that don't go back to a previous state
                    String stateStr = boardToString(newPuzzle);
                    if (!visited.contains(stateStr)) {
                        validMoves.add(newPuzzle);
                    }
                }
            }

            // If no new moves available, pick any valid move
            if (validMoves.isEmpty()) {
                for (int[] dir : directions) {
                    int newRow = emptyRow + dir[0];
                    int newCol = emptyCol + dir[1];

                    if (newRow >= 0 && newRow < 4 && newCol >= 0 && newCol < 4) {
                        int[][] newPuzzle = copyBoard(puzzle);
                        newPuzzle[emptyRow][emptyCol] = newPuzzle[newRow][newCol];
                        newPuzzle[newRow][newCol] = 0;
                        validMoves.add(newPuzzle);
                    }
                }
            }

            if (!validMoves.isEmpty()) {
                puzzle = validMoves.get(random.nextInt(validMoves.size()));
                visited.add(boardToString(puzzle));
            }
        }

        return puzzle;
    }

    // Generate puzzle by making exactly N optimal moves from solved state

            // Helper method to find empty tile in a given board
            private int[] findEmptyTileIn(int[][] puzzle) {
                for (int r = 0; r < 4; r++) {
                    for (int c = 0; c < 4; c++) {
                        if (puzzle[r][c] == 0) {
                            return new int[]{r, c};
                        }
                    }
                }
                return new int[]{3, 3}; // Default fallback
            }

            private int[] findEmptyTile() {
                return findEmptyTileIn(board);
            }

            private int[] getCorrectPosition(int value) {
                if (value == 0) return new int[]{3, 3};
                value--; // Convert to 0-based indexing
                return new int[]{value / 4, value % 4};
            }

            private int[][] copyBoard(int[][] original) {
                int[][] copy = new int[4][4];
                for (int r = 0; r < 4; r++) {
                    System.arraycopy(original[r], 0, copy[r], 0, 4);
                }
                return copy;
            }

            @Override
            protected void onDestroy() {
                super.onDestroy();
                stopTimer();
            }
        }