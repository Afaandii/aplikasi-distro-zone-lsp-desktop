//package view;
//
//import model.User;
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.*;
//
//public class DashboardKasir extends JFrame {
//    private User currentUser;
//    private JPanel contentPanel;
//    private CardLayout cardLayout;
//    
//    public DashboardKasir(User user) {
//        this.currentUser = user;
//        initComponents();
//    }
//    
//    private void initComponents() {
//        setTitle("DistroZone - Dashboard Kasir");
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setSize(1400, 800);
//        setLocationRelativeTo(null);
//        setExtendedState(JFrame.MAXIMIZED_BOTH);
//        
//        // Main container
//        JPanel mainContainer = new JPanel(new BorderLayout());
//        mainContainer.setBackground(new Color(236, 240, 241));
//        
//        // Sidebar
//        JPanel sidebar = createSidebar();
//        mainContainer.add(sidebar, BorderLayout.WEST);
//        
//        // Content area
//        cardLayout = new CardLayout();
//        contentPanel = new JPanel(cardLayout);
//        contentPanel.setBackground(new Color(236, 240, 241));
//        
//        // Add different panels
//        contentPanel.add(createDashboardPanel(), "dashboard");
////        contentPanel.add(new TransaksiManagementPanel(currentUser), "transaksi");
//        contentPanel.add(new ProdukViewPanel(currentUser), "produk");
//        contentPanel.add(new LaporanKasirPanel(currentUser), "laporan");
//        
//        mainContainer.add(contentPanel, BorderLayout.CENTER);
//        
//        add(mainContainer);
//    }
//    
//    private JPanel createSidebar() {
//        JPanel sidebar = new JPanel();
//        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
//        sidebar.setBackground(new Color(44, 62, 80));
//        sidebar.setPreferredSize(new Dimension(280, getHeight()));
//        
//        // Header with logo
//        JPanel header = new JPanel();
//        header.setBackground(new Color(52, 73, 94));
//        header.setPreferredSize(new Dimension(280, 100));
//        header.setMaximumSize(new Dimension(280, 100));
//        header.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 25));
//        
//        JLabel lblLogo = new JLabel("DistroZone");
//        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 24));
//        lblLogo.setForeground(Color.WHITE);
//        header.add(lblLogo);
//        
//        sidebar.add(header);
//        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
//        
//        // User info
//        JPanel userInfo = new JPanel();
//        userInfo.setBackground(new Color(44, 62, 80));
//        userInfo.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));
//        userInfo.setMaximumSize(new Dimension(280, 80));
//        
//        JLabel lblUserIcon = new JLabel("👤");
//        lblUserIcon.setFont(new Font("Segoe UI", Font.PLAIN, 32));
//        userInfo.add(lblUserIcon);
//        
//        JPanel userTextPanel = new JPanel();
//        userTextPanel.setLayout(new BoxLayout(userTextPanel, BoxLayout.Y_AXIS));
//        userTextPanel.setBackground(new Color(44, 62, 80));
//        
//        JLabel lblUserName = new JLabel(currentUser.getNama());
//        lblUserName.setFont(new Font("Segoe UI", Font.BOLD, 14));
//        lblUserName.setForeground(Color.WHITE);
//        
//        JLabel lblUserRole = new JLabel(currentUser.getNamaRole());
//        lblUserRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
//        lblUserRole.setForeground(new Color(149, 165, 166));
//        
//        userTextPanel.add(lblUserName);
//        userTextPanel.add(lblUserRole);
//        userInfo.add(userTextPanel);
//        
//        sidebar.add(userInfo);
//        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));
//        
//        // Menu items
//        sidebar.add(createMenuItem("🏠  Dashboard", "dashboard"));
//        sidebar.add(createMenuItem("💳  Transaksi Penjualan", "transaksi"));
//        sidebar.add(createMenuItem("📦  Lihat Produk", "produk"));
//        sidebar.add(createMenuItem("📊  Laporan Saya", "laporan"));
//        
//        sidebar.add(Box.createVerticalGlue());
//        
//        // Logout button
//        JButton btnLogout = new JButton("🚪  Keluar");
//        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
//        btnLogout.setForeground(Color.WHITE);
//        btnLogout.setBackground(new Color(192, 57, 43));
//        btnLogout.setFocusPainted(false);
//        btnLogout.setBorderPainted(false);
//        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
//        btnLogout.setMaximumSize(new Dimension(240, 45));
//        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
//        
//        btnLogout.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseEntered(MouseEvent e) {
//                btnLogout.setBackground(new Color(169, 50, 38));
//            }
//            
//            @Override
//            public void mouseExited(MouseEvent e) {
//                btnLogout.setBackground(new Color(192, 57, 43));
//            }
//        });
//        
//        btnLogout.addActionListener(e -> {
//            int confirm = JOptionPane.showConfirmDialog(this,
//                "Apakah Anda yakin ingin keluar?",
//                "Konfirmasi Logout",
//                JOptionPane.YES_NO_OPTION);
//            
//            if (confirm == JOptionPane.YES_OPTION) {
//                dispose();
//                new LoginPage().setVisible(true);
//            }
//        });
//        
//        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
//        sidebar.add(btnLogout);
//        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
//        
//        return sidebar;
//    }
//    
//    private JPanel createMenuItem(String text, String cardName) {
//        JPanel menuItem = new JPanel();
//        menuItem.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 12));
//        menuItem.setBackground(new Color(44, 62, 80));
//        menuItem.setMaximumSize(new Dimension(280, 50));
//        menuItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
//        
//        JLabel label = new JLabel(text);
//        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
//        label.setForeground(new Color(189, 195, 199));
//        menuItem.add(label);
//        
//        menuItem.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseEntered(MouseEvent e) {
//                menuItem.setBackground(new Color(52, 73, 94));
//                label.setForeground(Color.WHITE);
//            }
//            
//            @Override
//            public void mouseExited(MouseEvent e) {
//                menuItem.setBackground(new Color(44, 62, 80));
//                label.setForeground(new Color(189, 195, 199));
//            }
//            
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                cardLayout.show(contentPanel, cardName);
//            }
//        });
//        
//        return menuItem;
//    }
//    
//    private JPanel createDashboardPanel() {
//        JPanel panel = new JPanel(new BorderLayout(20, 20));
//        panel.setBackground(new Color(236, 240, 241));
//        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
//        
//        // Header
//        JLabel lblHeader = new JLabel("Dashboard Kasir");
//        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 28));
//        lblHeader.setForeground(new Color(44, 62, 80));
//        panel.add(lblHeader, BorderLayout.NORTH);
//        
//        // Stats cards
//        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
//        statsPanel.setBackground(new Color(236, 240, 241));
//        
//        statsPanel.add(createStatCard("Transaksi Hari Ini", "0", "💳", new Color(52, 152, 219)));
//        statsPanel.add(createStatCard("Total Penjualan", "Rp 0", "💰", new Color(46, 204, 113)));
//        statsPanel.add(createStatCard("Produk Terjual", "0", "📦", new Color(241, 196, 15)));
//        
//        panel.add(statsPanel, BorderLayout.CENTER);
//        
//        return panel;
//    }
//    
//    private JPanel createStatCard(String title, String value, String icon, Color color) {
//        JPanel card = new JPanel();
//        card.setLayout(new BorderLayout(10, 10));
//        card.setBackground(Color.WHITE);
//        card.setBorder(BorderFactory.createCompoundBorder(
//            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
//            BorderFactory.createEmptyBorder(20, 20, 20, 20)
//        ));
//        
//        JLabel lblIcon = new JLabel(icon);
//        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 40));
//        lblIcon.setForeground(color);
//        
//        JPanel textPanel = new JPanel();
//        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
//        textPanel.setBackground(Color.WHITE);
//        
//        JLabel lblTitle = new JLabel(title);
//        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//        lblTitle.setForeground(new Color(127, 140, 141));
//        
//        JLabel lblValue = new JLabel(value);
//        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
//        lblValue.setForeground(new Color(44, 62, 80));
//        
//        textPanel.add(lblTitle);
//        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
//        textPanel.add(lblValue);
//        
//        card.add(lblIcon, BorderLayout.WEST);
//        card.add(textPanel, BorderLayout.CENTER);
//        
//        return card;
//    }
//}
//
//// Panel untuk view produk (read-only untuk kasir)
//class ProdukViewPanel extends JPanel {
//    private User currentUser;
//    
//    public ProdukViewPanel(User user) {
//        this.currentUser = user;
//        initComponents();
//    }
//    
//    private void initComponents() {
//        setLayout(new BorderLayout(20, 20));
//        setBackground(new Color(236, 240, 241));
//        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
//        
//        JLabel lblTitle = new JLabel("Daftar Produk");
//        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
//        lblTitle.setForeground(new Color(44, 62, 80));
//        add(lblTitle, BorderLayout.NORTH);
//        
//        // TODO: Implement product view table
//        JLabel lblTemp = new JLabel("View produk akan ditampilkan di sini", SwingConstants.CENTER);
//        lblTemp.setFont(new Font("Segoe UI", Font.PLAIN, 16));
//        lblTemp.setForeground(new Color(127, 140, 141));
//        add(lblTemp, BorderLayout.CENTER);
//    }
//}
//
//// Panel laporan untuk kasir
//class LaporanKasirPanel extends JPanel {
//    private User currentUser;
//    
//    public LaporanKasirPanel(User user) {
//        this.currentUser = user;
//        initComponents();
//    }
//    
//    private void initComponents() {
//        setLayout(new BorderLayout(20, 20));
//        setBackground(new Color(236, 240, 241));
//        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
//        
//        JLabel lblTitle = new JLabel("Laporan Transaksi Saya");
//        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
//        lblTitle.setForeground(new Color(44, 62, 80));
//        add(lblTitle, BorderLayout.NORTH);
//        
//        // TODO: Implement report panel
//        JLabel lblTemp = new JLabel("Laporan transaksi akan ditampilkan di sini", SwingConstants.CENTER);
//        lblTemp.setFont(new Font("Segoe UI", Font.PLAIN, 16));
//        lblTemp.setForeground(new Color(127, 140, 141));
//        add(lblTemp, BorderLayout.CENTER);
//    }
//}