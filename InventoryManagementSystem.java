import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class InventoryManagementSystem extends JFrame {

    // ─── DB config ─────────────────────────────────────────────────────────────
    private static final String DB_URL  = System.getenv().getOrDefault("DB_URL",  "jdbc:mysql://localhost:3306/inventory_db");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "root");
    private static final String DB_PASS = System.getenv().getOrDefault("DB_PASS", "");
    private static final int    LOW_STOCK_THRESHOLD = 5;

    // ─── Theme palette ──────────────────────────────────────────────────────────
    static final Color PRIMARY      = new Color(67, 97, 238);
    static final Color PRIMARY_DARK = new Color(52, 73, 204);
    static final Color ACCENT       = new Color(76, 201, 160);
    static final Color DANGER       = new Color(239, 68, 68);
    static final Color WARNING      = new Color(245, 158, 11);
    static final Color SURFACE      = new Color(248, 250, 252);
    static final Color CARD         = Color.WHITE;
    static final Color BORDER       = new Color(226, 232, 240);
    static final Color TEXT_PRIMARY  = new Color(15, 23, 42);
    static final Color TEXT_MUTED    = new Color(100, 116, 139);
    static final Font  FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    static final Font  FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font  FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);
    static final Font  FONT_LABEL   = new Font("Segoe UI", Font.BOLD, 12);

    private Connection con;
    private JPanel     contentArea;  // swap panels here
    private JLabel     statusBar;

    // ─── Constructor ────────────────────────────────────────────────────────────
    public InventoryManagementSystem() {
        initDatabaseConnection();
        buildUI();
        showDashboard();
        setVisible(true);
    }

    // ─── Database ───────────────────────────────────────────────────────────────
    private void initDatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Database connection failed.\n" + e.getMessage(),
                "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Execute a query and return results; caller must close ResultSet & Statement */
    private ResultSet query(String sql) throws SQLException {
        return con.createStatement().executeQuery(sql);
    }

    // ─── Frame layout ───────────────────────────────────────────────────────────
    private void buildUI() {
        setTitle("Inventory Management System");
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 580));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(SURFACE);

        add(buildSidebar(),    BorderLayout.WEST);
        add(buildTopBar(),     BorderLayout.NORTH);
        add(buildStatusBar(),  BorderLayout.SOUTH);

        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(SURFACE);
        contentArea.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        add(contentArea, BorderLayout.CENTER);
    }

    // ─── Sidebar ────────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY, 0, getHeight(), PRIMARY_DARK);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Logo area
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 24));
        logo.setOpaque(false);
        JLabel logoIcon = new JLabel("◈");
        logoIcon.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        logoIcon.setForeground(Color.WHITE);
        JLabel logoText = new JLabel("<html><b style='font-size:13px'>InvTrack</b><br><span style='font-size:10px;color:#a5b4fc'>Pro Edition</span></html>");
        logoText.setForeground(Color.WHITE);
        logo.add(logoIcon);
        logo.add(logoText);
        sidebar.add(logo);

        // Divider
        sidebar.add(sidebarDivider());

        // Nav items
        String[][] items = {
            {"⊞", "Dashboard"},
            {"▤", "Inventory"},
            {"＋", "Add Item"},
            {"⚠", "Low Stock"},
            {"↗", "Reports"},
        };
        for (String[] item : items) {
            sidebar.add(sidebarButton(item[0], item[1]));
        }

        sidebar.add(Box.createVerticalGlue());

        // Bottom help button
        sidebar.add(sidebarDivider());
        JLabel version = new JLabel("  v2.0.0 — MySQL");
        version.setForeground(new Color(165, 180, 252, 160));
        version.setFont(FONT_SMALL);
        version.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 16));
        sidebar.add(version);

        return sidebar;
    }

    private Component sidebarDivider() {
        JPanel line = new JPanel();
        line.setOpaque(false);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setBackground(new Color(255, 255, 255, 40));
        line.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 40)));
        return line;
    }

    private JButton sidebarButton(String icon, String label) {
        JButton btn = new JButton(icon + "  " + label) {
            @Override protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fillRoundRect(8, 4, getWidth() - 16, getHeight() - 8, 10, 10);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setForeground(new Color(255, 255, 255, 220));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e)  { btn.setForeground(new Color(255, 255, 255, 220)); }
        });

        btn.addActionListener(e -> {
            switch (label) {
                case "Dashboard" -> showDashboard();
                case "Inventory" -> showInventory(null);
                case "Add Item"  -> showAddItemDialog();
                case "Low Stock" -> showInventory("quantity <= " + LOW_STOCK_THRESHOLD);
                case "Reports"   -> showReports();
            }
        });

        return btn;
    }

    // ─── Top bar ────────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setBackground(CARD);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));

        JLabel pageTitle = new JLabel("Dashboard");
        pageTitle.setFont(FONT_TITLE);
        pageTitle.setForeground(TEXT_PRIMARY);
        topBar.add(pageTitle, BorderLayout.WEST);

        // Global search
        JTextField searchField = new JTextField(20);
        searchField.setFont(FONT_BODY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        searchField.setToolTipText("Search inventory...");
        searchField.putClientProperty("placeholder", "🔍  Search inventory...");
        searchField.addActionListener(e -> showInventory("name LIKE '%" + searchField.getText().replace("'", "") + "%'"));

        JPanel searchWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchWrap.setOpaque(false);
        searchWrap.add(new JLabel("Search: "));
        searchWrap.add(searchField);
        topBar.add(searchWrap, BorderLayout.EAST);

        return topBar;
    }

    // ─── Status bar ─────────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
            BorderFactory.createEmptyBorder(5, 20, 5, 20)
        ));
        statusBar = new JLabel("Ready");
        statusBar.setFont(FONT_SMALL);
        statusBar.setForeground(TEXT_MUTED);
        bar.add(statusBar, BorderLayout.WEST);

        JLabel dbStatus = new JLabel("● Connected to MySQL");
        dbStatus.setFont(FONT_SMALL);
        dbStatus.setForeground(ACCENT);
        bar.add(dbStatus, BorderLayout.EAST);
        return bar;
    }

    // ─── DASHBOARD ──────────────────────────────────────────────────────────────
    private void showDashboard() {
        contentArea.removeAll();
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Stat cards row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        try {
            ResultSet rs = query("SELECT COUNT(*) total, SUM(quantity) units, SUM(price*quantity) value FROM inventory");
            rs.next();
            int    total = rs.getInt("total");
            int    units = rs.getInt("units");
            double value = rs.getDouble("value");

            ResultSet rLow = query("SELECT COUNT(*) cnt FROM inventory WHERE quantity <= " + LOW_STOCK_THRESHOLD);
            rLow.next();
            int lowCount = rLow.getInt("cnt");

            statsRow.add(statCard("Total SKUs",      String.valueOf(total),    PRIMARY,  "▤"));
            statsRow.add(statCard("Units in Stock",  String.valueOf(units),    ACCENT,   "●"));
            statsRow.add(statCard("Inventory Value", "₹" + fmt(value),        new Color(139, 92, 246), "◈"));
            statsRow.add(statCard("Low Stock Items", String.valueOf(lowCount), lowCount > 0 ? DANGER : ACCENT, "⚠"));
        } catch (SQLException e) {
            statsRow.add(new JLabel("Error loading stats: " + e.getMessage()));
        }

        panel.add(statsRow);
        panel.add(Box.createVerticalStrut(24));

        // Recent items table
        JLabel recentTitle = new JLabel("Recent Additions");
        recentTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        recentTitle.setForeground(TEXT_PRIMARY);
        recentTitle.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(recentTitle);
        panel.add(Box.createVerticalStrut(12));

        String[] cols = {"ID", "Name", "Category", "Qty", "Price (₹)", "Stock Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        try {
            ResultSet rs = query("SELECT * FROM inventory ORDER BY id DESC LIMIT 8");
            while (rs.next()) {
                int qty = rs.getInt("quantity");
                String status = qty == 0 ? "Out of Stock" : qty <= LOW_STOCK_THRESHOLD ? "Low Stock" : "In Stock";
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    qty,
                    fmt(rs.getDouble("price")),
                    status
                });
            }
        } catch (SQLException e) {
            setStatus("Error: " + e.getMessage());
        }

        JTable table = buildStyledTable(model);
        // Color the Status column
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(cardBorder());
        scroll.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(scroll);

        contentArea.add(panel, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
        setStatus("Dashboard loaded");
    }

    // ─── INVENTORY VIEW ─────────────────────────────────────────────────────────
    private void showInventory(String whereClause) {
        contentArea.removeAll();
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setOpaque(false);

        JComboBox<String> categoryFilter = new JComboBox<>(new String[]{"All Categories"});
        categoryFilter.setFont(FONT_BODY);
        categoryFilter.setPreferredSize(new Dimension(180, 34));
        try {
            ResultSet rs = query("SELECT DISTINCT category FROM inventory ORDER BY category");
            while (rs.next()) categoryFilter.addItem(rs.getString(1));
        } catch (Exception ignored) {}

        JButton addBtn    = primaryButton("＋  Add Item");
        JButton editBtn   = grayButton("✏  Edit");
        JButton deleteBtn = dangerButton("✕  Delete");
        JButton refreshBtn = grayButton("↺  Refresh");

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnGroup.setOpaque(false);
        btnGroup.add(categoryFilter);
        btnGroup.add(refreshBtn);
        btnGroup.add(editBtn);
        btnGroup.add(deleteBtn);
        btnGroup.add(addBtn);

        toolbar.add(btnGroup, BorderLayout.EAST);
        panel.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Name", "Category", "Quantity", "Price (₹)", "Total Value (₹)", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        Runnable loadData = () -> {
            model.setRowCount(0);
            StringBuilder sql = new StringBuilder("SELECT * FROM inventory");
            String cat = (String) categoryFilter.getSelectedItem();

            List<String> conditions = new ArrayList<>();
            if (whereClause != null && !whereClause.isBlank()) conditions.add(whereClause);
            if (cat != null && !cat.equals("All Categories")) conditions.add("category = '" + cat.replace("'","") + "'");
            if (!conditions.isEmpty()) sql.append(" WHERE ").append(String.join(" AND ", conditions));
            sql.append(" ORDER BY name");

            try {
                ResultSet rs = query(sql.toString());
                int rowCount = 0;
                while (rs.next()) {
                    int    qty   = rs.getInt("quantity");
                    double price = rs.getDouble("price");
                    String status = qty == 0 ? "Out of Stock" : qty <= LOW_STOCK_THRESHOLD ? "Low Stock" : "In Stock";
                    model.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("name"), rs.getString("category"),
                        qty, fmt(price), fmt(qty * price), status
                    });
                    rowCount++;
                }
                setStatus(rowCount + " items shown");
            } catch (SQLException e) {
                setStatus("Query error: " + e.getMessage());
            }
        };

        loadData.run();

        JTable table = buildStyledTable(model);
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(cardBorder());
        panel.add(scroll, BorderLayout.CENTER);

        // Wire buttons
        addBtn.addActionListener(e -> showAddItemDialog());
        refreshBtn.addActionListener(e -> loadData.run());
        categoryFilter.addActionListener(e -> loadData.run());

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an item to edit."); return; }
            int id = (int) model.getValueAt(row, 0);
            showEditItemDialog(id);
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an item to delete."); return; }
            int    id   = (int) model.getValueAt(row, 0);
            String name = (String) model.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + name + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM inventory WHERE id = ?");
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    loadData.run();
                    setStatus("Deleted: " + name);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage());
                }
            }
        });

        contentArea.add(panel, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    // ─── ADD / EDIT DIALOGS ─────────────────────────────────────────────────────
    private void showAddItemDialog() {
        showItemDialog(0, null, null, 0, 0.0);
    }

    private void showEditItemDialog(int id) {
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM inventory WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                showItemDialog(id, rs.getString("name"), rs.getString("category"),
                    rs.getInt("quantity"), rs.getDouble("price"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Load failed: " + e.getMessage());
        }
    }

    private void showItemDialog(int id, String name, String category, int qty, double price) {
        boolean isEdit = id > 0;
        JDialog dialog = new JDialog(this, isEdit ? "Edit Item" : "Add Item", true);
        dialog.setSize(460, 460);
        dialog.setMinimumSize(new Dimension(420, 440));
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(0, 0));
        dialog.getRootPane().setBorder(BorderFactory.createLineBorder(BORDER));

        // Header
        JPanel header = new JPanel();
        header.setBackground(PRIMARY);
        header.setPreferredSize(new Dimension(0, 56));
        JLabel hTitle = new JLabel(isEdit ? "✏  Edit Inventory Item" : "＋  Add Inventory Item");
        hTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        hTitle.setForeground(Color.WHITE);
        header.add(hTitle);
        dialog.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD);
        form.setBorder(BorderFactory.createEmptyBorder(24, 32, 16, 32));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JTextField nameField     = styledField(name != null ? name : "");
        JTextField categoryField = styledField(category != null ? category : "General");
        JTextField qtyField      = styledField(qty > 0 ? String.valueOf(qty) : "");
        JTextField priceField    = styledField(price > 0 ? String.valueOf(price) : "");

        gbc.gridx = 0; gbc.gridy = 0; form.add(formLabel("Item Name *"),    gbc);
        gbc.gridy = 1; form.add(nameField,     gbc);
        gbc.gridy = 2; form.add(formLabel("Category"),      gbc);
        gbc.gridy = 3; form.add(categoryField, gbc);
        gbc.gridy = 4; form.add(formLabel("Quantity *"),    gbc);
        gbc.gridy = 5; form.add(qtyField,      gbc);
        gbc.gridy = 6; form.add(formLabel("Unit Price (₹) *"), gbc);
        gbc.gridy = 7; form.add(priceField,    gbc);
        dialog.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        btnRow.setBackground(CARD);
        JButton cancel = grayButton("Cancel");
        JButton save   = primaryButton(isEdit ? "Save Changes" : "Add Item");
        btnRow.add(cancel);
        btnRow.add(save);
        dialog.add(btnRow, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            String n  = nameField.getText().trim();
            String c  = categoryField.getText().trim().isEmpty() ? "General" : categoryField.getText().trim();
            String q  = qtyField.getText().trim();
            String p  = priceField.getText().trim();

            if (n.isEmpty() || q.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all required fields.");
                return;
            }
            try {
                int    quantity = Integer.parseInt(q);
                double unitPrice = Double.parseDouble(p);
                if (quantity < 0 || unitPrice < 0) throw new NumberFormatException();

                if (isEdit) {
                    PreparedStatement ps = con.prepareStatement(
                        "UPDATE inventory SET name=?, category=?, quantity=?, price=? WHERE id=?");
                    ps.setString(1, n); ps.setString(2, c);
                    ps.setInt(3, quantity); ps.setDouble(4, unitPrice); ps.setInt(5, id);
                    ps.executeUpdate();
                    setStatus("Updated: " + n);
                } else {
                    PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO inventory(name, category, quantity, price) VALUES(?,?,?,?)");
                    ps.setString(1, n); ps.setString(2, c);
                    ps.setInt(3, quantity); ps.setDouble(4, unitPrice);
                    ps.executeUpdate();
                    setStatus("Added: " + n);
                }
                dialog.dispose();
                showInventory(null);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Quantity must be a whole number; Price must be numeric and non-negative.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Save failed: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    // ─── REPORTS ────────────────────────────────────────────────────────────────
    private void showReports() {
        contentArea.removeAll();
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Inventory Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(16));

        // Category breakdown table
        String[] cols = {"Category", "SKU Count", "Total Units", "Total Value (₹)", "Avg. Price (₹)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        try {
            ResultSet rs = query(
                "SELECT category, COUNT(*) skus, SUM(quantity) units, " +
                "SUM(price*quantity) value, AVG(price) avg_price " +
                "FROM inventory GROUP BY category ORDER BY value DESC");
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("category"),
                    rs.getInt("skus"),
                    rs.getInt("units"),
                    fmt(rs.getDouble("value")),
                    fmt(rs.getDouble("avg_price"))
                });
            }
        } catch (SQLException e) {
            setStatus("Report error: " + e.getMessage());
        }

        JTable table = buildStyledTable(model);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(cardBorder());
        scroll.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(scroll);

        contentArea.add(panel, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
        setStatus("Reports loaded");
    }

    // ─── UI helpers ─────────────────────────────────────────────────────────────

    private JTable buildStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(FONT_BODY);
        table.setRowHeight(36);
        table.setGridColor(BORDER);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 30));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_LABEL);
        header.setBackground(SURFACE);
        header.setForeground(TEXT_MUTED);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 40));

        // Alternate row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (!sel) {
                    setBackground(row % 2 == 0 ? CARD : SURFACE);
                    setForeground(TEXT_PRIMARY);
                }
                return this;
            }
        });

        return table;
    }

    /** Stat card for dashboard */
    private JPanel statCard(String label, String value, Color accent, String icon) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel ico = new JLabel(icon);
        ico.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        ico.setForeground(accent);

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 28));
        val.setForeground(TEXT_PRIMARY);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(lbl, BorderLayout.WEST);
        top.add(ico, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }

    private JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(PRIMARY_DARK); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(PRIMARY); }
        });
        return btn;
    }

    private JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(220, 38, 38)); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(DANGER); }
        });
        return btn;
    }

    private JButton grayButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(SURFACE);
        btn.setForeground(TEXT_PRIMARY);
        btn.setFont(FONT_BODY);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(226, 232, 240)); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(SURFACE); }
        });
        return btn;
    }

    private JTextField styledField(String value) {
        JTextField f = new JTextField(value);
        f.setFont(FONT_BODY);
        f.setPreferredSize(new Dimension(0, 38));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        return f;
    }

    private JLabel formLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    private Border cardBorder() {
        return BorderFactory.createLineBorder(BORDER, 1, true);
    }

    private void setStatus(String msg) {
        statusBar.setText(msg);
    }

    private static String fmt(double v) {
        return new DecimalFormat("#,##0.00").format(v);
    }

    // ─── Status cell renderer ───────────────────────────────────────────────────
    static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            if (!isSelected) {
                String s = value != null ? value.toString() : "";
                switch (s) {
                    case "In Stock"     -> { lbl.setForeground(ACCENT); lbl.setBackground(new Color(236, 253, 245)); }
                    case "Low Stock"    -> { lbl.setForeground(WARNING); lbl.setBackground(new Color(255, 251, 235)); }
                    case "Out of Stock" -> { lbl.setForeground(DANGER);  lbl.setBackground(new Color(254, 242, 242)); }
                    default             -> { lbl.setForeground(TEXT_PRIMARY); lbl.setBackground(row % 2 == 0 ? CARD : SURFACE); }
                }
            }
            return lbl;
        }
    }

    // ─── Entry point ────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(InventoryManagementSystem::new);
    }
}
