import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import oracle.spatial.geometry.JGeometry;


public class MapViewer extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Connection mainConnection = null;
	private final JPanel contentPane;
	JPanel panel;
	JCheckBox chkboxHighlight;
	JScrollPane scrollPane;
	JTextField mouseLocation;
	int coorx;
	int coory;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					MapViewer frame = new MapViewer();
					frame.ConnectToDB();
					frame.setTitle("Shivankur Kapoor");
					frame.setVisible(true);
					frame.frameInitialize();

				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
		});
	}

	protected void initialize() {
		// TODO Auto-generated method stub
		
	}

	protected void frameInitialize() {
		// TODO Auto-generated method stub
		this.panel.paintComponents(null);

	}

	/**
	 * Create the frame.
	 */
	public MapViewer() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(0, 0, screen.width - 840, screen.height - 70);
		contentPane = new JPanel();
		setContentPane(contentPane);
		panel = new paintPanel(mainConnection);
		javax.swing.GroupLayout imagePanelLayout = new javax.swing.GroupLayout(panel);
		panel.setLayout(imagePanelLayout);
		imagePanelLayout.setHorizontalGroup(imagePanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 700, Short.MAX_VALUE));
		imagePanelLayout.setVerticalGroup(imagePanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 580, Short.MAX_VALUE));

		panel.setBounds(2, 2, 900, 500);
		contentPane.setLayout(null);
		contentPane.setLayout(null);
		contentPane.add(panel);
       
		mouseLocation = new JTextField();
		panel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				coorx = e.getX();
				coory = e.getY();
				mouseLocation.setText("( " + coorx + " , " + coory + " )");
				if (chkboxHighlight.isSelected()) {
					DrawFigures.displayLions(panel,mainConnection);
					DrawFigures.displayPonds(panel,mainConnection);
					DrawFigures.highlight(coorx, coory,panel,mainConnection);
				}
			}
		});
		mouseLocation.setBounds(200, 560, 100, 35);
		contentPane.add(mouseLocation);

		JButton SubmitQueryButton = new JButton("Display");
		SubmitQueryButton.addActionListener(this);
		SubmitQueryButton.setFont(new Font("Arial", Font.PLAIN, 15));
		SubmitQueryButton.setBounds(170, 600, 167, 35);
		contentPane.add(SubmitQueryButton);

		chkboxHighlight = new JCheckBox("Show lions and ponds in the selected region");
		chkboxHighlight.setFont(new Font("Arial", Font.PLAIN, 15));
		chkboxHighlight.setHorizontalAlignment(SwingConstants.LEFT);
		chkboxHighlight.setBounds(110, 530, 700, 23);
		contentPane.add(chkboxHighlight);
		chkboxHighlight.addActionListener(this);
		

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(!chkboxHighlight.isSelected()){
			DrawFigures.displayRegions(panel,mainConnection);
			DrawFigures.displayLions(panel,mainConnection);
			DrawFigures.displayPonds(panel,mainConnection);
		}
	}

	public void ConnectToDB() {
		try {
			// loading Oracle Driver
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			// System.out.println(", Loaded.");

			String URL = "jdbc:oracle:thin:@localhost:1521:";
			String userName = "system";
			String password = "dabamalsi";

			// System.out.print("Connecting to DB...");
			mainConnection = DriverManager.getConnection(URL, userName, password);
			// System.out.println(", Connected!");

			// mainStatement = mainConnection.createStatement();
		} catch (Exception e) {
			System.out.println("Error while connecting to DB: " + e.toString());
			System.out.println(e.getMessage());
			System.exit(-1);
		}
	}

}

class DrawFigures{

	public static void highlight(int coorx, int coory,JPanel panel,Connection mainConnection) {
		Statement stmt = null;
		String sql1 = "select shape from lion LL where LL.lion_id in(SELECT lion_id FROM Lion l,Regions r  WHERE sdo_relate(l.shape,r.polygon\n"
				+ "                                       ,'mask = ANYINTERACT'\n"
				+ "                           ) = 'TRUE'  and id in (select id from regions r1 where sdo_relate(r1.polygon,SDO_geometry(1,NULL,NULL,\n"
				+ "                                       SDO_elem_info_array(1,1,1),\n"
				+ "                                       SDO_ordinate_array(" + coorx + "," + coory + "))\n"
				+ "                         ,'mask=ANYINTERACT') = 'TRUE' ))";

		String sql2 = "select polygon from pond pp where pp.pond_id in(SELECT pond_id FROM pond p,Regions r  WHERE sdo_relate(p.polygon,r.polygon\n"
				+ "                                       ,'mask = ANYINTERACT'\n"
				+ "                           ) = 'TRUE'  and id in (select id from regions r1 where sdo_relate(r1.polygon,SDO_geometry(1,NULL,NULL,\n"
				+ "                                       SDO_elem_info_array(1,1,1),\n"
				+ "                                       SDO_ordinate_array(" + coorx + "," + coory + "))\n"
				+ "                         ,'mask=ANYINTERACT') = 'TRUE' ))";

		try {

			int x;
			int y;
			stmt = mainConnection.createStatement();
			ResultSet rs = stmt.executeQuery(sql1);
			while (rs.next()) {
				oracle.sql.STRUCT struct = (oracle.sql.STRUCT) rs.getObject("shape");
				JGeometry jgeom = JGeometry.load(struct);
				Point2D point = jgeom.getJavaPoint();
				x = (int) point.getX();
				y = (int) point.getY();
				// draw the point
				paintPanel.highLions(panel.getGraphics(), x, y, Color.RED);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		try {

			double points[];
			int radius, x, y;
			stmt = mainConnection.createStatement();
			ArrayList<Integer> x1 = new ArrayList<Integer>();
			ArrayList<Integer> y1 = new ArrayList<Integer>();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next()) {
				oracle.sql.STRUCT struct = (oracle.sql.STRUCT) rs.getObject("polygon");
				JGeometry jgeom = JGeometry.load(struct);
				x1.clear();
				y1.clear();
				points = jgeom.getOrdinatesArray();
				for (int i = 0; i < points.length; i++) {
					if ((i % 2) == 0) {
						x1.add((int) points[i]);
					} else {
						y1.add((int) points[i]);
					}
				}
				radius = 15;

				y = y1.get(1);
				x = x1.get(0);
				paintPanel.highPonds(panel.getGraphics(), x, y, radius, Color.RED);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static void displayLions(JPanel panel,Connection mainConnection) {
		Statement stmt = null;
		int x;
		int y;
		String sql = "SELECT shape from Lion";

		try {
			stmt = mainConnection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				oracle.sql.STRUCT struct = (oracle.sql.STRUCT) rs.getObject("shape");
				JGeometry jgeom = JGeometry.load(struct);
				Point2D point = jgeom.getJavaPoint();
				x = (int) point.getX();
				y = (int) point.getY();
				// draw the point
				paintPanel.drawLions(panel.getGraphics(), x, y, Color.GREEN);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	public static void displayPonds(JPanel panel,Connection mainConnection) {
		Statement stmt = null;
		ArrayList<Integer> x1 = new ArrayList<Integer>();
		ArrayList<Integer> y1 = new ArrayList<Integer>();
		int x, y;
		double points[];
		int radius;
		String sql = "SELECT * from pond";

		try {
			stmt = mainConnection.createStatement();

			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				oracle.sql.STRUCT struct = (oracle.sql.STRUCT) rs.getObject("polygon");
				JGeometry jgeom = JGeometry.load(struct);
				x1.clear();
				y1.clear();
				points = jgeom.getOrdinatesArray();
				for (int i = 0; i < points.length; i++) {
					if ((i % 2) == 0) {
						x1.add((int) points[i]);
					} else {
						y1.add((int) points[i]);
					}
				}
				radius = 15;

				y = y1.get(1);
				x = x1.get(0);
				// draw the point
				// paintPanel.drawAS(panel.getGraphics(), x, y, Color.RED);
				// draw the circle
				paintPanel.drawPonds(panel.getGraphics(), x, y, radius, Color.BLACK);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	public static void displayRegions(JPanel panel,Connection mainConnection) {
		Statement stmt = null;
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> y = new ArrayList<Integer>();
		double[] points;
		String sql = "SELECT * from regions";

		try {
			stmt = mainConnection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				x.clear();
				y.clear();
				oracle.sql.STRUCT struct = (oracle.sql.STRUCT) rs.getObject("polygon");
				JGeometry jgeom = JGeometry.load(struct);
				points = jgeom.getOrdinatesArray();
				for (int i = 0; i < points.length; i++) {
					if ((i % 2) == 0) {
						x.add((int) points[i]);
					} else {
						y.add((int) points[i]);
					}
				}
				// draw the building
				paintPanel.drawRegions(panel.getGraphics(), x, y, x.size(), Color.BLACK);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}

class paintPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static BufferedImage map;

	public static void drawRegions(Graphics g, ArrayList<Integer> x, ArrayList<Integer> y, int verticesCount,
			Color c) {

		int[] coordinateX = new int[verticesCount];
		int[] coordinateY = new int[verticesCount];
		for (int i = 0; i < verticesCount; i++) {
			coordinateX[i] = x.get(i).intValue();
			coordinateY[i] = y.get(i).intValue();
		}
		g.setColor(Color.WHITE);
		g.fillPolygon(coordinateX, coordinateY, verticesCount);
		g.setColor(c);
		g.drawPolygon(coordinateX, coordinateY, verticesCount);

	}

	public static void drawLions(Graphics g, int x, int y, Color c) {
		g.setColor(c);
		g.fillRect(x - 5, y - 5, 10, 10);
	}

	public static void highLions(Graphics g, int x, int y, Color c) {
		g.setColor(c);
		g.fillRect(x - 5, y - 5, 10, 10);
	}

	public static void drawAS(Graphics g, int x, int y, Color c) {
		g.setColor(c);
		g.fillRect(x, y, 15, 15);
	}

	public static void drawPonds(Graphics g, int x, int y, int radius, Color c) {
		g.setColor(Color.BLUE);
		g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
		g.setColor(c);
		g.drawOval(x - radius, y - radius, 2 * radius, 2 * radius);

	}

	public static void highPonds(Graphics g, int x, int y, int radius, Color c) {
		g.setColor(Color.RED);
		g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
		g.setColor(c);
		g.drawOval(x - radius, y - radius, 2 * radius, 2 * radius);

	}


	public paintPanel(Connection mainConnection) {
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

	}

	public static void clearImage(Graphics g) {
		g.drawImage(map, 0, 0, null);
	}
}
