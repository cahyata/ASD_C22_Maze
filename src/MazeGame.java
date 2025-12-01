import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.sound.sampled.*;
import javax.swing.Timer;

public class MazeGame extends JFrame {

    // --- COLORS (Modern Apple/Clean Palette) ---
    public static final Color APP_BG = new Color(250, 250, 252);
    public static final Color SIDEBAR_BG = Color.WHITE;
    public static final Color PRIMARY_BLUE = new Color(0, 122, 255);
    public static final Color BUTTON_HOVER = new Color(0, 105, 215);
    public static final Color SECONDARY_BTN = new Color(242, 242, 247);
    public static final Color SECONDARY_HOVER = new Color(225, 225, 230);
    public static final Color TEXT_PRIMARY = new Color(29, 29, 31);
    public static final Color TEXT_SECONDARY = new Color(134, 134, 139);
    public static final Color BORDER_COLOR = new Color(230, 230, 235);

    // --- MAZE COLORS ---
    public static final Color MAZE_WALL = new Color(44, 44, 46);
    public static final Color T_DEFAULT = Color.WHITE;
    public static final Color T_GRASS = new Color(209, 250, 229);
    public static final Color T_MUD = new Color(254, 243, 199);
    public static final Color T_WATER = new Color(219, 234, 254);

    // Fonts
    public static final Font FONT_HEADER = new Font("-apple-system", Font.BOLD, 22);
    public static final Font FONT_LABEL = new Font("-apple-system", Font.BOLD, 12);
    public static final Font FONT_VALUE = new Font("-apple-system", Font.PLAIN, 12);

    private MazePanel mazePanel;
    private JComboBox<String> algoSelector;
    private JSlider sizeSlider;
    private JLabel sizeLabel;
    private JTextPane algoDescriptionArea;
    private Clip bgmClip;
    private FloatControl volumeControl;
    private MacButton btnPlayPause;
    private boolean isMusicPlaying = true;

    public MazeGame() {
        setTitle("Maze Ultimate: Data Structure Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(APP_BG);

        // --- 1. MAZE AREA (CENTER) ---
        mazePanel = new MazePanel(20, 20);

        JPanel mazeContainer = new JPanel(new GridBagLayout());
        mazeContainer.setBackground(APP_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(20, 20, 20, 20);
        mazeContainer.add(mazePanel, gbc);

        add(mazeContainer, BorderLayout.CENTER);

        // --- 2. SIDEBAR (RIGHT) ---
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBackground(SIDEBAR_BG);
        sidePanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER_COLOR));
        sidePanel.setPreferredSize(new Dimension(320, 0)); // Agak lebar dikit buat teks

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(SIDEBAR_BG);
        content.setBorder(new EmptyBorder(30, 20, 30, 20));
        content.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Components
        JLabel lblTitle = new JLabel("Pengaturan");
        lblTitle.setFont(FONT_HEADER);
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Grid Size
        JPanel gridHeader = createRowLabel("Ukuran Grid", "20 x 20");
        sizeLabel = (JLabel) gridHeader.getComponent(1);

        sizeSlider = new JSlider(10, 60, 20);
        sizeSlider.setBackground(SIDEBAR_BG);
        sizeSlider.setFocusable(false);
        sizeSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        sizeSlider.addChangeListener(e -> {
            int val = sizeSlider.getValue();
            sizeLabel.setText(val + " x " + val);
        });

        // Algo
        JLabel lblAlgo = new JLabel("Algoritma");
        lblAlgo.setFont(FONT_LABEL);
        lblAlgo.setForeground(TEXT_PRIMARY);
        lblAlgo.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] algos = {"Dijkstra (Biaya Terendah)", "A* Search (Cerdas)", "BFS (Langkah Terpendek)", "DFS (Acak)"};
        algoSelector = new JComboBox<>(algos);
        algoSelector.setFont(FONT_VALUE);
        algoSelector.setBackground(Color.WHITE);
        algoSelector.setMaximumSize(new Dimension(300, 35));
        algoSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
        algoSelector.addActionListener(e -> updateAlgoDescription());

        // Info Card
        algoDescriptionArea = new JTextPane();
        algoDescriptionArea.setContentType("text/html");
        algoDescriptionArea.setEditable(false);
        algoDescriptionArea.setOpaque(false);

        JPanel infoCard = new JPanel(new BorderLayout());
        infoCard.setBackground(new Color(245, 245, 247));
        infoCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        infoCard.add(algoDescriptionArea);
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoCard.setMaximumSize(new Dimension(300, 100)); // Sedikit lebih tinggi

        // Buttons
        MacButton btnGenerate = new MacButton("Buat Terrain Baru", false);
        MacButton btnSolve = new MacButton("Mulai Jalan", true);

        // Music
        JPanel audioSection = new JPanel(new BorderLayout());
        audioSection.setBackground(SIDEBAR_BG);
        audioSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        audioSection.setMaximumSize(new Dimension(300, 40));

        btnPlayPause = new MacButton("Pause", false);
        btnPlayPause.setPreferredSize(new Dimension(70, 30));
        btnPlayPause.setFont(new Font("-apple-system", Font.BOLD, 11));

        JSlider volSlider = new JSlider(0, 100, 70);
        volSlider.setBackground(SIDEBAR_BG);
        volSlider.setPreferredSize(new Dimension(100, 30));
        volSlider.addChangeListener(e -> updateVolume(volSlider.getValue()));

        audioSection.add(btnPlayPause, BorderLayout.WEST);
        audioSection.add(volSlider, BorderLayout.CENTER);

        // Adding
        content.add(lblTitle);
        content.add(Box.createVerticalStrut(25));
        content.add(gridHeader);
        content.add(Box.createVerticalStrut(5));
        content.add(sizeSlider);
        content.add(Box.createVerticalStrut(20));
        content.add(lblAlgo);
        content.add(Box.createVerticalStrut(8));
        content.add(algoSelector);
        content.add(Box.createVerticalStrut(10));
        content.add(infoCard);
        content.add(Box.createVerticalStrut(25));
        content.add(btnGenerate);
        content.add(Box.createVerticalStrut(10));
        content.add(btnSolve);
        content.add(Box.createVerticalGlue());
        content.add(new JSeparator());
        content.add(Box.createVerticalStrut(15));
        JLabel lblMusic = new JLabel("Musik Latar");
        lblMusic.setFont(FONT_LABEL);
        lblMusic.setForeground(TEXT_SECONDARY);
        lblMusic.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblMusic);
        content.add(Box.createVerticalStrut(5));
        content.add(audioSection);

        sidePanel.add(content);
        add(sidePanel, BorderLayout.EAST);

        // Listeners
        btnGenerate.addActionListener(e -> {
            int size = sizeSlider.getValue();
            mazePanel.setGridSize(size, size);
            mazePanel.generateMaze();
            mazeContainer.revalidate();
            mazePanel.repaint();
        });

        btnSolve.addActionListener(e -> mazePanel.solveMaze((String) algoSelector.getSelectedItem()));
        btnPlayPause.addActionListener(e -> toggleMusic());

        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                mazePanel.repaint();
            }
        });

        updateAlgoDescription();
        initAudio("sound.wav");

        setSize(1100, 750);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createRowLabel(String title, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(SIDEBAR_BG);
        p.setMaximumSize(new Dimension(300, 20));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l1 = new JLabel(title); l1.setFont(FONT_LABEL); l1.setForeground(TEXT_PRIMARY);
        JLabel l2 = new JLabel(value); l2.setFont(FONT_VALUE); l2.setForeground(TEXT_SECONDARY);
        p.add(l1, BorderLayout.WEST); p.add(l2, BorderLayout.EAST);
        return p;
    }

    class MacButton extends JButton {
        private boolean isPrimary;
        public MacButton(String text, boolean primary) {
            super(text);
            this.isPrimary = primary;
            setFont(new Font("-apple-system", Font.BOLD, 13));
            setForeground(primary ? Color.WHITE : TEXT_PRIMARY);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(300, 40));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(primary ? BUTTON_HOVER : SECONDARY_HOVER); repaint(); }
                public void mouseExited(MouseEvent e) { setBackground(primary ? PRIMARY_BLUE : SECONDARY_BTN); repaint(); }
            });
            setBackground(primary ? PRIMARY_BLUE : SECONDARY_BTN);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            super.paintComponent(g);
        }
    }

    // --- UPDATE: PENJELASAN DATA STRUKTUR (STACK/QUEUE) ---
    private void updateAlgoDescription() {
        String selected = (String) algoSelector.getSelectedItem();
        String desc = "";

        if (selected.contains("Dijkstra")) {
            desc = "<b>DIJKSTRA</b><br>" +
                    "Struktur Data: <b style='color:#007aff'>Priority Queue</b><br>" +
                    "Menjamin <b>Biaya Terendah</b>. Ia memeriksa simpul dengan akumulasi biaya terkecil terlebih dahulu, menghindari rintangan berat secara optimal.";
        }
        else if (selected.contains("A*")) {
            desc = "<b>A* (A-STAR)</b><br>" +
                    "Struktur Data: <b style='color:#007aff'>Priority Queue</b><br>" +
                    "Lebih cerdas dari Dijkstra karena menggunakan <b>Heuristik</b> (estimasi jarak ke finish) untuk memprioritaskan arah.";
        }
        else if (selected.contains("BFS")) {
            desc = "<b>BFS (Breadth First Search)</b><br>" +
                    "Struktur Data: <b style='color:#34c759'>Queue (Antrian FIFO)</b><br>" +
                    "Menyebar ke segala arah lapis demi lapis. Menjamin <b>Langkah Tersedikit</b>, tapi buta terhadap berat medan (lumpur/air).";
        }
        else {
            desc = "<b>DFS (Depth First Search)</b><br>" +
                    "Struktur Data: <b style='color:#ff3b30'>Stack (Tumpukan LIFO)</b><br>" +
                    "Menelusuri satu lorong sedalam mungkin sebelum mundur. Jalurnya sering acak, panjang, dan berputar-putar.";
        }

        algoDescriptionArea.setText("<html><body style='font-family:-apple-system, sans-serif; font-size:10px; color:#666;'>" + desc + "</body></html>");
    }

    private void initAudio(String filePath) {
        new Thread(() -> {
            try {
                File f = new File(filePath);
                if(f.exists()) {
                    AudioInputStream ais = AudioSystem.getAudioInputStream(f);
                    bgmClip = AudioSystem.getClip();
                    bgmClip.open(ais);
                    if (bgmClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        volumeControl = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
                        updateVolume(70);
                    }
                    bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                    bgmClip.start();
                } else { btnPlayPause.setText("No Audio"); btnPlayPause.setEnabled(false); }
            } catch(Exception ex) {}
        }).start();
    }

    private void toggleMusic() {
        if(bgmClip == null) return;
        if(isMusicPlaying) { bgmClip.stop(); btnPlayPause.setText("Play"); isMusicPlaying = false; }
        else { bgmClip.start(); bgmClip.loop(Clip.LOOP_CONTINUOUSLY); btnPlayPause.setText("Pause"); isMusicPlaying = true; }
    }

    private void updateVolume(int val) {
        if(volumeControl == null) return;
        float min = -60.0f, max = 6.0f;
        float gain = (val == 0) ? -80.0f : min + ((max - min) * (val / 100.0f));
        volumeControl.setValue(gain);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(MazeGame::new);
    }
}

// --- DATA ---
enum TerrainType { DEFAULT(0), GRASS(1), MUD(5), WATER(10); public final int cost; TerrainType(int cost){this.cost=cost;} }
class Cell implements Comparable<Cell> {
    int r,c; boolean[] walls={true,true,true,true}; boolean visited=false; Cell parent=null;
    TerrainType terrain=TerrainType.DEFAULT; double gCost=Double.POSITIVE_INFINITY,hCost=0,fCost=Double.POSITIVE_INFINITY;
    public Cell(int r,int c){this.r=r;this.c=c;}
    @Override public int compareTo(Cell o){return Double.compare(this.fCost,o.fCost);}
}

// --- STATS WINDOW (CSS Style) ---
class SolveStats {
    long timeTakenNano; int pathSteps,totalCost,nodesVisited; String algorithmName;
    public String getSummary(){
        double timeMs = timeTakenNano / 1_000_000.0;
        return String.format(
                "<html><body style='width: 280px; font-family: -apple-system, sans-serif; background-color: #f2f2f7; padding: 5px;'>" +
                        "<div style='background-color: white; border-radius: 10px; padding: 15px; border: 1px solid #d1d1d6;'>" +
                        "<h2 style='margin: 0 0 10px 0; color: #1c1c1e; font-size: 16px; text-align: center;'>Analisis Performa</h2>" +
                        "<hr style='border: 0; border-top: 1px solid #e5e5ea; margin-bottom: 10px;'>" +
                        "<table style='width: 100%%; border-spacing: 0;'>" +
                        "<tr><td style='color: #8e8e93; font-size: 11px; padding: 4px 0;'>Algoritma</td><td style='color: #007aff; font-weight: bold; font-size: 12px; text-align: right;'>%s</td></tr>" +
                        "<tr><td style='color: #8e8e93; font-size: 11px; padding: 4px 0;'>Waktu Komputasi</td><td style='color: #1c1c1e; font-weight: bold; font-size: 12px; text-align: right;'>%.4f ms</td></tr>" +
                        "<tr><td style='color: #8e8e93; font-size: 11px; padding: 4px 0;'>Total Langkah</td><td style='color: #34c759; font-weight: bold; font-size: 12px; text-align: right;'>%d</td></tr>" +
                        "<tr><td style='color: #8e8e93; font-size: 11px; padding: 4px 0;'>Total Biaya (Cost)</td><td style='color: #ff3b30; font-weight: bold; font-size: 12px; text-align: right;'>%d</td></tr>" +
                        "<tr><td style='color: #8e8e93; font-size: 11px; padding: 4px 0;'>Area Dipindai</td><td style='color: #ff9500; font-weight: bold; font-size: 12px; text-align: right;'>%d</td></tr>" +
                        "</table></div></body></html>",
                algorithmName, timeMs, pathSteps, totalCost, nodesVisited
        );
    }
}

// --- MAZE PANEL ---
class MazePanel extends JPanel {
    private int rows, cols;
    private Cell[][] grid; private Cell startCell, endCell; private List<Cell> currentPath = new ArrayList<>();
    private Timer animationTimer; private int animationIndex = 0; private Random rand = new Random(); private SolveStats lastStats;

    public MazePanel(int rows, int cols) {
        this.rows=rows; this.cols=cols;
        this.setBackground(MazeGame.APP_BG);
        this.setPreferredSize(new Dimension(800, 600));
        generateMaze();
    }

    public void setGridSize(int r, int c) { this.rows=r; this.cols=c; generateMaze(); }

    public void generateMaze() {
        grid = new Cell[rows][cols]; for(int r=0;r<rows;r++)for(int c=0;c<cols;c++) grid[r][c]=new Cell(r,c);
        currentPath.clear(); if(animationTimer!=null) animationTimer.stop();
        ArrayList<Cell> frontier = new ArrayList<>();
        Cell start=grid[0][0]; start.visited=true; addFrontier(start,frontier);
        while(!frontier.isEmpty()) {
            Cell current=frontier.remove(rand.nextInt(frontier.size()));
            List<Cell> vn=getVisitedNeighbors(current);
            if(!vn.isEmpty()) { Cell n=vn.get(rand.nextInt(vn.size())); removeWalls(current,n); current.visited=true; addFrontier(current,frontier); }
        }
        int wallsToRemove=(rows*cols)/8;
        for(int i=0;i<wallsToRemove;i++) { Cell c=grid[rand.nextInt(rows)][rand.nextInt(cols)]; List<Cell> wn=getWalledNeighbors(c); if(!wn.isEmpty()) removeWalls(c,wn.get(rand.nextInt(wn.size()))); }
        for(int r=0;r<rows;r++)for(int c=0;c<cols;c++) {
            double chance=rand.nextDouble();
            if(chance<0.60) grid[r][c].terrain=TerrainType.DEFAULT; else if(chance<0.80) grid[r][c].terrain=TerrainType.GRASS;
            else if(chance<0.95) grid[r][c].terrain=TerrainType.MUD; else grid[r][c].terrain=TerrainType.WATER;
        }
        startCell=grid[0][0]; endCell=grid[rows-1][cols-1];
        startCell.terrain=TerrainType.DEFAULT; endCell.terrain=TerrainType.DEFAULT;
        resetState(); repaint();
    }

    public void solveMaze(String algoType) {
        resetState(); currentPath.clear(); if(animationTimer!=null) animationTimer.stop();
        long startTime=System.nanoTime(); boolean found=false;
        if(algoType.startsWith("BFS")) found=runBFS(); else if(algoType.startsWith("DFS")) found=runDFS();
        else if(algoType.startsWith("Dijkstra")) found=runDijkstraOrAStar(false); else if(algoType.startsWith("A*")) found=runDijkstraOrAStar(true);
        long endTime=System.nanoTime();
        if(found) {
            reconstructPath(); int totalCost=0; for(Cell c:currentPath) totalCost+=c.terrain.cost;
            lastStats=new SolveStats(); lastStats.timeTakenNano=endTime-startTime; lastStats.pathSteps=currentPath.size(); lastStats.totalCost=totalCost; lastStats.nodesVisited=countVisited(); lastStats.algorithmName=algoType.split(" ")[0];
            startAnimation();
        } else JOptionPane.showMessageDialog(this,"Tidak ada jalan!", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private boolean runBFS(){Queue<Cell> q=new LinkedList<>();startCell.visited=true;q.add(startCell);while(!q.isEmpty()){Cell cur=q.poll();if(cur==endCell)return true;for(Cell n:getAccessibleNeighbors(cur)){if(!n.visited){n.visited=true;n.parent=cur;q.add(n);}}}return false;}
    private boolean runDFS(){Stack<Cell> s=new Stack<>();startCell.visited=true;s.push(startCell);while(!s.isEmpty()){Cell cur=s.pop();if(cur==endCell)return true;List<Cell> n=getAccessibleNeighbors(cur);Collections.shuffle(n);for(Cell neighbor:n){if(!neighbor.visited){neighbor.visited=true;neighbor.parent=cur;s.push(neighbor);}}}return false;}
    private boolean runDijkstraOrAStar(boolean isAStar){PriorityQueue<Cell> pq=new PriorityQueue<>();startCell.gCost=0;startCell.hCost=isAStar?heuristic(startCell,endCell):0;startCell.fCost=startCell.gCost+startCell.hCost;pq.add(startCell);while(!pq.isEmpty()){Cell cur=pq.poll();if(cur.visited)continue;cur.visited=true;if(cur==endCell)return true;for(Cell n:getAccessibleNeighbors(cur)){if(n.visited)continue;double newG=cur.gCost+n.terrain.cost;if(newG<n.gCost){n.gCost=newG;n.hCost=isAStar?heuristic(n,endCell):0;n.fCost=n.gCost+n.hCost;n.parent=cur;pq.remove(n);pq.add(n);}}}return false;}
    private double heuristic(Cell a,Cell b){return Math.abs(a.r-b.r)+Math.abs(a.c-b.c);}
    private void addFrontier(Cell cell,ArrayList<Cell> frontier){int[] dr={-1,1,0,0},dc={0,0,-1,1};for(int i=0;i<4;i++){int nr=cell.r+dr[i],nc=cell.c+dc[i];if(isValid(nr,nc)&&!grid[nr][nc].visited&&!frontier.contains(grid[nr][nc]))frontier.add(grid[nr][nc]);}}
    private List<Cell> getVisitedNeighbors(Cell cell){List<Cell> n=new ArrayList<>();int[] dr={-1,1,0,0},dc={0,0,-1,1};for(int i=0;i<4;i++){int nr=cell.r+dr[i],nc=cell.c+dc[i];if(isValid(nr,nc)&&grid[nr][nc].visited)n.add(grid[nr][nc]);}return n;}
    private List<Cell> getWalledNeighbors(Cell cell){List<Cell> n=new ArrayList<>();int[] dr={-1,1,0,0},dc={0,0,-1,1};for(int i=0;i<4;i++){int nr=cell.r+dr[i],nc=cell.c+dc[i];if(isValid(nr,nc)){boolean w=(i==0&&cell.walls[0])||(i==1&&cell.walls[2])||(i==2&&cell.walls[3])||(i==3&&cell.walls[1]);if(w)n.add(grid[nr][nc]);}}return n;}
    private void removeWalls(Cell a,Cell b){int dr=a.r-b.r,dc=a.c-b.c;if(dr==1){a.walls[0]=false;b.walls[2]=false;}if(dr==-1){a.walls[2]=false;b.walls[0]=false;}if(dc==1){a.walls[3]=false;b.walls[1]=false;}if(dc==-1){a.walls[1]=false;b.walls[3]=false;}}
    private List<Cell> getAccessibleNeighbors(Cell cell){List<Cell> n=new ArrayList<>();if(!cell.walls[0]&&isValid(cell.r-1,cell.c))n.add(grid[cell.r-1][cell.c]);if(!cell.walls[1]&&isValid(cell.r,cell.c+1))n.add(grid[cell.r][cell.c+1]);if(!cell.walls[2]&&isValid(cell.r+1,cell.c))n.add(grid[cell.r+1][cell.c]);if(!cell.walls[3]&&isValid(cell.r,cell.c-1))n.add(grid[cell.r][cell.c-1]);return n;}
    private void reconstructPath(){Cell cur=endCell;while(cur!=null){currentPath.add(0,cur);cur=cur.parent;}}
    private int countVisited(){int c=0;for(int r=0;r<rows;r++)for(int col=0;col<cols;col++)if(grid[r][col].visited)c++;return c;}
    private void resetState(){for(int r=0;r<rows;r++)for(int c=0;c<cols;c++){grid[r][c].visited=false;grid[r][c].parent=null;grid[r][c].gCost=Double.POSITIVE_INFINITY;grid[r][c].fCost=Double.POSITIVE_INFINITY;}}
    private boolean isValid(int r,int c){return r>=0&&r<rows&&c>=0&&c<cols;}

    private void startAnimation(){
        animationIndex=0;
        int delay=80;
        animationTimer=new Timer(delay,e->{
            if(animationIndex<currentPath.size()){
                animationIndex++;
                repaint();
            } else {
                animationTimer.stop();
                if(lastStats!=null) {
                    JOptionPane.showMessageDialog(this, lastStats.getSummary(), "Hasil", JOptionPane.PLAIN_MESSAGE);
                }
            }
        });
        animationTimer.start();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g); Graphics2D g2=(Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        int w=getWidth(), h=getHeight(); if(w==0||h==0)return;
        float cellW=(float)w/cols, cellH=(float)h/rows, cs=Math.min(cellW,cellH);
        float offsetX=(w-(cols*cs))/2, offsetY=(h-(rows*cs))/2; g2.translate(offsetX,offsetY);
        for(int r=0;r<rows;r++)for(int c=0;c<cols;c++){
            int x=(int)(c*cs),y=(int)(r*cs),s=(int)Math.ceil(cs);
            if(grid[r][c].terrain==TerrainType.GRASS)g2.setColor(MazeGame.T_GRASS);else if(grid[r][c].terrain==TerrainType.MUD)g2.setColor(MazeGame.T_MUD);else if(grid[r][c].terrain==TerrainType.WATER)g2.setColor(MazeGame.T_WATER);else g2.setColor(MazeGame.T_DEFAULT);
            g2.fillRect(x,y,s,s);
        }
        g2.setColor(MazeGame.MAZE_WALL); g2.setStroke(new BasicStroke(Math.max(1.5f,cs/15)));
        for(int r=0;r<rows;r++)for(int c=0;c<cols;c++){
            int x=(int)(c*cs),y=(int)(r*cs),s=(int)cs;
            if(grid[r][c].walls[0])g2.drawLine(x,y,x+s,y);if(grid[r][c].walls[1])g2.drawLine(x+s,y,x+s,y+s);if(grid[r][c].walls[2])g2.drawLine(x,y+s,x+s,y+s);if(grid[r][c].walls[3])g2.drawLine(x,y,x,y+s);
        }
        if(!currentPath.isEmpty()){
            g2.setColor(MazeGame.PRIMARY_BLUE); g2.setStroke(new BasicStroke(cs*0.4f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            for(int i=1;i<animationIndex&&i<currentPath.size();i++){Cell p=currentPath.get(i-1),c=currentPath.get(i);g2.drawLine((int)(p.c*cs+cs/2),(int)(p.r*cs+cs/2),(int)(c.c*cs+cs/2),(int)(c.r*cs+cs/2));}
            if(animationIndex>0){Cell h2=currentPath.get(Math.min(animationIndex-1,currentPath.size()-1));int hx=(int)(h2.c*cs+cs/2),hy=(int)(h2.r*cs+cs/2),hs=(int)(cs*0.65);g2.setColor(new Color(0,64,221));g2.fillOval(hx-hs/2,hy-hs/2,hs,hs);}
        }
        drawMarker(g2,startCell,new Color(16,185,129),cs); drawMarker(g2,endCell,new Color(239,68,68),cs);
    }
    private void drawMarker(Graphics2D g2,Cell cell,Color c,float cs){if(cell==null)return;g2.setColor(c);int m=(int)(cs*0.2),s=(int)(cs-m*2);g2.fillRoundRect((int)(cell.c*cs+m),(int)(cell.r*cs+m),s,s,8,8);}
}